package com.milan.service;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.handler.PageableResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;

public interface OrderService {

    OrderDto createOrder(CreateOrderRequestDto request) throws MessagingException, UnsupportedEncodingException;

    PageableResponse<OrderDto> getOrdersOfUser(int pageNo, int pageSize, String sortBy, String sortDir);

    PageableResponse<OrderDto> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir);

    OrderDto updateOrderStatus(Integer orderId, String status) throws MessagingException, UnsupportedEncodingException;

    OrderDto searchOrderByIdentifier(String orderIdentifier);

    PageableResponse<OrderDto> filterOrders(int pageNo, int pageSize, String sortBy, String sortDir, String status, String startDate, String endDate);

    void exportOrdersForMonth(String status, String startDate, String endDate, HttpServletResponse response) throws IOException;

    public void generateInvoice(String orderIdentifier, HttpServletResponse response) throws IOException;

    void requestOrderCancellation(String orderIdentifier) throws AccessDeniedException;

}
