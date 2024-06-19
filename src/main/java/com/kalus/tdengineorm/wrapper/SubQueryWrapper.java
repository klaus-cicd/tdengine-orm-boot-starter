package com.kalus.tdengineorm.wrapper;

import com.kalus.tdengineorm.func.GetterFunction;
import com.kalus.tdengineorm.util.SqlUtil;

/**
 * @author Silas
 */
public class SubQueryWrapper<L, R> {

    public SubQueryWrapper<L, R> eq(Class<L> leftClass, Class<R> rightClass, GetterFunction<L, ?> leftGetterFunction,
                                    GetterFunction<R, ?> rightGetterFunction) {
        String leftColumnName = SqlUtil.getColumnName(leftClass, leftGetterFunction);
        String rightColumnName = SqlUtil.getColumnName(rightClass, rightGetterFunction);


        return this;
    }

    public <T, X> R eq(GetterFunction<T, ?> left, GetterFunction<X, ?> right) {


        return null;
    }
}
