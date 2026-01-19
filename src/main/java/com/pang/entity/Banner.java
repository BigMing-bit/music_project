package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_banner")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("image_url")
    private String imageUrl;

    @TableField("jump_type")
    private Integer jumpType; // 0无跳转 1URL 2歌单 3专辑 4歌曲...（你自定义）

    @TableField("jump_target")
    private String jumpTarget;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
