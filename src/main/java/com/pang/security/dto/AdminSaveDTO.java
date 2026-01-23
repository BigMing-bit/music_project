package com.pang.security.dto;

import com.pang.entity.SysAdmin;
import lombok.Data;

import java.util.List;

@Data
public class AdminSaveDTO {
    private SysAdmin admin;
    private List<Long> roleIds;
}
