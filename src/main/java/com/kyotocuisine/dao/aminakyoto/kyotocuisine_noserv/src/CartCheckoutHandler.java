import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class CartCheckoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        int customerId = 1;
        List<CartItem> cart = CartStore.getCart(customerId);

        try {
            System.out.println("CHECKOUT HIT");
            System.out.println("Cart size = " + (cart == null ? 0 : cart.size()));

            if (cart == null || cart.isEmpty()) {
                exchange.getResponseHeaders().add("Location", "/cart/view?empty=1");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            OrderService.createOrderFromCart(customerId, cart);
            CartStore.clearCart(customerId);

            exchange.getResponseHeaders().add("Location", "/cart/view?success=1");
            exchange.sendResponseHeaders(302, -1);
        } catch (Exception e) {
            System.out.println("CHECKOUT ERROR:");
            e.printStackTrace();

            exchange.getResponseHeaders().add("Location", "/cart/view?error=1");
            exchange.sendResponseHeaders(302, -1);
        }

        exchange.close();
    }
}