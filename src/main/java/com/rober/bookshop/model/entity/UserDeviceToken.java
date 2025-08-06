package com.rober.bookshop.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_device_tokens")
@Getter
@Setter
public class UserDeviceToken extends Base {

    @Column(name = "device_token", nullable = false)
    private String deviceToken;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

}
