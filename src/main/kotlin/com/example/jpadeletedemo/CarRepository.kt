package com.example.jpadeletedemo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CarRepository: JpaRepository<Car, Int> {
    fun deleteAllByMake(make: String)

    @Modifying
    @Query("delete from Car c where c.make = :make")
    fun deleteAllByMakeWithExplicitQuery(make: String)
}