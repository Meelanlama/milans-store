package com.milan.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class AccountStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isAccountActive;

    private String verificationToken;

    //for the token expiry time
    private LocalDateTime verificationTokenExpiry;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

}
