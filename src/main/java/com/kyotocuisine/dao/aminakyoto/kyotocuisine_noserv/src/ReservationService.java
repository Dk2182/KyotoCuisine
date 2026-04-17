public class ReservationService {

    public static boolean createReservation(int userId, String datetime, int guests) {
        if (userId <= 0) return false;
        if (datetime == null || datetime.trim().isEmpty()) return false;
        if (guests <= 0) return false;

        try {
            ReservationDAO.insert(userId, datetime.trim(), guests);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
