package com.pang.mapper;

import com.pang.entity.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeMapper {

    List<PlaylistHomeVo> hotPlaylists(@Param("cursorScore") Long cursorScore,
                                      @Param("cursorId") Long cursorId,
                                      @Param("size") Integer size);
    List<AlbumHomeVo> hotAlbums(@Param("cursorScore") Long cursorScore,
                                @Param("cursorId") Long cursorId,
                                @Param("size") Integer size);


    List<SongHomeVo> hotSongs(@Param("cursorScore") Long cursorScore,
                              @Param("cursorId") Long cursorId,
                              @Param("size") Integer size);


    List<BannerHomeVo> banners(@Param("cursorScore") Long cursorScore,
                               @Param("cursorId") Long cursorId,
                               @Param("size") Integer size);

    List<NoticeHomeVo> notices(@Param("cursorScore") Long cursorScore,
                               @Param("cursorId") Long cursorId,
                               @Param("size") Integer size);
}
