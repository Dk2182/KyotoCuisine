import java.sql.Connection;
import java.sql.PreparedStatement;

public class OrderItemDAO {

    public static void insert(Connection conn, int orderId, int menuItemId, int quantity, double unitPrice,
            double lineTotal) throws Exception {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, orderId);
        stmt.setInt(2, menuItemId);
        stmt.setInt(3, quantity);
        stmt.setDouble(4, unitPrice);
        stmt.setDouble(5, lineTotal);

        stmt.executeUpdate();
        stmt.close();
    }
}