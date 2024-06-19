package com.kalus.tdengineorm.mapper;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.json.JSONUtil;
import com.kalus.tdengineorm.annotation.TdTag;
import com.kalus.tdengineorm.constant.SqlConstant;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.dto.Page;
import com.kalus.tdengineorm.entity.BaseTdEntity;
import com.kalus.tdengineorm.enums.TdLogLevelEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.kalus.tdengineorm.strategy.DefaultDynamicNameStrategy;
import com.kalus.tdengineorm.strategy.DynamicNameStrategy;
import com.kalus.tdengineorm.util.*;
import com.kalus.tdengineorm.wrapper.AbstractTdQueryWrapper;
import com.kalus.tdengineorm.wrapper.TdQueryWrapper;
import com.kalus.tdengineorm.wrapper.TdWrappers;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TDengine工具类
 *
 * @author Silas
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class TDengineMapper {

    private final JdbcTemplatePlus jdbcTemplatePlus;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 按ts字段倒叙, 获取最新的一条数据
     *
     * @param clazz clazz
     * @return {@link T }
     */
    public <T extends BaseTdEntity> T getLastOneByTs(Class<T> clazz) {
        TdQueryWrapper<T> wrapper = TdWrappers.queryWrapper(clazz)
                .selectAll()
                .orderByDesc("ts")
                .limit(1);
        return getOne(wrapper);
    }


    /**
     * 查询单条数据
     *
     * @param wrapper 包装器
     * @return {@link T }
     */
    public <T> T getOne(AbstractTdQueryWrapper<T> wrapper) {
        return getOne(wrapper, wrapper.getEntityClass());
    }


    /**
     * 查询单条数据, 可以响应和实体类不一样的对象
     *
     * @param wrapper     包装器
     * @param resultClass 结果类
     * @return {@link R }
     */
    public <T, R> R getOne(AbstractTdQueryWrapper<T> wrapper, Class<R> resultClass) {
        String sql = wrapper.getSql();
        Map<String, Object> paramsMap = wrapper.getParamsMap();
        return getOneWithTdLog(resultClass, sql, paramsMap);
    }


    /**
     * 列表
     *
     * @param wrapper 包装器
     * @return {@link List }<{@link T }>
     */
    public <T> List<T> list(AbstractTdQueryWrapper<T> wrapper) {
        return list(wrapper, wrapper.getEntityClass());
    }


    public <T, R> List<R> list(AbstractTdQueryWrapper<T> wrapper, Class<R> resultClass) {
        return listWithTdLog(wrapper.getSql(), wrapper.getParamsMap(), resultClass);
    }

    public <T extends BaseTdEntity> Page<T> page(long pageNo, long pageSize, TdQueryWrapper<T> wrapper) {
        return page(pageNo, pageSize, wrapper, wrapper.getEntityClass());
    }

    public <T extends BaseTdEntity, R> Page<R> page(long pageNo, long pageSize, TdQueryWrapper<T> wrapper, Class<R> resultClass) {
        String countSql = "select count(*) from (" + wrapper.getSql() + ") t";
        Long count = namedParameterJdbcTemplate.queryForObject(countSql, wrapper.getParamsMap(), Long.class);
        Page<R> page = Page.<R>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(count).build();
        if (count != null && count > 0) {
            List<R> list = listWithTdLog(wrapper.getSql(), wrapper.getParamsMap(), resultClass);
            page.setDataList(list);
        }
        return page;
    }

    /**
     * 插入单条数据
     *
     * @param object 对象
     * @return int
     */
    public int insert(Object object) {
        return insert(object, new DefaultDynamicNameStrategy());
    }

    /**
     * 向指定子表插入数据, 无需指定TAG相关字段
     *
     * @param object              对象
     * @param dynamicNameStrategy 日期动态命名策略
     * @return int
     */
    public int insert(Object object, DynamicNameStrategy dynamicNameStrategy) {
        // 获取超级表表名&所有Field
        List<Field> noTagFieldList = TdSqlUtil.getNoTagFieldList(object.getClass());
        if (CollectionUtils.isEmpty(noTagFieldList)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_COMM_FIELD);
        }

        Map<String, Object> paramsMap = new HashMap<>(noTagFieldList.size());
        String tbName = SqlUtil.getTbName(object.getClass());
        if (null != dynamicNameStrategy) {
            tbName = dynamicNameStrategy.dynamicTableName(tbName);
        }
        String sql = SqlConstant.INSERT_INTO + tbName + SqlUtil.joinColumnNamesAndValuesSql(object, noTagFieldList, paramsMap);
        return updateWithTdLog(sql, paramsMap);
    }


    public int insertUsing(Object object) {
        return insertUsing(object, new DefaultDynamicNameStrategy());
    }

    public <T> void batchInsertUsing(Class<T> clazz, List<T> entityList) {
        batchInsertUsing(clazz, entityList, 1000, new DefaultDynamicNameStrategy());
    }

    public <T> void batchInsertUsing(Class<T> clazz, List<T> entityList, DynamicNameStrategy dynamicTbNameStrategy) {
        batchInsertUsing(clazz, entityList, 1000, dynamicTbNameStrategy);
    }

    public int insertUsing(Object object, DynamicNameStrategy dynamicTbNameStrategy) {
        // 获取表表名&所有Field
        Pair<String, List<Field>> tbNameAndFieldsPair = TdSqlUtil.getTbNameAndFieldListPair(object.getClass());
        // 获取SQL&参数值
        Pair<String, Map<String, Object>> finalSqlAndParamsMapPair = TdSqlUtil.getFinalInsertUsingSql(object, tbNameAndFieldsPair.getValue(), tbNameAndFieldsPair.getKey(), dynamicTbNameStrategy);

        String finalSql = finalSqlAndParamsMapPair.getKey();
        Map<String, Object> paramsMap = finalSqlAndParamsMapPair.getValue();

        return updateWithTdLog(finalSql, paramsMap);
    }

    /**
     * 创建超级表
     *
     * @param clazz clazz
     * @return int
     */
    public <T extends BaseTdEntity> int createStableTable(Class<T> clazz) {
        List<Field> fieldList = ClassUtil.getAllFields(clazz);
        // 区分普通字段和Tag字段
        Pair<List<Field>, List<Field>> fieldListPairByTag = TdSqlUtil.differentiateByTag(fieldList);

        List<Field> commFieldList = fieldListPairByTag.getValue();
        if (CollectionUtils.isEmpty(commFieldList)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_COMM_FIELD);
        }

        Field primaryTsField = TdSqlUtil.checkPrimaryTsField(commFieldList);

        String finalSql = TdSqlConstant.CREATE_STABLE + SqlUtil.getTbName(clazz) + TdSqlUtil.buildCreateColumn(commFieldList, primaryTsField);
        List<Field> tagFieldList = fieldListPairByTag.getKey();

        if (CollectionUtils.isEmpty(tagFieldList)) {
            throw new TdOrmException(TdOrmExceptionCode.NO_TAG_FIELD);
        }
        String tagColumnSql = TdSqlUtil.buildCreateColumn(tagFieldList, null);
        finalSql += SqlConstant.BLANK + TdSqlConstant.TAGS + tagColumnSql;
        return updateWithTdLog(finalSql, new HashMap<>(0));
    }


    public <T> void batchInsertUsing(Class<T> clazz, List<T> entityList, int pageSize, DynamicNameStrategy dynamicTbNameStrategy) {
        // 获取超级表表名&所有字段
        Pair<String, List<Field>> tbNameAndFieldsPair = TdSqlUtil.getTbNameAndFieldListPair(clazz);

        // 目前仅支持同子表的数据批量插入, 所以随意取一个对象的tag的值都是一样的
        List<List<T>> partition = ListUtil.partition(entityList, pageSize);
        T t = partition.get(0).get(0);

        List<Field> tagFields = ClassUtil.getAllFields(t.getClass()).stream()
                .filter(field -> field.isAnnotationPresent(TdTag.class))
                .collect(Collectors.toList());

        Map<String, Object> tagValueMap = SqlUtil.getFiledValueMap(tagFields, t);

        for (List<T> list : partition) {
            Map<String, Object> paramsMap = new HashMap<>(list.size());
            paramsMap.putAll(tagValueMap);
            StringBuilder finalSql = new StringBuilder(TdSqlUtil.justGetInsertUsingSql(t, tbNameAndFieldsPair.getKey(), tbNameAndFieldsPair.getValue(), dynamicTbNameStrategy, paramsMap));
            for (int i = 0; i < list.size(); i++) {
                T entity = list.get(i);
                List<Field> fields = ClassUtil.getAllFields(entity.getClass());
                finalSql.append(TdSqlUtil.joinSqlValue(entity, fields, paramsMap, i));
            }
            int result = namedParameterJdbcTemplate.update(finalSql.toString(), paramsMap);
            if (log.isDebugEnabled()) {
                log.debug("{} =====execute result====>{}", finalSql, result);
            }
        }
    }

    public <T extends BaseTdEntity> int deleteByTs(Class<T> clazz, Timestamp ts) {
        String tbName = SqlUtil.getTbName(clazz);
        String sql = "DELETE FROM " + tbName + " WHERE ts = :ts";
        Map<String, Object> paramsMap = new HashMap<>(1);
        paramsMap.put("ts", ts);
        return namedParameterJdbcTemplate.update(sql, paramsMap);
    }

    public <T extends BaseTdEntity> int batchDeleteByTs(Class<T> clazz, List<Timestamp> tsList) {
        String tbName = SqlUtil.getTbName(clazz);
        String sql = "DELETE FROM " + tbName + " WHERE ts IN (:tsList)";
        Map<String, Object> paramsMap = new HashMap<>(1);
        paramsMap.put("tsList", tsList);
        return namedParameterJdbcTemplate.update(sql, paramsMap);
    }


    private static void tdLog(String sql, Map<String, Object> paramsMap) {
        TdLogLevelEnum tdLogLevelEnum = TdOrmUtil.getLogLevel();
        if (null != tdLogLevelEnum) {
            String logFormat = "【TDengineMapperLog】 \n【SQL】 : {} \n【Params】: {}";
            switch (tdLogLevelEnum) {
                case DEBUG:
                    if (log.isDebugEnabled()) {
                        log.debug(logFormat, sql, JSONUtil.toJsonStr(paramsMap));
                    }
                    break;
                case INFO:
                    log.info(logFormat, sql, JSONUtil.toJsonStr(paramsMap));
                    break;
                default:
            }
        }
    }

    private int updateWithTdLog(String finalSql, Map<String, Object> paramsMap) {
        tdLog(finalSql, paramsMap);
        return namedParameterJdbcTemplate.update(finalSql, paramsMap);
    }

    private <T, R> List<R> listWithTdLog(String sql, Map<String, Object> paramsMap, Class<R> resultClass) {
        tdLog(sql, paramsMap);
        return jdbcTemplatePlus.list(sql, paramsMap, resultClass);
    }

    private <R> R getOneWithTdLog(Class<R> resultClass, String sql, Map<String, Object> paramsMap) {
        tdLog(sql, paramsMap);
        return jdbcTemplatePlus.get(sql, paramsMap, resultClass);
    }

}
