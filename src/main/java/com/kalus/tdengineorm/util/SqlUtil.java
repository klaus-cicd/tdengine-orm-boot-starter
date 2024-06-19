package com.kalus.tdengineorm.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kalus.tdengineorm.constant.SqlConstant;
import com.kalus.tdengineorm.func.GetterFunction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Klaus
 */
public class SqlUtil {

    public static String getTbName(Class<?> entityClass) {
        String tbNameByAnno = getTbNameByAnno(entityClass);
        return StringUtils.hasText(tbNameByAnno) ?
                tbNameByAnno : StrUtil.toUnderlineCase(entityClass.getSimpleName());
    }

    public static String getTbNameByAnno(Class<?> entityClass) {
        TableName annotation = entityClass.getAnnotation(TableName.class);
        return annotation == null ? StrUtil.EMPTY : annotation.value();
    }

    public static Collector<CharSequence, ?, String> getParenthesisCollector() {
        return Collectors.joining(SqlConstant.COMMA, SqlConstant.LEFT_BRACKET, SqlConstant.RIGHT_BRACKET);
    }

    /**
     * 将列表字符使用逗号分隔
     *
     * @param itemList 参数列表
     * @return 拼接后的字符串, 如(1,2,3,4,5,6,7)
     */
    public static String separateByCommas(List<String> itemList, boolean bracket) {
        return bracket ? itemList.stream().collect(SqlUtil.getParenthesisCollector()) : String.join(SqlConstant.COMMA, itemList);
    }

    /**
     * 根据字段解析获取对应字段名和参数名部分SQL
     * 如 (column1, column2, column3) 和 (:param1, :param2, :param3)
     *
     * @param fields List<Field>
     * @return Pair<String, String>
     */
    public static Pair<String, String> getColumnNameSqlAndParamNameSqlPair(List<Field> fields) {
        // 获取字段名称部分SQL
        List<String> paramNameList = new ArrayList<>(fields.size());
        return Pair.of(
                fields.stream()
                        .map(field -> {
                            paramNameList.add(":" + field.getName());
                            return getColumnName(field);
                        })
                        .collect(SqlUtil.getParenthesisCollector()),
                SqlUtil.separateByCommas(paramNameList, true)
        );
    }


    /**
     * 根据字段解析获取对应字段名和参数名部分SQL
     * 如 (column1, column2, column3) 和 (:param1, :param2, :param3) ON DUPLICATE KEY UPDATE column2 = :param2, column3 = :param3
     *
     * @param fields List<Field>
     * @return Pair<String, String>
     */
    public static <T> Pair<String, String> getColumnNameSqlAndParamNameWithUpdateSqlPair(List<Field> fields, Class<T> entityClass) {
        // 获取ID字段名称
        String idFieldName = getIdColumnName(entityClass, fields);

        List<String> paramNameList = new ArrayList<>(fields.size());
        List<String> updateColumnList = new ArrayList<>(fields.size());

        String columnNameSql = fields.stream()
                .map(field -> buildColumnSql(field, idFieldName, paramNameList, updateColumnList))
                .collect(SqlUtil.getParenthesisCollector());

        // 拼接参数部分SQL
        String valuesSql = separateByCommas(paramNameList, true);
        String updateSql = SqlConstant.ON_DUPLICATE_KEY_UPDATE + separateByCommas(updateColumnList, false);

        return Pair.of(columnNameSql, valuesSql + updateSql);
    }

    /**
     * 根据字段解析获取对应字段名和参数名部分SQL
     * 如 (column1, column2, column3) 和 (:param1, :param2, :param3)
     *
     * @param map Map<String, Object>
     * @return Pair<String, String>
     */
    public static Pair<String, String> getColumnNameSqlAndParamNameSqlPair(Map<String, Object> map, String idFieldName, boolean update) {
        List<String> paramNameList = new ArrayList<>(map.size());
        List<String> updateColumnList = new ArrayList<>(map.size());

        String columnNameSql = map.keySet().stream()
                .map(fieldName -> buildColumnSql(idFieldName, paramNameList, updateColumnList, fieldName))
                .collect(SqlUtil.getParenthesisCollector());

        // 拼接参数部分SQL
        String valuesSql = separateByCommas(paramNameList, true);
        String rightSql = update ? valuesSql + SqlConstant.ON_DUPLICATE_KEY_UPDATE + separateByCommas(updateColumnList, false) : valuesSql;
        return Pair.of(columnNameSql, rightSql);
    }

