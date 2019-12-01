package com.gej.poi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel导入导出注解
 * 在需要导出excel的类中加上该注解
 *
 * @Author: 高尔稽 技术交流群;781943947
 * @Date: 2019/11/29 15:23
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Excel {

    /**
     * 该列是否需要时间格式化
     */
    boolean needFormat() default false;

    /**
     * 时间格式化
     */
    String format() default "";

    /**
     * 导出时在excel中每个列的高度 单位为字符，一个汉字=2个字符
     */
    double height() default 10;

    /**
     * 导出时的列名。不可重复
     */
    String name();

    /**
     * 导出时在excel中每个列的宽 单位为字符，一个汉字=2个字符 如 以列名列内容中较合适的长度 例如姓名列6 【姓名一般三个字】
     * 性别列4【男女占1，但是列标题两个汉字】 限制1-255
     */
    double width() default 10;

    /**
     * 是否自动统计数据,如果是统计,true的话在最后追加一行统计,把所有数据求和
     */
    boolean isStatistics() default false;

    /**
     * 是否设置列宽自适应
     */
    boolean autoSize() default false;

}
