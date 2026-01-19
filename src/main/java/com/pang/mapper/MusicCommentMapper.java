package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.MusicComment;
import com.pang.entity.vo.AdminCommentPageVO;
import com.pang.entity.vo.CommentVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MusicCommentMapper extends BaseMapper<MusicComment> {

    @Select("<script>" +
            "SELECT " +
            " c.id AS id, " +
            " c.user_id AS userId, " +
            " COALESCE(u.nickname, u.username) AS userNickName, " +
            " c.target_type AS targetType, " +
            " c.parent_id AS parentId, " +
            " CASE " +
            "   WHEN c.target_type = 1 THEN s.song_name " +  // 如果是歌曲，显示歌曲名
            "   WHEN c.target_type = 2 THEN pl.name " +  // 如果是歌单，显示歌单名
            "   WHEN c.target_type = 3 THEN a.album_name " +  // 如果是专辑，显示专辑名
            "   ELSE '' " +
            " END AS targetName, " +  // 动态目标名称
            " ms.name AS singerName, " +
            " c.content AS content, " +
            " c.reply_content AS replyContent, " +
            " c.reply_time AS replyTime, " +
            " c.reply_admin_name AS replyAdminName, " +
            " c.create_time AS createTime " +
            "FROM music_comment c " +
            "LEFT JOIN app_user u ON u.id = c.user_id " +
            "LEFT JOIN music_song s ON s.id = c.target_id " +
            "LEFT JOIN music_playlist pl ON pl.id = c.target_id " +  // 假设歌单存储在 music_playlist 表
            "LEFT JOIN music_album a ON a.id = c.target_id " +  // 假设专辑存储在 music_album 表
            "LEFT JOIN music_singer ms  ON ms.id = s.singer_id " +
            "WHERE 1=1 " +
            " AND c.creator_type = 1 " +
            "<if test='parentId != null and parentId != 0'> AND c.parent_id = #{parentId} </if>" +
            "<if test='targetType != null'> AND c.target_type = #{targetType} </if>" +
            "<if test='targetId != null'> AND c.target_id = #{targetId} </if>" +
            "<if test='replied != null'> AND (CASE WHEN #{replied} = 1 THEN c.reply_content IS NOT NULL ELSE c.reply_content IS NULL END) </if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (c.content LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR u.nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR ms.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<AdminCommentPageVO> pageAdminComments(
            Page<AdminCommentPageVO> page,
            @Param("keyword") String keyword,
            @Param("parentId") Long parentId,  // 新增父评论ID过滤
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId,
            @Param("replied") Integer replied,
            @Param("singerName") String singerName
    );

    @Update("""
    UPDATE music_comment
    SET reply_content = #{replyContent},
        reply_time = NOW(),
        reply_admin_id = #{adminId},
        reply_admin_name = #{adminName}
    WHERE id = #{commentId}
  """)
    int replyComment(@Param("commentId") Long commentId,
                     @Param("replyContent") String replyContent,
                     @Param("adminId") Long adminId,
                     @Param("adminName") String adminName);

    @Delete("""
        DELETE FROM music_comment
        WHERE id = #{id}
           OR root_id = #{id}
           OR parent_id = #{id}
    """)
    void deleteCommentAndChildren(@Param("id") Long id);

        @Select("""
    SELECT
      c.id, c.user_id AS userId, c.content, c.create_time AS createTime,
      c.parent_id AS parentId, c.root_id AS rootId,
      u.avatar_url AS avatar, COALESCE(u.nickname, u.username) AS nickname
    FROM music_comment c
    LEFT JOIN app_user u ON u.id = c.user_id
    WHERE c.status = 1
      AND c.target_type = #{targetType}
      AND c.target_id = #{targetId}
      AND c.parent_id = 0
    ORDER BY c.id DESC
    """)
        IPage<CommentVo> pageRoot(IPage<CommentVo> page,
                                  @Param("targetType") Integer targetType,
                                  @Param("targetId") Long targetId);



    @Select("""
    SELECT
      c.id, c.user_id AS userId, c.content, c.create_time AS createTime,
      c.parent_id AS parentId, c.root_id AS rootId,
      c.reply_to_user_id AS replyToUserId, c.reply_to_comment_id AS replyToCommentId,
      u.avatar_url AS avatar, COALESCE(u.nickname, u.username) AS nickname,
      ru.nickname AS replyToNickname,
      rc.content AS replyToContent
    FROM music_comment c
    LEFT JOIN app_user u ON u.id = c.user_id
    LEFT JOIN app_user ru ON ru.id = c.reply_to_user_id
    LEFT JOIN music_comment rc ON rc.id = c.reply_to_comment_id
    WHERE c.status = 1
      AND c.root_id = #{commentId}  
      AND c.parent_id != 0
    ORDER BY c.id ASC
""")
    List<CommentVo> listReplies(@Param("commentId") Long commentId);  // 根据 commentId 获取
}

