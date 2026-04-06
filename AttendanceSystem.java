import java.sql.*;
import java.util.Scanner;

public class AttendanceSystem {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- Student Attendance Management System ---");
            System.out.println("1. Add Student");
            System.out.println("2. Delete Student");
            System.out.println("3. Mark Attendance");
            System.out.println("4. View Report");
            System.out.println("5. Search by Roll Number");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> deleteStudent();
                    case 3 -> markAttendance();
                    case 4 -> viewReport();
                    case 5 -> searchStudent();
                    case 6 -> {
                        System.out.println("Thank You!");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid Choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ADD STUDENT
    static void addStudent() throws Exception {
        System.out.print("Section: ");
        sc.nextLine(); // Clear buffer
        String dept = sc.nextLine();

        System.out.print("Roll No: ");
        int roll = sc.nextInt();

        Connection con = DBConnection.getConnection();
        PreparedStatement check =
            con.prepareStatement("SELECT * FROM student WHERE roll_no=?");
        check.setInt(1, roll);
        ResultSet rs = check.executeQuery();

        if (rs.next()) {
            System.out.println("Roll number already exists!");
            con.close();
            return;
        }

        System.out.print("Name: ");
        sc.nextLine(); // Clear buffer
        String name = sc.nextLine();

        System.out.print("Initial Total Days (default 0): ");
        int total = sc.hasNextInt() ? sc.nextInt() : 0;
        sc.nextLine(); // Clear buffer

        PreparedStatement ps =
            con.prepareStatement("INSERT INTO student (roll_no, name, department, total_days) VALUES(?,?,?,?)");
        ps.setInt(1, roll);
        ps.setString(2, name);
        ps.setString(3, dept);
        ps.setInt(4, total);

        ps.executeUpdate();
        con.close();

        System.out.println("Student Added Successfully");
    }

    // MARK ATTENDANCE
    static void markAttendance() throws Exception {
        System.out.print("Enter Section: ");
        sc.nextLine(); // Clear buffer
        String section = sc.nextLine();

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT s.roll_no, s.name, s.total_days, a.days_present " +
            "FROM student s LEFT JOIN attendance a ON s.roll_no = a.roll_no " +
            "WHERE s.department = ?");
        ps.setString(1, section);
        ResultSet rs = ps.executeQuery();

        boolean found = false;
        while (rs.next()) {
            found = true;
            int roll = rs.getInt(1);
            String name = rs.getString(2);
            int total = rs.getInt(3);
            int present = rs.getInt(4); // Default to 0 if null handled by JDBC

            System.out.print(name + " (Roll: " + roll + ") - Present? (y/n): ");
            String status = sc.next();

            int newTotal = total + 1;
            int newPresent = status.equalsIgnoreCase("y") ? present + 1 : present;
            float percentage = (newPresent * 100.0f) / newTotal;

            // Update student's total days
            PreparedStatement updateStudent = con.prepareStatement(
                "UPDATE student SET total_days = ? WHERE roll_no = ?");
            updateStudent.setInt(1, newTotal);
            updateStudent.setInt(2, roll);
            updateStudent.executeUpdate();

            // Update attendance
            PreparedStatement updateAttendance = con.prepareStatement(
                "REPLACE INTO attendance (roll_no, days_present, percentage) VALUES(?,?,?)");
            updateAttendance.setInt(1, roll);
            updateAttendance.setInt(2, newPresent);
            updateAttendance.setFloat(3, percentage);
            updateAttendance.executeUpdate();
        }

        if (!found) {
            System.out.println("No students found in section: " + section);
        } else {
            System.out.println("Attendance marking complete for section: " + section);
        }
        con.close();
    }

    // DELETE STUDENT
    static void deleteStudent() throws Exception {
        System.out.print("Enter Roll No to Delete: ");
        int roll = sc.nextInt();

        Connection con = DBConnection.getConnection();
        
        // Check if student exists
        PreparedStatement check = con.prepareStatement("SELECT name FROM student WHERE roll_no=?");
        check.setInt(1, roll);
        ResultSet rs = check.executeQuery();
        
        if (!rs.next()) {
            System.out.println("Student not found!");
            con.close();
            return;
        }
        
        String name = rs.getString(1);
        System.out.print("Are you sure you want to delete student " + name + " (Roll: " + roll + ")? (y/n): ");
        String confirm = sc.next();
        
        if (confirm.equalsIgnoreCase("y")) {
            // Delete from attendance first due to potential foreign key or to keep data clean
            PreparedStatement ps1 = con.prepareStatement("DELETE FROM attendance WHERE roll_no=?");
            ps1.setInt(1, roll);
            ps1.executeUpdate();

            // Delete from student
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM student WHERE roll_no=?");
            ps2.setInt(1, roll);
            ps2.executeUpdate();

            System.out.println("Student Deleted Successfully");
        } else {
            System.out.println("Deletion cancelled.");
        }
        con.close();
    }

    // VIEW REPORT
    static void viewReport() throws Exception {
        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();

        ResultSet rs = st.executeQuery(
            "SELECT s.roll_no, s.name, s.department, a.days_present, s.total_days, a.percentage " +
            "FROM student s LEFT JOIN attendance a ON s.roll_no=a.roll_no");

        System.out.println("\n----------------------- Attendance Report -----------------------");
        System.out.printf("%-8s %-20s %-12s %-8s %-8s %-8s%n", "Roll", "Name", "Section", "Present", "Total", "%");
        System.out.println("---------------------------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%-8d %-20s %-12s %-8d %-8d %-8.2f%n",
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getInt(5),
                rs.getFloat(6)
            );
        }
        System.out.println("---------------------------------------------------------------------");
        con.close();
    }

    // SEARCH STUDENT
    static void searchStudent() throws Exception {
        System.out.print("Enter Roll No: ");
        int roll = sc.nextInt();

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT s.roll_no, s.name, s.department, a.days_present, a.percentage " +
            "FROM student s LEFT JOIN attendance a ON s.roll_no=a.roll_no WHERE s.roll_no=?");
        ps.setInt(1, roll);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println("Roll No: " + rs.getInt(1));
            System.out.println("Name: " + rs.getString(2));
            System.out.println("Section: " + rs.getString(3));
            System.out.println("Days Present: " + rs.getInt(4));
            System.out.println("Percentage: " + rs.getFloat(5));
        } else {
            System.out.println("Student not found!");
        }
        con.close();
    }
}
