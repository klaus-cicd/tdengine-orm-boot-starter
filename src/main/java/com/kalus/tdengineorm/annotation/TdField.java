package com.kalus.tdengineorm.annotation;

import com.kalus.tdengineorm.enums.TdFieldTypeEnum;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于描述字段对于TDengine的类型和长度, 主要在创建表时使用
 *
 * @author Klaus
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TdField {

    /**
     * 字段类型
     *
     * @return {@link TdFieldTypeEnum }
     */
    @AliasFor("type")
    TdFieldTypeEnum value() default TdFieldTypeEnum.NCHAR;

    /**
     * 字段类型
     *
     * @return {@link TdFieldTypeEnum }
     */
    @AliasFor("value")
    TdFieldTypeEnum type() default TdFieldTypeEnum.NCHAR;

    /**
     * 字段长度
     * 为0则不设置
     *
     * @return int
     */
    int length() default 0;
}
