package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.*;
import com.pang.utils.SaTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pang.entity.vo.MyPlaylistVO;

import java.util.List;

@RestController
@RequestMapping("/app/user")
public class UserMusicController {

    @Autowired
    private SongLikeService songLikeService;
    @Autowired
    private PlayHistoryService playHistoryService;

    // ✅ 新增：个人信息 & 统计 & 歌单
    @Autowired
    private AppUserProfileService appUserProfileService;
    @Autowired
    private PlaylistLikeService playlistLikeService;
    @Autowired
    private AlbumLikeService albumLikeService;
    @Autowired
    private PlaylistService playlistService;

    // ✅ 我的收藏歌曲
    @GetMapping("/liked-songs")
    public Result likedSongs(@RequestParam(required = false) String cursor,
                             @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(songLikeService.getMyLikedSongs(userId, cursor, size));
    }

    // ✅ 根据用户ID获取收藏歌曲
    @GetMapping("/liked-songs/{id}")
    public Result getUserLikedSongs(@PathVariable Long id, @RequestParam(required = false) String cursor,
                             @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(songLikeService.getMyLikedSongs(id, cursor, size));
    }

    @GetMapping("/liked-playlists")
    public Result likedPlaylists(@RequestParam(required = false) String cursor,
                                 @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(playlistLikeService.getMyLikedPlaylists(userId, cursor, size));
    }

    // ✅ 根据用户ID获取收藏歌单
    @GetMapping("/liked-playlists/{id}")
    public Result getUserLikedPlaylists(@PathVariable Long id, @RequestParam(required = false) String cursor,
                                 @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(playlistLikeService.getMyLikedPlaylists(id, cursor, size));
    }

    // ✅ 我的收藏专辑
    @GetMapping("/liked-albums")
    public Result likedAlbums(@RequestParam(required = false) String cursor,
                              @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(albumLikeService.getMyLikedAlbums(userId, cursor, size));
    }

    // ✅ 根据用户ID获取收藏专辑
    @GetMapping("/liked-albums/{id}")
    public Result getUserLikedAlbums(@PathVariable Long id, @RequestParam(required = false) String cursor,
                              @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(albumLikeService.getMyLikedAlbums(id, cursor, size));
    }


    // ✅ 我的播放历史
    @GetMapping("/play-history")
    public Result playHistory(@RequestParam(required = false) String cursor,
                              @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(playHistoryService.getMyPlayHistory(userId, cursor, size));
    }

    // ========================= ✅ 新增接口 =========================

    // ✅ 当前登录用户信息
    @GetMapping("/me")
    public Result me() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(appUserProfileService.getMe(userId));
    }

    // ✅ 根据用户ID获取用户信息
    @GetMapping("/info/{id}")
    public Result getUserInfo(@PathVariable Long id) {
        return Result.success(appUserProfileService.getUserInfo(id));
    }

    // ✅ 根据用户ID获取用户统计信息
    @GetMapping("/stats/{id}")
    public Result getUserStats(@PathVariable Long id) {
        return Result.success(appUserProfileService.getUserStats(id));
    }

    // ✅ 个人中心统计（喜欢数/歌单数/播放历史数）
    @GetMapping("/stats")
    public Result stats() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(appUserProfileService.getStats(userId));
    }

    // ✅ 我的歌单
    @GetMapping("/playlists")
    public Result playlists() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(playlistService.getMyPlaylists(userId));
    }

    // ✅ 根据用户ID获取用户歌单
    @GetMapping("/playlists/{id}")
    public Result getUserPlaylists(@PathVariable Long id) {
        return Result.success(playlistService.getMyPlaylists(id));
    }

    @GetMapping("/my-playlists")
    public Result getMyCreatedPlaylists() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        List<MyPlaylistVO> playlists = playlistService.getMyPlaylists(userId);  // 通过服务层获取
        return Result.success(playlists);
    }

}
