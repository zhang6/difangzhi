package com.chronicle.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private long total;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setData(page.getContent());
        r.setTotal(page.getTotalElements());
        return r;
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        PageResult<T> r = new PageResult<>();
        r.setData(list);
        r.setTotal(total);
        return r;
    }
}
