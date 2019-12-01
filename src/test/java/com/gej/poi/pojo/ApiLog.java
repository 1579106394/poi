package com.gej.poi.pojo;

import com.gej.poi.annotation.Excel;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 接口访问日志表实体类
 * </p>
 *
 * @author 高尔稽 技术交流群;781943947
 * @date 2019-09-29 19:15:31
 * @Version 1.0
 *
 * 注意：本内容仅限于风越云力内部传阅，禁止外泄以及用于其他的商业目
 */
@Data
public class ApiLog implements Serializable {

    private static final long serialVersionUID = -3286564461647015367L;

    /**
     * 日志id
     */
    @Excel(name = "编号")
    private Integer logId;

    /**
     * 请求路径
     */
    @Excel(name = "请求地址")
    private String logUrl;

    /**
     * 参数
     */
    @Excel(name = "请求参数")
    private String logParams;

    /**
     * 访问状态，1正常0异常
     */
    @Excel(name = "访问状态")
    private Integer logStatus;

    /**
     * 异常信息
     */
    @Excel(name = "异常信息")
    private String logMessage;

    /**
     * 浏览器UA标识
     */
    @Excel(name = "浏览器标识", autoSize = true)
    private String logUa;

    /**
     * 访问controller
     */
    @Excel(name = "控制层")
    private String logController;

    /**
     * 请求方式，get、post等
     */
    @Excel(name = "请求方式")
    private String logMethod;

    /**
     * 响应时间，单位毫秒
     */
    @Excel(name = "响应时间", isStatistics = true)
    private Long logTime;

    /**
     * 请求ip
     */
    @Excel(name = "请求ip")
    private String logIp;

    /**
     * 设备MAC
     */
    @Excel(name = "设备号")
    private String logDevice;

    /**
     * 创建时间
     */
    @Excel(name = "请求时间")
    private String createdDate;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    @Excel(name = "创建人", autoSize = true)
    private String createdName;

    /**
     * 返回值
     */
    @Excel(name = "返回值")
    private String logResult;

    /**
     * 日志内容
     */
    @Excel(name = "日志内容")
    private String logContent;

    /**
     * 日志类型  0:操作日志;1:登录日志;2:定时任务;
     */
    private Integer logType;

    /**
     * 操作类型  1查询，2添加，3修改，4删除，5导入，6导出
     */
    private Integer logOperateType;

    @Override
    public String toString() {
        return "ApiLog{" +
                "logId=" + logId +
                ", logUrl='" + logUrl + '\'' +
                ", logParams='" + logParams + '\'' +
                ", logStatus=" + logStatus +
                ", logMessage='" + logMessage + '\'' +
                ", logUa='" + logUa + '\'' +
                ", logController='" + logController + '\'' +
                ", logMethod='" + logMethod + '\'' +
                ", logTime=" + logTime +
                ", logIp='" + logIp + '\'' +
                ", logDevice='" + logDevice + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdName='" + createdName + '\'' +
                ", logResult='" + logResult + '\'' +
                ", logContent='" + logContent + '\'' +
                ", logType=" + logType +
                ", logOperateType=" + logOperateType +
                '}';
    }
}
