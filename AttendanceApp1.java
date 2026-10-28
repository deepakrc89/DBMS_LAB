/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package attendanceapp;

/**
 *
 * @author deepa
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class AttendanceApp extends JFrame {

    JTextField idField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField courseField = new JTextField();
    JTextField dateField =
            new JTextField(LocalDate.now().toString());

    JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Present", "Absent"});

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"Attendance ID", "Student ID",
                "Name", "Course", "Date", "Status"}, 0) {

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    JTable table = new JTable(model);

    public AttendanceApp() {

        setTitle("Attendance Management System");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel(
                "ATTENDANCE MANAGEMENT SYSTEM",
                SwingConstants.CENTER);

        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(Color.WHITE);
        heading.setOpaque(true);
        heading.setBackground(new Color(32, 55, 100));
        heading.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15));

        form.add(new JLabel("Student ID"));
        form.add(idField);

        form.add(new JLabel("Student Name"));
        form.add(nameField);

        form.add(new JLabel("Course"));
        form.add(courseField);

        form.add(new JLabel("Date (YYYY-MM-DD)"));
        form.add(dateField);

        form.add(new JLabel("Status"));
        form.add(statusBox);

        JButton add = new JButton("Add Attendance");
        JButton view = new JButton("View Records");
        JButton update = new JButton("Update Selected");
        JButton delete = new JButton("Delete Selected");
        JButton clear = new JButton("Clear Fields");

        form.add(add);
        form.add(view);
        form.add(update);
        form.add(delete);
        form.add(clear);

        JScrollPane scroll = new JScrollPane(table);

        setLayout(new BorderLayout(10, 10));
        add(heading, BorderLayout.NORTH);
        add(form, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        add.addActionListener(e -> addAttendance());
        view.addActionListener(e -> loadAttendance());
        update.addActionListener(e -> updateAttendance());
        delete.addActionListener(e -> deleteAttendance());
        clear.addActionListener(e -> clearFields());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() >= 0) {

                int r = table.getSelectedRow();

                idField.setText(
                        model.getValueAt(r, 1).toString());

                nameField.setText(
                        model.getValueAt(r, 2).toString());

                courseField.setText(
                        model.getValueAt(r, 3).toString());

                dateField.setText(
                        model.getValueAt(r, 4).toString());

                statusBox.setSelectedItem(
                        model.getValueAt(r, 5).toString());
            }
        });

        loadAttendance();
    }


    private void addAttendance() {

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String course = courseField.getText().trim();

            Date date = Date.valueOf(
                    dateField.getText().trim());

            String status = statusBox.getSelectedItem().toString();

            if (name.isEmpty() || course.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Enter student name and course.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    String studentSQL =
                            "INSERT INTO students "
                            + "(student_id, student_name, course) "
                            + "VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE "
                            + "student_name = VALUES(student_name), "
                            + "course = VALUES(course)";

                    try (PreparedStatement ps =
                            con.prepareStatement(studentSQL)) {

                        ps.setInt(1, id);
                        ps.setString(2, name);
                        ps.setString(3, course);
                        ps.executeUpdate();
                    }

                    String attendanceSQL =
                            "INSERT INTO attendance "
                            + "(student_id, attendance_date, status) "
                            + "VALUES (?, ?, ?)";

                    try (PreparedStatement ps =
                            con.prepareStatement(attendanceSQL)) {

                        ps.setInt(1, id);
                        ps.setDate(2, date);
                        ps.setString(3, status);
                        ps.executeUpdate();
                    }

                    con.commit();

                    JOptionPane.showMessageDialog(this,
                            "Attendance saved to MySQL!");

                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }

            loadAttendance();
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Student ID must be a number.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid date in YYYY-MM-DD format.");

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }


    private void loadAttendance() {

        model.setRowCount(0);

        String sql =
                "SELECT a.attendance_id, s.student_id, "
                + "s.student_name, s.course, "
                + "a.attendance_date, a.status "
                + "FROM attendance a JOIN students s "
                + "ON a.student_id = s.student_id "
                + "ORDER BY a.attendance_date DESC, s.student_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("attendance_id"),
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("course"),
                    rs.getDate("attendance_date"),
                    rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // UPDATE ATTENDANCE
    private void updateAttendance() {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a record from the table.");
            return;
        }

        try {
            int attendanceId = Integer.parseInt(
                    model.getValueAt(row, 0).toString());

            int studentId = Integer.parseInt(
                    idField.getText().trim());

            String name = nameField.getText().trim();
            String course = courseField.getText().trim();

            Date date = Date.valueOf(
                    dateField.getText().trim());

            String status = statusBox.getSelectedItem().toString();

            if (name.isEmpty() || course.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Enter student name and course.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    int oldStudentId = Integer.parseInt(
                            model.getValueAt(row, 1).toString());

                    String studentSQL =
                            "UPDATE students SET student_id=?, "
                            + "student_name=?, course=? "
                            + "WHERE student_id=?";

                    try (PreparedStatement ps =
                            con.prepareStatement(studentSQL)) {

                        ps.setInt(1, studentId);
                        ps.setString(2, name);
                        ps.setString(3, course);
                        ps.setInt(4, oldStudentId);

                        if (ps.executeUpdate() == 0) {
                            throw new SQLException(
                                    "Student record not found.");
                        }
                    }

                    String attendanceSQL =
                            "UPDATE attendance SET student_id=?, "
                            + "attendance_date=?, status=? "
                            + "WHERE attendance_id=?";

                    try (PreparedStatement ps =
                            con.prepareStatement(attendanceSQL)) {

                        ps.setInt(1, studentId);
                        ps.setDate(2, date);
                        ps.setString(3, status);
                        ps.setInt(4, attendanceId);
                        ps.executeUpdate();
                    }

                    con.commit();

                    JOptionPane.showMessageDialog(this,
                            "Attendance updated in MySQL!");

                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }

            loadAttendance();
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Student ID must be a valid number.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid date in YYYY-MM-DD format.");

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }


    private void deleteAttendance() {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a record to delete.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this attendance record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        int attendanceId = Integer.parseInt(
                model.getValueAt(row, 0).toString());

        String sql =
                "DELETE FROM attendance WHERE attendance_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, attendanceId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Attendance deleted from MySQL.");

            loadAttendance();
            clearFields();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

 
    private void clearFields() {

        idField.setText("");
        nameField.setText("");
        courseField.setText("");
        dateField.setText(LocalDate.now().toString());
        statusBox.setSelectedIndex(0);
        table.clearSelection();
    }


    private void showDatabaseError(SQLException ex) {

        JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new AttendanceApp().setVisible(true);
        });
    }
}



class DBConnection {

    public static Connection getConnection()
            throws SQLException {

        String url =
                "jdbc:mysql://localhost:3306/attendance_db";

        String user = "root";

        String password = "1234@Deepu";

        return DriverManager.getConnection(
                url, user, password);
    }
}