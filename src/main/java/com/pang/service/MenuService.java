package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.pang.entity.SysMenu;

import java.util.List;

public interface MenuService extends IService<SysMenu> {
    IPage<SysMenu> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status);
    List<SysMenu> getMenuTree(Integer status);

    List<SysMenu> getAdminMenuTree();
    List<String> getAdminPerms();   // ✅ 就加这个


}
