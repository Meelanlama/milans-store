package com.milan.controller;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.handler.PageableResponse;
import com.milan.service.OrderService;
import com.milan.service.RefundService;
import com.milan.util.CommonUtil;
import com.milan.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.io.IOException;

import static com.milan.util.MyConstants.*;

//@SecurityRequirement(name = "Authorization")
@Tag(name = "ORDER MANAGEMENT", description = "APIs for Order operations and management")
@RestController
@RequestMapping(path = "${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final RefundService refundService;


    @Operation(summary = "Create new order: User only", description = "Creates an order based on items in the user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    // create order for logged in user according to their cart items
    @PostMapping("/create")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequestDto request) throws MessagingException, UnsupportedEncodingException {

        OrderDto orderDto = this.orderService.createOrder(request);
        return CommonUtil.createBuildResponse(orderDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get current user's orders", description = "Retrieves all orders for the currently logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    //get currently logged in user's orders
    @GetMapping("/my-orders")
    public ResponseEntity<?> getUserOrders(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy",defaultValue = "orderDate") String sortBy,
            @RequestParam(value = "sortDir",defaultValue = "desc") String sortDir) {


        PageableResponse<OrderDto> orders = orderService.getOrdersOfUser(pageNo,pageSize,sortBy,sortDir);

        return CommonUtil.createBuildResponse(orders, HttpStatus.OK);
    }

    @Operation(summary = "Get all orders", description = "Admin only: Retrieves all orders in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    //get all orders
    @GetMapping("/all")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> getAllOrders(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "orderDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PageableResponse<OrderDto> orders = orderService.getAllOrders(pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(orders, HttpStatus.OK);
    }

    @Operation(summary = "Get all order statuses", description = "Retrieves all possible order statuses for frontend dropdowns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order statuses retrieved successfully")
    })
    //populating a dropdown in the frontend
    // Endpoint to get all possible order statuses for dropdowns (while updating order status and while exporting excel and others)
    @GetMapping("/order-statuses")
    public ResponseEntity<List<Map<String, String>>> getOrderStatuses() {

        // Convert each OrderStatus enum instance into a key-value pair
        List<Map<String, String>> statuses = Arrays.stream(OrderStatus.values())
                .map(status -> Map.of(
                        "key", status.name(),           // Enum name for backend usage, e.g., "SHIPPED"
                        "label", status.getLabel()      // User-friendly name for dropdown, e.g., "Order Shipped"
                ))
                .toList();

        // Return the list as HTTP 200 OK response
        return ResponseEntity.ok(statuses);
    }

    @Operation(summary = "Update order status", description = "Admin only: Updates the status of a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    //UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer orderId, @RequestBody Map<String, String> request) throws MessagingException, UnsupportedEncodingException {

        //get the order status from request body
        String status = request.get("status");

        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);
        return CommonUtil.createBuildResponse(updatedOrder, HttpStatus.OK);
    }

    @Operation(summary = "Search order by identifier", description = "Admin only: Search for an order using its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/search")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> getOrderByIdentifier(@RequestParam("orderIdentifier") String orderIdentifier) {

        // Get the order using the service
       OrderDto order = orderService.searchOrderByIdentifier(orderIdentifier);

        return CommonUtil.createBuildResponse(order, HttpStatus.OK);
    }


    @Operation(summary = "Filter orders", description = "Admin only: Filter orders by status and date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    //filter orders according to status and date range
    @GetMapping("/filter")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> filterOrders(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "orderDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        // Trim any extra spaces or newline characters from date inputs
        if (startDate != null) {
            startDate = startDate.trim();
        }
        if (endDate != null) {
            endDate = endDate.trim();
        }

        PageableResponse<OrderDto> filteredOrders = orderService.filterOrders(pageNo, pageSize, sortBy, sortDir, status, startDate, endDate);

        return CommonUtil.createBuildResponse(filteredOrders, HttpStatus.OK);
    }

    //helper method to check if a date string is valid for export
    public boolean isValidDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return true;  // Valid date
        } catch (DateTimeParseException e) {
            return false;  // Invalid date
        }
    }

    @Operation(summary = "Export orders to Excel of 30 days", description = "Admin only: Export filtered orders to Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    //Export excel file
    @GetMapping("/export-excel")
    @PreAuthorize(ROLE_ADMIN)
    public void exportOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response) throws IOException {

        // Validate start and end date
        if (startDate != null && !isValidDate(startDate)) {
            throw new IllegalArgumentException("Invalid start date: " + startDate);
        }
        if (endDate != null && !isValidDate(endDate)) {
            throw new IllegalArgumentException("Invalid end date: " + endDate);
        }

        // Export orders to Excel and write to the output stream
        orderService.exportOrdersForMonth(status, startDate, endDate, response);
    }

    @Operation(summary = "Download invoice", description = "Download invoice PDF for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    // Download the invoice PDF for a specific order.
    // Only accessible by the owner of the order.
    // In the frontend, loop through user orders to get each orderIdentifier and use it to trigger this download.
    @GetMapping("/download-invoice/{orderIdentifier}")
    @PreAuthorize(ROLE_USER)
    public void downloadInvoice(@PathVariable String orderIdentifier, HttpServletResponse response) throws IOException {

        orderService.generateInvoice(orderIdentifier, response);
    }

    @Operation(summary = "Cancel order", description = "Cancel an order if it's in progress or has been received by seller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    //CANCEL ORDER IF IT'S IN PROGRESS OR ORDER RECEIVED BY SELLER
    @PostMapping("/cancel-order/{orderIdentifier}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderIdentifier) throws AccessDeniedException {

        orderService.requestOrderCancellation(orderIdentifier);

        return CommonUtil.createBuildResponseMessage("Order cancelled successfully", HttpStatus.OK);
    }

}
