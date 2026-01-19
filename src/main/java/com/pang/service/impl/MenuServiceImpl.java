package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SysMenu;
import com.pang.mapper.SysMenuMapper;
import com.pang.service.MenuService;
import com.pang.utils.SaTokenUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements MenuService {

    /**
     * ✅ 菜单管理页：分页（不做权限过滤）
     */
    @Override
    public IPage<SysMenu> adminPage(Integer pageNum, Integer pageSize,
                                    String keyword, Integer status) {

        Page<SysMenu> page = new Page<>(
                pageNum == null || pageNum < 1 ? 1 : pageNum,
                pageSize == null || pageSize < 1 ? 10 : pageSize
        );

        LambdaQueryWrapper<SysMenu> qw = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            qw.like(SysMenu::getMenuName, keyword);
        }
        if (status != null) {
            qw.eq(SysMenu::getStatus, status);
        }

        qw.orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId);

        return this.page(page, qw);
    }
    @Override
    public List<SysMenu> getMenuTree(Integer status) {
        LambdaQueryWrapper<SysMenu> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(SysMenu::getStatus, status);
        }

        qw.in(SysMenu::getMenuType, 1, 2)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId);

        return buildTree(this.list(qw));
    }
    @Override
    public List<SysMenu> getAdminMenuTree() {

        List<String> perms = getSessionPerms();

        List<SysMenu> all = this.lambdaQuery()
                .eq(SysMenu::getStatus, 1)
                .eq(SysMenu::getVisible, 1)
                .in(SysMenu::getMenuType, 1, 2)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId)
                .list();

        // 🔑 核心：权限过滤
        List<SysMenu> filtered = all.stream()
                .filter(m -> {
                    String code = m.getPermissionCode();
                    // 没配权限码的菜单：所有人可见（如 首页）
                    return StrUtil.isBlank(code) || perms.contains(code);
                })
                .toList();

        return buildTree(filtered);
    }
    @Override
    public List<String> getAdminPerms() {
        return getSessionPerms();
    }
    private List<SysMenu> buildTree(List<SysMenu> all) {
        Map<Long, SysMenu> map = all.stream()
                .collect(Collectors.toMap(
                        SysMenu::getId,
                        x -> x,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        map.values().forEach(m -> m.setChildren(new ArrayList<>()));

        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : map.values()) {
            Long pid = m.getParentId();
            if (pid == null || pid == 0L || !map.containsKey(pid)) {
                roots.add(m);
            } else {
                map.get(pid).getChildren().add(m);
            }
        }
        return roots;
    }
    private List<String> getSessionPerms() {
        Object obj = SaTokenUtil.ADMIN.getSession().get("permissions");

        if (obj == null) return List.of();

        if (obj instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (obj instanceof Set<?> set) {
            return set.stream().map(String::valueOf).toList();
        }
        if (obj instanceof String s) {
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .toList();
        }
        return List.of(String.valueOf(obj));
    }
}


