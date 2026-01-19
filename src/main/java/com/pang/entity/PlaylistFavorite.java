package com.pang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("music_favorite_playlist")
public class PlaylistFavorite extends BaseFavorite {

    private Long playlistId;

}
