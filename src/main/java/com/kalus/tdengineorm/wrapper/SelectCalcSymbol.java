package com.kalus.tdengineorm.wrapper;

import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import lombok.RequiredArgsConstructor;

/**
 * @author Klaus
 */
@RequiredArgsConstructor
public class SelectCalcSymbol<T> extends AbstractSelectCalc {

    private final SelectCalcWrapper<T> selectCalcWrapper;

    public SelectCalcWrapper<T> operate(SelectJoinSymbolEnum selectJoinSymbolEnum) {
        selectCalcWrapper.operate(selectJoinSymbolEnum);
        return this.selectCalcWrapper;
    }

    public SelectCalcWrapper<T> setFinalColumnAliasName(String aliasName) {
        selectCalcWrapper.setFinalColumnAliasName(aliasName);
        return selectCalcWrapper;
    }

}
