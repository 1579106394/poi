package com.gej.poi.entity;

import lombok.Data;

/**
 * 导入参数设置
 *
 * @author
 * @date
 */
@Data
public class ImportParams {
    /**
     * 表格标题行数
     * 这里的标题指的是列标题，不是表头
     */
    private int titleRows = 1;
    /**
     * 表头行数
     */
    private int headRows = 0;
    /**
     * 上传表格需要读取的sheet 数量,默认为1
     */
    private int sheetNum = 1;

    public ImportParams() {
    }

    public ImportParams(int titleRows, int headRows) {
        this.titleRows = titleRows;
        this.headRows = headRows;
    }

    public ImportParams(int sheetNum) {
        this.sheetNum = sheetNum;
    }

}
