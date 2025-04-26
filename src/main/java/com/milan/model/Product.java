package com.milan.model;

import com.milan.dto.ProductDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
@Builder(toBuilder = true) // allows modifying existing instances
public class Product extends BaseDates{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 250,nullable = false)
    private String productName;

    @Column(length = 160, nullable = false)
    private String shortDescription;

    @Column(length = 8000,nullable = false)
    private String description;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Integer stock;

    private String productImage;

    private Integer discountPercent;

    private Double discountedPrice;

    @Column(nullable = false)
    private Boolean isActive;

    //Many products belong to one category
    // Load category immediately when product is fetched
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    //helps in updating product using builder pattern
    public Product updateFromDto(ProductDto dto) {
        return this.toBuilder()
                .productName(dto.getProductName() != null ? dto.getProductName() : this.productName)
                .shortDescription(dto.getShortDescription() != null ? dto.getShortDescription() : this.shortDescription)
                .description(dto.getDescription() != null ? dto.getDescription() : this.description)
                .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : this.unitPrice)
                .stock(dto.getStock() != null ? dto.getStock() : this.stock)
                .productImage(dto.getProductImage() != null ? dto.getProductImage() : this.productImage)
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : this.discountPercent)
                .discountedPrice(dto.getDiscountedPrice() != null ? dto.getDiscountedPrice() : this.discountedPrice)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : this.isActive)
                .build();
    }

}
