package com.pang.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class SingerVo {
    private Long id;
    private String name;
    private Integer gender;
    private String genderText; // 性别文字
    private Integer area;
    private String areaText; // 地区文字
    private Integer style;
    private String styleText; // 风格文字
    private String avatarUrl;
    private String intro;
    private Integer status;
    private String statusText; // 状态文字
    private Integer songCount; // 歌曲数量（统计）
    private Long totalPlayCount; // 总播放量（统计）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}