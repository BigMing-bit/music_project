package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.Album;
import com.pang.entity.vo.AlbumVo;
import com.pang.entity.vo.OptionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbumMapper extends BaseMapper<Album> {

    List<OptionVo> selectOptions(@Param("keyword") String keyword);

    IPage<AlbumVo> pageAlbumVo(Page<?> page,
                               @Param("keyword") String keyword,
                               @Param("status") Integer status,
                               @Param("singerId") Long singerId);

    /**
     * 插入专辑收藏记录
     * @param userId 用户ID
     * @param albumId 专辑ID
     */
    void insertCollect(@Param("userId") Long userId, @Param("albumId") Long albumId);

    /**
     * 删除专辑收藏记录
     * @param userId 用户ID
     * @param albumId 专辑ID
     */
    void deleteCollect(@Param("userId") Long userId, @Param("albumId") Long albumId);

    /**
     * 统计专辑收藏次数
     * @param userId 用户ID
     * @param albumId 专辑ID
     * @return 收藏次数
     */
    Long countUserFavorite(@Param("userId") Long userId, @Param("albumId") Long albumId);

    /**
     * 更新专辑收藏数
     * @param albumId 专辑ID
     * @param delta 变化量
     */
    void updateCollectCount(@Param("albumId") Long albumId, @Param("delta") int delta);

    Album selectAlbumWithSingerName(Long id);

}

