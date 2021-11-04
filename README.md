# Fitness Backend Service
This is the backend service that connects the fitness-ui to the database (MongoDB) via APIs.  This codebase is written in Kotlin and uses the Spring Boot framework.

### Requirements

- Java 16
- Gradle

My personal IDE preference is IntelliJ.

#### Getting Started
1. Clone the repository
2. Import the project into your IDE of choice
3. Run `docker-compose up` to bring up a database to connect to
4. Start changing code

## Learn More
The service and database are driven by three linked data models:
#### `FitnessWorkout`
```
{
  "id": "5b5fbc59-698b-121c-909b-a5527e610d22",  // String
  "timestamp": "2030-01-01T01:00:00.00Z",  // ZonedDateTime
  "start_of_day": "2030-01-01T00:00:00Z", // ZonedDateTime
  "exercises": [], // List<FitnessExercise>
  "emphasis": "", // String
  "notes": "" // String
}
```

#### `FitnessExercise`
```
{
  "id": "f672dedf-ab64-4b4f-8e3e-e73ff66919cc", // String
  "workoutId": "5b5fbc59-698b-121c-909b-a5527e610d22",  // String
  "timestamp": "2030-01-01T00:00:00.000Z",  // ZonedDateTime
  "order": 0, // Long
  "name": "Machine Shoulder Press", // String
  "sets": [], // List<FitnessSet>
  "measure": {
    "type": "MASS", // Type Enum
    "unit": "LBS" // Unit Measure Enum
  }
}
```

#### `FitnessSet`
```
{
  "id": "8c135221-716a-4c3f-b30f-a23b093b3ac8", // String
  "exerciseId": "f672dedf-ab64-4b4f-8e3e-e73ff66919cc",  // String
  "timestamp": "2030-01-01T00:00:00.000Z",  // ZonedDateTime
  "order": 0, // Long
  "reps": 12, // Long
  "of": 20 // Long
}
```

The hierarchy follows:
- `FitnessWorkout` contains top-level information about a workout and the exercises that compose the workout.
- `FitnessExercise` contains mid-level information about an exercise and the sets that compose the exercise. Sets as in, the reps (repetitions) of a set.
- `FitnessSet` contains the bottom-level information about a set.

###### Example:

An arbitrary workout (`FitnessWorkout`) will contain exercises (`List<FitnessExercise>`) such as a Chest Press, Pec Fly, and Triceps Dip, each with sets of repetitions (`List<FitnessSet>`) - "For this workout I did each exercise with 4 sets of 12 repetitions, all at 50 LBS"


## APIs
### `GET /workouts`
Parameters: `none`

Returns up to 20 recent workouts over the last 60 days.
___
### `GET /measure`
Parameters: `none`

Returns a map of `types` and `units` that detail the various units of measurement that may be used to describe an exercise.
___
### `GET /uuid`
Parameters: `none`

Returns a new UUID as a string, mainly for debug purposes.
___
### `GET /exercise/{exerciseId}`
Parameters: `none`

Returns the exercise by its unique UUID.
___
### `GET /exercise/pr`
Parameters: `name: String`

Returns at most 1 exercise from the last 60 days with a matching `name` as a personal record best.
___
### `GET /workout/{workoutId}`
Parameters: `none`

Returns the workout by its unique UUID, along with the workout's exercises and each exercises' sets.
___
### `GET /workout/{workoutId}/exercises`
Parameters: `none`

Returns the workout's exercises, along with each exercises' sets.