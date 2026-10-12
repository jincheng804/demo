package com.beefarm.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计分析聚合 Mapper：直接查库返回 Map，不依赖业务 Service
 */
@Mapper
public interface ReportMapper {

    /** 看板核心指标（单 SQL 一次聚合） */
    Map<String, Object> selectDashboardStats(@Param("monthStart") LocalDate monthStart,
                                             @Param("monthEnd") LocalDate monthEnd);

    /** 近12月采收重量 */
    List<Map<String, Object>> selectHarvestTrend(@Param("begin") LocalDate begin,
                                                 @Param("end") LocalDate end);

    /** 蜜种分布 */
    List<Map<String, Object>> selectHoneyPie();

    /** 蜂箱状态分布 */
    List<Map<String, Object>> selectHiveStatus();

    /** 近12月出库（仅已审核） */
    List<Map<String, Object>> selectOutputTrend(@Param("begin") LocalDate begin,
                                                @Param("end") LocalDate end);

    /** 近30天扫码 */
    List<Map<String, Object>> selectScanTrend(@Param("begin") LocalDate begin);

    /** 库存预警 Top */
    List<Map<String, Object>> selectWarnTop();

    /** 产量报表 dim=month/hive/source */
    List<Map<String, Object>> selectProductionReport(@Param("dim") String dim,
                                                     @Param("begin") LocalDate begin,
                                                     @Param("end") LocalDate end);

    /** 销售报表 dim=month/customer/product */
    List<Map<String, Object>> selectSalesReport(@Param("dim") String dim,
                                                @Param("begin") LocalDate begin,
                                                @Param("end") LocalDate end);

    /** 库存报表（联仓库名称） */
    List<Map<String, Object>> selectInventoryReport();

    /** 成本按类型 */
    List<Map<String, Object>> selectCostByType(@Param("begin") LocalDate begin,
                                               @Param("end") LocalDate end);

    /** 产值按月 */
    List<Map<String, Object>> selectOutputByMonth(@Param("begin") LocalDate begin,
                                                 @Param("end") LocalDate end);

    /** 成本合计 */
    Map<String, Object> selectProfitSummary(@Param("begin") LocalDate begin,
                                             @Param("end") LocalDate end);
}
