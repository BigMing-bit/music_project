package com.pang.entity.vo;

import lombok.Data;

@Data
public class OptionVo {
    private Long value; // 歌单里的增加或编辑   歌曲远程搜索接口
    private String label;

    public OptionVo(Long value, String label) {
        this.value = value;
        this.label = label;
    }
}
