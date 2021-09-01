package com.supernet.fitnesstracker.processor

import com.supernet.fitnesstracker.api.ResourceNotFoundException
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
        return workoutsRepo.findById(workoutId)
            .throwResourceNotFoundIfEmpty("Workout not found")
            .flatMap{ Mono.zip( Mono.just(it), getExercisesForWorkout(it.id)) }
            .map{ (workout, exercises) -> workout.copy(exercises = exercises) }
    }

    fun getExercise(exerciseId: String): Mono<FitnessExercise> {
        return exercisesRepo.findById(exerciseId)
            .throwResourceNotFoundIfEmpty("Exercise not found")
            .flatMap{ Mono.zip(Mono.just(it), getSetsForExercise(it.id)) }
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
        return workoutsRepo.findById(workoutId)
            .throwResourceNotFoundIfEmpty("Workout not found")
            .flatMap{ getExercisesForWorkout(workoutId) }
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
        return exercisesRepo.findById(exerciseId)
            .flatMapMany{ setsRepo.findByExerciseId(exerciseId) }
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
            .throwResourceNotFoundIfEmpty("Exercise not found")
            .map{ it.copy(measure = newMeasure) }
            .flatMap(exercisesRepo::save)
            .zipWith(getSetsForExercise(exerciseId))
            .map{ (exercise, sets) -> exercise.copy(sets = sets) }
    }

    fun deleteWorkout(workoutId: String): Mono<String> {
        return getWorkout(workoutId)
            .throwResourceNotFoundIfEmpty("Workout not found")
            .flatMapIterable{ it.exercises }
            .flatMap{ deleteExercise(it.id) }
            .collectList()
            .flatMap{ workoutsRepo.deleteById(workoutId).thenReturn("{}") }
    }

    fun deleteExercise(exerciseId: String): Mono<FitnessWorkout> {
        return getExercise(exerciseId)
            .throwResourceNotFoundIfEmpty("Exercise not found")
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
            .throwResourceNotFoundIfEmpty(("Set not found"))
            .flatMap{ Mono.zip(
                Mono.just(it.exerciseId),
                setsRepo.deleteById(setId).thenReturn("")
            )}
            .flatMap{ (exerciseId, _) -> getExercise(exerciseId) }
    }

    fun getRecentActivity(
        days: Long = 60
    ): Flux<FitnessWorkout> {
        val timestamp = ZonedDateTime.now().minusDays(days)
        return Flux.merge(
            workoutsRepo.findByTimestampAfter(timestamp),
            exercisesRepo.findByTimestampAfter(timestamp)
                .map{it.workoutId}
                .flatMap(workoutsRepo::findById),
            setsRepo.findByTimestampAfter(timestamp)
                .map{it.exerciseId}
                .flatMap(exercisesRepo::findById)
                .map{it.workoutId}
                .flatMap(workoutsRepo::findById)
        )
            .distinct()
            .sort()
            // .flatMap{getWorkout(it.id)} // TODO: Figure out how to get this pre-loaded in UI
    }

    private fun checkIfWorkoutExists(workoutId: String) = workoutsRepo.existsById(workoutId)
    private fun checkIfExerciseExists(exerciseId: String) = exercisesRepo.existsById(exerciseId)

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

    private fun <T: Any> Mono<T>.throwResourceNotFoundIfEmpty(msg: String) =
        switchIfEmpty(Mono.error(ResourceNotFoundException(msg)))
}