package com.kalus.tdengineorm.wrapper;

import com.kalus.tdengineorm.entity.BaseTdEntity;

/**
 * @author Klaus
 */
public class TdWrappers {

    public static <T extends BaseTdEntity> TdQueryWrapper<T> queryWrapper(Class<T> targerClass) {
        return new TdQueryWrapper<>(targerClass);
    }
}
