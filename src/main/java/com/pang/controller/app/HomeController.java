package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping
    public Result home() {
        return Result.success(homeService.getHome());
    }

    @GetMapping("/playlists")
    public Result playlists(@RequestParam(required = false) String cursor,
                            @RequestParam(defaultValue = "25") Integer size) {
        return Result.success(homeService.getHotPlaylists(cursor, size));
    }

    @GetMapping("/albums")
    public Result albums(@RequestParam(required = false) String cursor,
                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(homeService.getHotAlbums(cursor, size));
    }

    @GetMapping("/hot-songs")
    public Result hotSongs(@RequestParam(required = false) String cursor,
                           @RequestParam(defaultValue = "9") Integer size) {
        return Result.success(homeService.getHotSongs(cursor, size));
    }

    @GetMapping("/banners")
    public Result banners(@RequestParam(required = false) String cursor,
                          @RequestParam(defaultValue = "6") Integer size) {
        return Result.success(homeService.getBanners(cursor, size));
    }

    @GetMapping("/notices")
    public Result notices(@RequestParam(required = false) String cursor,
                          @RequestParam(defaultValue = "6") Integer size) {
        return Result.success(homeService.getNotices(cursor, size));
    }
}
