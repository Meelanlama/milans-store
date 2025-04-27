package com.milan.util;

import com.milan.handler.GenericResponse;

import com.milan.model.SiteUser;
import com.milan.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

public class CommonUtil {

//    public static SiteUser getLoggedInUser() {
//        try{
//            CustomUserDetails logInUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            return logInUser.getSiteUser();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    //Problem: Direct casting of Principal to CustomUserDetails can cause ClassCastException.
    //Add explicit checks and handle unauthenticated users.
    public static SiteUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        // Handle custom UserDetails implementation
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getSiteUser();
        } else {
            throw new ClassCastException("Unexpected principal type: " + principal.getClass());
        }
    }

    //for generating dynamic url for sending in email
    public static String getUrl(HttpServletRequest request) {
        String apiUrl = request.getRequestURL().toString(); // http:localhost:8080/api/v1/auth
        apiUrl = apiUrl.replace(request.getServletPath(),""); // http:localhost:8080
        return apiUrl;
    }

    public static ResponseEntity<?> createBuildResponse(Object data,HttpStatus status){

        GenericResponse response = GenericResponse.builder()
                .responseStatus(status)
                .status("Success")
                .message("Success")
                .data(data)
                .build();
        return response.create();
    }

    public static ResponseEntity<?> createBuildResponseMessage(String message, HttpStatus status) {

        GenericResponse response = GenericResponse.builder()
                .responseStatus(status)
                .status("Success")
                .message(message)
                .build();
        return response.create();
    }

    public static ResponseEntity<?> createErrorResponse(Object data,HttpStatus status){

        GenericResponse response = GenericResponse.builder()
                .responseStatus(status)
                .status("Failure")
                .message("Failure")
                .data(data)
                .build();
        return response.create();
    }

    public static ResponseEntity<?> createErrorResponseMessage(String message, HttpStatus status) {

        GenericResponse response = GenericResponse.builder()
                .responseStatus(status)
                .status("Failure")
                .message(message)
                .build();
        return response.create();
    }


}
