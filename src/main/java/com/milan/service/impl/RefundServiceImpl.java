package com.milan.service.impl;

import com.milan.dto.RefundDto;
import com.milan.dto.RefundSellerDto;
import com.milan.dto.RefundRequestDto;
import com.milan.dto.request.EmailRequest;
import com.milan.handler.PageableResponse;
import com.milan.handler.PageMapper;
import com.milan.model.Order;
import com.milan.model.Refund;
import com.milan.model.SiteUser;
import com.milan.repository.OrderRepository;
import com.milan.repository.RefundRepository;
import com.milan.service.RefundService;
import com.milan.util.CommonUtil;
import com.milan.enums.OrderStatus;
import com.milan.enums.RefundStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;

import static com.milan.util.MyConstants.REFUND_WINDOW_DAYS;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RefundServiceImpl.class);

    private final RefundRepository refundRepo;

    private final ModelMapper mapper;

    private final EmailService emailService;

    private final OrderRepository orderRepo;

    @Override
    public void requestRefund(String orderIdentifier, RefundRequestDto refundDto) {

        SiteUser loggedInUser = CommonUtil.getLoggedInUser();

        //fetch the order of logged in user
        Order order = orderRepo.findByOrderIdentifier(orderIdentifier)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if the order belongs to the logged-in user
        if (!order.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("You can only request a refund for your own orders.");
        }

        // Ensuring that the order status is "DELIVERED" before allowing refund
        if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new RuntimeException("Refund can only be requested for delivered orders.");
        }

        // Refund must be requested within 7 days of delivery
        LocalDateTime deliveredAt = order.getEstimatedDeliveryDate(); // Assuming this field exists
        if (deliveredAt == null || deliveredAt.plusDays(REFUND_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refund request been has expired. You can only request refund within " + REFUND_WINDOW_DAYS + " days after delivery.");
        }

        //Prevent duplicate refund request
        // This checks if a refund has already been submitted for this order.
        // It works by checking if a refund record exists in the 'refunds' table with the same order_id.
        // The existsByOrder method uses the order foreign key to perform the check.
        //if that order_id is already present in refunds table then it will throw an exception
        boolean alreadyRequested = refundRepo.existsByOrder(order);
        if (alreadyRequested) {
            throw new RuntimeException("Refund has already been requested for this order.");
        }

        // Map the refund request DTO to the Refund entity
        Refund refund = mapper.map(refundDto, Refund.class);
        refund.setOrder(order);
        refund.setStatus(RefundStatus.PENDING);  // Set status to pending
        refundRepo.save(refund);
    }

    @Override
    public void approveRefund(Integer refundId,RefundSellerDto approveRefundRequestDTO) throws MessagingException, UnsupportedEncodingException {

        // Fetch the refund from the database
        Refund refund = refundRepo.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        // Check if the refund status is PENDING
        if (!refund.getStatus().equals(RefundStatus.PENDING)) {
            throw new RuntimeException("Refund cannot be approved because it is not in PENDING status.");
        }

        // Update the refund status to APPROVED
        refund.setStatus(RefundStatus.APPROVED);

        // Add the rejection reason if provided
        refund.setSellerComment(approveRefundRequestDTO.getSellerComment());

        //set resolved date
        refund.setResolvedDate(LocalDateTime.now());

        //Update the order status to REFUNDED
        Order order = refund.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepo.save(order); // Save the updated order status

        // Save the updated refund
        refundRepo.save(refund);

        //Send a notification email to the user about the approval
        sendRefundStatusEmail(order.getUser(), refund);
    }

    // Reject Refund
    @Override
    public void rejectRefund(Integer refundId, RefundSellerDto rejectRefundRequestDTO) throws MessagingException, UnsupportedEncodingException {

        // Fetch the refund from the database
        Refund refund = refundRepo.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        // Check if the refund status is PENDING
        if (!refund.getStatus().equals(RefundStatus.PENDING)) {
            throw new RuntimeException("Refund cannot be rejected because it is not in PENDING status.");
        }

        // Update the refund status to REJECTED
        refund.setStatus(RefundStatus.REJECTED);

        // Add the rejection reason if provided
        refund.setSellerComment(rejectRefundRequestDTO.getSellerComment());

        //set resolved date
        refund.setResolvedDate(LocalDateTime.now());

        // Save the updated refund
        refundRepo.save(refund);

        //Send a notification email to the user about the rejection
         sendRefundStatusEmail(refund.getOrder().getUser(), refund);
    }

    @Override
    public PageableResponse<RefundDto> getAllRefunds(int pageNo, int pageSize, String sortBy, String sortDir) {

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Refund> page = refundRepo.findAll(pageable);

//        if(page.isEmpty()){
//            throw new RuntimeException("No refund requests found");
//        }

        logger.info("Refund requests found: {}", page.getContent().size());

        return PageMapper.getPageableResponse(page,RefundDto.class);
    }

    @Override
    public PageableResponse<RefundDto> getMyRefundRequests(int pageNo, int pageSize, String sortBy, String sortDir) {

        SiteUser loggedInUser = CommonUtil.getLoggedInUser();

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        /*
         * Finds refunds requested by a specific user by navigating through the order's user_id.
         * Ensures users only see their own refunds.
         *Filter refunds based on the provided user, i.e., `order.user.id = :userId`.
         */
        Page<Refund> myRefunds = refundRepo.findByOrder_User(loggedInUser, pageable);

        if(myRefunds.isEmpty()){
            throw new RuntimeException("No refund requests found for this user");
        }

        logger.info("Refund requests found for this user: {}", myRefunds.getContent().size());

        return PageMapper.getPageableResponse(myRefunds,RefundDto.class);
    }

    // Send refund approval email
    public void sendRefundStatusEmail(SiteUser user, Refund refund) throws MessagingException, UnsupportedEncodingException {

        try {
            String msg = "<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>"
                    + "<p>Hello, [[name]],</p>"
                    + "<p>Your refund request for <b>Order ID: [[orderId]]</b> has been updated.</p>"
                    + "<p><b>Refund Status: </b> [[refundStatus]]</p>"
                    + "<p><b>Your Refund Reason: </b> [[reason]]</p>";

            // Add seller comment only if it's present (for approved or rejected)
            if (refund.getSellerComment() != null && !refund.getSellerComment().isEmpty()) {
                msg += "<p><b>Seller's Comment:</b> [[sellerComment]]</p>";
            }

            msg += "<hr>"
                    + "<p><b>Refund Date: </b> [[refundDate]]<br>"
                    + "<b>Resolved Date: </b> [[resolvedDate]]</p>"
                    + "<p>Thank you for shopping with us.</p>"
                    + "<p>If you have any query,Contact Us: milanstore7@gmail.com</p>"
                    + "<p>Best regards,<br>Milan's store</p>"
                    + "</body></html>";

            msg = msg.replace("[[name]]", user.getFirstName() + " " + user.getLastName());
            msg = msg.replace("[[orderId]]", refund.getOrder().getOrderIdentifier());
            msg = msg.replace("[[refundStatus]]", refund.getStatus().name());
            msg = msg.replace("[[reason]]", refund.getReason());
            msg = msg.replace("[[sellerComment]]", refund.getSellerComment() != null ? refund.getSellerComment() : "N/A");
            msg = msg.replace("[[refundDate]]", refund.getCreatedOn().toLocalDate().toString());
            msg = msg.replace("[[resolvedDate]]", refund.getUpdatedOn().toLocalDate().toString());

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(user.getEmail())
                    .title("Refund Status Update")
                    .subject("Update on Your Refund Request for Order" + refund.getOrder().getOrderIdentifier())
                    .message(msg)
                    .build();

            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Error sending email for refund status update: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
