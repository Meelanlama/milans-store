package com.milan.controller;

import com.milan.dto.RefundDto;
import com.milan.dto.RefundSellerDto;
import com.milan.dto.RefundRequestDto;
import com.milan.handler.PageableResponse;
import com.milan.service.RefundService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

import static com.milan.util.MyConstants.*;
import static com.milan.util.MyConstants.DEFAULT_PAGE_SIZE;

//@SecurityRequirement(name = "Authorization")
@Tag(name = "REFUND MANAGEMENT", description = "APIs for handling refund requests and processing")
@RestController
@RequestMapping(path = "${api.prefix}/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RefundController.class);


    @Operation(summary = "Request refund for order", description = "Submit a refund request for a specific order (User only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Error in refund request")
    })
    //Request Refund for user's order
    @PostMapping("/request-refund/{orderIdentifier}")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<String> requestRefund(@PathVariable String orderIdentifier, @RequestBody RefundRequestDto refundRequestDto) {

        logger.info("Requesting refund for order: {}", orderIdentifier);
        try {
            refundService.requestRefund(orderIdentifier, refundRequestDto);
            return ResponseEntity.ok("Refund request submitted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error requesting refund: " + e.getMessage());
        }
    }

    @Operation(summary = "Get user's refund requests", description = "Retrieve paginated list of the current user's refund requests")
    @ApiResponse(responseCode = "200", description = "List of refunds retrieved",
            content = @Content(schema = @Schema(implementation = PageableResponse.class)))
    //get my refund requests
    @GetMapping("/my-refunds")
    public ResponseEntity<?> getMyRefunds(@RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
                                          @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                          @RequestParam(value = "sortBy", defaultValue = "createdOn") String sortBy,
                                          @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PageableResponse<RefundDto> myRefundRequests = refundService.getMyRefundRequests(pageNo,pageSize,sortBy,sortDir);

        return CommonUtil.createBuildResponse(myRefundRequests, HttpStatus.OK);
    }


    @Operation(summary = "Approve refund request", description = "Approve a refund request (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund approved successfully"),
            @ApiResponse(responseCode = "400", description = "Error approving refund")
    })
    @PostMapping("/approve-refund/{refundId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<String> approveRefund(@PathVariable Integer refundId,@RequestBody RefundSellerDto refundSellerDto) throws MessagingException, UnsupportedEncodingException {

        try {
            refundService.approveRefund(refundId,refundSellerDto);
            return ResponseEntity.ok("Refund approved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error approving refund: " + e.getMessage());
        }
    }

    @Operation(summary = "Reject refund request", description = "Reject a refund request (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Error rejecting refund")
    })
    @PostMapping("/reject-refund/{refundId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> rejectRefund(@PathVariable Integer refundId, @RequestBody RefundSellerDto refundSellerDto) throws MessagingException, UnsupportedEncodingException {
        try {
            refundService.rejectRefund(refundId, refundSellerDto);
            return ResponseEntity.ok("Refund rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error rejecting refund: " + e.getMessage());
        }
    }


    @Operation(summary = "Get all refund requests", description = "Retrieve paginated list of all refund requests (Admin only)")
    @ApiResponse(responseCode = "200", description = "List of all refunds",
            content = @Content(schema = @Schema(implementation = PageableResponse.class)))
    @GetMapping("/all-refunds")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllRefunds(@RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
                                           @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                           @RequestParam(value = "sortBy", defaultValue = "createdOn") String sortBy,
                                           @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PageableResponse<RefundDto> refunds = refundService.getAllRefunds(pageNo,pageSize,sortBy,sortDir);

        return CommonUtil.createBuildResponse(refunds, HttpStatus.OK);
    }
}
