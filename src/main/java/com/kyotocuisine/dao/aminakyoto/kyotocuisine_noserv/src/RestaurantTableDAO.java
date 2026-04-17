import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RestaurantTableDAO {
    public static ResultSet getAll() throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM restaurant_tables";
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }
}
