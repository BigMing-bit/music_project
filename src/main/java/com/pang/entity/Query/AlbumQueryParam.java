package com.pang.entity.Query;



import lombok.Data;

@Data
public class AlbumQueryParam {

    private Integer pageNum = 1; // 当前页码
    private Integer pageSize = 10; // 每页数量
    private String keyword; // 搜索关键词
    private Integer status; // 专辑状态
    private Long singerId; // 歌手ID
}
