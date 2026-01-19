package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.models.auth.In;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_playlist")
public class Playlist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;


    private Long creatorUserId;

    private String description;

    private String coverUrl;

    private Integer status;

    private Long collectCount;

    private Long playCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
