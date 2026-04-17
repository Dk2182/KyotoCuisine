import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReservationDAO {
    public static void insert(int customerId, String datetime, int guests) throws Exception {
        String sql = "INSERT INTO reservations (customer_id, table_id, reservation_status_id, reservation_start, reservation_end, guest_count) VALUES (?, 1, 1, ?, DATE_ADD(?, INTERVAL 2 HOUR), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, datetime);
            stmt.setString(3, datetime);
            stmt.setInt(4, guests);
            stmt.executeUpdate();
        }
    }
}
