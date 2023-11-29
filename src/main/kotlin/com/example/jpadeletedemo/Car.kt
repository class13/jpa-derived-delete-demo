package com.example.jpadeletedemo

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "cars")
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,
    @Column(name = "make")
    var make: String? = null,
    @Column(name = "model")
    var model: String? = null,
    @Column(name = "`year`")
    var year: Int? = null,
    @Column(name = "color")
    var color: String? = null,
    @Column(name = "engine_type")
    var engineType: String? = null,
    @Column(name = "horsepower")
    var horsepower: Int? = null,
    @Column(name = "price")
    var price: BigDecimal? = null
)
