package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用收藏实体基类
 */
@Data
public abstract class BaseFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer status; // 1=收藏，0=取消
    private LocalDateTime createTime;

}
