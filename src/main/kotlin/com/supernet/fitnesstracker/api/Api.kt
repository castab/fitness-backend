package com.supernet.fitnesstracker.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.supernet.fitnesstracker.model.FitnessExercise
import com.supernet.fitnesstracker.model.FitnessMeasure
import com.supernet.fitnesstracker.model.FitnessSet
import com.supernet.fitnesstracker.model.FitnessWorkout
import com.supernet.fitnesstracker.processor.Processor
import com.supernet.fitnesstracker.util.Constants
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@CrossOrigin(origins = ["http://localhost:3000", "http://ultra6mobile:3000"], maxAge = 3600)
class Api (
    private val processor: Processor
) {
    private val uuidPattern =
        Regex("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")

    @GetMapping("/measure")
    fun getMeasuring(): Map<String, List<String>> {
        return mapOf(
            "types" to Constants.UnitTypeEnum.values().toList().map{it.toString()},
            "units" to Constants.UnitMeasureEnum.values().toList().map{it.toString()}
        )
    }

    @GetMapping("/uuid")
    fun getUuid() = UUID.randomUUID().toString()

    @GetMapping("/exercise/{exerciseId}")
    fun getExercise(@PathVariable exerciseId: String): Mono<FitnessExercise> {
        return exerciseId.checkValidity()
            .flatMap(processor::getExercise)
    }

    @GetMapping("/workout/{workoutId}")
    fun getWorkout(@PathVariable workoutId: String): Mono<FitnessWorkout> {
        return workoutId.checkValidity()
            .flatMap(processor::getWorkout)
    }

    @GetMapping("/workout/{workoutId}/exercises")
    fun getExercisesAndSets(@PathVariable workoutId: String): Flux<FitnessExercise> {
        return workoutId.checkValidity()
            .flatMapMany(processor::getExercisesByWorkoutId)
    }

    @PostMapping("/workout")
    fun startNewWorkout() = processor.startNewWorkout()

    @PostMapping("/workout/{workoutId}/exercise")
    fun addExerciseToWorkout(
        @PathVariable workoutId: String,
        @RequestBody newExercise: FitnessExercise
    ): Mono<FitnessWorkout> {
        return workoutId.checkValidity()
            .flatMap{processor.addExerciseToWorkout(workoutId, newExercise)}
    }

    @PostMapping("/exercise/{exerciseId}/set")
    fun addSetToExercise(
        @PathVariable exerciseId: String,
        @RequestBody setToAdd: FitnessSet
    ): Mono<FitnessExercise> {
        return exerciseId.checkValidity()
            .flatMap{processor.addSetToExercise(exerciseId, setToAdd)}
    }

    @PatchMapping("/exercise/{exerciseId}/measure")
    fun changeExerciseMeasure(
        @PathVariable exerciseId: String,
        @RequestBody newMeasure: FitnessMeasure
    ): Mono<FitnessExercise> {
        return exerciseId.checkValidity()
            .flatMap{processor.changeExerciseMeasure(it, newMeasure)}
    }

    @DeleteMapping("/exercise/{exerciseId}")
    fun deleteExercise(@PathVariable exerciseId: String): Mono<FitnessWorkout> {
        return exerciseId.checkValidity()
            .flatMap(processor::deleteExercise)
    }

    @DeleteMapping("/set/{setId}")
    fun deleteSet(@PathVariable setId: String): Mono<FitnessExercise> {
        return setId.checkValidity()
            .flatMap(processor::deleteSet)
    }

    private fun String?.checkValidity() = when {
        this == null || equals("null", true) -> Mono.error(BadRequestException("ID cannot be 'null'"))
        equals("undefined", true) -> Mono.error(BadRequestException("ID cannot be 'undefined'"))
        isBlank() -> Mono.error(BadRequestException("ID cannot be blank"))
        !this.matches(uuidPattern) -> Mono.error(BadRequestException("ID failed pattern check"))
        else -> Mono.just(this)
    }

}

@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleBadRequestException(e: BadRequestException) = ErrorResponse("${e.message}")

    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleResourceNotFoundException(e: ResourceNotFoundException) = ErrorResponse("${e.message}")

    @ExceptionHandler(HttpMessageConversionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleConversionException(e: HttpMessageConversionException) = ErrorResponse("There was a problem reading the JSON that was provided: ${e.cause}")
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ErrorResponse(val message: String)

class BadRequestException(msg: String = ""): RuntimeException(msg)
class ResourceNotFoundException(msg: String = ""): RuntimeException(msg)