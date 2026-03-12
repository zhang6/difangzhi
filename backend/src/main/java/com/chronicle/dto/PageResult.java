package com.chronicle.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getContent());
        result.setTotal(page.getTotalElements());
        result.setPage(page.getNumber() + 1);
        result.setPageSize(page.getSize());
        return result;
    }

    public static <T> PageResult<T> of(List<T> data, long total) {
        PageResult<T> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        return result;
    }
}
