package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdTwoParamsSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.kalus.tdengineorm.enums.TdWrapperTypeEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.utils.AssertUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Klaus
 */
public abstract class AbstractTdQueryWrapper<T> extends AbstractTdWrapper<T> {

    protected String limit;
    protected String[] selectColumnNames;
    protected String windowFunc;
    protected final StringBuilder orderBy = new StringBuilder();
    /**
     * 外层的Wrapper对象
     */
    protected AbstractTdQueryWrapper<T> outerQueryWrapper;

    /**
     * 当前层, 最内层为0, 向上递增
     */
    protected int layer;

    public AbstractTdQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    protected TdWrapperTypeEnum type() {
        return TdWrapperTypeEnum.QUERY;
    }


    public String getSql() {
        buildSelect();
        buildFrom();
        if (StrUtil.isNotBlank(where)) {
            finalSql.append(SqlConstant.WHERE).append(where);
        }

        if (StrUtil.isNotBlank(windowFunc)) {
            finalSql.append(windowFunc);
        }
        if (StrUtil.isNotBlank(orderBy)) {
            finalSql.append(orderBy);
        }
        if (StrUtil.isNotBlank(limit)) {
            finalSql.append(limit);
        }
        StringBuilder innerSb = finalSql;
        if (outerQueryWrapper != null) {
            outerQueryWrapper.tbName = " (" + innerSb.append(") t").append(layer).append(SqlConstant.BLANK);
            return outerQueryWrapper.getSql();
        }

        return finalSql.toString();
    }


    protected void doLimit(String limitCount) {
        limit = limitCount;
    }

    protected void doLimit(int pageNo, int pageSize) {
        limit = SqlConstant.LIMIT + (pageNo - 1) + SqlConstant.COMMA + pageSize;
    }

    private void buildSelect() {
        Assert.notEmpty(selectColumnNames, "【TDengineQueryWrapper】查询字段不可为空");
        finalSql.append(SqlConstant.SELECT);
        for (int i = 1; i <= selectColumnNames.length; i++) {
            if (i > 1) {
                finalSql.append(SqlConstant.COMMA);
            }
            finalSql.append(selectColumnNames[i - 1]);
        }
    }

    protected void doSelectAll() {
        selectColumnNames = new String[]{SqlConstant.ALL};
    }

    protected void doWindowFunc(TdWindFuncTypeEnum funcType, String winFuncValue) {
        Assert.isNull(windowFunc, "[TDengineQueryWrapper] 不可重复设置窗口函数");
        windowFunc = buildWindowFunc(funcType);
        getParamsMap().put(TdSqlConstant.WINDOW_FUNC_PARAM_NAME + layer, winFuncValue);
    }

    protected String buildWindowFunc(TdWindFuncTypeEnum tdWindFuncTypeEnum) {
        return tdWindFuncTypeEnum.getKey() + SqlConstant.LEFT_BRACKET
                + SqlConstant.COLON + TdSqlConstant.WINDOW_FUNC_PARAM_NAME + layer
                + SqlConstant.RIGHT_BRACKET;
    }


    protected void doInnerWrapper(AbstractTdQueryWrapper<T> innerWrapper) {
        // 限制最多调用一次
        AssertUtil.isTrue(layer == 0, new TdOrmException(TdOrmExceptionCode.SQL_LAYER_OUT_LIMITED));
        innerWrapper.layer = 1;
        innerWrapper.outerQueryWrapper = this;
        innerWrapper.getParamsMap().putAll(this.getParamsMap());
    }


    protected void addColumnName(String columnName) {
        if (ArrayUtil.isEmpty(selectColumnNames)) {
            selectColumnNames = new String[]{columnName};
            return;
        }
        List<String> newList = Arrays.stream(selectColumnNames).collect(Collectors.toList());
        newList.add(columnName);
        selectColumnNames = newList.toArray(new String[0]);
    }

    protected void addColumnNames(String[] columnNames) {
        if (ArrayUtil.isEmpty(selectColumnNames)) {
            selectColumnNames = columnNames;
            return;
        }

        selectColumnNames = ArrayUtil.addAll(selectColumnNames, columnNames);
    }

    protected String buildAggregationFunc(TdTwoParamsSelectFuncEnum tdTwoParamsSelectFuncEnum, String columnName, String aliasName) {
        return StrUtil.format(tdTwoParamsSelectFuncEnum.getFunc(), columnName, aliasName);
    }

    protected void addWhereParam(Object value, String columnName, String paramName, String symbol) {
        if (StrUtil.isNotBlank(where)) {
            where.append(SqlConstant.AND);
        }
        where
                .append(columnName)
                .append(symbol)
                .append(SqlConstant.COLON)
                .append(paramName)
                .append(SqlConstant.BLANK);

        getParamsMap().put(paramName, value);
    }
}
