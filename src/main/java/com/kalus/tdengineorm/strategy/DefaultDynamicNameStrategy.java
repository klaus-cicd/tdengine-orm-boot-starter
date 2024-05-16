package com.kalus.tdengineorm.strategy;

/**
 * 根据当天日期作为后缀拼接 生成新的名称
 *
 * @author Silas
 */
public class DefaultDynamicNameStrategy extends AbstractDynamicNameStrategy {

    @Override
    public String dynamicTableName(String tableName) {
        return tableName;
    }

}
