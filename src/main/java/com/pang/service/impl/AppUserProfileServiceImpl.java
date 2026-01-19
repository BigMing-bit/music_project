package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pang.entity.*;
import com.pang.mapper.*;
import com.pang.service.AppUserProfileService;
import com.pang.entity.vo.AppUserVO;
import com.pang.entity.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserProfileServiceImpl  implements AppUserProfileService {

    private final UserMapper userMapper;
    private final SongLikeMapper songLikeMapper;
    private final PlaylistMapper playlistMapper;


    private final PlaylistFavoriteMapper playlistFavoriteMapper;

    private final AlbumFavoriteMapper albumFavoriteMapper;




    @Override
    public AppUserVO getMe(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return null;

        AppUserVO vo = new AppUserVO();
        BeanUtils.copyProperties(u, vo);
        return vo;
    }

    @Override
    public UserStatsVO getStats(Long userId) {
        UserStatsVO vo = new UserStatsVO();

        // 喜欢歌曲数（status=1）
        Long likeCount = songLikeMapper.selectCount(
                new LambdaQueryWrapper<SongLike>()
                        .eq(SongLike::getUserId, userId)
                        .eq(SongLike::getStatus, 1)
        );


        Long likePlaylistCount = playlistFavoriteMapper.selectCount(
                new LambdaQueryWrapper<PlaylistFavorite>()
                        .eq(PlaylistFavorite::getUserId, userId)
                        .eq(PlaylistFavorite::getStatus, 1)
        );

        // 喜欢专辑数
        Long likeAlbumCount = albumFavoriteMapper.selectCount(
                new LambdaQueryWrapper<AlbumFavorite>()
                        .eq(AlbumFavorite::getUserId, userId)
                        .eq(AlbumFavorite::getStatus, 1)
        );

        // 我创建的歌单数（creatorUserId=userId，且 status=1 或不筛也行）
        Long playlistCount = playlistMapper.selectCount(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getCreatorUserId, userId)
        );

        vo.setLikeSongCount(likeCount);
        vo.setLikePlaylistCount(likePlaylistCount);
        vo.setLikeAlbumCount(likeAlbumCount);
        vo.setPlaylistCount(playlistCount);

        // 你没关注/粉丝表，就先 0
        vo.setFollowCount(0L);
        vo.setFanCount(0L);

        return vo;
    }

    @Override
    public AppUserVO getUserInfo(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return null;

        AppUserVO vo = new AppUserVO();
        BeanUtils.copyProperties(u, vo);
        return vo;
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO vo = new UserStatsVO();

        // 喜欢歌曲数（status=1）
        Long likeCount = songLikeMapper.selectCount(
                new LambdaQueryWrapper<SongLike>()
                        .eq(SongLike::getUserId, userId)
                        .eq(SongLike::getStatus, 1)
        );


        Long likePlaylistCount = playlistFavoriteMapper.selectCount(
                new LambdaQueryWrapper<PlaylistFavorite>()
                        .eq(PlaylistFavorite::getUserId, userId)
                        .eq(PlaylistFavorite::getStatus, 1)
        );

        // 喜欢专辑数
        Long likeAlbumCount = albumFavoriteMapper.selectCount(
                new LambdaQueryWrapper<AlbumFavorite>()
                        .eq(AlbumFavorite::getUserId, userId)
                        .eq(AlbumFavorite::getStatus, 1)
        );

        // 我创建的歌单数（creatorUserId=userId，且 status=1 或不筛也行）
        Long playlistCount = playlistMapper.selectCount(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getCreatorUserId, userId)
        );

        vo.setLikeSongCount(likeCount);
        vo.setLikePlaylistCount(likePlaylistCount);
        vo.setLikeAlbumCount(likeAlbumCount);
        vo.setPlaylistCount(playlistCount);

        // 你没关注/粉丝表，就先 0
        vo.setFollowCount(0L);
        vo.setFanCount(0L);

        return vo;
    }
}
