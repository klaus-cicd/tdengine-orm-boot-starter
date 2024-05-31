package com.kalus.tdengineorm.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.kalus.tdengineorm.annotation.PrimaryTs;
import com.kalus.tdengineorm.annotation.TdField;
import com.kalus.tdengineorm.annotation.TdTag;
import com.kalus.tdengineorm.constant.SqlConstant;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdFieldTypeEnum;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.kalus.tdengineorm.strategy.DynamicNameStrategy;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Klaus
 */
public class TdSqlUtil {

    public static <T> String joinSqlValue(T entity, List<Field> fields, Map<String, Object> paramsMapList, int index) {
        Map<Boolean, List<Field>> fieldGroups = fields.stream()
                .collect(Collectors.partitioningBy(field -> field.isAnnotationPresent(TdTag.class)));
        List<Field> commFields = fieldGroups.get(Boolean.FALSE);

        return commFields.stream()
                .map(field -> {
                    String fieldName = field.getName();
                    paramsMapList.put(fieldName + index, ReflectUtil.getFieldValue(entity, field));
                    return ":" + fieldName + index;
                })
                .collect(SqlUtil.getColumnWithBracketCollector());
    }

    public static String justGetInsertUsingSql(Object object, String sTbName, List<Field> fieldList, DynamicNameStrategy dynamicTbNameStrategy, Map<String, Object> map) {
        // 根据是否为TAG字段做分组
        Pair<List<Field>, List<Field>> fieldsPair = differentiateByTag(fieldList);
        // 获取TAGS字段名称&对应的值
        String tagFieldSql = getTagFieldNameAndValuesSql(object, fieldsPair.getKey(), map, true);
        // 获取普通字段的名称
        String commFieldSql = SqlUtil.joinColumnNamesWithBracket(fieldsPair.getValue());
        // 根据策略生成表名
        String childTbName = dynamicTbNameStrategy.dynamicTableName(sTbName);
        return SqlConstant.INSERT_INTO + childTbName + TdSqlConstant.USING + sTbName + tagFieldSql + commFieldSql + SqlConstant.VALUES;
    }


    public static Pair<String, Map<String, Object>> getFinalInsertUsingSql(Object object, List<Field> fieldList, String sTbName, DynamicNameStrategy dynamicTbNameStrategy) {
        Map<String, Object> paramsMap = new HashMap<>(fieldList.size());

        // 根据是否为TAG字段做分组
        Pair<List<Field>, List<Field>> fieldsPair = differentiateByTag(fieldList);

        // 获取TAGS字段相关SQL
        String tagFieldSql = getTagFieldNameAndValuesSql(object, fieldsPair.getKey(), paramsMap, true);
        // 获取普通字段相关SQL
        String commFieldSql = getTagFieldNameAndValuesSql(object, fieldsPair.getValue(), paramsMap, false);

        // 根据策略生成表名
        String childTbName = dynamicTbNameStrategy.dynamicTableName(sTbName);

        // 拼接最终SQL
        String finalSql = SqlConstant.INSERT_INTO + childTbName + TdSqlConstant.USING + sTbName + tagFieldSql + commFieldSql;

        return Pair.of(finalSql, paramsMap);
    }

    /**
     * 按是否有Tag注解区分Field
     *
     * @param fieldList 所有字段列表
     * @return {@link Pair }<{@link List }<{@link Field }> Tag字段, {@link List }<{@link Field }>> 非Tag字段
     */
    public static Pair<List<Field>, List<Field>> differentiateByTag(List<Field> fieldList) {
        Map<Boolean, List<Field>> fieldGroups = fieldList.stream().collect(Collectors.partitioningBy(field -> field.isAnnotationPresent(TdTag.class)));
        List<Field> tagFields = fieldGroups.get(Boolean.TRUE);
        List<Field> commFields = fieldGroups.get(Boolean.FALSE);
        return Pair.of(tagFields, commFields);
    }


    public static String getTagFieldNameAndValuesSql(Object object, List<Field> fields, Map<String, Object> paramsMap, boolean isTag) {
        if (CollectionUtils.isEmpty(fields)) {
            return StrUtil.EMPTY;
        }

        List<String> fieldValueParamNames = new ArrayList<>();
        String fieldNameStr = fields.stream().map(field -> {
            String fieldName = field.getName();
            fieldValueParamNames.add(field.getName());
            paramsMap.put(fieldName, ReflectUtil.getFieldValue(object, field));
            return StrUtil.toUnderlineCase(fieldName);
        }).collect(SqlUtil.getColumnWithBracketCollector());

        String fieldValueParamsStr = fieldValueParamNames.stream().map(item -> ":" + item).collect(SqlUtil.getColumnWithBracketCollector());
        return fieldNameStr + (isTag ? TdSqlConstant.TAGS : SqlConstant.VALUES) + fieldValueParamsStr;
    }

