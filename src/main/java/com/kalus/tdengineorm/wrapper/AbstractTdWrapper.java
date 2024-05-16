package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdWrapperTypeEnum;
import com.klaus.fd.constant.SqlConstant;
import com.klaus.fd.utils.BeanUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Klaus
 */
@Data
@NoArgsConstructor
public abstract class AbstractTdWrapper<T> {

    private StringBuilder finalSql = new StringBuilder();
    private String tbName;
    private StringBuilder where = new StringBuilder();
    private Map<String, Object> paramsMap = new HashMap<>(16);
    private Class<T> entityClass;
    protected AtomicInteger paramNameSeq;

    public AbstractTdWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        initTbName();
    }

    protected abstract TdWrapperTypeEnum type();


    /**
     * 自定义TB名称, 比如嵌套查询时
     *
     * @param innerSql 内层Sql
     */
    protected abstract void changeInnerTbName(String innerSql);

    /**
     * 自定义TB名称, 比如嵌套查询时
     *
     * @param innerSql 内层Sql
     */
    protected abstract void changeOuterTbName(String innerSql);


    protected void buildFrom() {
        getFinalSql().append(SqlConstant.FROM).append(getTbName()).append(SqlConstant.BLANK);
    }

    protected void initTbName() {
        String name = AnnotationUtil.getAnnotationValue(entityClass, TableName.class, "value");
        if (StrUtil.isBlank(name)) {
            name = StrUtil.toUnderlineCase(entityClass.getSimpleName());
        }
        tbName = name;
    }

    public String getLogLevel() {
        Environment environment = BeanUtil.getBean(Environment.class);
        return environment.getProperty("td-orm.log-level");
    }

    protected Integer getParamNameSeq() {
        if (paramNameSeq == null) {
            paramNameSeq = new AtomicInteger(0);
            return paramNameSeq.getAndIncrement();
        }
        return paramNameSeq.incrementAndGet();
    }

    protected String genParamName() {
        return TdSqlConstant.MAP_PARAM_NAME_PREFIX + getParamNameSeq();
    }
}
