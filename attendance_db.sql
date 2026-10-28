
mysql> CREATE DATABASE IF NOT EXISTS attendance_db;
Query OK, 1 row affected (0.430 sec)

mysql> USE attendance_db;
Database changed
mysql> CREATE TABLE IF NOT EXISTS students (
    -> student_id INT PRIMARY KEY,
    -> student_name VARCHAR(100) NOT NULL,
    ->  course VARCHAR(50) NOT NULL);
Query OK, 0 rows affected (0.683 sec)

mysql> CREATE TABLE IF NOT EXISTS attendance (
    -> attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    -> student_id INT NOT NULL,
    -> attendance_date DATE NOT NULL,
    -> status ENUM('Present',
    -> 'Absent') NOT NULL,
    ->  FOREIGN KEY (student_id)
    ->  REFERENCES students(student_id),
    ->  UNIQUE (student_id,
    -> attendance_date));
Query OK, 0 rows affected (0.603 sec)

mysql> INSERT IGNORE INTO students
    -> VALUES
    -> (101, 'Ananya', 'BCA'),
    -> (102, 'Rahul', 'BCA'),
    -> (103, 'Priya', 'BCA');
Query OK, 3 rows affected (0.311 sec)
Records: 3  Duplicates: 0  Warnings: 0

mysql> SELECT * FROM students;
+------------+--------------+--------+
| student_id | student_name | course |
+------------+--------------+--------+
|        101 | Ananya       | BCA    |
|        102 | Rahul        | BCA    |
|        103 | Priya        | BCA    |
+------------+--------------+--------+
3 rows in set (0.013 sec)

mysql> SELECT * FROM attendance;
Empty set (0.261 sec)

mysql>s