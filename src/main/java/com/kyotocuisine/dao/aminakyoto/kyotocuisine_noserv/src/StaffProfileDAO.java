import java.sql.Connection;
import java.sql.PreparedStatement;

public class StaffProfileDAO {
    public static void create(int userId, String position) throws Exception {
        String sql = "INSERT INTO staff_profiles (user_id, staff_position, hire_date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, position);
            stmt.executeUpdate();
        }
    }
}
