package com.heojin.async_excel_export.dummyData.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Data {


    // pdf 요구사항은 Integer 로 되어있음
    // 나중에 한번 더 보고 필요하면 Integer 로 수정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "product_name")
    private String productName;

    private String category;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "order_date")
    private LocalDateTime orderDate;
}
