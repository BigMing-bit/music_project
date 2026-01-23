package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.AlbumService;
import com.pang.utils.SaTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;


    @GetMapping("/{id}")
    public Result albumDetail(@PathVariable("id") Long albumId) {
        return Result.success(albumService.getAlbumDetail(albumId));
    }

    @GetMapping("/{id}/songs")
    public Result albumSongs(@PathVariable("id") Long albumId,
                             @RequestParam(required = false) String cursor,
                             @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(albumService.getAlbumSongs(albumId, cursor, size));
    }

    @GetMapping("/list")
    public Result getAlbumList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long singerId) {
        return Result.success(albumService.getAlbumPage(name, singerId, page, pageSize));
    }

    @PostMapping("/{id}/collect")
    public Result collectAlbum(@PathVariable Long id) {
        SaTokenUtil.USER.checkLogin();
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        boolean collected = albumService.toggleFavorite(id, userId);
        return Result.success(collected);
    }

}
