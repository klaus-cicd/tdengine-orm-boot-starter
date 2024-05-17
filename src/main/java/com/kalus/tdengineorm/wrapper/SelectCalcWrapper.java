package com.kalus.tdengineorm.wrapper;

import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.klaus.fd.util.SqlUtil;
import com.klaus.fd.utils.AssertUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;


/**
 * @author Klaus
 */
@RequiredArgsConstructor
public class SelectCalcWrapper<T> extends AbstractSelectCalc {
    private final Class<T> entityClass;
    private final SelectCalcSymbol<T> selectCalcSymbol = new SelectCalcSymbol<>(this);
    private String finalColumnAliasName;

    public SelectCalcSymbol<T> select(String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).build());
        return this.selectCalcSymbol;
    }

    public final SelectCalcSymbol<T> select(Function<T, ?> getterFunc) {
        return this.select(SqlUtil.getColumnName(entityClass, getterFunc));
    }

    public SelectCalcSymbol<T> select(TdSelectFuncEnum selectFuncEnum, String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).selectFuncEnum(selectFuncEnum).build());
        return this.selectCalcSymbol;
    }

    public SelectCalcSymbol<T> select(TdSelectFuncEnum selectFuncEnum, Function<T, ?> getterFunc) {
        return select(selectFuncEnum, SqlUtil.getColumnName(this.entityClass, getterFunc));
    }

    /**
     * 给最后一个字段增加操作符
     *
     * @param selectJoinSymbolEnum 选择连接符号enum
     */
    void operate(SelectJoinSymbolEnum selectJoinSymbolEnum) {
        AssertUtil.notEmpty(selectColumnList, new TdOrmException(TdOrmExceptionCode.NO_SELECT));
        SelectColumn lastOne = selectColumnList.get(selectColumnList.size() - 1);
        lastOne.selectJoinSymbolSuffix = selectJoinSymbolEnum;
    }

    private void addSelect(SelectColumn selectColumn) {
        selectColumnList.add(selectColumn);
    }

    public void setFinalColumnAliasName(Function<T, ?> aliasColumn) {
        setFinalColumnAliasName(SqlUtil.getColumnName(entityClass, aliasColumn));
    }

    public void setFinalColumnAliasName(String aliasName) {
        finalColumnAliasName = aliasName;
    }

    @Override
    protected String getFinalColumnAliasName() {
        return finalColumnAliasName;
    }

    @Data
    @Builder
    static class SelectColumn {
        private TdSelectFuncEnum selectFuncEnum;
        private String columnName;
        private SelectJoinSymbolEnum selectJoinSymbolSuffix;
    }
}
