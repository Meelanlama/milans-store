package com.milan.controller;

import com.milan.service.VerifyService;
import com.milan.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store/v1/account-verify")
public class AccountVerificationController {

    private final VerifyService verifyService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AccountVerificationController.class);

    @GetMapping("/verify-register")
    ResponseEntity<?> verifyUserAccount(@RequestParam Integer userId, @RequestParam String verificationToken) {

        logger.info("Verifying user account for userId={} with code={}", userId, verificationToken);
        Boolean success = verifyService.verifyRegisterAccount(userId, verificationToken);

        if (success) {
            logger.info("Account verified successfully for userId={}", userId);
            return CommonUtil.createBuildResponse("Account verification successful", HttpStatus.OK);
        }

        logger.warn("Account verification failed for userId={}", userId);
        return CommonUtil.createErrorResponseMessage("Invalid verification link", HttpStatus.BAD_REQUEST);
    }


}
