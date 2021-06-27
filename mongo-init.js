db = db.getSiblingDB('fitness_log')
db.workouts.drop()
db.exercises.drop()
db.sets.drop()
db.createCollection('workouts')
db.createCollection('exercises')
db.createCollection('sets')
var now = ISODate().getTime()
db.workouts.createIndex(
    { "timestamp": 1 }
)
db.exercises.createIndex(
    { "timestamp": 1 }
)
db.sets.createIndex(
    { "timestamp": 1 }
)
db.exercises.createIndex(
    { "workoutId": 1 }
)
db.sets.createIndex(
    { "exerciseId": 1 }
)
db.workouts.insert(
    {
        "_id": "53e2f9cf-85ca-4474-989b-b3577cba2677",
        "timestamp": new Date(now - 5000),
        "startOfDay": new Date(now - 5000),
        "exercises": [],
        "emphasis": "",
        "notes": "",
        "_class": "com.supernet.fitnesstracker.model.FitnessEntry"
    }
)
db.exercises.insert([
    {
        "_id": "f3daa7cc-c5c0-4739-aaed-1a416c054461",
        "workoutId": "53e2f9cf-85ca-4474-989b-b3577cba2677",
        "timestamp": new Date(now - 4000),
        "order": NumberInt(6),
        "name": "Chest Press",
        "sets": [],
        "measure": {
          "type": "MASS",
          "unit": "LBS"
        },
        "_class": "com.supernet.fitnesstracker.model.FitnessExercise"
    },
    {
        "_id": "3877166a-3ba9-4075-9c87-7417154099f9",
        "workoutId": "53e2f9cf-85ca-4474-989b-b3577cba2677",
        "timestamp": new Date(now - 4000),
        "order": NumberInt(2),
        "name": "Triceps Dip",
        "sets": [],
        "measure": {
          "type": "MASS",
          "unit": "LBS"
        },
        "_class": "com.supernet.fitnesstracker.model.FitnessExercise"
    },
    {
        "_id": "988a81e3-b882-4f4a-8fb7-5fafb02c66a9",
        "workoutId": "53e2f9cf-85ca-4474-989b-b3577cba2677",
        "timestamp": new Date(now - 4000),
        "order": NumberInt(1),
        "name": "Shoulder Press",
        "sets": [],
        "measure": {
          "type": "MASS",
          "unit": "LBS"
        },
        "_class": "com.supernet.fitnesstracker.model.FitnessExercise"
    }
])
db.sets.insert([
    {
        "_id": "0ca01a33-4663-408d-9d8c-c20c2ba29947",
        "exerciseId": "988a81e3-b882-4f4a-8fb7-5fafb02c66a9",
        "timestamp": new Date(now - 3000),
        "order": NumberInt(5),
        "reps": NumberInt(12),
        "of": NumberInt(40),
        "_class": "com.supernet.fitnesstracker.model.FitnessSet"
    },
    {
        "_id" : "1a132e21-3fc2-437e-83ed-6e932517cf2e",
        "exerciseId" : "3877166a-3ba9-4075-9c87-7417154099f9",
        "timestamp" : new Date(now - 3000),
        "order" : NumberInt(0),
        "reps" : NumberInt(12),
        "of" : NumberInt(30),
        "_class" : "com.supernet.fitnesstracker.model.FitnessSet"
    },
    {
        "_id" : "2ec086d8-7fd4-455e-8731-c18ed67be5fa",
        "exerciseId" : "3877166a-3ba9-4075-9c87-7417154099f9",
        "timestamp" : new Date(now - 3000),
        "order" : NumberInt(1),
        "reps" : NumberInt(12),
        "of" : NumberInt(40),
        "_class" : "com.supernet.fitnesstracker.model.FitnessSet"
    }
])
db.createUser({
    user: 'fitness_service',
    pwd: 'fitness_password',
    roles: [
        {
            role: 'readWrite',
            db: 'fitness_log'
        }
    ]
});
