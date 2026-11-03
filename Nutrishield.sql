
CREATE DATABASE IF NOT EXISTS nutrishield_db;
USE nutrishield_db;


CREATE TABLE IF NOT EXISTS user_profiles (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    weight_kg DOUBLE NOT NULL,
    height_cm DOUBLE NOT NULL,
    fitness_goal VARCHAR(50) NOT NULL,
    bmi DOUBLE NOT NULL,
    bmi_category VARCHAR(30) NOT NULL,
    diet_plan TEXT NOT NULL,
    exercise_plan TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


SELECT * FROM user_profiles;