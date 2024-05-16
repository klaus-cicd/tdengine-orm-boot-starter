package com.kalus.tdengineorm.enums;

import lombok.Getter;

/**
 * TDengine常用聚合函数
 *
 * @author Klaus
 */
@Getter
public enum TdSelectFuncEnum {

    /**
     * 计数
     */
    COUNT("COUNT({}) {}"),
    /**
     * 获取平均值
     */
    AVG("AVG({}) {}"),
    /**
     * 取区间结果的第一条
     */
    FIRST("FIRST({}) {}"),
    /**
     * 取区间最后一条
     */
    LAST("LAST({}) {}");

    private final String func;

    TdSelectFuncEnum(String func) {
        this.func = func;
    }
}
