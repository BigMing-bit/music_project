package com.pang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("music_favorite_mv")
public class MvFavorite extends BaseFavorite{
    private Long mvId;
}
