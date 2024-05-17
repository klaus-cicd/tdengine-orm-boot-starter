package com.kalus.tdengineorm.wrapper;

import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import lombok.RequiredArgsConstructor;

/**
 * @author Klaus
 */
@RequiredArgsConstructor
public class SelectJoinerSymbol<T> extends AbstractSelectJoiner {

    private final SelectJoinerWrapper<T> selectJoinerWrapper;

    public SelectJoinerWrapper<T> operate(SelectJoinSymbolEnum selectJoinSymbolEnum) {
        selectJoinerWrapper.operate(selectJoinSymbolEnum);
        return this.selectJoinerWrapper;
    }

    public SelectJoinerWrapper<T> setFinalColumnAliasName(String aliasName) {
        selectJoinerWrapper.setFinalColumnAliasName(aliasName);
        return selectJoinerWrapper;
    }

}
