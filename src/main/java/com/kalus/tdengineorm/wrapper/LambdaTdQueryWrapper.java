package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.enums.TdWindFuncTypeEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.utils.ClassUtil;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Klaus
 */
@EqualsAndHashCode(callSuper = true)
public class LambdaTdQueryWrapper<T> extends AbstractTdQueryWrapper<T> {

    public LambdaTdQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public LambdaTdQueryWrapper<T> selectAll() {
        doSelectAll();
        return this;
    }


    @SafeVarargs
    public final LambdaTdQueryWrapper<T> select(SFunction<T, ?>... sFunctions) {
        if (ArrayUtil.isEmpty(sFunctions)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_SELECT);
        }

        String[] fieldNameArray = Arrays.stream(sFunctions)
                .map(this::getColumnName)
                .toArray(String[]::new);

        addColumnNames(fieldNameArray);
        return this;
    }


    @SafeVarargs
    public final LambdaTdQueryWrapper<T> first(SFunction<T, ?>... functions) {
        String[] array = Arrays.stream(functions)
                .map(sFunction -> {
                    String column = getColumnName(sFunction);
                    return TdSqlConstant.FIRST + SqlConstant.LEFT_BRACKET + column + SqlConstant.RIGHT_BRACKET + SqlConstant.BLANK + column;
                })
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    @SafeVarargs
    public final LambdaTdQueryWrapper<T> last(SFunction<T, ?>... functions) {
        String[] array = Arrays.stream(functions)
                .map(sFunction -> {
                    String column = getColumnName(sFunction);
                    return TdSqlConstant.LAST + SqlConstant.LEFT_BRACKET + column + SqlConstant.RIGHT_BRACKET + SqlConstant.BLANK + column;
                })
                .toArray(String[]::new);
        addColumnNames(array);
        return this;
    }

    public <R> LambdaTdQueryWrapper<T> firstAlias(SFunction<T, ?> column, SFunction<R, ?> aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.FIRST, getColumnName(column), getColumnName(aliasColumn)));
        return this;
    }

    public LambdaTdQueryWrapper<T> lastAlias(SFunction<T, ?> column, SFunction<?, ?> aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.LAST, getColumnName(column), getColumnName(aliasColumn)));
        return this;
    }

    public LambdaTdQueryWrapper<T> count() {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.COUNT, "1", "count"));
        return this;
    }

    public LambdaTdQueryWrapper<T> avg(SFunction<T, ?> column, SFunction<?, ?> aliasColumn) {
        addColumnName(buildAggregationFunc(TdSelectFuncEnum.AVG, getColumnName(column), getColumnName(aliasColumn)));
        return this;
    }

    public LambdaTdQueryWrapper<T> and(Consumer<LambdaTdQueryWrapper<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    private static String getFieldName(LambdaMeta sFunction) {
        return PropertyNamer.methodToProperty(sFunction.getImplMethodName());
    }

    private String getColumnName(SFunction<?, ?> sFunction) {
        String fieldName = getFieldName(LambdaUtils.extract(sFunction));
        Field field = ClassUtil.getField(getEntityClass(), fieldName);
        Assert.notNull(field, TdOrmExceptionCode.NO_FILED.getMsg());

        String tableFiledAnnoValue = AnnotationUtil.getAnnotationValue(field, TableField.class, "value");
        return StrUtil.isNotBlank(tableFiledAnnoValue) ? tableFiledAnnoValue : StrUtil.toUnderlineCase(fieldName);
    }

    public LambdaTdQueryWrapper<T> eq(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.EQUAL);
        return this;
    }


    public LambdaTdQueryWrapper<T> intervalWindow(String interval) {
        doWindowFunc(TdWindFuncTypeEnum.INTERVAL, interval);
        return this;
    }


    public LambdaTdQueryWrapper<T> stateWindow(SFunction<T, ?> sFunction) {
        doWindowFunc(TdWindFuncTypeEnum.STATE_WINDOW, getColumnName(sFunction));
        return this;
    }


    public LambdaTdQueryWrapper<T> orderByAsc(SFunction<T, ?> sFunction) {
        if (StrUtil.isNotBlank(orderBy)) {
            orderBy.append(SqlConstant.COMMA);
        }
        orderBy.append(getColumnName(sFunction));
        return this;
    }

    public LambdaTdQueryWrapper<T> orderByDesc(SFunction<T, ?> sFunction) {
        if (StrUtil.isNotBlank(orderBy)) {
            orderBy.append(SqlConstant.COMMA);
        }
        orderBy.append(SqlConstant.ORDER_BY)
                .append(getColumnName(sFunction))
                .append(SqlConstant.BLANK)
                .append(SqlConstant.DESC);
        return this;
    }


    public LambdaTdQueryWrapper<T> innerQueryWrapper(Consumer<LambdaTdQueryWrapper<T>> innerQueryWrapperConsumer) {
        LambdaTdQueryWrapper<T> innerWrapper = TdWrappers.lambdaQueryWrapper(getEntityClass());
        innerQueryWrapperConsumer.accept(innerWrapper);
        doInnerWrapper(innerWrapper);
        return this;
    }

    public LambdaTdQueryWrapper<T> limit(int count) {
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
    public LambdaTdQueryWrapper<T> limit(int pageNo, int pageSize) {
        doLimit(pageNo, pageSize);
        return this;
    }

    public LambdaTdQueryWrapper<T> lt(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LT);
        return this;
    }

    public LambdaTdQueryWrapper<T> le(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LE);
        return this;
    }

    public LambdaTdQueryWrapper<T> gt(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.GE);
        return this;
    }

    public LambdaTdQueryWrapper<T> ge(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.GT);
        return this;
    }

    public LambdaTdQueryWrapper<T> like(SFunction<T, ?> sFunction, Object value) {
        String columnName = getColumnName(sFunction);
        addWhereParam(value, columnName, getParamName(columnName), SqlConstant.LIKE);
        return this;
    }

    /**
     * 左闭右开区间范围
     *
     * @param sFunction  get方法
     * @param leftValue  左区间值
     * @param rightValue 右区间值
     * @return {@link LambdaTdQueryWrapper }<{@link T }>
     */
    public LambdaTdQueryWrapper<T> between(SFunction<T, ?> sFunction, Object leftValue, Object rightValue) {
        if (StrUtil.isNotBlank(getWhere())) {
            getWhere().append(SqlConstant.AND);
        }

        String columnName = getColumnName(sFunction);
        String leftParamName = getParamName(columnName + "__left");
        String rightParamName = getParamName(columnName + "__right");

        getWhere()
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

    private String getParamName(String fieldName) {
        int index = 0;
        while (true) {
            if (getWhere().toString().contains(SqlConstant.COLON + buildParam(fieldName, index))) {
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
