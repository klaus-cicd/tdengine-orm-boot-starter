package com.kalus.tdengineorm.strategy;

import com.klaus.fd.constant.DateConstant;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.utils.DateUtil;

import java.time.format.DateTimeFormatter;

/**
 * 根据当天日期作为后缀拼接 生成新的名称
 *
 * @author Silas
 */
public class DateDynamicNameStrategy extends AbstractDynamicNameStrategy {
    @Override
    public String dynamicTableName(String tableName) {
        return tableName + SqlConstant.UNDERLINE +
                DateUtil.now().format(DateTimeFormatter.ofPattern(DateConstant.D_FORMAT));
    }

}
