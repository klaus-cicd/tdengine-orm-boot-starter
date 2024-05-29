package com.kalus.tdengineorm.func;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Klaus
 */
@FunctionalInterface
public interface GetterFunction<T, R> extends Function<T, R>, Serializable {

}
