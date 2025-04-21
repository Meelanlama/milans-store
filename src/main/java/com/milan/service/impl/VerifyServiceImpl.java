package com.milan.service.impl;

import com.milan.exception.ResourceNotFoundException;
import com.milan.exception.SuccessException;
import com.milan.model.AccountStatus;
import com.milan.model.SiteUser;
import com.milan.repository.UserRepository;
import com.milan.service.VerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final UserRepository userRepo;

    @Override
    public Boolean verifyRegisterAccount(Integer userId, String verificationToken) {

        //first find if user id is in db or not
        SiteUser user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Invalid user"));

        //if user try to verify account again through link then throw exception as the account is already verified and token will be null in db
        if(user.getAccountStatus().getVerificationToken()==null){
            throw new SuccessException("Account already verified.");
        }

        //if user_id and verification token in url match as stored in db of that user
        if(user.getAccountStatus().getVerificationToken().equals(verificationToken)){

            //make token null in db as account is verified successfully
            AccountStatus status = user.getAccountStatus();
            status.setIsAccountActive(true);
            status.setVerificationToken(null);

            //update user
            userRepo.save(user);
            return true;
        }
        return false;
    }
}
