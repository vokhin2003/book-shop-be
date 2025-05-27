package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {

    private Meta meta;
    private Object result;

    @Getter
    @Setter
    public static class Meta {
        private int current;
        private int pageSize;
        private int pages;
        private long total;
    }

}
