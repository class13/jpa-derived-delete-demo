# JPA Derived Delete Queries are evil.
## Statement
Derived delete queries of JPA repositories cannot be used safely and will produce exceptions in high-parallel environments like REST APIs.

## Explanation
JPA Repository's derived delete queries always fetch entities, before it deletes them. If the entity is deleted between
the fetch and the delete by another transaction, it results in an unexpected `ObjectOptimisticLockingFailureException`.

## Example
First we'll look at standard JpaRepository function `deleteById`  

`carRepository.deleteById(1)`  

This results in a fetching of the entity by the id:  
```
Hibernate:
    select
        c1_0.id,
        c1_0.color,
        c1_0.engine_type,
        c1_0.horsepower,
        c1_0.make,
        c1_0.model,
        c1_0.price,
        c1_0.year
    from
        cars c1_0
    where
        c1_0.id=?
```
And only then we delete by id:  
```
Hibernate:
    delete
    from
        cars
    where
        id=?
```

Secondly we'll try a derived query method. We want to delete all cars made by Ford:  
`carRepository.deleteAllByMake("Ford")`

First Hibernate fetches all entities with the make `Ford`:
```
Hibernate:
    select
        c1_0.id,
        c1_0.color,
        c1_0.engine_type,
        c1_0.horsepower,
        c1_0.make,
        c1_0.model,
        c1_0.price,
        c1_0.year
    from
        cars c1_0
    where
        c1_0.make=?
```
And then it deletes each returned entity by id (in this case 2 cars made by Ford):
```
Hibernate:
    delete
    from
        cars
    where
        id=?
```

```
Hibernate:
    delete
    from
        cars
    where
        id=?
```

Both of these cases are inherently vulnerable to `ObjectOptimisticLockingFailureException` when running in parallel (which can always happen in rest APIs).

For example deleting by make in 10 parallel threads will cause several `ObjectOptimisticLockingFailureException`:
```kotlin
val threads = (1..10).map {
    Thread {
        transactionTemplate.execute { action ->
            carRepository.deleteAllByMake("Ford")
        }
    }
}
threads.forEach { it.start() }
threads.forEach { it.join() }
```

## Remedy
If we instead define an explicit delete query with the `@Query` annotation, we get a single delete query. 
This is safe against `ObjectOptimisticLockingFailureException`.

```kotlin
@Repository
interface CarRepository: JpaRepository<Car, Int> {

    @Modifying
    @Query("delete from Car c where c.make = :make")
    fun deleteAllByMake(make: String)
}
```
This results in one single query at call:
```
Hibernate: 
    delete 
    from
        cars 
    where
        make=?
```

And parallel executions run through without an exception:
```kotlin
val threads = (1..10).map {
    Thread {
        transactionTemplate.execute { action ->
            carRepository.deleteAllByMake("Ford")
        }
    }
}
threads.forEach { it.start() }
threads.forEach { it.join() }
```

## Try for yourself
In this project you'll find junit tests, that proof this concept, by running parallel deletions against a H2 Database.

## TL;DR
Don't use derived deletion queries, use explicit deletion queries instead. :)


