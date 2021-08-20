package com.supernet.fitnesstracker.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.supernet.fitnesstracker.model.FitnessMeasure
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.*

@Document(collection = "exercises")
@JsonIgnoreProperties(ignoreUnknown = true)
data class FitnessExerciseEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Indexed
    val workoutId: String = "00000000-0000-0000-0000-000000000000",
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val order: Int = 0,
    val name: String = "",
    val sets: List<String> = emptyList(),
    val measure: FitnessMeasure = FitnessMeasure()
)