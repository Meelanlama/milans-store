package com.milan.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public  class AccountStatusDto {
    private Integer id;
    private Boolean isActive;
}