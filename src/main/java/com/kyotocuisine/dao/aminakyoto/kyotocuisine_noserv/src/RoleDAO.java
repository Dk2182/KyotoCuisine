import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoleDAO {
    public static ResultSet getAll() throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM roles";
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }
}
