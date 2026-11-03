/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nutrishield;

public class UserProfile {
    // Encapsulated private attributes
    private int userId;
    private String fullName;
    private int age;
    private double weightKg;
    private double heightCm;
    private String fitnessGoal;
    private double bmi;
    private String bmiCategory;
    private String dietPlan;
    private String exercisePlan;

    // Constructor
    public UserProfile(String fullName, int age, double weightKg, double heightCm, String fitnessGoal) {
        this.fullName = fullName;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.fitnessGoal = fitnessGoal;
        calculateBMI();
    }

    // Method to calculate BMI and determine category
    private void calculateBMI() {
        double heightMeters = this.heightCm / 100.0;
        this.bmi = Math.round((this.weightKg / (heightMeters * heightMeters)) * 10.0) / 10.0;

        if (this.bmi < 18.5) {
            this.bmiCategory = "Underweight";
        } else if (this.bmi < 24.9) {
            this.bmiCategory = "Normal Weight";
        } else if (this.bmi < 29.9) {
            this.bmiCategory = "Overweight";
        } else {
            this.bmiCategory = "Obese";
        }
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public double getWeightKg() { return weightKg; }
    public double getHeightCm() { return heightCm; }
    public String getFitnessGoal() { return fitnessGoal; }
    public double getBmi() { return bmi; }
    public String getBmiCategory() { return bmiCategory; }
    
    public String getDietPlan() { return dietPlan; }
    public void setDietPlan(String dietPlan) { this.dietPlan = dietPlan; }
    
    public String getExercisePlan() { return exercisePlan; }
    public void setExercisePlan(String exercisePlan) { this.exercisePlan = exercisePlan; }
}