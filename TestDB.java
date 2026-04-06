import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            System.out.println("Connected to database successfully!");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
