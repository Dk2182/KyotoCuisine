import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LookupDAO {
    public static ResultSet getOrderStatuses() throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM order_statuses");
        return stmt.executeQuery();
    }

    public static ResultSet getReservationStatuses() throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reservation_statuses");
        return stmt.executeQuery();
    }
}
