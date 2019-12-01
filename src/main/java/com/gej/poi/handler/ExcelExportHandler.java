package com.gej.poi.handler;

import com.gej.poi.annotation.Excel;
import com.gej.poi.entity.ExcelEntity;
import com.gej.poi.entity.ExportParams;
import com.gej.poi.utils.ReflectionUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel处理器
 *
 * @Author: 高尔稽 技术交流群;781943947
 * @Date: 2019/11/29 16:42
 * @Version 1.0
 */
@Slf4j
@Data
public final class ExcelExportHandler {

    /**
     * 合并数据
     */
    private Map<Integer, Double> statistics = new HashMap<>();

    /**
     * 当前索引位置
     */
    private int currentIndex = 0;

    /**
     * 默认最大行数。超过自动多Sheet
     */
    private static final int MAX_NUM = 60000;

    /**
     * 导出Excel
     *
     * @param exportParams
     * @param pojoClass
     * @param datas
     * @return
     */
    public Workbook createSheet(ExportParams exportParams, Class<?> pojoClass, Collection<?> datas) {
        Workbook workbook = new XSSFWorkbook();
        createSheet(workbook, exportParams, pojoClass, datas);
        return workbook;
    }

    /**
     * 创建一个标签页
     *
     * @param workbook
     * @param exportParams 表格标题属性
     * @param pojoClass    对象Class
     * @param datas        对象数据
     */
    private void createSheet(Workbook workbook, ExportParams exportParams, Class<?> pojoClass, Collection<?> datas) {
        if (workbook == null || exportParams == null || pojoClass == null || datas == null) {
            throw new RuntimeException();
        }
        Sheet sheet = null;
        try {
            sheet = workbook.createSheet(exportParams.getSheetName());
        } catch (Exception e) {
            // 重复遍历,出现了重名现象,名称+1
            int sheetIndex = exportParams.getSheetIndex();
            sheet = workbook.createSheet(exportParams.getSheetName() + sheetIndex++);
            exportParams.setSheetIndex(sheetIndex);
        }
        try {
            Drawing patriarch = sheet.createDrawingPatriarch();
            // 得到所有字段
            List<Field> fields = ReflectionUtils.getFields(pojoClass);
            // 导出实体集合
            List<ExcelEntity> excelList = new ArrayList<>();
            // 获取所有需要导出的字段
            getAllExcelField(fields, excelList);
            int index = 0;
            if (exportParams.isCreateHeadRows()) {
                index = createHeaderAndTitle(exportParams, sheet, workbook, excelList);
            }
            // 设置列宽
            setCellWith(excelList, sheet);
            // 设置行高
            short rowHeight = getRowHeight(excelList);
            setCurrentIndex(1);
            Iterator<?> its = datas.iterator();
            List<Object> tempList = new ArrayList<>();
            while (its.hasNext()) {
                Object data = its.next();
                index += createCells(patriarch, index, data, excelList, sheet, workbook, rowHeight);
                tempList.add(data);
                if (index >= MAX_NUM) {
                    break;
                }
            }

            its = datas.iterator();
            for (int i = 0, le = tempList.size(); i < le; i++) {
                its.next();
                its.remove();
            }
            // 创建合计信息
            addStatisticsRow(sheet);
            // 设置自适应列宽
            setSizeColumn(sheet, excelList);
            // 发现还有剩余list 继续循环创建Sheet
            if (datas.size() > 0) {
                createSheet(workbook, exportParams, pojoClass, datas);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建合计列
     *
     * @param sheet
     */
    private void addStatisticsRow(Sheet sheet) {
        if (statistics.size() > 0) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = statistics.keySet();
            createStringCell(row, 0, "合计", null, null);
            for (Integer key : keys) {
                createStringCell(row, key, new DecimalFormat("######0.00").format(statistics.get(key)), null, null);
            }
            statistics.clear();
        }
    }

    /**
     * 创建单元格
     *
     * @param patriarch
     * @param index
     * @param data
     * @param excelList
     * @param sheet
     * @param workbook
     * @param rowHeight
     * @return
     */
    private int createCells(Drawing patriarch, int index, Object data, List<ExcelEntity> excelList, Sheet sheet, Workbook workbook, short rowHeight) throws Exception {
        ExcelEntity entity;
        Row row = sheet.createRow(index);
        row.setHeight(rowHeight);
        int maxHeight = 1, cellNum = 0;
        for (ExcelEntity excelEntity : excelList) {
            entity = excelEntity;
            Object value = getCellValue(entity, data);
            createStringCell(row, cellNum++, value.toString(), entity, null);
        }
        return maxHeight;
    }

    /**
     * 获取单元格的值
     *
     * @param entity
     * @param obj
     * @return
     */
    private Object getCellValue(ExcelEntity entity, Object obj) throws Exception {
        Object value = ReflectionUtils.getFieldValue(obj, entity.getDataField().getName());
        if (entity.getFormat() != null && !"".equals(entity.getFormat())) {
            // 需要格式化日期
            value = formatValue(value, entity);
        }
        if (value == null) {
            value = "";
        }
        return value.toString();
    }

    /**
     * 格式化日期
     *
     * @param value
     * @param entity
     * @return
     */
    private Object formatValue(Object value, ExcelEntity entity) throws Exception {
        Date temp = null;
        if (value instanceof String) {
            SimpleDateFormat format = new SimpleDateFormat(entity.getFormat());
            temp = format.parse(value.toString());
        } else if (value instanceof Date) {
            temp = (Date) value;
        }
        if (temp != null) {
            SimpleDateFormat format = new SimpleDateFormat(entity.getFormat());
            value = format.format(temp);
        }
        return value;
    }

    /**
     * 设置行高
     *
     * @param excelList
     * @return
     */
    private short getRowHeight(List<ExcelEntity> excelList) {
        double maxHeight = 0;
        for (ExcelEntity excelEntity : excelList) {
            maxHeight = Math.max(maxHeight, excelEntity.getHeight());
        }
        return (short) (maxHeight * 50);
    }

    /**
     * 设置列宽
     *
     * @param excelList
     * @param sheet
     */
    private void setCellWith(List<ExcelEntity> excelList, Sheet sheet) {
        int index = 0;
        for (ExcelEntity excelEntity : excelList) {
            if (!excelEntity.isAutoSize()) {
                sheet.setColumnWidth(index, (int) (256 * excelEntity.getWidth()));
            }
            index++;
        }
    }

    /**
     * 创建表头和标题
     *
     * @param entity
     * @param sheet
     * @param workbook
     * @param excelList
     */
    private int createHeaderAndTitle(ExportParams entity, Sheet sheet, Workbook workbook, List<ExcelEntity> excelList) {
        int rows = 0;
        // 获取字段的总长度
        int fieldWidth = getFieldWidth(excelList);
        if (entity.getTitle() != null) {
            rows += createHeaderRow(entity, sheet, workbook, fieldWidth);
        }
        rows += createTitleRow(entity, sheet, workbook, rows, excelList);
        sheet.createFreezePane(0, rows, 0, rows);
        return rows;
    }

    /**
     * 创建标题行
     *
     * @param title
     * @param sheet
     * @param workbook
     * @param index
     * @param excelParams
     * @return
     */
    private int createTitleRow(ExportParams title, Sheet sheet, Workbook workbook, int index, List<ExcelEntity> excelParams) {
        Row row = sheet.createRow(index);
        int rows = 1;
        row.setHeight((short) 450);
        int cellIndex = 0;
        // 循环创建单元格
        for (ExcelEntity entity : excelParams) {
            if (entity.getName() != null && !"".equals(entity.getName())) {
                createStringCell(row, cellIndex, entity.getName(), entity, null);
            }
            cellIndex++;
        }
        return rows;
    }

    /**
     * 设置字段总长度
     *
     * @param excelList
     * @return
     */
    private int getFieldWidth(List<ExcelEntity> excelList) {
        // 从0开始计算单元格的
        int length = -1;
        for (ExcelEntity entity : excelList) {
            length += 1;
        }
        return length;
    }

    /**
     * 创建 表头
     *
     * @param entity
     * @param sheet
     * @param workbook
     * @param fieldWidth
     */
    private int createHeaderRow(ExportParams entity, Sheet sheet, Workbook workbook, int fieldWidth) {
        Row row = sheet.createRow(0);
        CellStyle style = getHeaderStyle(workbook);
        row.setHeight(entity.getTitleHeight());
        createStringCell(row, 0, entity.getTitle(), null, style);
        for (int i = 1; i <= fieldWidth; i++) {
            createStringCell(row, i, "");
        }
        // 合并表头
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, fieldWidth));
        // 子标题
        if (entity.getSecondTitle() != null) {
            row = sheet.createRow(1);
            row.setHeight(entity.getSecondTitleHeight());
            createStringCell(row, 0, entity.getSecondTitle());
            row.setRowStyle(style);
            for (int i = 1; i <= fieldWidth; i++) {
                createStringCell(row, i, "", null, style);
            }
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, fieldWidth));
            return 2;
        }
        return 1;
    }

    /**
     * 设置表头样式
     *
     * @param workbook
     * @return
     */
    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 水平居中
        style.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("黑体");
        font.setFontHeight((short) (24 * 20));
        style.setFont(font);
        return style;
    }

    /**
     * 创建文本类型的Cell
     *
     * @param row
     * @param index
     * @param text
     */
    private void createStringCell(Row row, int index, String text) {
        createStringCell(row, index, text, null, null);
    }

    /**
     * 创建文本类型的Cell
     *
     * @param row
     * @param index
     * @param text
     */
    private void createStringCell(Row row, int index, String text, ExcelEntity excelEntity, CellStyle style) {
        Cell cell = row.createCell(index);
        RichTextString rts = new XSSFRichTextString(text);
        cell.setCellValue(rts);
        cell.setCellStyle(style);
        addStatisticsData(index, text, excelEntity);
    }

    /**
     * 设置是否需要统计
     *
     * @param index
     * @param text
     * @param excelEntity
     */
    private void addStatisticsData(int index, String text, ExcelEntity excelEntity) {
        if (excelEntity != null && excelEntity.isStatistics()) {
            Double temp = 0D;
            if (!statistics.containsKey(index)) {
                statistics.put(index, temp);
            }
            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException e) {
            }
            statistics.put(index, statistics.get(index) + temp);
        }
    }

    /**
     * 获取所有需要导出的列
     *
     * @param fields
     * @param excelList
     */
    private void getAllExcelField(List<Field> fields, List<ExcelEntity> excelList) {
        for (Field field : fields) {
            Excel excelAnnotation = field.getAnnotation(Excel.class);
            if (excelAnnotation != null) {
                // 有注解，设置实体
                excelList.add(createdExcelEntity(field));
            }
        }
    }

    /**
     * 创建Excel导出实体
     *
     * @param field
     * @return
     */
    private ExcelEntity createdExcelEntity(Field field) {
        Excel excel = field.getAnnotation(Excel.class);
        ExcelEntity excelEntity = new ExcelEntity();
        if (excel.needFormat()) {
            excelEntity.setFormat(excel.format());
        }
        excelEntity.setHeight(excel.height());
        excelEntity.setName(excel.name());
        excelEntity.setStatistics(excel.isStatistics());
        excelEntity.setWidth(excel.width());
        excelEntity.setDataField(field);
        excelEntity.setAutoSize(excel.autoSize());
        return excelEntity;
    }

    /**
     * 设置自适应列宽
     *
     * @param sheet
     * @param excelList
     */
    private void setSizeColumn(Sheet sheet, List<ExcelEntity> excelList) {
        if (excelList != null) {
            for (int i = 0; i < excelList.size(); i++) {
                ExcelEntity excelEntity = excelList.get(i);
                if (excelEntity.isAutoSize()) {
                    // 处理列宽，支持中文
                    this.setSizeColumn(sheet, i);
                }
            }
        }
    }

    /**
     * 自适应宽度(中文支持)
     *
     * @param sheet
     * @param columnNum
     */
    private void setSizeColumn(Sheet sheet, int columnNum) {
        int columnWidth = sheet.getColumnWidth(columnNum) / 256;
        for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
            Row currentRow;
            // 当前行未被使用过
            if (sheet.getRow(rowNum) == null) {
                continue;
            } else {
                currentRow = sheet.getRow(rowNum);
            }

            if (currentRow.getCell(columnNum) != null) {
                Cell currentCell = currentRow.getCell(columnNum);
                if (currentCell.getCellType() == CellType.STRING) {
                    int length = 0;
                    try {
                        length = currentCell.getStringCellValue().getBytes("GBK").length;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (columnWidth < length) {
                        columnWidth = length;
                    }
                }
            }
        }
        // +1为了防止上面除的时候有误差
        sheet.setColumnWidth(columnNum, (columnWidth + 1) * 256);
    }

}
