import java.sql.*;

public class CheckAttendanceTable {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("DESCRIBE attendance");
            System.out.println("Attendance Table Structure:");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " - " + rs.getString(2));
            }
            con.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
