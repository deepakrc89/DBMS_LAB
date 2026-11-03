/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nutrishield;

// Abstract Base Class (Abstraction)
abstract class BaseHealthAdvisor {
    public abstract String generateDietPlan(UserProfile user);
    public abstract String generateExercisePlan(UserProfile user);
}

// Concrete Child Class (Inheritance & Polymorphism)
public class PlanGenerator extends BaseHealthAdvisor {

    @Override
    public String generateDietPlan(UserProfile user) {
        String goal = user.getFitnessGoal();
        if (goal.equalsIgnoreCase("Weight Loss")) {
            return "Calorie Deficit (1,600 kcal/day): Breakfast: Oats & berries. Lunch: Grilled paneer/chicken salad with quinoa. Dinner: Steamed veggies & dal soup. Avoid refined sugar.";
        } else if (goal.equalsIgnoreCase("Muscle Gain")) {
            return "Calorie Surplus & High Protein (2,600 kcal/day): Breakfast: 4 Eggs / Sprouts & Peanut Butter Toast. Lunch: Brown rice, chicken breast/soya chunks, curd. Dinner: Roti, paneer bhurji/fish & whey protein.";
        } else {
            return "Balanced Maintenance (2,000 kcal/day): Breakfast: Idli/Poha with nuts. Lunch: 2 Rotis, dal, mixed vegetable sabzi, curd. Dinner: Light khichdi or grilled salad.";
        }
    }

    @Override
    public String generateExercisePlan(UserProfile user) {
        String goal = user.getFitnessGoal();
        int age = user.getAge();
        String intensityNote = (age > 45) ? " (Keep impact low and focus on joint mobility)" : " (Progressive overload recommended)";

        if (goal.equalsIgnoreCase("Weight Loss")) {
            return "45 mins/day: 25 mins HIIT / Brisk Running + 20 mins Full Body Circuit (Burpees, Plank, Mountain Climbers, Squats) - 5 Days/Week." + intensityNote;
        } else if (goal.equalsIgnoreCase("Muscle Gain")) {
            return "60 mins/day: Push-Pull-Legs Hypertrophy Split (Bench Press, Deadlifts, Squats, Overhead Press - 4 sets x 8-12 reps) - 5 Days/Week." + intensityNote;
        } else {
            return "30 mins/day: 15 mins Jogging/Cycling + 15 mins Yoga, Surya Namaskar & Core Mobility - 4 Days/Week." + intensityNote;
        }
    }
}