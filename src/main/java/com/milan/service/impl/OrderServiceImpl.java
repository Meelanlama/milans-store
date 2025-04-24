package com.milan.service.impl;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.dto.OrderItemDto;
import com.milan.dto.request.EmailRequest;
import com.milan.dto.response.PageableResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.*;
import com.milan.repository.CartRepository;
import com.milan.repository.OrderRepository;
import com.milan.repository.ProductRepository;
import com.milan.service.OrderService;
import com.milan.util.CommonUtil;
import com.milan.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;

    private final ModelMapper mapper;

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final EmailService emailService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public OrderDto createOrder(CreateOrderRequestDto orderRequest) throws MessagingException, UnsupportedEncodingException {

        // Get the logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        //Fetch the user's cart from the database
        Cart cart = this.cartRepository.findByUserId(user.getId());

        //check if cart exists for the user
        if(cart == null){
            throw new ResourceNotFoundException("Cart not found");
        }

        //Get items from the cart
        List<CartItem> cartItems = cart.getCartItems();

        //Validate that cart has at least one item
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("No items found in cart");
        }

        // Generate unique identifiers for the order
        String orderIdentifier = UUID.randomUUID().toString();

        // Prepare to calculate total order amount
        AtomicReference<Double> totalOrderAmount = new AtomicReference<>((double) 0);

        // Create a new Order instance with default status = IN_PROGRESS for order status
        Order order = Order.builder()
                .orderIdentifier(orderIdentifier)
                .shippingPhoneNumber(orderRequest.getShippingPhoneNumber())
                .status(OrderStatus.IN_PROGRESS) // default status
                .paymentMethod(orderRequest.getPaymentMethod())
                .shippingAddress(orderRequest.getShippingAddress())
                .shippingProvince(orderRequest.getShippingProvince())
                .shippingZipCode(orderRequest.getShippingZipCode())
                .user(user)
                .orderDate(LocalDateTime.now())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(3)) // set it default: delivery in 3 days
                .build();

        // Convert cart items to order items
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int orderProductQuantity = cartItem.getQuantity();
            int availableProductStock = product.getStock();

            //Skip the product if it don't have enough stock left
            if (orderProductQuantity > availableProductStock) {
                continue;
            }

            // Convert cart item to order item
            double itemPrice = product.getDiscountedPrice() != 0 ? product.getDiscountedPrice() : product.getUnitPrice();
            OrderItem orderItem = OrderItem.builder()
                    .quantity(orderProductQuantity)
                    .product(product)
                    .priceAtPurchase(orderProductQuantity * itemPrice)
                    .order(order)
                    .build();

            // Add to total order amount
            totalOrderAmount.set(totalOrderAmount.get() + orderItem.getPriceAtPurchase());

            // 🔄 Update product stock
            product.setStock(availableProductStock - orderProductQuantity);
            productRepository.save(product);

            orderItems.add(orderItem);
        }

        // If no order items were added (due to insufficient stock), abort
        if (orderItems.isEmpty()) {
            throw new ResourceNotFoundException("Insufficient stock! No items available for order");
        }

        // Attach order items and calculated amount to the order
        order.setItems(orderItems);
        order.setTotalOrderAmount(totalOrderAmount.get());

        //Clear the cart after successful order creation
        cart.getCartItems().clear();
        this.cartRepository.save(cart);

        //Save the order
        Order savedOrder = orderRepo.save(order);

        //Mark products as out-of-stock if quantity becomes 0
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            if (product.getStock() == 0) {
                product.setStock(0);
                this.productRepository.save(product);
            }
        }

        //Convert saved order to DTO and return it
        OrderDto orderDto = mapper.map(savedOrder, OrderDto.class);

        //send email
        sendMailForOrderStatus(orderDto,user);

        return orderDto;
    }

    @Override
    public PageableResponse<OrderDto> getOrdersOfUser(int pageNo, int pageSize, String sortBy, String sortDir) {

        // Get the logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        Page<Order> userOrder = orderRepo.findByUserId(user.getId(), pageRequest);

        if(userOrder.isEmpty()){
            throw new ResourceNotFoundException("No orders found for this user");
        }

        //convert pageable order entity to dto class
        PageableResponse<OrderDto> orders = PageMapper.getPageableResponse(userOrder,OrderDto.class);

        logger.info("Found all orders for user {}", orders.getTotalElements());

        return orders;
    }


    //send code of status enum
    public void sendMailForOrderStatus(OrderDto order,SiteUser user) throws MessagingException, UnsupportedEncodingException {
        try {
            StringBuilder productTable = new StringBuilder();
            for (OrderItemDto item : order.getItems()) {
                productTable.append("<tr>")
                        .append("<td>").append(item.getProductId()).append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>").append(item.getPriceAtPurchase()).append("</td>")
                        .append("</tr>");
            }

            String msg = "<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>"
                    + "<p>Hello [[name]],</p>"
                    + "<p>Your order status is: <b>[[orderStatus]]</b>.</p>"
                    + "<p>Estimated Delivery Date: <b>[[deliveryDate]]</b></p>"
                    + "<hr>"
                    + "<p><b>Order Summary:</b></p>"
                    + "<p><b>Order ID:</b> [[orderId]]<br>"
                    + "<b>Order Date:</b> [[orderDate]]<br>"
                    + "<b>Payment Method:</b> [[paymentType]]</p>"
                    + "<p><b>Shipping To:</b><br>"
                    + "[[address]], [[zipCode]], [[province]]<br>"
                    + "Phone: [[phone]]</p>"
                    + "<hr>"
                    + "<p><b>Items:</b></p>"
                    + "<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>"
                    + "<tr><th>Product</th><th>Quantity</th><th>Price</th></tr>"
                    + productTable
                    + "</table>"
                    + "<p><b>Total Amount:</b> $" + order.getTotalOrderAmount() + "</p>"
                    + "</body></html>";

            msg = msg.replace("[[name]]", user.getFirstName() + " " + user.getLastName() );
            msg = msg.replace("[[orderStatus]]", order.getStatus().name());
            msg = msg.replace("[[deliveryDate]]", order.getEstimatedDeliveryDate().toLocalDate().toString());
            msg = msg.replace("[[orderId]]", order.getOrderIdentifier());
            msg = msg.replace("[[orderDate]]", order.getOrderDate().toLocalDate().toString());
            msg = msg.replace("[[paymentType]]", order.getPaymentMethod());
            msg = msg.replace("[[address]]", order.getShippingAddress());
            msg = msg.replace("[[zipCode]]", order.getShippingZipCode());
            msg = msg.replace("[[province]]", order.getShippingProvince());
            msg = msg.replace("[[phone]]", order.getShippingPhoneNumber());

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(user.getEmail())
                    .title("Order Update")
                    .subject("Your Order Status Has Been Updated")
                    .message(msg)
                    .build();

            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Error sending email for order status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
