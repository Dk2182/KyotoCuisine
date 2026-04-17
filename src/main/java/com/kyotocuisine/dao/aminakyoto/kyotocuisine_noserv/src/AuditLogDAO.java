import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditLogDAO {
    public static void log(int userId, String action, String entity, int entityId) throws Exception {
        String sql = "INSERT INTO audit_logs (user_id, action_type, entity_name, entity_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, entity);
            stmt.setInt(4, entityId);
            stmt.executeUpdate();
        }
    }
}
