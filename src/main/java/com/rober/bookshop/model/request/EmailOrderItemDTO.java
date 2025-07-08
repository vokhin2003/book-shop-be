package com.rober.bookshop.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EmailOrderItemDTO {
    private String thumbnail;
    private String title;
    private String quantity;
    private String price;
}
