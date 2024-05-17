package com.kalus.tdengineorm.wrapper;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.exception.TdOrmException;
import com.kalus.tdengineorm.exception.TdOrmExceptionCode;
import com.kalus.tdengineorm.util.TdSqlUtil;
import com.klaus.fd.utils.AssertUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


/**
 * @author Klaus
 */
@RequiredArgsConstructor
public class SelectJoinerWrapper<T> extends AbstractSelectJoiner {
    private final Class<T> entityClass;
    private final SelectJoinerSymbol<T> selectJoinerSymbol = new SelectJoinerSymbol<>(this);
    private String finalColumnAliasName;

    public SelectJoinerSymbol<T> select(String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).build());
        return this.selectJoinerSymbol;
    }

    public final SelectJoinerSymbol<T> select(SFunction<T, ?> sFunction) {
        return this.select(TdSqlUtil.getColumnName(entityClass, sFunction));
    }

    public SelectJoinerSymbol<T> select(TdSelectFuncEnum selectFuncEnum, String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).selectFuncEnum(selectFuncEnum).build());
        return this.selectJoinerSymbol;
    }

    public SelectJoinerSymbol<T> select(TdSelectFuncEnum selectFuncEnum, SFunction<T, ?> sFunction) {
        return select(selectFuncEnum, TdSqlUtil.getColumnName(this.entityClass, sFunction));
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

    public void setFinalColumnAliasName(SFunction<T, ?> aliasColumn) {
        setFinalColumnAliasName(TdSqlUtil.getColumnName(entityClass, aliasColumn));
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
