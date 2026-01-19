package com.pang.service;


import com.pang.entity.vo.AppUserVO;
import com.pang.entity.vo.UserStatsVO;

public interface AppUserProfileService {
    AppUserVO getMe(Long userId);
    UserStatsVO getStats(Long userId);
    UserStatsVO getUserStats(Long userId);
    AppUserVO getUserInfo(Long userId);


}
