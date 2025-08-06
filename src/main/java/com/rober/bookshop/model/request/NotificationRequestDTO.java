package com.rober.bookshop.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationRequestDTO {
    private String title;
    private String body;
    private String image;
    private Map<String, String> data;
    private String deviceToken;

}
