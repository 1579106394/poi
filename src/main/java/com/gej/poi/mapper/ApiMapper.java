package com.gej.poi.mapper;

import com.gej.poi.pojo.ApiLog;

import java.util.List;

/**
 * <p>
 * 接口访问日志表Mapper
 * </p>
 *
 * @author 高尔稽 技术交流群;781943947
 * @date 2019-09-29 19:15:31
 * @Version 1.0
 *
 * 注意：本内容仅限于风越云力内部传阅，禁止外泄以及用于其他的商业目
 */
@Component
public interface ApiMapper {

    /**
     * 查询所有
     * @return
     */
    List<ApiLog> findAll();

}
