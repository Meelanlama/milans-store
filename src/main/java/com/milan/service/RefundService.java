package com.milan.service;

import com.milan.dto.RefundDto;
import com.milan.dto.RefundSellerDto;
import com.milan.dto.RefundRequestDto;
import com.milan.handler.PageableResponse;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface RefundService {

    void requestRefund(String orderIdentifier, RefundRequestDto refundDto);

    void approveRefund(Integer refundId,RefundSellerDto refundSellerDto) throws MessagingException, UnsupportedEncodingException;

    void rejectRefund(Integer refundId, RefundSellerDto refundSellerDto) throws MessagingException, UnsupportedEncodingException;

    PageableResponse<RefundDto> getAllRefunds(int pageNo, int pageSize, String sortBy, String sortDir);

    PageableResponse<RefundDto> getMyRefundRequests(int pageNo, int pageSize, String sortBy, String sortDir);

}
