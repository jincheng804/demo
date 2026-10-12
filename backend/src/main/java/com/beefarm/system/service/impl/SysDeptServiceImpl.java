package com.beefarm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.utils.TreeUtils;
import com.beefarm.system.entity.SysDept;
import com.beefarm.system.mapper.SysDeptMapper;
import com.beefarm.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {

    @Override
    public List<SysDept> treeList() {
        List<SysDept> list = baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getOrderNum));
        return TreeUtils.build(list, 0L, new TreeUtils.TreeNode<>() {
            @Override
            public Long getId(SysDept node) {
                return node.getId();
            }

            @Override
            public Long getParentId(SysDept node) {
                return node.getParentId();
            }

            @Override
            public void setChildren(SysDept node, List<SysDept> children) {
                node.setChildren(children);
            }
        });
    }

    @Override
    public void saveDept(SysDept dept) {
        if (dept.getId() == null) {
            baseMapper.insert(dept);
        } else {
            baseMapper.updateById(dept);
        }
    }
}
