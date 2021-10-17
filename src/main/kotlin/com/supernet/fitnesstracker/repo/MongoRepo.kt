package com.supernet.fitnesstracker.repo

import com.supernet.fitnesstracker.model.FitnessExercise
import com.supernet.fitnesstracker.model.FitnessSet
import com.supernet.fitnesstracker.model.FitnessWorkout
import com.supernet.fitnesstracker.model.GenericDocumentId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

@Service
interface WorkoutsRepo : ReactiveMongoRepository<FitnessWorkout, String> {
    @Query(value = "{}", fields = "{exercises: 0}")
    fun findAllWorkoutIds(): Flux<FitnessWorkout>
    fun findByTimestampAfter(date: ZonedDateTime): Flux<FitnessWorkout>
}

@Service
interface ExercisesRepo : ReactiveMongoRepository<FitnessExercise, String> {
    fun findByWorkoutId(workoutId: String): Flux<FitnessExercise>
    fun findByTimestampAfter(timestamp: ZonedDateTime): Flux<FitnessExercise>
    @Query(value = "{ workoutId: ?0 }", fields = "{ _id : 1 }")
    fun findExerciseIdsForWorkoutId(workoutId: String): Flux<GenericDocumentId>
    fun findByNameRegexAndTimestampAfter(regexName: String, timestamp: ZonedDateTime): Flux<FitnessExercise>
}

@Service
interface SetsRepo : ReactiveMongoRepository<FitnessSet, String> {
    fun findByExerciseId(exerciseId: String): Flux<FitnessSet>
    fun findByTimestampAfter(timestamp: ZonedDateTime): Flux<FitnessSet>
    fun deleteByExerciseId(exerciseId: String): Mono<Void>
}