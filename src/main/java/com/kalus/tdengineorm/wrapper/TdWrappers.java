package com.kalus.tdengineorm.wrapper;

/**
 * @author Klaus
 */
public class TdWrappers {

    public static <T> TdQueryWrapper<T> queryWrapper(Class<T> targerClass) {
        return new TdQueryWrapper<T>(targerClass);
    }

    public static <T> SelectJoinerWrapper<T> selectWrapper(Class<T> targerClass){
        return new SelectJoinerWrapper<T>(targerClass);
    }
}
