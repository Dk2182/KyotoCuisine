import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    public static User findByEmailAndPassword(String email, String password) throws Exception {
        String sql = "SELECT user_id, email, role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE email = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role_name"));
                    return user;
                }
            }
        }
        return null;
    }
}
