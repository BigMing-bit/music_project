package com.pang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("music_song_like")
public class SongLike extends BaseFavorite {

    private Long songId;

    @TableField(exist = false)
    private LocalDateTime updateTime;

}
