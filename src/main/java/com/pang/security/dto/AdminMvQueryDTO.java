package com.pang.security.dto;

import lombok.Data;

@Data
public class AdminMvQueryDTO {
    private String keyword;
    private Long singerId;
    private Integer status;
}
