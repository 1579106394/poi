package com.gej.poi;

import com.gej.poi.entity.ExportParams;
import com.gej.poi.entity.ImportParams;
import com.gej.poi.handler.ExcelExportHandler;
import com.gej.poi.handler.ExcelImportHandler;
import com.gej.poi.mapper.ApiMapper;
import com.gej.poi.pojo.ApiLog;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author: 高尔稽 技术交流群;781943947
 * @Date: 2019/11/30 11:23
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExportTest {

    @Autowired
    private ApiMapper apiMapper;

    /**
     * 导出测试
     * @throws Exception
     */
    @Test
    public void testExportLog() throws Exception {
        List<ApiLog> list = apiMapper.findAll();
        Workbook workbook = new ExcelExportHandler().createSheet(new ExportParams("测试导出", "最新日志"), ApiLog.class, list);
        OutputStream outputStream = new FileOutputStream(new File("D:/测试.xlsx"));
        workbook.write(outputStream);
    }

    /**
     * 导入测试
     * @throws Exception
     */
    @Test
    public void testImportLog() throws Exception {
        InputStream inputStream = new FileInputStream(new File("D:/测试.xlsx"));
        List<ApiLog> apiLogs = new ExcelImportHandler().importExcel(inputStream, ApiLog.class, new ImportParams());
        for (ApiLog apiLog : apiLogs) {
            System.out.println(apiLog);
        }
    }

}
