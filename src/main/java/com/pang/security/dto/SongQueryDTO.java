package com.pang.security.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("歌曲查询参数")
public class SongQueryDTO extends PageQueryDTO {

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "歌手ID")
    private Long singerId;

    @ApiModelProperty(value = "专辑ID")
    private Long albumId;

    @ApiModelProperty(value = "排序方式")
    private String orderBy;
}