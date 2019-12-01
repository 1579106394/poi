package com.gej.poi.handler;

import com.gej.poi.annotation.Excel;
import com.gej.poi.constant.JavaClassConstant;
import com.gej.poi.entity.ExcelEntity;
import com.gej.poi.entity.ImportParams;
import com.gej.poi.utils.ReflectionUtils;
import com.gej.poi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入工具
 *
 * @author
 * @version 1.0
 * @date
 */
@Slf4j
public final class ExcelImportHandler {

    /**
     * 通过输入流解析Excel，返回解析后的数据
     *
     * @param pojoClass
     * @param params
     * @return
     * @throws Exception
     */
    public <T> List<T> importExcel(InputStream inputStream, Class<T> pojoClass, ImportParams params) throws Exception {
        List<T> result = new ArrayList<T>();
        Workbook book = new XSSFWorkbook(OPCPackage.open(inputStream));
        for (int i = 0; i < params.getSheetNum(); i++) {
            // 导入这一个Sheet的数据
            result.addAll(importExcel(result, book.getSheetAt(i), pojoClass, params));
        }
        return result;
    }

    /**
     * 处理数据并存放到集合中
     *
     * @param result
     * @param sheet
     * @param pojoClass
     * @param params
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> List<T> importExcel(Collection<T> result, Sheet sheet, Class<T> pojoClass, ImportParams params) throws Exception {
        List<T> collection = Lists.newArrayList();
        // 存储导入的字段，。使用Map，key是列名，和excel对应
        Map<String, ExcelEntity> excelParams = new HashMap<>();
        // 导入的对象数据不是map时才进行操作
        if (!Map.class.equals(pojoClass)) {
            List<Field> fieldList = ReflectionUtils.getFields(pojoClass);
            getAllExcelField(fieldList, excelParams);
        }
        Iterator<Row> rows = sheet.rowIterator();
        // 获取每一列的标题
        Map<Integer, String> titleMap = getTitleMap(sheet, params);
        Row row;
        // 跳过表头和标题行
        for (int j = 0; j <= params.getTitleRows() + params.getHeadRows(); j++) {
            row = rows.next();
        }
        T rowData;
        int errNum = 0;
        while (rows.hasNext()) {
            row = rows.next();
            // 创建对象
            rowData = pojoClass.newInstance();
            try {
                for (int i = row.getFirstCellNum(), le = row.getLastCellNum(); i < le; i++) {
                    Cell cell = row.getCell(i);
                    String titleString = titleMap.get(i);
                    if (excelParams.containsKey(titleString)) {
                        // 如果excelParams里有这个标题，就设置一下字段值
                        saveFieldValue(rowData, cell, excelParams, titleString);
                    }
                }
                collection.add(rowData);
            } catch (Exception e) {
                e.printStackTrace();
                errNum++;
                // 失败次数大于5次就跳过
                if (errNum >= 5) {
                    break;
                }
            }
        }
        return collection;
    }

    /**
     * 设置字段值
     *
     * @param rowData
     * @param cell
     * @param excelParams
     * @param titleString
     * @param <T>
     */
    private <T> void saveFieldValue(T rowData, Cell cell, Map<String, ExcelEntity> excelParams, String titleString) {
        // 拿到这列的实体
        ExcelEntity excelEntity = excelParams.get(titleString);
        // 拿到字段
        Field dataField = excelEntity.getDataField();
        // 拿到值
        Object value = getValue(cell, excelEntity);
        // 设置值
        ReflectionUtils.setFieldValue(rowData, dataField.getName(), value);
    }

    /**
     * 获取字段值
     *
     * @param cell
     * @param excelParam
     * @param <T>
     * @return
     */
    public <T> Object getValue(Cell cell, ExcelEntity excelParam) {
        // 获取字段类型
        Field dataField = excelParam.getDataField();
        Type type = dataField.getGenericType();
        String fieldClass = type.toString();
        Object result = getCellValue(cell);
        return getValueByType(fieldClass, result);
    }

