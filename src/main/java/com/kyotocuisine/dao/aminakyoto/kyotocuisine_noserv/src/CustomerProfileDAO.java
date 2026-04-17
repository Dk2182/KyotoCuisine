import java.sql.Connection;
import java.sql.PreparedStatement;

public class CustomerProfileDAO {
    public static void create(int userId) throws Exception {
        String sql = "INSERT INTO customer_profiles (user_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
