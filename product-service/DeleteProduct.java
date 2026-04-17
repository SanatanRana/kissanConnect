
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DeleteProduct {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kisan_product_db", "root", "root@123");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM products WHERE name = 'Apple'");
            System.out.println("Apple deleted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

