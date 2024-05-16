package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.kalus.tdengineorm.enums.TdWrapperTypeEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.utils.AssertUtil;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Klaus
 */
@EqualsAndHashCode(callSuper = true)
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

    @Override
    protected void changeInnerTbName(String innerSql) {
        setTbName(" (" + innerSql + ") t ");
    }

    @Override
    protected void changeOuterTbName(String outerSql) {
        setTbName(" (" + outerSql + ") t ");
    }

    public String getSql() {
        buildSelect();
        buildFrom();
        if (StrUtil.isNotBlank(getWhere())) {
            getFinalSql().append(SqlConstant.WHERE).append(getWhere());
        }

        if (StrUtil.isNotBlank(windowFunc)) {
            getFinalSql().append(windowFunc);
        }
        if (StrUtil.isNotBlank(orderBy)) {
            getFinalSql().append(orderBy);
        }
        if (StrUtil.isNotBlank(limit)) {
            getFinalSql().append(limit);
        }
        StringBuilder innerSb = getFinalSql();
        if (outerQueryWrapper != null) {
            outerQueryWrapper.setTbName(" (" + innerSb.append(") t").append(layer).append(SqlConstant.BLANK));
            return outerQueryWrapper.getSql();
        }

        return getFinalSql().toString();
    }


    protected void doLimit(String limitCount) {
        limit = limitCount;
    }

    protected void doLimit(int pageNo, int pageSize) {
        limit = SqlConstant.LIMIT + (pageNo - 1) + SqlConstant.COMMA + pageSize;
    }

    private void buildSelect() {
        Assert.notEmpty(selectColumnNames, "【TDengineQueryWrapper】查询字段不可为空");
        getFinalSql().append(SqlConstant.SELECT);
        for (int i = 1; i <= selectColumnNames.length; i++) {
            if (i > 1) {
                getFinalSql().append(SqlConstant.COMMA);
            }
            getFinalSql().append(selectColumnNames[i - 1]);
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

    protected String buildAggregationFunc(TdSelectFuncEnum tdSelectFuncEnum, String columnName, String aliasName) {
        return StrUtil.format(tdSelectFuncEnum.getFunc(), columnName, aliasName);
    }

    protected void addWhereParam(Object value, String columnName, String paramName, String symbol) {
        if (StrUtil.isNotBlank(getWhere())) {
            getWhere().append(SqlConstant.AND);
        }
        getWhere()
                .append(columnName)
                .append(symbol)
                .append(SqlConstant.COLON)
                .append(paramName)
                .append(SqlConstant.BLANK);

        getParamsMap().put(paramName, value);
    }
}
