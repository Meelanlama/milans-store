package com.milan.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.dto.OrderItemDto;
import com.milan.dto.request.EmailRequest;
import com.milan.handler.PageableResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.*;
import com.milan.repository.CartRepository;
import com.milan.repository.OrderRepository;
import com.milan.repository.ProductRepository;
import com.milan.service.OrderService;
import com.milan.util.CommonUtil;
import com.milan.enums.OrderStatus;
import com.milan.util.PageUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
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
                    // while storing in db only we store total price according to product quantity.
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

        // Set total CartPrice to 0 as cart items is cleared.
        if (cart.getCartItems().isEmpty()) {
            cart.setTotalCartPrice(0.0) ;
        }

        cartRepository.save(cart);

        //Save the order
        Order savedOrder = orderRepo.save(order);

        //Mark products as out of stock if quantity becomes 0
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            if (product.getStock() == 0) {
                product.setStock(0);
                productRepository.save(product);
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
//        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
//        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        // Creating Pageable with sorting using utility method for reuse and cleaner code
        Pageable pageRequest = PageUtil.getPageable(pageNo, pageSize, sortBy, sortDir);

        Page<Order> userOrder = orderRepo.findByUserId(user.getId(), pageRequest);

//        if(userOrder.isEmpty()){
//            throw new ResourceNotFoundException("No orders found for this user");
//        }

        //convert pageable order entity to dto class
        PageableResponse<OrderDto> orders = PageMapper.getPageableResponse(userOrder,OrderDto.class);

        logger.info("Found all orders for user {}", orders.getTotalElements());

        return orders;
    }

    @Override
    public PageableResponse<OrderDto> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir) {

        //ternary operator for checking sortDir value
//        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
//        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Creating Pageable with sorting using utility method for reuse and cleaner code
        Pageable pageable = PageUtil.getPageable(pageNo, pageSize, sortBy, sortDir);

        Page<Order> page = this.orderRepo.findAll(pageable);

        return PageMapper.getPageableResponse(page, OrderDto.class);
    }


    //send code of status enum
    // Order Creation Email
    public void sendMailForOrderStatus(OrderDto order,SiteUser user) throws MessagingException, UnsupportedEncodingException {
        try {
            StringBuilder productTable = new StringBuilder();
            for (OrderItemDto item : order.getItems()) {
                productTable.append("<tr>")
                        .append("<td>").append(item.getProduct().getProductName()).append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>").append(item.getPriceAtPurchase()).append("</td>")
                        .append("</tr>");
            }

            String msg = "<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>"
                    + "<p>Hello, [[name]],</p>"
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
                    .subject("Your Order Details For Your New Order")
                    .message(msg)
                    .build();

            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Error sending email for order status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderDto updateOrderStatus(Integer orderId, String statusStr) throws MessagingException, UnsupportedEncodingException {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Get the logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        //get the enum
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr);
        }

        order.setStatus(status);
        Order saved = orderRepo.save(order);
        logger.info("Order status updated successfully: {}", saved);

        OrderDto orderDto = mapper.map(saved, OrderDto.class);

        sendMailForUpdate(orderDto,user);

        return orderDto;
    }

    @Override
    public OrderDto searchOrderByIdentifier(String orderIdentifier) {

        String trimOrderIdentifier = orderIdentifier.trim();

        // Search for the order by unique identifier
        Order orderById = orderRepo.findByOrderIdentifier(trimOrderIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with identifier: " + orderIdentifier));

        return mapper.map(orderById, OrderDto.class);
    }

    @Override
    public PageableResponse<OrderDto> filterOrders(int pageNo, int pageSize, String sortBy, String sortDir, String status, String startDate, String endDate) {

//        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Creating Pageable with sorting using utility method for reuse and cleaner code
        Pageable pageable = PageUtil.getPageable(pageNo, pageSize, sortBy, sortDir);

        // Convert String date to LocalDateTime
        //yyyy-MM-ddTHH:mm:ss -> Time is added manually here
        LocalDateTime  start = null;
        LocalDateTime  end = null;

        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDateTime.parse(startDate.trim() + "T00:00:00");  // Parsing the string to LocalDateTime and adding time part here
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDateTime.parse(endDate.trim() + "T23:59:59");
        }

        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }
        }

        // Debugging info: log the values before executing the query
        logger.info("Filtering Orders: status={}, startDate={}, endDate={}", orderStatus, start, end);

        // Filter based on the status and date range
        Page<Order> page = orderRepo.findAllWithFilters(orderStatus, start, end, pageable);

        return PageMapper.getPageableResponse(page, OrderDto.class);
    }

    // Order Status Update Email
    public void sendMailForUpdate(OrderDto orderDto,SiteUser user) throws MessagingException, UnsupportedEncodingException {
        try {

            String msg = "<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>"
                    + "<p>Hello, [[name]],</p>"
                    + "<p>Your order status for your previous order is: <b>[[orderStatus]]</b>.</p>"
                    + "<p>Estimated Delivery Date: <b>[[deliveryDate]]</b></p>"
                    + "<hr>"
                    + "<p><b>Order Details:</b></p>"
                    + "<p><b>Order ID:</b> [[orderId]]<br>"
                    + "<b>Order Date:</b> [[orderDate]]<br>"
                    + "<b>Total Amount:</b> Rs." + orderDto.getTotalOrderAmount() + "</p>"
                    + "</body></html>";

            msg = msg.replace("[[name]]", user.getFirstName() + " " + user.getLastName());
            msg = msg.replace("[[orderStatus]]", orderDto.getStatus().name());
            msg = msg.replace("[[deliveryDate]]", orderDto.getEstimatedDeliveryDate().toLocalDate().toString());
            msg = msg.replace("[[orderId]]", orderDto.getOrderIdentifier());
            msg = msg.replace("[[orderDate]]", orderDto.getOrderDate().toLocalDate().toString());

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(user.getEmail())
                    .title("Order Status Update")
                    .subject("Your Order Status")
                    .message(msg)
                    .build();

            // Send the email using the email service
            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Error sending email for order status: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //export excel file for all orders in a given month
    public void exportOrdersForMonth(String status, String startDate, String endDate, HttpServletResponse response) throws IOException {

        // Fetch orders using the filter method from above
        PageableResponse<OrderDto> orders = filterOrders(0, 15, "orderDate", "desc", status, startDate, endDate);

        // Create a new workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Order Identifier");
        headerRow.createCell(1).setCellValue("Status");
        headerRow.createCell(2).setCellValue("Order Date");
        headerRow.createCell(3).setCellValue("Total Order Amount");
        headerRow.createCell(4).setCellValue("Ordered Items");
        headerRow.createCell(5).setCellValue("Quantity");
        headerRow.createCell(6).setCellValue("Shipping Address");
        headerRow.createCell(7).setCellValue("Estimated Delivery Date");
        headerRow.createCell(8).setCellValue("Payment Method");
        headerRow.createCell(9).setCellValue("User Email");
        //more columns as needed


        // Adding rows with order data
        int rowNum = 1;
        for (OrderDto order : orders.getContent()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderIdentifier());
            row.createCell(1).setCellValue(order.getStatus().getLabel());
            row.createCell(2).setCellValue(order.getOrderDate().toString());
            row.createCell(3).setCellValue(order.getTotalOrderAmount().toString());

            // Iterate through each order in the page content for OrderItem DTO
            StringBuilder itemsString = new StringBuilder();
            // Initialize itemsString for each order to reset it for the next order
            itemsString.setLength(0);
            int totalQuantity = 0;

            // Iterate over each order's items of OrderItemDto to set in the excel sheet
            // Collect product names and quantities
            for (OrderItemDto item : order.getItems()) {
                if (item.getProduct() != null) {
                    itemsString.append(item.getProduct().getProductName()).append(", ");
                }
                totalQuantity += item.getQuantity();
            }

                // Remove the trailing comma and space if necessary
                if (itemsString.length() > 0) {
                    itemsString.setLength(itemsString.length() - 2);  // Remove the last comma and space in excel in products name
                }

                row.createCell(4).setCellValue(itemsString.toString());
                row.createCell(5).setCellValue(totalQuantity);
                row.createCell(6).setCellValue(order.getShippingAddress());
                row.createCell(7).setCellValue(order.getEstimatedDeliveryDate().toString());
                row.createCell(8).setCellValue(order.getPaymentMethod());
            if (order.getUser() != null) {
                row.createCell(9).setCellValue(order.getUser().getEmail());
            }
        }

        // Set response headers for downloading the Excel file
        String fileName = "our_orders_from" + startDate + "_to_" + endDate + ".xlsx";
        // Set the content type for Excel file
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // Set the header for file download
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Write the workbook to the response output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    //GENERATE PDF INVOICE
    @Override
    public void generateInvoice(String orderIdentifier, HttpServletResponse response) throws IOException {

        //get current logged in user and its details including order
        SiteUser currentUser = CommonUtil.getLoggedInUser();

        //find the order by unique identifier
        Order order = orderRepo.findByOrderIdentifier(orderIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderIdentifier));

        logger.info("Generating invoice for order {}", orderIdentifier);

        //if that order doesnt belong to user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            logger.warn("User {} tried to access order {}", currentUser.getEmail(), orderIdentifier);
            throw new AccessDeniedException("Unauthorized access to invoice");
        }

        // Converting Order to OrderDto using ModelMapper, so we dont expose entity directly
        OrderDto orderDto = mapper.map(order, OrderDto.class);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename = Order_Invoice_Of_" + currentUser.getFirstName() + currentUser.getLastName() + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        //Header
        document.add(new Paragraph( "MILAN's STORE")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Gokarneshowr-2, Kathmandu, Nepal")
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph(" "));

        //Title
        Paragraph title = new Paragraph("Order Invoice")
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);


        //Customer and Order Details from the OrderDto instead of entity
        document.add(new Paragraph("Order ID: " + orderDto.getOrderIdentifier()));
        document.add(new Paragraph("Order Date: " + orderDto.getOrderDate().toLocalDate()));
        document.add(new Paragraph("Full Name: " + currentUser.getFirstName() + " " + currentUser.getLastName()));
        document.add(new Paragraph("Estimated Delivery Date: " + orderDto.getEstimatedDeliveryDate()));
        document.add(new Paragraph("Payment Method: " + orderDto.getPaymentMethod()));
        document.add(new Paragraph("Phone Number: " + orderDto.getShippingPhoneNumber()));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Shipping Address:").setBold().setFontSize(18));
        document.add(new Paragraph(orderDto.getShippingAddress()));
        document.add(new Paragraph("Province: " + orderDto.getShippingProvince()));
        document.add(new Paragraph("ZIP Code: " + orderDto.getShippingZipCode()));

        document.add(new Paragraph(" "));
        document.add(new LineSeparator(new SolidLine()));

        // Ordered Items Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell("Product Name");
        table.addHeaderCell("Quantity");
        table.addHeaderCell("Price");
        table.addHeaderCell("Total");

        double total = 0.0;
        // Iterating over items in the OrderItemDto instead of entity OrderItems
        for (OrderItemDto itemDto : orderDto.getItems()) {
            String productName = itemDto.getProduct().getProductName();
            int quantity = itemDto.getQuantity();
            double unitPrice = itemDto.getPriceAtPurchase();

            //set total price for each item
            double itemTotal = quantity * unitPrice;
            total += itemTotal;

            table.addCell(productName);
            table.addCell(String.valueOf(quantity));
            table.addCell(String.format("Rs. %.2f", unitPrice));
            table.addCell(String.format("Rs. %.2f", itemTotal));
        }
        document.add(table);

        //Total summary
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Order Amount: Rs. " + orderDto.getTotalOrderAmount())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());


        //Footer
        document.add(new Paragraph(" "));
        document.add(new Paragraph("For any queries, Contact us: support@milanstore.com")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("This is a system generated invoice.").setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));

        logger.info("Invoice generated for order {}", orderIdentifier);

        document.close();
    }

    @Override
    public void requestOrderCancellation(String orderIdentifier) throws AccessDeniedException {

        SiteUser currentUser = CommonUtil.getLoggedInUser();

        Order order = orderRepo.findByOrderIdentifier(orderIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderIdentifier));

        //if that order doesnt belong to user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Unauthorized access to cancel order");
        }

        //get status from enum
        OrderStatus status = order.getStatus();

        //cancel order only if it's received or in progress status
        if (status != OrderStatus.RECEIVED && status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel right now: " + status);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

    }

}
