package com.kalus.tdengineorm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author Klaus
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {

    /**
     * 查询数据列表
     */
    protected List<T> dataList = Collections.emptyList();
    /**
     * 总数
     */
    protected Long total = 0L;
    /**
     * 每页显示条数，默认 10
     */
    protected Long pageSize = 10L;
    /**
     * 当前页
     */
    protected Long pageNo = 1L;

    /**
     * 是否还有下一页
     *
     * @return boolean
     */
    public boolean hasNextPage() {
        return this.pageNo < this.getPageCount();
    }

    /**
     * 获得总页数
     *
     * @return long
     */
    public long getPageCount() {
        if (this.getPageSize() == 0L) {
            return 0L;
        } else {
            return (long) Math.ceil((double) this.getTotal() / this.getPageSize());
        }
    }

}
