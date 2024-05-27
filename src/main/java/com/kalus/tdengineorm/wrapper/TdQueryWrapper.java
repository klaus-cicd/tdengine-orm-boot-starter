package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.kalus.tdengineorm.util.TdSqlUtil;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.func.GetterFunction;
import com.klaus.fd.util.SqlUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Tdengine查询包装
 *
 * @author Klaus
 * @date 2024/05/11
 */
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

    @SafeVarargs
    public final TdQueryWrapper<T> select(GetterFunction<T, ?>... getterFuncArray) {
        if (ArrayUtil.isEmpty(getterFuncArray)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_SELECT);
        }

        String[] fieldNameArray = Arrays.stream(getterFuncArray)
                .map(this::getColumnName)
                .toArray(String[]::new);

        addColumnNames(fieldNameArray);
        return this;
    }

    public TdQueryWrapper<T> selectCalc(String aliasColumnName, Consumer<SelectCalcWrapper<T>> consumer) {
        SelectCalcWrapper<T> selectCalcWrapper = new SelectCalcWrapper<>(getEntityClass());
        consumer.accept(selectCalcWrapper);
        selectCalcWrapper.setFinalColumnAliasName(aliasColumnName);
        super.selectCalcWrapper = selectCalcWrapper;
        return this;
    }

    public TdQueryWrapper<T> selectCalc(GetterFunction<T, ?> aliasColumnFunc, Consumer<SelectCalcWrapper<T>> consumer) {
        return selectCalc(getColumnName(aliasColumnFunc), consumer);
    }

    public final TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, String... columnNames) {
        String[] array = Arrays.stream(columnNames)
                .map(columnName -> TdSqlUtil.buildAggregationFunc(selectFuncEnum, columnName, columnName))
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    public TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, String columnName) {
        addColumnName(TdSqlUtil.buildAggregationFunc(selectFuncEnum, columnName, columnName));
        return this;
    }

    public TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, String columnName, String aliasColumnName) {
        addColumnName(TdSqlUtil.buildAggregationFunc(selectFuncEnum, columnName, aliasColumnName));
        return this;
    }

    @SafeVarargs
    public final TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, GetterFunction<T, ?>... getterFuncArray) {
        String[] array = Arrays.stream(getterFuncArray)
                .map(getterFunc -> {
                    String columnName = getColumnName(getterFunc);
                    return TdSqlUtil.buildAggregationFunc(selectFuncEnum, columnName, columnName);
                })
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    public TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, GetterFunction<T, ?> column) {
        String columnName = getColumnName(column);
        selectFunc(selectFuncEnum, columnName);
        return this;
    }

    public <R> TdQueryWrapper<T> selectFunc(TdSelectFuncEnum selectFuncEnum, GetterFunction<T, ?> column, Class<R> aliasClass, GetterFunction<R, ?> aliasColumn) {
        selectFunc(selectFuncEnum, getColumnName(column), SqlUtil.getColumnName(aliasClass, aliasColumn));
        return this;
    }


    public TdQueryWrapper<T> eq(String columnName, Object value) {
        addWhereParam(value, columnName, genParamName(), SqlConstant.EQUAL);
        return this;
    }


    public TdQueryWrapper<T> eq(GetterFunction<T, ?> getterFunc, Object value) {
        return eq(getColumnName(getterFunc), value);
    }

    public TdQueryWrapper<T> and() {
        this.where.append(SqlConstant.BLANK).append(SqlConstant.AND).append(SqlConstant.BLANK);
        return this;
    }

    // public TdQueryWrapper<T> and(Consumer<TdQueryWrapper<T>> consumer) {
    //     consumer.accept(this);
    //     return this;
    // }

    public TdQueryWrapper<T> or(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.OR);
        return this;
    }

    public TdQueryWrapper<T> ne(String columnName, Object value) {
        addWhereParam(value, columnName, columnName, SqlConstant.NE);
        return this;
    }


    public TdQueryWrapper<T> notNull(String columnName, Object value) {
        addWhereParam(value, columnName, columnName, SqlConstant.IS_NOT_NULL);
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


    // public TdQueryWrapper<T> and(Consumer<LambdaTdQueryWrapper<T>> consumer) {
    //     consumer.accept(this);
    //     return this;
    // }


    public TdQueryWrapper<T> ne(GetterFunction<T, ?> getterFunc, Object value) {
        return ne(getColumnName(getterFunc), value);
    }


    public TdQueryWrapper<T> notNull(GetterFunction<T, ?> getterFunc, Object value) {
        return notNull(getColumnName(getterFunc), value);
    }

    public TdQueryWrapper<T> intervalWindow(String interval) {
        doWindowFunc(TdWindFuncTypeEnum.INTERVAL, interval);
        return this;
    }


    public TdQueryWrapper<T> stateWindow(GetterFunction<T, ?> getterFunc) {
        return stateWindow(getColumnName(getterFunc));
    }


    public TdQueryWrapper<T> orderByAsc(GetterFunction<T, ?> getterFunc) {
        return orderByAsc(getColumnName(getterFunc));
    }


    public TdQueryWrapper<T> orderByDesc(GetterFunction<T, ?> getterFunc) {
        return orderByDesc(getColumnName(getterFunc));
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

    public TdQueryWrapper<T> lt(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LT);
        return this;
    }

    public TdQueryWrapper<T> lt(GetterFunction<T, ?> getterFunc, Object value) {
        return lt(getColumnName(getterFunc), value);
    }


    public TdQueryWrapper<T> le(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LE);
        return this;
    }

    public TdQueryWrapper<T> le(GetterFunction<T, ?> getterFunc, Object value) {
        return le(getColumnName(getterFunc), value);
    }


    public TdQueryWrapper<T> gt(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.GE);
        return this;
    }

    public TdQueryWrapper<T> gt(GetterFunction<T, ?> getterFunc, Object value) {
        return gt(getColumnName(getterFunc), value);
    }


    public TdQueryWrapper<T> ge(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.GT);
        return this;
    }

    public TdQueryWrapper<T> ge(GetterFunction<T, ?> getterFunc, Object value) {
        return ge(getColumnName(getterFunc), value);
    }


    public TdQueryWrapper<T> like(String columnName, Object value) {
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LIKE);
        return this;
    }

    public TdQueryWrapper<T> like(GetterFunction<T, ?> getterFunc, Object value) {
        return like(getColumnName(getterFunc), value);
    }


    /**
     * 左闭右开区间范围
     *
     * @param columnName 列名
     * @param leftValue  左区间值
     * @param rightValue 右区间值
     * @return {@link TdQueryWrapper }<{@link T }>
     */
    public TdQueryWrapper<T> between(String columnName, Object leftValue, Object rightValue) {
        if (StrUtil.isNotBlank(where)) {
            where.append(SqlConstant.AND);
        }

        String leftParamName = getParamName(columnName + "__left");
        String rightParamName = getParamName(columnName + "__right");

        where
                .append(SqlConstant.LEFT_BRACKET)
                .append(columnName)
                .append(SqlConstant.LE)
                .append(SqlConstant.COLON)
                .append(leftParamName)
                .append(SqlConstant.OR)
                .append(columnName)
                .append(SqlConstant.GT)
                .append(SqlConstant.COLON)
                .append(rightParamName)
                .append(SqlConstant.RIGHT_BRACKET);

        getParamsMap().put(leftParamName, leftValue);
        getParamsMap().put(rightParamName, rightValue);
        return this;
    }

    /**
     * 左闭右开区间范围
     *
     * @param getterFunc get方法
     * @param leftValue  左区间值
     * @param rightValue 右区间值
     * @return {@link TdQueryWrapper }<{@link T }>
     */
    public TdQueryWrapper<T> between(GetterFunction<T, ?> getterFunc, Object leftValue, Object rightValue) {
        return between(getColumnName(getterFunc), leftValue, rightValue);
    }

    private String getParamName(String fieldName) {
        int index = 0;
        while (true) {
            if (where.toString().contains(SqlConstant.COLON + buildParam(fieldName, index))) {
                ++index;
            } else {
                return buildParam(fieldName, index);
            }
        }
    }

    private String buildParam(String fieldName, int index) {
        return fieldName + SqlConstant.UNDERLINE + SqlConstant.UNDERLINE + layer + SqlConstant.UNDERLINE + index;
    }
}
