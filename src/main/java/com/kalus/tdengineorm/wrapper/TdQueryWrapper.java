package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.klaus.fd.constant.SqlConstant;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Tdengine查询包装
 *
 * @author Klaus
 * @date 2024/05/11
 */
@EqualsAndHashCode(callSuper = true)
public class TdQueryWrapper<T> extends AbstractTdQueryWrapper<T> {

    public TdQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public TdQueryWrapper<T> selectAll() {
        doSelectAll();
        return this;
    }

    public TdQueryWrapper<T> select(String... columnNames) {
        addColumnNames(columnNames);
        return this;
    }

    public TdQueryWrapper<T> first(String... firstColumnNames) {
        String[] array = Arrays.stream(firstColumnNames)
                .map(column -> TdSqlConstant.FIRST + SqlConstant.LEFT_BRACKET + column + SqlConstant.RIGHT_BRACKET + SqlConstant.BLANK + column)
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    public TdQueryWrapper<T> last(String... columns) {
        String[] array = Arrays.stream(columns)
                .map(column -> TdSqlConstant.LAST + SqlConstant.LEFT_BRACKET + column + SqlConstant.RIGHT_BRACKET + SqlConstant.BLANK + column)
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    public TdQueryWrapper<T> firstAlias(String column, String aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.FIRST, column, aliasColumn));
        return this;
    }

    public TdQueryWrapper<T> lastAlias(String column, String aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.LAST, column, aliasColumn));
        return this;
    }

    public TdQueryWrapper<T> count() {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.COUNT, "1", "count"));
        return this;
    }

    public TdQueryWrapper<T> avg(String column, String aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.AVG, column, aliasColumn));
        return this;
    }


    public TdQueryWrapper<T> from(String tbName) {
        setTbName(tbName);
        return this;
    }

    public TdQueryWrapper<T> eq(String columnName, Object value) {
        if (StrUtil.isNotBlank(getWhere())) {
            getWhere().append(SqlConstant.AND);
        }
        String paramName = genParamName();
        this.getWhere().append(columnName).append(SqlConstant.EQUAL).append(SqlConstant.COLON).append(paramName);
        getParamsMap().put(paramName, value);
        return this;
    }

    public TdQueryWrapper<T> and() {
        this.getWhere().append(SqlConstant.BLANK).append(SqlConstant.AND).append(SqlConstant.BLANK);
        return this;
    }

    public TdQueryWrapper<T> and(Consumer<TdQueryWrapper<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    public TdQueryWrapper<T> or(String columnName, Object value) {
        this.getWhere().append(SqlConstant.BLANK)
                .append(SqlConstant.OR)
                .append(SqlConstant.BLANK);
        return this;
    }

    public TdQueryWrapper<T> intervalWindow(String interval) {
        doWindowFunc(TdWindFuncTypeEnum.INTERVAL, interval);
        return this;
    }


    public TdQueryWrapper<T> stateWindow(String column) {
        doWindowFunc(TdWindFuncTypeEnum.STATE_WINDOW, column);
        return this;
    }


    public TdQueryWrapper<T> orderByAsc(String columnName) {
        if (StrUtil.isNotBlank(orderBy)) {
            orderBy.append(SqlConstant.COMMA);
        }
        orderBy.append(columnName);
        return this;
    }


    public TdQueryWrapper<T> orderByDesc(String columnName) {
        if (StrUtil.isNotBlank(orderBy)) {
            orderBy.append(SqlConstant.COMMA);
        }
        orderBy.append(SqlConstant.ORDER_BY)
                .append(columnName)
                .append(SqlConstant.BLANK)
                .append(SqlConstant.DESC);
        return this;
    }


    public TdQueryWrapper<T> innerQueryWrapper(Consumer<TdQueryWrapper<T>> innerQueryWrapperConsumer) {
        TdQueryWrapper<T> innerWrapper = TdWrappers.queryWrapper(getEntityClass());
        innerQueryWrapperConsumer.accept(innerWrapper);
        doInnerWrapper(innerWrapper);
        return this;
    }

    public TdQueryWrapper<T> limit(int count) {
        doLimit(SqlConstant.LIMIT + count);
        return this;
    }

    /**
     * 分页
     *
     * @param pageNo   页码, 起始为1
     * @param pageSize 页大小
     * @return {@link TdQueryWrapper}<{@link T}>
     */
    public TdQueryWrapper<T> limit(int pageNo, int pageSize) {
        doLimit(pageNo, pageSize);
        return this;
    }
}
