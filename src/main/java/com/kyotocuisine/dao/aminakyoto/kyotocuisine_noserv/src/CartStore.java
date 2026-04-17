import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartStore {

    private static final Map<Integer, List<CartItem>> carts = new HashMap<>();

    public static List<CartItem> getCart(int customerId) {
        return carts.computeIfAbsent(customerId, k -> new ArrayList<>());
    }

    public static void addItem(int customerId, int menuItemId, int quantity) {
        List<CartItem> cart = getCart(customerId);

        for (CartItem item : cart) {
            if (item.getMenuItemId() == menuItemId) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        cart.add(new CartItem(menuItemId, quantity));
    }

    public static void clearCart(int customerId) {
        carts.remove(customerId);
    }
}