    public static String getFieldTypeAndLength(Field field) {
        TdField tdField = field.getAnnotation(TdField.class);
        TdFieldTypeEnum type = null == tdField ? getColumnTypeByField(field) : tdField.type();
        if (type.isNeedLengthLimit()) {
            if (tdField == null || tdField.length() <= 0) {
                throw new TdOrmException(TdOrmExceptionCode.FIELD_NO_LENGTH);
            }
            int length = tdField.length();
            return type.getFiledType() + SqlConstant.LEFT_BRACKET + length + SqlConstant.RIGHT_BRACKET;
        }
        return type.getFiledType();
    }

    private static TdFieldTypeEnum getColumnTypeByField(Field field) {
        Class<?> fieldType = field.getType();
        TdFieldTypeEnum tdFieldTypeEnum = TdFieldTypeEnum.matchByFieldType(fieldType);
        if (null == tdFieldTypeEnum) {
            throw new TdOrmException(TdOrmExceptionCode.CANT_NOT_MATCH_FIELD_TYPE);
        }

        return tdFieldTypeEnum;
    }

    public static String buildCreateColumn(List<Field> fields, Field primaryTsField) {
        fields.remove(primaryTsField);

        String tsColumn = primaryTsField == null ? StrUtil.EMPTY
                : SqlConstant.HALF_ANGLE_DASH
                + SqlUtil.getColumnName(primaryTsField)
                + SqlConstant.HALF_ANGLE_DASH
                + SqlConstant.BLANK
                + TdFieldTypeEnum.TIMESTAMP.getFiledType()
                + SqlConstant.COMMA;

        StringBuilder finalSb = new StringBuilder(SqlConstant.LEFT_BRACKET).append(tsColumn);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            // 组装 字段名称 类型(长度)
            finalSb.append(SqlConstant.HALF_ANGLE_DASH)
                    .append(SqlUtil.getColumnName(field))
                    .append(SqlConstant.HALF_ANGLE_DASH)
                    .append(SqlConstant.BLANK)
                    .append(TdSqlUtil.getFieldTypeAndLength(field));

            // 最后一个不用➕逗号
            if (i != fields.size() - 1) {
                finalSb.append(SqlConstant.COMMA);
            }
        }
        // 首位必须是Timestamp字段
        return finalSb.append(SqlConstant.RIGHT_BRACKET).toString();
    }


    public static Pair<String, List<Field>> getTbNameAndFieldListPair(Class<?> clazz) {
        // 获取所有字段
        List<Field> fieldList = ClassUtil.getAllFields(clazz);
        Assert.notEmpty(fieldList, "No field found!");
        return Pair.of(SqlUtil.getTbName(clazz), fieldList);
    }


    /**
     * 获取非Tag字段列表
     *
     * @param clazz clazz
     * @return {@link List }<{@link Field }>
     */
    public static List<Field> getNoTagFieldList(Class<?> clazz) {
        return ClassUtil.getAllFields(clazz, field -> !AnnotationUtil.hasAnnotation(field, TdTag.class));
    }


    /**
     * 检查是否有且只有一个被@PrimaryTs注解标记且类型为Timestamp的字段
     *
     * @param fieldList 待检查的字段列表
     * @return {@link Field }
     */
    public static Field checkPrimaryTsField(List<Field> fieldList) {
        List<Field> tsFieldList = fieldList.stream().filter(field -> AnnotationUtil.hasAnnotation(field, PrimaryTs.class)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tsFieldList)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_PRIMARY_TS);
        }
        if (tsFieldList.size() > 1) {
            throw new TdOrmException(TdOrmExceptionCode.MULTI_PRIMARY_TS);
        }
        Field field = tsFieldList.get(0);
        if (!Timestamp.class.equals(field.getType())) {
            throw new TdOrmException(TdOrmExceptionCode.PRIMARY_TS_NOT_TIMESTAMP);
        }
        return field;
    }

    public static String buildAggregationFunc(TdSelectFuncEnum tdSelectFuncEnum, String columnName, String aliasName) {
        return StrUtil.format(tdSelectFuncEnum.getFunc(), columnName, aliasName);
    }
}
