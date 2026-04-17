import java.sql.Connection;
import java.util.List;

public class OrderService {

    public static void createOrderFromCart(int customerId, List<CartItem> cart) throws Exception {
        if (customerId <= 0) {
            throw new RuntimeException("Invalid customer id.");
        }

        if (cart == null || cart.isEmpty()) {
            throw new RuntimeException("Cart is empty.");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double totalAmount = 0.0;

            for (CartItem item : cart) {
                double unitPrice = MenuItemDAO.getPriceById(conn, item.getMenuItemId());

                if (unitPrice < 0) {
                    throw new RuntimeException("Menu item not found: " + item.getMenuItemId());
                }

                totalAmount += unitPrice * item.getQuantity();
            }

            int orderId = OrderDAO.create(conn, customerId, totalAmount);

            for (CartItem item : cart) {
                double unitPrice = MenuItemDAO.getPriceById(conn, item.getMenuItemId());
                double lineTotal = unitPrice * item.getQuantity();

                OrderItemDAO.insert(
                        conn,
                        orderId,
                        item.getMenuItemId(),
                        item.getQuantity(),
                        unitPrice,
                        lineTotal);
            }

            conn.commit();
            System.out.println("Checkout complete. Order ID = " + orderId);

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}