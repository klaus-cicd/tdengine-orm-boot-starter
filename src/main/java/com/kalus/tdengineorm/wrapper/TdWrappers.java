package com.kalus.tdengineorm.wrapper;

/**
 * @author Klaus
 */
public class TdWrappers<T> {

    public static <T> TdQueryWrapper<T> queryWrapper(Class<T> targerClass) {
        return new TdQueryWrapper<T>(targerClass);
    }

    public static <T> LambdaTdQueryWrapper<T> lambdaQueryWrapper(Class<T> targerClass) {
        return new LambdaTdQueryWrapper<T>(targerClass);
    }

}
