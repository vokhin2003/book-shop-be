package com.rober.bookshop.model.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book extends Base {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnail;
    private Integer quantity;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> slider;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "book")
    private List<Cart> carts;

    @OneToMany(mappedBy = "book")
    private List<OrderItem> orderItems;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (discount >= 0 AND discount <= 100)")
    private Integer discount;

    private Integer sold;
    private Integer age;

    private Instant publicationDate;
    private String publisher;
    private Integer pageCount;
    private String coverType;
}
