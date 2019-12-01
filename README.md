# 注解版POI操作工具
* 创建数据库 poi (过程较简单，不贴代码了)
* 导入项目下db目录的sql文件
* 创建类ApiLog，在需要导出/导入的字段上加上@Excel注解
* 执行测试类中的测试方法
* Excel注解中属性的作用都标有注释，请根据需求使用
* 如果您对本工具比较喜欢，请加群进行技术交流：781943947
```java
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
```
