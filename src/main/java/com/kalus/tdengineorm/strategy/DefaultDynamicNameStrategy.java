package com.kalus.tdengineorm.strategy;

/**
 * 直接返回原表名称
 *
 * @author Silas
 */
public class DefaultDynamicNameStrategy implements DynamicNameStrategy {

    @Override
    public String dynamicTableName(String tableName) {
        return tableName;
    }

}
