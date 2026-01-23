package com.pang.entity.vo;

import com.pang.entity.SysAdmin;
import com.pang.entity.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminVO extends SysAdmin {
    private List<SysRole> roles;
}
