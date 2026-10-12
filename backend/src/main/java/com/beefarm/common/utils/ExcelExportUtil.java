package com.beefarm.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具（基于 Apache POI SXSSF，流式低内存）
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    /**
     * 导出单个 sheet
     *
     * @param response 响应
     * @param fileName 文件名(不含后缀)
     * @param title    表头大标题(可为 null)
     * @param headers  列标题
     * @param rows     数据行(与列顺序一致)
     */
    public static void export(HttpServletResponse response, String fileName, String title,
                              String[] headers, List<Object[]> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(500)) {
            Sheet sheet = workbook.createSheet(fileName);
            CellStyle headStyle = buildHeadStyle(workbook);
            CellStyle titleStyle = buildTitleStyle(workbook);
            int rowIndex = 0;

            // 大标题
            if (title != null) {
                Row t = sheet.createRow(rowIndex++);
                Cell c = t.createCell(0);
                c.setCellValue(title);
                c.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
            }

            // 表头
            Row head = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell c = head.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headStyle);
            }

            // 数据
            if (rows != null) {
                for (Object[] data : rows) {
                    Row r = sheet.createRow(rowIndex++);
                    for (int i = 0; i < headers.length; i++) {
                        Cell c = r.createCell(i);
                        setCell(c, i < data.length ? data[i] : null);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }

            setResponseHeader(response, fileName);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            workbook.dispose();
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    private static void setCell(Cell c, Object value) {
        if (value == null) {
            c.setCellValue("");
        } else if (value instanceof Number n) {
            c.setCellValue(n.doubleValue());
        } else {
            c.setCellValue(value.toString());
        }
    }

    private static void setResponseHeader(HttpServletResponse response, String fileName)
            throws IOException {
        String name = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename=" + name + ";filename*=UTF-8''" + name);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    private static CellStyle buildHeadStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle buildTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }
}
