package com.supernet.fitnesstracker.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.UUID

@Document(collection = "exercises")
@JsonIgnoreProperties(ignoreUnknown = true)
data class FitnessExercise(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Indexed
    val workoutId: String = "00000000-0000-0000-0000-000000000000",
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val order: Int = 0,
    val name: String = "",
    val sets: List<FitnessSet> = emptyList(),
    val measure: FitnessMeasure = FitnessMeasure()
)