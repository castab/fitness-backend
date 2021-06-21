package com.supernet.fitnesstracker.processor

import com.supernet.fitnesstracker.model.FitnessExercise
import com.supernet.fitnesstracker.model.FitnessMeasure
import com.supernet.fitnesstracker.model.FitnessSet
import com.supernet.fitnesstracker.model.FitnessWorkout
import com.supernet.fitnesstracker.repo.ExercisesRepo
import com.supernet.fitnesstracker.repo.SetsRepo
import com.supernet.fitnesstracker.repo.WorkoutsRepo
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import java.time.ZonedDateTime

@Service
class Processor(
    private val workoutsRepo: WorkoutsRepo,
    private val exercisesRepo: ExercisesRepo,
    private val setsRepo: SetsRepo
) {
    private val log = KotlinLogging.logger{}

    fun getWorkout(workoutId: String): Mono<FitnessWorkout> {
        return Mono.zip(workoutsRepo.findById(workoutId), getExercisesForWorkout(workoutId))
            .map{ (workout, exercises) -> workout.copy(exercises = exercises)}
    }

    fun getExercise(exerciseId: String): Mono<FitnessExercise> {
        return Mono.zip(exercisesRepo.findById(exerciseId), getSetsForExercise(exerciseId))
            .map{ (exercise, sets) -> exercise.copy(sets = sets) }
    }

    fun getExercisesByWorkoutId(workoutId: String): Flux<FitnessExercise> {
        return exercisesRepo.findByWorkoutId(workoutId)
            .flatMap{Mono.zip(Mono.just(it), getSetsForExercise(it.id))}
            .map{(exercise, sets) -> exercise.copy(sets = sets)}
    }

    fun startNewWorkout(): Mono<FitnessWorkout> {
        return workoutsRepo.save(FitnessWorkout())
    }

    fun addExerciseToWorkout(workoutId: String, exerciseToAdd: FitnessExercise): Mono<FitnessWorkout> {
        return getExercisesForWorkout(workoutId)
            .map{ list ->
                if (list.isEmpty()) return@map -1
                list.map{it.order}.maxByOrNull{it} ?: -1
            }
            .map{
                FitnessExercise(
                    workoutId = workoutId,
                    name = exerciseToAdd.name,
                    order = it + 1
                )
            }
            .flatMap(exercisesRepo::save)
            .flatMap{getWorkout(workoutId)}
    }

    fun addSetToExercise(exerciseId: String, setToAdd: FitnessSet): Mono<FitnessExercise> {
        return setsRepo.findByExerciseId(exerciseId)
            .collectList()
            .map{ list ->
                if (list.isEmpty()) return@map -1
                list.map{it.order}.maxByOrNull{it} ?: -1
            }
            .map{
                FitnessSet(
                    exerciseId = exerciseId,
                    reps = setToAdd.reps,
                    of = setToAdd.of,
                    order = it + 1
                )
            }
            .flatMap(setsRepo::save)
            .flatMap{getExercise(exerciseId)}
    }

    fun changeExerciseMeasure(exerciseId: String, newMeasure: FitnessMeasure): Mono<FitnessExercise> {
        return exercisesRepo.findById(exerciseId)
            .map{ it.copy(measure = newMeasure) }
            .flatMap(exercisesRepo::save)
            .zipWith(getSetsForExercise(exerciseId))
            .map{ (exercise, sets) -> exercise.copy(sets = sets) }
    }

    fun deleteExercise(exerciseId: String): Mono<FitnessWorkout> {
        return getExercise(exerciseId)
            .flatMap{ Mono.zip(
                Mono.just(it.workoutId),
                exercisesRepo.deleteById(it.id).thenReturn(""),
                setsRepo.deleteByExerciseId(it.id).thenReturn("")
            )}
            .flatMap{ (workoutId, _, _) -> getWorkout(workoutId) }
            .switchIfEmpty(Mono.just(FitnessWorkout()))
    }

    fun deleteSet(setId: String): Mono<FitnessExercise> {
        return setsRepo.findById(setId)
            .flatMap{ Mono.zip(
                Mono.just(it.exerciseId),
                setsRepo.deleteById(setId).thenReturn("")
            )}
            .flatMap{ (exerciseId, _) -> getExercise(exerciseId) }
    }

    fun findRecentActivityAfter(
        timestamp: ZonedDateTime = ZonedDateTime.now().minusDays(5)
    ): Flux<FitnessWorkout> {
        return Flux.merge(
            workoutsRepo.findByTimestampAfter(timestamp),
            exercisesRepo.findByTimestampAfter(timestamp)
                .map{it.workoutId}
                .distinct()
                .flatMap(workoutsRepo::findById),
            setsRepo.findByTimestampAfter(timestamp)
                .map{it.exerciseId}
                .distinct()
                .flatMap(exercisesRepo::findById)
                .map{it.workoutId}
                .distinct()
                .flatMap(workoutsRepo::findById)
        )
            .distinct()
    }

    private fun getExercisesForWorkout(workoutId: String): Mono<List<FitnessExercise>> {
        return exercisesRepo.findByWorkoutId(workoutId)
            .flatMap{ Mono.zip(Mono.just(it), getSetsForExercise(it.id)) }
            .map{ (exercise, sets) -> exercise.copy(sets = sets) }
            .collectList()
            .map{list -> list.sortedBy{ it.order }}
    }

    private fun getSetsForExercise(exerciseId: String): Mono<List<FitnessSet>> {
        return setsRepo.findByExerciseId(exerciseId)
            .collectList()
            .map{list -> list.sortedBy{ it.order }}
    }

    private operator fun <T: Any, U: Any> Tuple2<T, U>.component1() = t1
    private operator fun <T: Any, U: Any> Tuple2<T, U>.component2() = t2
    private operator fun <T: Any, U: Any, V: Any> Tuple3<T, U, V>.component3() = t3
}