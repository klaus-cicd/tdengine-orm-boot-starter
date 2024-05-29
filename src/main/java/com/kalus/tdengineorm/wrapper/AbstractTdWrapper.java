package com.kalus.tdengineorm.wrapper;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kalus.tdengineorm.constant.SqlConstant;
import com.kalus.tdengineorm.constant.TdSqlConstant;
import com.kalus.tdengineorm.enums.TdWrapperTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Klaus
 */
@NoArgsConstructor
public abstract class AbstractTdWrapper<T> {

    protected StringBuilder finalSql = new StringBuilder();
    protected String tbName;
    protected StringBuilder where = new StringBuilder();
    protected AtomicInteger paramNameSeq;
    @Getter
    @Setter
    private Class<T> entityClass;
    @Getter
    @Setter
    private Map<String, Object> paramsMap = new HashMap<>(16);


    public AbstractTdWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        initTbName();
    }

    protected abstract TdWrapperTypeEnum type();


    protected void buildFrom() {
        finalSql.append(SqlConstant.FROM).append(tbName).append(SqlConstant.BLANK);
    }

    protected void initTbName() {
        String name = AnnotationUtil.getAnnotationValue(entityClass, TableName.class, "value");
        if (StrUtil.isBlank(name)) {
            name = StrUtil.toUnderlineCase(entityClass.getSimpleName());
        }
        tbName = name;
    }

    public String getLogLevel() {
        Environment environment = SpringUtil.getBean(Environment.class);
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
