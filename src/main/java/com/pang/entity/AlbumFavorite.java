package com.pang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专辑收藏实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("music_favorite_album")
public class AlbumFavorite extends BaseFavorite {

    private Long albumId;

}
