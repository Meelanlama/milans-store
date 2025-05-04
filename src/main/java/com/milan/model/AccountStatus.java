package com.milan.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private Boolean isAccountActive;

    @Column(unique = true)
    private String verificationToken;

    //for the token expiry time
    private LocalDateTime verificationTokenExpiry;

    @Column(unique = true)
    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

}
