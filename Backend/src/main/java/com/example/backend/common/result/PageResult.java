package com.example.backend.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long page;
    private long size;
    private long total;
    private List<T> records;

    public PageResult(long page, long size, long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }
}
