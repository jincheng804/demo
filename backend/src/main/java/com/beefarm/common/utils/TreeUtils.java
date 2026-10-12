package com.beefarm.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树形构建工具
 */
public class TreeUtils {

    public interface TreeNode<T, ID> {
        ID getId(T node);
        ID getParentId(T node);
        void setChildren(T node, List<T> children);
    }

    public static <T, ID> List<T> build(List<T> list, ID rootParentId, TreeNode<T, ID> node) {
        // 按父ID分组
        var grouped = list.stream()
                .collect(Collectors.groupingBy(n -> node.getParentId(n)));
        // 递归设置子节点
        list.forEach(n -> {
            List<T> children = grouped.getOrDefault(node.getId(n), new ArrayList<>());
            node.setChildren(n, children.isEmpty() ? null : children);
        });
        return grouped.getOrDefault(rootParentId, new ArrayList<>());
    }
}
