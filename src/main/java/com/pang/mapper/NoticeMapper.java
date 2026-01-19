package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.Notice;
import com.pang.entity.vo.NoticeFrontVo;
import com.pang.entity.vo.NoticeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    // ✅ 后台分页（支持关键词、状态、可见）
    @Select({
            "<script>",
            "SELECT",
            " id, title, content, cover_url AS coverUrl, publisher,",
            " start_time AS startTime, end_time AS endTime,",
            " sort, visible, status, create_time AS createTime",
            "FROM music_notice",
            "WHERE 1=1",
            "<if test='keyword!=null and keyword.trim()!=\"\"'>",
            " AND title LIKE CONCAT('%',#{keyword},'%')",
            "</if>",
            "<if test='status!=null'>",
            " AND status = #{status}",
            "</if>",
            "<if test='visible!=null'>",
            " AND visible = #{visible}",
            "</if>",
            "ORDER BY sort DESC, id DESC",
            "</script>"
    })
    IPage<NoticeVo> adminPage(Page<?> page,
                              @Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("visible") Integer visible);

    // ✅ 前台列表（只取可见 + 启用 + 时间窗口）
    @Select({
            "<script>",
            "SELECT id, title, cover_url AS coverUrl, publisher, create_time AS createTime",
            "FROM music_notice",
            "WHERE status=1 AND visible=1",
            "AND (start_time IS NULL OR start_time &lt;= NOW())",
            "AND (end_time IS NULL OR end_time &gt;= NOW())",
            "ORDER BY sort DESC, id DESC",
            "LIMIT #{size}",
            "</script>"
    })
    List<NoticeFrontVo> frontLatest(@Param("size") Integer size);

    @Select({
            "SELECT id, title, content, cover_url AS coverUrl, publisher, create_time AS createTime",
            "FROM music_notice",
            "WHERE id = #{id} AND status=1 AND visible=1",
            "AND (start_time IS NULL OR start_time <= NOW())",
            "AND (end_time IS NULL OR end_time >= NOW())"
    })
    com.pang.entity.vo.NoticeDetailVo frontDetail(@Param("id") Long id);

    @Select("""
    SELECT
      id,
      title,
      content,
      cover_url AS coverUrl,
      publisher,
      start_time AS startTime,
      end_time AS endTime,
      sort,
      visible,
      status,
      create_time AS createTime
    FROM music_notice
    WHERE id = #{id}
""")
    NoticeVo adminDetail(@Param("id") Long id);

}
