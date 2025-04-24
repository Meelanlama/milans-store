package com.milan.controller;

import com.milan.dto.CreateOrderRequestDto;
import com.milan.dto.OrderDto;
import com.milan.dto.response.PageableResponse;
import com.milan.service.OrderService;
import com.milan.util.CommonUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.milan.util.MyConstants.DEFAULT_PAGE_NO;
import static com.milan.util.MyConstants.DEFAULT_PAGE_SIZE;

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

}
