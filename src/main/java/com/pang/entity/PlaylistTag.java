package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_playlist_tag")
public class PlaylistTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long playlistId;
    private Long tagId;
    private LocalDateTime createTime;
}
