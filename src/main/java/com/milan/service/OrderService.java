package com.milan.service;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.dto.ProductDto;
import com.milan.dto.response.PageableResponse;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface OrderService {

    OrderDto createOrder(CreateOrderRequestDto request) throws MessagingException, UnsupportedEncodingException;

    PageableResponse<OrderDto> getOrdersOfUser(int pageNo, int pageSize, String sortBy, String sortDir);
}
