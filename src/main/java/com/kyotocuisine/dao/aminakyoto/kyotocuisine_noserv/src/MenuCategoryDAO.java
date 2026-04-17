import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MenuCategoryDAO {
    public static ResultSet getAll() throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM menu_categories";
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }
}
