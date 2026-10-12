package com.beefarm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.common.utils.TreeUtils;
import com.beefarm.system.entity.SysMenu;
import com.beefarm.system.mapper.SysMenuMapper;
import com.beefarm.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> selectMenusByCurrentUser() {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin()) {
            return menuMapper.selectAllMenus();
        }
        return menuMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> buildRouters() {
        List<SysMenu> menus = selectMenusByCurrentUser();
        List<SysMenu> tree = buildMenuTree(menus);
        List<Map<String, Object>> routers = new ArrayList<>();
        for (SysMenu menu : tree) {
            routers.add(convertRouter(menu));
        }
        return routers;
    }

    private Map<String, Object> convertRouter(SysMenu menu) {
        Map<String, Object> router = new LinkedHashMap<>();
        boolean isTop = menu.getParentId() == null || menu.getParentId() == 0L;
        // 路径：顶级目录补 /
        String path = menu.getPath();
        if (path == null) {
            path = "";
        }
        // 去掉多余的前导斜杠
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        // 顶级节点（无论目录/菜单）统一补 "/"；子节点保持相对路径
        if (isTop) {
            path = "/" + path;
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", menu.getMenuName());
        meta.put("icon", menu.getIcon());
        meta.put("noCache", false);

        // 顶级单页菜单（type=C）：用 Layout 包裹，自身作为子路由，避免渲染成空壳
        if (isTop && "C".equals(menu.getMenuType())) {
            router.put("name", "Menu" + menu.getId());
            router.put("path", path);
            router.put("component", "Layout");
            router.put("redirect", path + "/index");
            router.put("hidden", menu.getVisible() != null && menu.getVisible() == 0);
            router.put("meta", meta);

            Map<String, Object> child = new LinkedHashMap<>();
            child.put("name", "Page" + menu.getId());
            child.put("path", "index");
            child.put("component", menu.getComponent());
            child.put("hidden", false);
            Map<String, Object> childMeta = new HashMap<>();
            childMeta.put("title", menu.getMenuName());
            childMeta.put("icon", menu.getIcon());
            childMeta.put("noCache", false);
            child.put("meta", childMeta);

            List<Map<String, Object>> children = new ArrayList<>();
            children.add(child);
            router.put("children", children);
            return router;
        }

        router.put("name", "Menu" + menu.getId());
        router.put("path", path);
        if (isTop) {
            router.put("component", "M".equals(menu.getMenuType()) ? "Layout" : "InnerLink");
        } else if (menu.getComponent() != null && !menu.getComponent().isEmpty()) {
            router.put("component", menu.getComponent());
        } else {
            router.put("component", "ParentView");
        }
        router.put("hidden", menu.getVisible() != null && menu.getVisible() == 0);
        router.put("redirect", "noRedirect");
        router.put("meta", meta);

        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (SysMenu child : menu.getChildren()) {
                children.add(convertRouter(child));
            }
            router.put("children", children);
        }
        return router;
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        return TreeUtils.build(menus, 0L, new TreeUtils.TreeNode<>() {
            @Override
            public Long getId(SysMenu node) {
                return node.getId();
            }

            @Override
            public Long getParentId(SysMenu node) {
                return node.getParentId();
            }

            @Override
            public void setChildren(SysMenu node, List<SysMenu> children) {
                node.setChildren(children);
            }
        });
    }

    @Override
    public void saveMenu(SysMenu menu) {
        if (menu.getId() == null) {
            menuMapper.insert(menu);
        } else {
            menuMapper.updateById(menu);
        }
    }
}
