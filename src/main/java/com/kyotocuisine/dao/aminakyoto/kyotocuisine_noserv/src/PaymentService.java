public class PaymentService {
    public static boolean processPayment(int orderId, double amount) {
        if (orderId <= 0 || amount <= 0) {
            return false;
        }
        try {
            PaymentDAO.insert(orderId, amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
