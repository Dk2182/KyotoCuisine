import java.sql.Connection;
import java.sql.PreparedStatement;

public class PaymentDAO {
    public static void insert(int orderId, double amount) throws Exception {
        String sql = "INSERT INTO payments (order_id, payment_method_id, payment_status_id, amount, paid_at) VALUES (?, 1, 2, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        }
    }
}
