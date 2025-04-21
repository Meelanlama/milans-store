package com.milan.service;

public interface VerifyService {

    Boolean verifyRegisterAccount(Integer userId, String verificationToken);
}
