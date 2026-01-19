package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.Album;
import com.pang.entity.vo.AlbumVo;
import com.pang.entity.vo.OptionVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AlbumMapper extends BaseMapper<Album> {

    @Select({
            "<script>",
            "SELECT id AS value, album_name AS label",
            "FROM music_album",
            "WHERE status = 1",
            "<if test='keyword != null and keyword.trim() != \"\"'>",
            "  AND album_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "ORDER BY id DESC",
            "LIMIT 50",
            "</script>"
    })
    List<OptionVo> selectOptions(@Param("keyword") String keyword);


    @Select({
            "<script>",
            "SELECT",
            "  a.id,",
            "  a.album_name AS albumName,",
            "  a.singer_id AS singerId,",
            "  s.name AS singerName,",
            "  a.cover_url AS coverUrl,",
            "  a.status,",
            "  a.publish_date AS publishDate,",
            "  a.create_time AS createTime,",
            "  a.update_time AS updateTime",
            "FROM music_album a",
            "LEFT JOIN music_singer s ON a.singer_id = s.id",
            "WHERE 1 = 1",
            "<if test='keyword != null and keyword.trim() != \"\"'>",
            "  AND a.album_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "<if test='status != null'>",
            "  AND a.status = #{status}",
            "</if>",
            "<if test='singerId != null'>",
            "  AND a.singer_id = #{singerId}",
            "</if>",
            "ORDER BY a.id DESC",
            "</script>"
    })
    IPage<AlbumVo> pageAlbumVo(Page<?> page,
                               @Param("keyword") String keyword,
                               @Param("status") Integer status,
                               @Param("singerId") Long singerId);

    /**
     * 插入专辑收藏记录
     * @param userId 用户ID
     * @param albumId 专辑ID
     */
    @Insert({
            "<script>",
            "INSERT INTO music_favorite_album(user_id, album_id, status, create_time)",
            "VALUES(#{userId}, #{albumId}, 1, NOW())",
            "ON DUPLICATE KEY UPDATE status = 1, create_time = NOW()",
            "</script>"
    })
    void insertCollect(@Param("userId") Long userId, @Param("albumId") Long albumId);

    /**
     * 删除专辑收藏记录
     * @param userId 用户ID
     * @param albumId 专辑ID
     */
    @Delete({
            "<script>",
            "DELETE FROM music_favorite_album",
            "WHERE user_id = #{userId} AND album_id = #{albumId}",
            "</script>"
    })
    void deleteCollect(@Param("userId") Long userId, @Param("albumId") Long albumId);

    /**
     * 统计专辑收藏次数
     * @param userId 用户ID
     * @param albumId 专辑ID
     * @return 收藏次数
     */
    @Select("""
        <script>
        SELECT COUNT(1) 
        FROM music_favorite_album 
        WHERE user_id = #{userId} 
        AND album_id = #{albumId} 
        AND status = 1
        </script>
    """)
    Long countUserFavorite(@Param("userId") Long userId, @Param("albumId") Long albumId);


    /**
     * 更新专辑收藏数
     * @param albumId 专辑ID
     * @param delta 变化量
     */
    @Update({
            "<script>",
            "UPDATE music_album",
            "SET collect_count = collect_count + #{delta}",
            "WHERE id = #{albumId}",
            "</script>"
    })
    void updateCollectCount(@Param("albumId") Long albumId, @Param("delta") int delta);

}
