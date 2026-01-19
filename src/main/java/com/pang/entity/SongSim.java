package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_song_sim")
public class SongSim {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long songId;
    private Long simSongId;

    private java.math.BigDecimal score;

    private LocalDateTime createTime;
}
