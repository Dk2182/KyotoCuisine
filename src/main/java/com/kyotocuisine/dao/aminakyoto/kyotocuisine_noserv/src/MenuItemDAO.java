import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MenuItemDAO {

    public static double getPriceById(Connection conn, int menuItemId) throws Exception {
        String sql = "SELECT price FROM menu_items WHERE menu_item_id = ? AND is_available = TRUE";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, menuItemId);

        ResultSet rs = stmt.executeQuery();

        double price = -1;

        if (rs.next()) {
            price = rs.getDouble("price");
        }

        rs.close();
        stmt.close();

        return price;
    }

}