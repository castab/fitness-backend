package com.supernet.fitnesstracker.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Document(collection = "workouts")
data class FitnessWorkout(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Indexed
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    @JsonProperty("start_of_day")
    val startOfDay: ZonedDateTime = timestamp.truncatedTo(ChronoUnit.DAYS).withEarlierOffsetAtOverlap(),
    val exercises: List<FitnessExercise> = emptyList(),
    val emphasis: String = "",
    val notes: String = ""
)
