package com.pang.controller.api;

import com.pang.common.Result;
import com.pang.service.RecoBuildService;
import com.pang.service.RecoService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reco")
public class RecoController {

    private final RecoService recoService;
    private final RecoBuildService recoBuildService;

    public RecoController(RecoService recoService, RecoBuildService recoBuildService) {
        this.recoService = recoService;
        this.recoBuildService = recoBuildService;
    }

    // 推荐歌曲（userId 可选）
    @GetMapping("/songs")
    public Result songs(@RequestParam(required = false) Long userId,
                        @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(recoService.recommendSongIds(userId, size));
    }

    // 推荐歌单（seedPlaylistId 可选）
    @GetMapping("/playlists")
    public Result playlists(@RequestParam(required = false) Long userId,
                            @RequestParam(required = false) Long seedPlaylistId,
                            @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(recoService.recommendPlaylistIds(userId, seedPlaylistId, size));
    }

    // 推荐歌单（带过滤参数）
    @PostMapping("/playlists/filtered")
    public Result playlistsWithFilter(@RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Long seedPlaylistId,
                                     @RequestParam(defaultValue = "12") Integer size,
                                     @RequestBody(required = false) Map<String, Object> filters) {
        return Result.success(recoService.recommendPlaylistIdsWithFilter(userId, seedPlaylistId, size, filters));
    }

    // ✅ 先给你一个手动重建（后面你再改成定时任务）
    @PostMapping("/rebuild")
    public Result rebuild(@RequestParam(defaultValue = "30") Integer topK) {
        recoBuildService.rebuildSongSimFromPlaylists(topK);
        recoBuildService.rebuildPlaylistSim(topK);
        return Result.success("重建完成");
    }
}
