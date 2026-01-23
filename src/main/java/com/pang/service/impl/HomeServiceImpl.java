package com.pang.service.impl;

import com.pang.entity.vo.*;
import com.pang.mapper.*;
import com.pang.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;


@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeMapper homeMapper;

    @Override
    public HomeResultVo getHome() {
        HomeResultVo vo = new HomeResultVo();
        vo.setBanners(getBanners(null, 6));        // 轮播图一般 4~8
        vo.setNotices(getNotices(null, 6));        // 公告 5~10
        vo.setPlaylists(getHotPlaylists(null, 25));
        vo.setAlbums(getHotAlbums(null, 10));
        vo.setHotSongs(getHotSongs(null, 9));
        return vo;
    }

    @Override
    public CursorPageResult<BannerHomeVo> getBanners(String cursor, Integer size) {
        Long[] c = parseCursor(cursor);
        int s = safeSize(size, 6);
        List<BannerHomeVo> list = homeMapper.banners(c[0], c[1], s);

        CursorPageResult<BannerHomeVo> r = new CursorPageResult<>();
        r.setList(list);
        r.setHasMore(list != null && list.size() == s);
        if (list != null && !list.isEmpty()) {
            BannerHomeVo last = list.get(list.size() - 1);
            r.setNextCursor(last.getScore() + "_" + last.getId());
        }
        return r;
    }


    @Override
    public CursorPageResult<NoticeHomeVo> getNotices(String cursor, Integer size) {
        Long[] c = parseCursor(cursor);
        int s = safeSize(size, 6);
        List<NoticeHomeVo> list = homeMapper.notices(c[0], c[1], s);

        CursorPageResult<NoticeHomeVo> r = new CursorPageResult<>();
        r.setList(list);
        r.setHasMore(list != null && list.size() == s);
        if (list != null && !list.isEmpty()) {
            NoticeHomeVo last = list.get(list.size() - 1);
            r.setNextCursor(last.getScore() + "_" + last.getId());
        }
        return r;
    }

    @Override
    public CursorPageResult<PlaylistHomeVo> getHotPlaylists(String cursor, Integer size) {
        Long[] c = parseCursor(cursor); // [score, id]
        List<PlaylistHomeVo> list = homeMapper.hotPlaylists(c[0], c[1], safeSize(size, 25));
        return toCursorPage(list);
    }

    @Override
    public CursorPageResult<AlbumHomeVo> getHotAlbums(String cursor, Integer size) {
        Long[] c = parseCursor(cursor);
        List<AlbumHomeVo> list = homeMapper.hotAlbums(c[0], c[1], safeSize(size, 10));
        return toCursorPage(list);
    }

    @Override
    public CursorPageResult<SongHomeVo> getHotSongs(String cursor, Integer size) {
        Long[] c = parseCursor(cursor);
        List<SongHomeVo> list = homeMapper.hotSongs(c[0], c[1], safeSize(size, 9));
        return toCursorPage(list);
    }

    private int safeSize(Integer size, int def) {
        if (size == null || size < 1) return def;
        return Math.min(size, 50);
    }


    // cursor: "score_id"
    private Long[] parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return new Long[]{null, null};
        String[] arr = cursor.split("_");
        if (arr.length != 2) return new Long[]{null, null};
        try {
            return new Long[]{Long.parseLong(arr[0]), Long.parseLong(arr[1])};
        } catch (Exception e) {
            return new Long[]{null, null};
        }
    }

    private <T> CursorPageResult<T> toCursorPage(List<T> list) {
        CursorPageResult<T> r = new CursorPageResult<>();
        r.setList(list);
        r.setHasMore(list != null && !list.isEmpty());

        if (list == null || list.isEmpty()) {
            r.setNextCursor(null);
            r.setHasMore(false);
            return r;
        }
        T last = list.get(list.size() - 1);
        try {
            Long score = (Long) last.getClass().getMethod("getScore").invoke(last);
            Long id = (Long) last.getClass().getMethod("getId").invoke(last);
            r.setNextCursor(score + "_" + id);
        } catch (Exception e) {
            r.setNextCursor(null);
        }
        return r;
    }
}
