package com.example.jpadeletedemo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
class JpaDeleteDemoApplicationTests {
    @Autowired
    lateinit var carRepository: CarRepository
    @Autowired
    lateinit var platformTransactionManager: PlatformTransactionManager

    val transactionTemplate: TransactionTemplate
        get() = TransactionTemplate(platformTransactionManager)




    @Test
    fun `when deleting fords with derived query in parallel then ObjectOptimisticLockingFailureException is thrown`() {
        createFordFocus()
        createFordFiesta()
        assertThrows<ObjectOptimisticLockingFailureException> {
            runInParallel{
                deleteAllFordsWithDerivedQuery()
            }
        }
    }

    @Test
    fun `when deleting fords with explicit query in parallel then no exception is thrown`() {
        createFordFocus()
        createFordFiesta()
        runInParallel{
            deleteWithExplicitQuery()
        }
    }

    private fun createFordFocus(): Int {
        return transactionTemplate.execute { action ->
            val car = Car(
                make = "Ford",
                model = "Focus",
                year = 2003,
                color = "Green",
                horsepower = 118,
                engineType = "Default",
                price = (2500).toBigDecimal()
            )
            carRepository.save(car)
            car.id!!
        }!!
    }

    private fun createFordFiesta(): Int {
        return transactionTemplate.execute { action ->
            val car = Car(
                make = "Ford",
                model = "Fiesta",
                year = 2014,
                color = "BLue",
                horsepower = 98,
                engineType = "Default",
                price = (2500).toBigDecimal()
            )
            carRepository.save(car)
            car.id!!
        }!!
    }

    private fun deleteAllFordsWithDerivedQuery() {
        transactionTemplate.execute { action ->
            carRepository.deleteAllByMake("Ford")
        }
    }

    private fun deleteWithExplicitQuery() {
        transactionTemplate.execute { action ->
            carRepository.deleteAllByMakeWithExplicitQuery("Ford")
        }
    }

    private fun runInParallel(runnable: Runnable) {
        val threads = (1..10).map {
            Thread {
                runnable.run()
            }
        }
        val exceptions = mutableListOf<Throwable>()
        threads.forEach {
            it.setUncaughtExceptionHandler { t, e ->
                exceptions += e
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        exceptions.forEach {
            throw it
        }
    }

}
