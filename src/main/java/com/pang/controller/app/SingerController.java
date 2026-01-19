package com.pang.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.common.Result;
import com.pang.entity.vo.SingerVo;
import com.pang.entity.vo.SongVo;
import com.pang.service.SingerService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/singers")
public class SingerController{

     @Autowired
     SingerService singerService;

    @GetMapping("/hot")
    @ApiOperation("热门歌手 - Cursor 换一批")
    public Result getHotSingers(@RequestParam(required = false) String cursor,
                                @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(singerService.getHotSingersCursor(cursor, size));
    }

    @GetMapping("/new")
    @ApiOperation("最新歌手 - Cursor 换一批")
    public Result getNewSingers(@RequestParam(required = false) String cursor,
                                @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(singerService.getNewSingersCursor(cursor, size));
    }



    @GetMapping("/list")
    @ApiOperation("分页查询歌手列表 - 导航栏点击歌手")
    public Result getSingerList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer area,
            @RequestParam(required = false) Integer style,
            @RequestParam(required = false) Integer status) {

        Integer finalStatus = status != null ? status : 1;

        Page<SingerVo> singerPage = singerService.getSingerPage(name, gender, area, style, finalStatus, page, pageSize);
        return Result.success(singerPage);
    }

    @GetMapping("/{id}")
    @ApiOperation("歌手详情 - 点击歌手头像进入详情页")
    public Result getSingerDetail(@PathVariable Long id) {
        SingerVo singer = singerService.getSingerDetail(id);
        if (singer == null) {
            return Result.error(404, "歌手不存在或已下架");
        }
        return Result.success(singer);
    }

    @GetMapping("/{id}/songs")
    @ApiOperation("歌手的歌曲列表 - 包含专辑信息")
    public Result getSingerSongs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String orderBy) { // 排序方式：hot(热门)、new(最新)、like(点赞)

        Page<SongVo> songsPage = singerService.getSingerSongsPage(id, orderBy, page, pageSize);
        return Result.success(songsPage);
    }

    @GetMapping("/{id}/similar")
    @ApiOperation("相似歌手推荐")
    public Result getSimilarSingers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") Integer limit) {

        List<SingerVo> similarSingers = singerService.getSimilarSingers(id, limit);
        return Result.success(similarSingers);
    }
}