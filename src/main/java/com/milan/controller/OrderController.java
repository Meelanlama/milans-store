package com.milan.controller;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.dto.response.PageableResponse;
import com.milan.service.OrderService;
import com.milan.util.CommonUtil;
import com.milan.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;

import static com.milan.util.MyConstants.*;

@RestController
@RequestMapping(path = "/store/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // create order for logged in user according to their cart items
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequestDto request) throws MessagingException, UnsupportedEncodingException {

        OrderDto orderDto = this.orderService.createOrder(request);
        return CommonUtil.createBuildResponse(orderDto, HttpStatus.CREATED);
    }

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

    //UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer orderId, @RequestBody Map<String, String> request) throws MessagingException, UnsupportedEncodingException {

        //get the order status from request body
        String status = request.get("status");

        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);
        return CommonUtil.createBuildResponse(updatedOrder, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> getOrderByIdentifier(@RequestParam("orderIdentifier") String orderIdentifier) {

        // Get the order using the service
       OrderDto order = orderService.searchOrderByIdentifier(orderIdentifier);

        return CommonUtil.createBuildResponse(order, HttpStatus.OK);
    }

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

    //Export excel file
    @GetMapping("/export")
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


}
