package com.kalus.tdengineorm.exception;

/**
 * 异常编号
 *
 * @author klaus
 * @date 2024/01/04
 */
public interface ExceptionCode {

    /**
     * 获取编号
     *
     * @return {@link Integer}
     */
    Integer getCode();

    /**
     * 获取消息内容
     *
     * @return {@link String}
     */
    String getMsg();

}
