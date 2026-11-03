package nutrishield;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class NutriShieldServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new WebpageHandler());
        server.createContext("/generate", new FormSubmitHandler()); // CREATE
        server.createContext("/update", new UpdateHandler());       // UPDATE
        server.createContext("/delete", new DeleteHandler());       // DELETE
        server.setExecutor(null);
        System.out.println("NutriShield CRUD Server started! Open http://localhost:8080 in your browser.");
        server.start();
    }

    // READ: Serves the main HTML Webpage and loads profiles via JDBC SELECT
    static class WebpageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>NutriShield - Personalized Fitness CRUD</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', sans-serif; background: #f0fdf4; margin: 0; padding: 20px; color: #1e293b; }")
                .append(".container { max-width: 850px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }")
                .append("h1 { color: #16a34a; margin-top: 0; }")
                .append("input, select { width: 100%; padding: 10px; margin: 8px 0 16px 0; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; }")
                .append(".btn-main { background: #16a34a; color: white; border: none; padding: 12px 20px; font-size: 16px; border-radius: 6px; cursor: pointer; width: 100%; font-weight: bold; }")
                .append(".btn-main:hover { background: #15803d; }")
                .append(".card { background: #f8fafc; border-left: 5px solid #16a34a; padding: 18px; margin-top: 18px; border-radius: 6px; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }")
                .append(".actions { margin-top: 14px; padding-top: 12px; border-top: 1px dashed #cbd5e1; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }")
                .append(".inline-form { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin: 0; }")
                .append(".inline-form input, .inline-form select { width: auto; padding: 6px 10px; margin: 0; font-size: 13px; }")
                .append(".btn-update { background: #0284c7; color: white; border: none; padding: 7px 14px; border-radius: 5px; cursor: pointer; font-weight: 600; font-size: 13px; }")
                .append(".btn-update:hover { background: #0369a1; }")
                .append(".btn-delete { background: #dc2626; color: white; border: none; padding: 7px 14px; border-radius: 5px; cursor: pointer; font-weight: 600; font-size: 13px; }")
                .append(".btn-delete:hover { background: #b91c1c; }")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<h1>🛡️ NutriShield: Smart Diet & Exercise Planner</h1>")
                .append("<p>Enter your personal metrics below to generate and save your customized health blueprint.</p>")
                .append("<form action='/generate' method='POST'>")
                .append("<label>Full Name:</label><input type='text' name='fullName' required placeholder='Enter your name'>")
                .append("<label>Age (Years):</label><input type='number' name='age' required min='10' max='100'>")
                .append("<label>Weight (kg):</label><input type='number' step='0.1' name='weight' required placeholder='e.g. 65.5'>")
                .append("<label>Height (cm):</label><input type='number' step='0.1' name='height' required placeholder='e.g. 172'>")
                .append("<label>Fitness Goal:</label>")
                .append("<select name='goal'><option value='Weight Loss'>Weight Loss</option>")
                .append("<option value='Muscle Gain'>Muscle Gain</option>")
                .append("<option value='Stay Fit'>General Wellness / Stay Fit</option></select>")
                .append("<button type='submit' class='btn-main'>Generate My NutriShield Plan</button>")
                .append("</form><hr style='margin: 30px 0;'>")
                .append("<h2>📋 Saved Member Profiles (JDBC CRUD Dashboard)</h2>");

            // JDBC READ (SELECT)
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM user_profiles ORDER BY user_id DESC")) {

                boolean hasRows = false;
                while (rs.next()) {
                    hasRows = true;
                    int id = rs.getInt("user_id");
                    String name = rs.getString("full_name");
                    int age = rs.getInt("age");
                    double weight = rs.getDouble("weight_kg");
                    double height = rs.getDouble("height_cm");
                    String goal = rs.getString("fitness_goal");

                    html.append("<div class='card'>")
                        .append("<strong>👤 ").append(name).append("</strong> (ID: #").append(id)
                        .append(" | Age: ").append(age)
                        .append(" | Weight: ").append(weight).append(" kg")
                        .append(" | Height: ").append(height).append(" cm")
                        .append(" | BMI: ").append(rs.getDouble("bmi")).append(" - <b>").append(rs.getString("bmi_category")).append("</b>")
                        .append(" | Goal: <b>").append(goal).append("</b>)<br><br>")
                        .append("🥗 <b>Diet Plan:</b> ").append(rs.getString("diet_plan")).append("<br><br>")
                        .append("🏋️ <b>Exercise Plan:</b> ").append(rs.getString("exercise_plan"))
                        
                        // UPDATE & DELETE Controls
                        .append("<div class='actions'>")
                        
                        // Update Form
                        .append("<form class='inline-form' action='/update' method='POST'>")
                        .append("<input type='hidden' name='userId' value='").append(id).append("'>")
                        .append("<input type='hidden' name='fullName' value='").append(name).append("'>")
                        .append("<input type='hidden' name='age' value='").append(age).append("'>")
                        .append("<input type='hidden' name='height' value='").append(height).append("'>")
                        .append("<label style='font-size:13px; font-weight:600;'>New Weight (kg):</label>")
                        .append("<input type='number' step='0.1' name='weight' value='").append(weight).append("' required>")
                        .append("<select name='goal'>")
                        .append("<option value='Weight Loss'").append(goal.equals("Weight Loss") ? " selected" : "").append(">Weight Loss</option>")
                        .append("<option value='Muscle Gain'").append(goal.equals("Muscle Gain") ? " selected" : "").append(">Muscle Gain</option>")
                        .append("<option value='Stay Fit'").append(goal.equals("Stay Fit") ? " selected" : "").append(">Stay Fit</option>")
                        .append("</select>")
                        .append("<button type='submit' class='btn-update'>✏️ Update Plan</button>")
                        .append("</form>")
                        
                        // Delete Form
                        .append("<form class='inline-form' action='/delete' method='POST' onsubmit=\"return confirm('Delete this profile?');\">")
                        .append("<input type='hidden' name='userId' value='").append(id).append("'>")
                        .append("<button type='submit' class='btn-delete'>🗑️ Delete</button>")
                        .append("</form>")
                        
                        .append("</div></div>");
                }
                if (!hasRows) {
                    html.append("<p>No profiles saved yet. Fill out the form above to create one!</p>");
                }
            } catch (SQLException e) {
                html.append("<p style='color:red;'>Database Error: ").append(e.getMessage()).append("</p>");
            }

            html.append("</div></body></html>");
            byte[] response = html.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    // CREATE: Handles Form POST Request, applies OOP logic, and runs JDBC INSERT
    static class FormSubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequest(exchange);

                UserProfile user = new UserProfile(
                        params.get("fullName"),
                        Integer.parseInt(params.get("age")),
                        Double.parseDouble(params.get("weight")),
                        Double.parseDouble(params.get("height")),
                        params.get("goal")
                );

                PlanGenerator generator = new PlanGenerator();
                user.setDietPlan(generator.generateDietPlan(user));
                user.setExercisePlan(generator.generateExercisePlan(user));

                String sql = "INSERT INTO user_profiles (full_name, age, weight_kg, height_cm, fitness_goal, bmi, bmi_category, diet_plan, exercise_plan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, user.getFullName());
                    pstmt.setInt(2, user.getAge());
                    pstmt.setDouble(3, user.getWeightKg());
                    pstmt.setDouble(4, user.getHeightCm());
                    pstmt.setString(5, user.getFitnessGoal());
                    pstmt.setDouble(6, user.getBmi());
                    pstmt.setString(7, user.getBmiCategory());
                    pstmt.setString(8, user.getDietPlan());
                    pstmt.setString(9, user.getExercisePlan());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                redirectHome(exchange);
            }
        }
    }

    // UPDATE: Recalculates BMI & Diet/Exercise Plans via OOP and runs JDBC UPDATE
    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequest(exchange);

                int userId = Integer.parseInt(params.get("userId"));
                UserProfile updatedUser = new UserProfile(
                        params.get("fullName"),
                        Integer.parseInt(params.get("age")),
                        Double.parseDouble(params.get("weight")),
                        Double.parseDouble(params.get("height")),
                        params.get("goal")
                );

                PlanGenerator generator = new PlanGenerator();
                updatedUser.setDietPlan(generator.generateDietPlan(updatedUser));
                updatedUser.setExercisePlan(generator.generateExercisePlan(updatedUser));

                String sql = "UPDATE user_profiles SET weight_kg = ?, fitness_goal = ?, bmi = ?, bmi_category = ?, diet_plan = ?, exercise_plan = ? WHERE user_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setDouble(1, updatedUser.getWeightKg());
                    pstmt.setString(2, updatedUser.getFitnessGoal());
                    pstmt.setDouble(3, updatedUser.getBmi());
                    pstmt.setString(4, updatedUser.getBmiCategory());
                    pstmt.setString(5, updatedUser.getDietPlan());
                    pstmt.setString(6, updatedUser.getExercisePlan());
                    pstmt.setInt(7, userId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                redirectHome(exchange);
            }
        }
    }

    // DELETE: Removes a user profile from MySQL using JDBC DELETE
    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequest(exchange);
                int userId = Integer.parseInt(params.get("userId"));

                String sql = "DELETE FROM user_profiles WHERE user_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                redirectHome(exchange);
            }
        }
    }

    // Helper methods for parsing POST data and redirecting
    private static Map<String, String> parseRequest(HttpExchange exchange) throws IOException {
        String formData = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().reduce("", (acc, line) -> acc + line);
        Map<String, String> map = new HashMap<>();
        for (String pair : formData.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            }
        }
        return map;
    }

    private static void redirectHome(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(303, -1);
    }
}