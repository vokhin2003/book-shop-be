package com.rober.bookshop.model.entity;

import com.rober.bookshop.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "province", length = 120, nullable = false)
    private String province;

    @Column(name = "ward", length = 120)
    private String ward;

    @Column(name = "address_detail", length = 255, nullable = false)
    private String addressDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20, nullable = false)
    private AddressType addressType;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}


