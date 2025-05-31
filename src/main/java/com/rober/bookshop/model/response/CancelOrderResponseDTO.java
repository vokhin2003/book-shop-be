package com.rober.bookshop.model.response;

import com.rober.bookshop.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelOrderResponseDTO {

    private Long orderId;
    private OrderStatus status;


}
