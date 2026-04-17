import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrderDAO {

    public static int create(Connection conn, int customerId, double totalAmount) throws Exception {
        String sql = "INSERT INTO orders (" +
                "customer_id, order_status_id, order_type_id, total_amount, placed_at, updated_at" +
                ") VALUES (" +
                "?, " +
                "(SELECT order_status_id FROM order_statuses WHERE status_name = 'NOT_PREPARING'), " +
                "(SELECT order_type_id FROM order_types WHERE type_name = 'TAKEAWAY'), " +
                "?, NOW(), NOW()" +
                ")";

        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, customerId);
        stmt.setDouble(2, totalAmount);

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        int orderId = -1;

        if (rs.next()) {
            orderId = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return orderId;
    }
}