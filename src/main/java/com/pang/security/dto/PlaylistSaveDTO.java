package com.pang.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistSaveDTO {
    private Long id;
    @NotBlank(message = "歌单名称不能为空")
    private String name;
    private String description;
    private String coverUrl;
    private Integer status;
    private Long creatorUserId;

    private List<Long> songIds;
    private List<Long> tagIds;
}
