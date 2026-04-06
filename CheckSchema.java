import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            
            System.out.println("Tables:");
            ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("\nTable: " + tableName);
                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                while (columns.next()) {
                    System.out.println(" - " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
                }
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
