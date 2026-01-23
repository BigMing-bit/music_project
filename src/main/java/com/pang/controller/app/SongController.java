package com.pang.controller.app;


import com.pang.common.Result;
import com.pang.entity.vo.SongVo;
import com.pang.service.PlayHistoryService;
import com.pang.service.SongLikeService;
import com.pang.service.SongService;
import com.pang.utils.SaTokenUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/app/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final SongLikeService songLikeService;
    private final PlayHistoryService playHistoryService;

    @GetMapping("/page")
    @ApiOperation("歌曲分页查询（返回 Song 实体）")
    public Result page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long singerId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) String orderBy
    ) {
        return Result.success(songService.pageSongs(page, pageSize, keyword, singerId, albumId, orderBy));
    }


    @GetMapping("/{id}")
    @ApiOperation("根据ID查询歌曲详情")
    public Result getSong(@PathVariable Long id) {
        SongVo song = songService.getSongDetail(id);
        if (song == null) {
            return Result.error(401,"歌曲不存在");
        }
        return Result.success(song);
    }

    @GetMapping("/singer/{singerId}")
    @ApiOperation("根据歌手ID查询歌曲")
    public Result getSongsBySinger(@PathVariable Long singerId) {
        try {
            List<SongVo> songs = songService.getSongsBySinger(singerId);
            return Result.success(songs);
        } catch (Exception e) {
            log.error("根据歌手ID查询歌曲异常，歌手ID：{}", singerId, e);
            return Result.error(500, "服务器内部错误");
        }
    }


    @GetMapping("/hot")
    @ApiOperation("热门歌曲")
    public Result getHotSongs(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("开始获取热门歌曲，limit: {}", limit);
        try {
            // 1. 检查参数
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            // 2. 调用服务
            log.info("调用songService.getHotSongs，参数: {}", limit);
            List<SongVo> songs = songService.getHotSongs(limit);
            return Result.success(songs);
        } catch (Exception e) {
            log.error("获取热门歌曲失败，limit: {}", limit, e);
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/new")
    @ApiOperation("新歌推荐")
    public Result getNewSongs(@RequestParam(defaultValue = "10") Integer limit) {
        List<SongVo> songs = songService.getNewSongs(limit);
        return Result.success(songs);
    }

    @PostMapping("/playCount/{id}")
    @ApiOperation("更新歌曲播放数量")
    public Result playSong(@PathVariable Long id) {
        int count = songService.incrementPlayCount(id);
        return Result.success(count);
    }

    @PostMapping("/{id}/play")
    public Result play(@PathVariable("id") Long songId) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        playHistoryService.addPlayHistory(userId, songId);
        return Result.success("success");
    }

    // ✅ 收藏/取消收藏歌曲
    @PostMapping("/{id}/like")
    public Result toggleLike(@PathVariable("id") Long songId) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        boolean liked = songLikeService.toggleLike(songId, userId);
        return Result.success(liked);
    }

    // ✅ 是否已收藏歌曲
    @GetMapping("/{id}/like-status")
    public Result likeStatus(@PathVariable("id") Long songId) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(songLikeService.isLiked(songId, userId));
    }

}
