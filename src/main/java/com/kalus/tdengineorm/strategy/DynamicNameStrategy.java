package com.kalus.tdengineorm.strategy;

/**
 * 动态表名称策略
 *
 * @author Silas
 */
@FunctionalInterface
public interface DynamicNameStrategy {

    /**
     * 动态表名生成
     *
     * @param tableName 原始表名
     * @return 根据策略修改后的表名
     */
    String dynamicTableName(String tableName);

}
