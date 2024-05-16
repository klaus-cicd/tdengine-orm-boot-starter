package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.kalus.tdengineorm.enums.TdWrapperTypeEnum;
import com.klaus.fd.constant.SqlConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Klaus
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractTdQueryWrapper<T> extends AbstractTdWrapper<T> {

    private String limit;
    private String[] selectColumnNames;
    private String windowFunc;
    private final StringBuilder orderBy = new StringBuilder();
    /**
     * 内层的Wrapper对象
     */
    private AbstractTdQueryWrapper<T> innerQueryWrapper;
    /**
     * 当前层, 最内层为0, 向上递增
     */
    private int layer;

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

    public String getSql() {
        buildSelect();
        buildFrom();
        if (StrUtil.isNotBlank(getWhere())) {
            getFinalSql().append(SqlConstant.WHERE).append(getWhere());
        }

        if (StrUtil.isNotBlank(getWindowFunc())) {
            getFinalSql().append(getWindowFunc());
        }
        if (StrUtil.isNotBlank(getOrderBy())) {
            getFinalSql().append(getOrderBy());
        }
        if (StrUtil.isNotBlank(getLimit())) {
            getFinalSql().append(getLimit());
        }

        return getFinalSql().toString();
    }


    protected void doLimit(String limitCount) {
        setLimit(limitCount);
    }

    protected void doLimit(int pageNo, int pageSize) {
        setLimit(SqlConstant.LIMIT + (pageNo - 1) + SqlConstant.COMMA + pageSize);
    }

    private void buildSelect() {
        Assert.notEmpty(getSelectColumnNames(), "【TDengineQueryWrapper】查询字段不可为空");
        getFinalSql().append(SqlConstant.SELECT);
        for (int i = 1; i <= getSelectColumnNames().length; i++) {
            if (i > 1) {
                getFinalSql().append(SqlConstant.COMMA);
            }
            getFinalSql().append(getSelectColumnNames()[i - 1]);
        }
    }

    protected void doSelectAll() {
        setSelectColumnNames(new String[]{SqlConstant.ALL});
    }

    protected void doWindowFunc(TdWindFuncTypeEnum funcType, String winFuncValue) {
        Assert.isNull(windowFunc, "[TDengineQueryWrapper] 不可重复设置窗口函数");
        setWindowFunc(buildWindowFunc(funcType));
        getParamsMap().put(TdSqlConstant.WINDOW_FUNC_PARAM_NAME + getLayer(), winFuncValue);
    }

    protected String buildWindowFunc(TdWindFuncTypeEnum tdWindFuncTypeEnum) {
        return tdWindFuncTypeEnum.getKey() + SqlConstant.LEFT_BRACKET
                + SqlConstant.COLON + TdSqlConstant.WINDOW_FUNC_PARAM_NAME + getLayer()
                + SqlConstant.RIGHT_BRACKET;
    }

    protected void doOuterWrapper(AbstractTdQueryWrapper<T> outerWrapper) {
        outerWrapper.setInnerQueryWrapper(this);
        outerWrapper.changeInnerTbName(this.getSql());
        outerWrapper.setLayer(++layer);
        outerWrapper.getParamsMap().putAll(this.getParamsMap());
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
