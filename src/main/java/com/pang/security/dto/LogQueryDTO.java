package com.pang.security.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("日志查询参数")
public class LogQueryDTO extends PageQueryDTO {

    @ApiModelProperty(value = "操作是否成功")
    private Integer success;
}