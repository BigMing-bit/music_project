package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_playlist_sim")
public class PlaylistSim {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playlistId;
    private Long simPlaylistId;

    private java.math.BigDecimal score;

    private LocalDateTime createTime;
}