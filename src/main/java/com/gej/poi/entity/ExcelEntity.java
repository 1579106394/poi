package com.gej.poi.entity;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * Excel注解对应的实体类
 *
 * @Author: 高尔稽 技术交流群;781943947
 * @Date: 2019/11/30 9:13
 * @Version 1.0
 */
@Data
public class ExcelEntity {

    private double width = 10;

    private double height = 10;

    /**
     * 统计
     */
    private boolean isStatistics;

    /**
     * 对应name
     */
    protected String name;
    /**
     * 导出日期格式
     */
    private String format;

    /**
     * 数据的字段
     */
    private Field dataField;

    /**
     * 自适应列宽
     */
    private boolean autoSize;

}