    private static String buildColumnSql(String idFieldName, List<String> paramNameList, List<String> updateColumnList, String fieldName) {
        String columnName = StrUtil.toUnderlineCase(fieldName);
        String paramName = SqlConstant.COLON + fieldName;
        paramNameList.add(paramName);
        if (!Arrays.asList("createTime", idFieldName).contains(fieldName)) {
            updateColumnList.add(columnName + SqlConstant.EQUAL + paramName);
        }
        return columnName;
    }

    private static String buildColumnSql(Field field, String idFieldName, List<String> paramNameList, List<String> updateColumnList) {
        String fieldName = field.getName();
        String columnName = getColumnName(field, fieldName);
        String paramName = SqlConstant.COLON + fieldName;
        paramNameList.add(paramName);
        if (!Arrays.asList("createTime", idFieldName).contains(fieldName)) {
            updateColumnList.add(columnName + SqlConstant.EQUAL + paramName);
        }
        return columnName;
    }

    private static String getColumnName(Field field, String fieldName) {
        TableField tableFieldAnno = field.getAnnotation(TableField.class);
        String columnName;
        if (tableFieldAnno != null) {
            columnName = tableFieldAnno.value();
        } else {
            columnName = StrUtil.toUnderlineCase(fieldName);
        }
        return columnName;
    }


    /**
     * 获取ID字段名, 若为找到@TableId, 则默认为id
     *
     * @param entityClass 实体类class对象
     * @param fields      实体类的所有字段
     * @param <T>         实体类泛型
     * @return String
     */
    public static <T> String getIdColumnName(Class<T> entityClass, List<Field> fields) {
        return fields.stream()
                .filter(field -> AnnotationUtil.hasAnnotation(entityClass, TableId.class))
                .map(Field::getName)
                .findAny()
                .orElse("id");
    }


    /**
     * 获取插入语句的前缀 SQL
     *
     * @param entityList    实体列表
     * @param defaultTbName 默认表名
     * @param entityClass   实体类class 对象
     * @param fields        List<Field>
     * @param <T>           实体类泛型
     * @return SQL string builder
     */
    public static <T> StringBuilder getInsertIntoSql(boolean entityList, String defaultTbName, Class<T> entityClass, List<Field> fields, boolean update) {
        if (entityList || CollectionUtils.isEmpty(fields)) {
            return null;
        }

        // 获取字段名称部分SQL
        Pair<String, String> columnNameSqlAndParamNameSqlPair = update ? getColumnNameSqlAndParamNameWithUpdateSqlPair(fields, entityClass)
                : getColumnNameSqlAndParamNameSqlPair(fields);

        // 拼接INSERT INTO语句的初始化 SQL
        return new StringBuilder(SqlConstant.INSERT_INTO)
                .append(StrUtil.isBlankIfStr(defaultTbName) ? getTbName(entityClass) : defaultTbName)
                .append(columnNameSqlAndParamNameSqlPair.getKey())
                .append(SqlConstant.VALUES)
                .append(columnNameSqlAndParamNameSqlPair.getValue());
    }


    public static StringBuilder getInsertIntoSql(String tbName, Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        List<String> columNames = new ArrayList<>();
        List<String> paramsNames = new ArrayList<>();
        keySet.forEach(key -> {
            columNames.add(StrUtil.toUnderlineCase(key));
            paramsNames.add(SqlConstant.COLON + key);
        });

        // 拼接INSERT INTO语句的初始化 SQL
        return new StringBuilder(SqlConstant.INSERT_INTO)
                .append(tbName)
                .append(separateByCommas(columNames, true))
                .append(SqlConstant.VALUES)
                .append(separateByCommas(paramsNames, true));
    }

    public static StringBuilder getInsertIntoSql(String tbName, Map<String, Object> map, String idKeyName, boolean update) {
        Pair<String, String> columnNameSqlAndParamNameSqlPair = getColumnNameSqlAndParamNameSqlPair(map, idKeyName, update);

        // 拼接INSERT INTO语句的初始化 SQL
        return new StringBuilder(SqlConstant.INSERT_INTO)
                .append(tbName)
                .append(columnNameSqlAndParamNameSqlPair.getKey())
                .append(SqlConstant.VALUES)
                .append(columnNameSqlAndParamNameSqlPair.getValue());
    }


