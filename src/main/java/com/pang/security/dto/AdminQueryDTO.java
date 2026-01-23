package com.pang.security.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("管理员查询参数")
public class AdminQueryDTO extends PageQueryDTO {

    @ApiModelProperty(value = "状态")
    private Integer status;
}