    /**
     * 获取每列的标题
     *
     * @param sheet
     * @param params
     * @return
     */
    private Map<Integer, String> getTitleMap(Sheet sheet, ImportParams params) {
        Map<Integer, String> titleMap = new HashMap<>();
        Iterator<Cell> cellTitle;
        // 标题行
        Row headRow = null;
        int headBegin = params.getTitleRows();
        // 找到首行表头，每个sheet都必须至少有一行表头
        while (headRow == null) {
            headRow = sheet.getRow(headBegin++);
        }
        cellTitle = headRow.cellIterator();
        while (cellTitle.hasNext()) {
            Cell cell = cellTitle.next();
            String value = getStringValue(cell);
            if (value != null) {
                //加入表头列表
                titleMap.put(cell.getColumnIndex(), value);
            }
        }
        return titleMap;
    }

    /**
     * 获取字符串类型的值
     *
     * @param cell
     * @return
     * @Author JueYue
     * @date 2013-11-21
     */
    private String getStringValue(Cell cell) {
        Object obj = getCellValue(cell);
        return obj == null ? null : obj.toString().trim();
    }

    private Object getCellValue(Cell cell) {
        Object obj = null;
        switch (cell.getCellType()) {
            case STRING:
                obj = cell.getStringCellValue();
                break;
            case BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                obj = cell.getNumericCellValue();
                break;
            case FORMULA:
                obj = cell.getCellFormula();
                break;
            default:
                break;
        }
        return obj;
    }

    /**
     * 根据返回类型获取返回值
     *
     * @param fieldClass
     * @param result
     * @return
     */
    private Object getValueByType(String fieldClass, Object result) {
        if (JavaClassConstant.DATE_CLASS.equals(fieldClass)) {
            return result;
        }
        if (JavaClassConstant.BOOLEAN_CLASS.equals(fieldClass) || JavaClassConstant.BOOLEAN_BASE.equals(fieldClass)) {
            return Boolean.valueOf(String.valueOf(result));
        }
        if (JavaClassConstant.DOUBLE_CLASS.equals(fieldClass) || JavaClassConstant.DOUBLE_BASE.equals(fieldClass)) {
            return Double.valueOf(String.valueOf(result));
        }
        if (JavaClassConstant.LONG_CLASS.equals(fieldClass) || JavaClassConstant.LONG_BASE.equals(fieldClass)) {
            return Long.valueOf(String.valueOf(result));
        }
        if (JavaClassConstant.FLOAT_CLASS.equals(fieldClass) || JavaClassConstant.FLOAT_BASE.equals(fieldClass)) {
            return Float.valueOf(String.valueOf(result));
        }
        if (JavaClassConstant.INTEGER_CLASS.equals(fieldClass) || JavaClassConstant.INT_BASE.equals(fieldClass)) {
            return Integer.valueOf(String.valueOf(result));
        }
        if (JavaClassConstant.BIG_DECIMAL_CLASS.equals(fieldClass)) {
            return new BigDecimal(String.valueOf(result));
        }
        if (JavaClassConstant.STRING_CLASS.equals(fieldClass)) {
            // 针对String 类型,但是Excel获取的数据却不是String,比如Double类型,防止科学计数法
            if (result instanceof String) {
                return result;
            }
            // double类型防止科学计数法
            if (result instanceof Double) {
                return StringUtils.doubleToString((Double) result);
            }
            return String.valueOf(result);
        }
        return result;
    }

    /**
     * 获取所有需要导入的列
     *
     * @param fields
     * @param excelParams
     */
    private void getAllExcelField(List<Field> fields, Map<String, ExcelEntity> excelParams) {
        for (Field field : fields) {
            Excel excelAnnotation = field.getAnnotation(Excel.class);
            if (excelAnnotation != null) {
                // 有注解，设置实体
                addEntityToMap(field, excelParams);
            }
        }
    }

    /**
     * 把这个注解解析放到类型对象中
     *
     * @param field
     */
    private void addEntityToMap(Field field, Map<String, ExcelEntity> excelParams) {
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
        excelParams.put(excelEntity.getName(), excelEntity);

    }

}
