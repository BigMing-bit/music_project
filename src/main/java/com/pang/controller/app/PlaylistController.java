package com.pang.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.common.Result;
import com.pang.entity.Playlist;
import com.pang.entity.vo.SongListVo;
import com.pang.security.dto.PlaylistSaveDTO;
import com.pang.utils.SaTokenUtil; // 你项目里取 userId 的工具类，按你的路径改
import com.pang.service.PlaylistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/home")
    public Result homePlaylists(@RequestParam(required = false) String cursor,
                                @RequestParam(defaultValue = "25") Integer size) {
        return Result.success(playlistService.getHomePlaylists(cursor, size));
    }

    @GetMapping("/{id}")
    public Result playlistDetail(@PathVariable Long id) {
        Long userId = SaTokenUtil.USER.isLogin() ? SaTokenUtil.USER.getLoginIdAsLong() : null;
        return Result.success(playlistService.getPlaylistDetail(id, userId));
    }

    // ✅ 歌单歌曲列表（分页）
    @GetMapping("/{id}/songs")
    public Result playlistSongs(@PathVariable Long id,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<SongListVo> songPage = playlistService.getPlaylistSongs(id, page, pageSize);
        return Result.success(songPage);
    }

    // ✅ 歌单歌曲列表（Cursor 分页）
    @GetMapping("/{id}/songs/cursor")
    public Result playlistSongsCursor(@PathVariable Long id,
                                      @RequestParam(required = false) String cursor,
                                      @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(playlistService.getPlaylistSongsCursor(id, cursor, size));
    }

    @PostMapping("/{id}/play")
    public Result playPlaylist(@PathVariable Long id) {
        playlistService.increasePlayFavorite(id);
        return Result.success("success");
    }

    @PostMapping("/{id}/collect")
    public Result collectPlaylist(@PathVariable Long id) {
        SaTokenUtil.USER.checkLogin();
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        boolean collected = playlistService.toggleFavorite(id, userId);
        return Result.success(collected);
    }

    @PostMapping
    public Result createPlaylist(@RequestBody PlaylistSaveDTO dto) {
        SaTokenUtil.USER.checkLogin();
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();

        dto.setCreatorUserId(userId); // ✅ 只保留这个
        Long playlistId = playlistService.saveOrUpdateWithSongs(dto);
        return Result.success(playlistId);
    }


    @PutMapping("/{id}")
    public Result editPlaylist(@PathVariable Long id, @RequestBody PlaylistSaveDTO dto) {
        SaTokenUtil.USER.checkLogin();
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();

        playlistService.editPlaylist(id, dto, userId);  // ✅ 业务进 service
        return Result.success("歌单更新成功");
    }

    /**
     * 按标签筛选歌单
     * /app/playlists/by-tags?tagIds=1,3,5
     */
    @GetMapping("/by-tags")
    public Result byTags(@RequestParam List<Long> tagIds) {
        return Result.success(playlistService.listByTags(tagIds));
    }
}
