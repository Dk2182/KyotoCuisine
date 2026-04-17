public class AuditLogService {
    public static void log(int userId, String action, String entity, int entityId) {
        try {
            AuditLogDAO.log(userId, action, entity, entityId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
