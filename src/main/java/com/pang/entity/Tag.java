package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 1主题 2场景 3心情 4语言 5风格... */
    private Integer type;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
