package com.beefarm.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.system.entity.SysLoginLog;
import com.beefarm.system.entity.SysOperLog;
import com.beefarm.system.mapper.SysLoginLogMapper;
import com.beefarm.system.mapper.SysOperLogMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    @PreAuthorize("@ss.hasPermi('operlog:list')")
    @GetMapping("/oper/list")
    public TableDataInfo<SysOperLog> operList(SysOperLog query,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysOperLog>(pageNum, pageSize);
        return TableDataInfo.build(operLogMapper.selectPage(page,
                new LambdaQueryWrapper<SysOperLog>()
                        .like(query.getTitle() != null, SysOperLog::getTitle, query.getTitle())
                        .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                        .orderByDesc(SysOperLog::getOperTime)));
    }

    @PreAuthorize("@ss.hasPermi('loginlog:list')")
    @GetMapping("/login/list")
    public TableDataInfo<SysLoginLog> loginList(SysLoginLog query,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysLoginLog>(pageNum, pageSize);
        return TableDataInfo.build(loginLogMapper.selectPage(page,
                new LambdaQueryWrapper<SysLoginLog>()
                        .like(query.getUsername() != null, SysLoginLog::getUsername, query.getUsername())
                        .orderByDesc(SysLoginLog::getLoginTime)));
    }
}
