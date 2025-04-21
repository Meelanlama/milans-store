package com.milan.exception;

import com.milan.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("In GlobalExceptionHandler :: handleException ::", e.getMessage());
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        log.error("In GlobalExceptionHandler :: handleNullPointerException ::", e.getMessage());
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(JwtTokenExpiredException e) {
        return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExistDataException.class)
    public ResponseEntity<?> handleExistDataException(ExistDataException e) {
        log.error("In GlobalExceptionHandler :: handleExistDataException ::");
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("In GlobalExceptionHandler :: handleResourceNotFoundException ::", e.getMessage());
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SuccessException.class)
    public ResponseEntity<?> handleSuccessException(SuccessException e) {
        log.error("In GlobalExceptionHandler :: SuccessException ::", e.getMessage());
        return CommonUtil.createBuildResponse(e.getMessage(), HttpStatus.OK);
    }

    @ExceptionHandler(MyValidationException.class)
    public ResponseEntity<?> handleValidationException(MyValidationException e) {
        log.error("In GlobalExceptionHandler :: handleMyValidationException ::");
        return CommonUtil.createErrorResponse(e.getErrors(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentExceptionException(IllegalArgumentException e) {
        log.error("In GlobalExceptionHandler :: handleIllegalArgumentExceptionException ::", e.getMessage());
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(FileNotFoundException e) {
        log.error("In GlobalExceptionHandler :: handleFileNotFoundException ::", e.getMessage());
        return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return CommonUtil.createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }


}