    /**
     * 获取Java字段对应的数据库的字段名称
     * 优先取@TableField注解的value属性, 没有则取Snake形式的字段名称
     *
     * @param field Field
     * @return String
     */
    public static String getColumnName(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField == null ? StrUtil.toUnderlineCase(field.getName()) : tableField.value();
    }

    /**
     * 获取Field对应的数据库字段名, 用逗号","拼接
     *
     * @param fields 字段
     * @return {@link String }
     */
    public static String joinColumnNames(List<Field> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return StrUtil.EMPTY;
        }

        return fields.stream().map(SqlUtil::getColumnName).collect(getColumnWithoutBracketCollector());
    }


    /**
     * 获取Field对应的数据库字段名, 用逗号","拼接, 并使用括号进行包裹
     *
     * @param fields 字段
     * @return {@link String }
     */
    public static String joinColumnNamesWithBracket(List<Field> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return StrUtil.EMPTY;
        }

        return fields.stream().map(SqlUtil::getColumnName).collect(getColumnWithBracketCollector());
    }

    public static Collector<CharSequence, ?, String> getColumnWithBracketCollector() {
        return Collectors.joining(SqlConstant.COMMA, SqlConstant.LEFT_BRACKET, SqlConstant.RIGHT_BRACKET);
    }

    public static Collector<CharSequence, ?, String> getColumnWithoutBracketCollector() {
        return Collectors.joining(SqlConstant.COMMA);
    }

    public Pair<String, List<Field>> getTbNameAndFieldListPair(Class<?> clazz) {
        // 获取所有字段
        List<Field> fieldList = ClassUtil.getAllFields(clazz);
        Assert.notEmpty(fieldList, "[SqlUtil#getTbNameAndFieldListPair] No field found!");

        // 获取表名称, 优先获取@TableName注解的内容
        String sTbName = getTbNameByAnno(clazz);
        sTbName = StrUtil.isBlankIfStr(sTbName) ? ClassUtil.getClassUnderLineName(clazz) : sTbName;

        return Pair.of(sTbName, fieldList);
    }

    /**
     * 获取字段名称以及对应的值的Map, 组成映射关系
     *
     * @param fields 字段
     * @param o      o
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    public static Map<String, Object> getFiledValueMap(List<Field> fields, Object o) {
        Map<String, Object> tagValueMap = new HashMap<>(fields.size());
        for (Field field : fields) {
            tagValueMap.put(field.getName(), ReflectUtil.getFieldValue(o, field));
        }
        return tagValueMap;
    }

    public static String joinColumnNamesAndValuesSql(Object object, List<Field> fields, Map<String, Object> paramsMap) {
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

        String fieldValueParamsStr = fieldValueParamNames.stream()
                .map(item -> SqlConstant.COLON + item)
                .collect(SqlUtil.getColumnWithBracketCollector());

        return fieldNameStr + SqlConstant.VALUES + fieldValueParamsStr;
    }

    public static <T> String getColumnName(GetterFunction<T, ?> getterFunc) {
        String fieldName = LambdaUtil.getFiledNameByGetter(getterFunc);
        Field field = ClassUtil.getFieldByName(LambdaUtil.getEntityClass(getterFunc), fieldName);
        String tableFiledAnnoValue = AnnotationUtil.getAnnotationValue(field, TableField.class, "value");
        return StrUtil.isNotBlank(tableFiledAnnoValue) ? tableFiledAnnoValue : StrUtil.toUnderlineCase(fieldName);
    }

    public static <T> String getColumnName(Class<T> tClass, GetterFunction<T, ?> getterFunc) {
        String fieldName = LambdaUtil.getFiledNameByGetter(getterFunc);
        Field field = ClassUtil.getFieldByName(tClass, fieldName);
        String tableFiledAnnoValue = AnnotationUtil.getAnnotationValue(field, TableField.class, "value");
        return StrUtil.isNotBlank(tableFiledAnnoValue) ? tableFiledAnnoValue : StrUtil.toUnderlineCase(fieldName);
    }
}
