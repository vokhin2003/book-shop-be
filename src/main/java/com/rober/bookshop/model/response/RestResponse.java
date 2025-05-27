package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {

    private int statusCode;
    private String error;

    private Object message;
    private T data;

}
