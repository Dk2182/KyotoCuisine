import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class CartViewHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int customerId = 1;
        List<CartItem> cart = CartStore.getCart(customerId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Cart</title>");
        html.append("<link rel='stylesheet' href='/style.css'>");
        html.append("</head><body>");
        html.append("<div class='panel form-panel'>");
        html.append("<h1>Your Cart</h1>");

        if (cart == null || cart.isEmpty()) {
            html.append("<p>Your cart is empty.</p>");
        } else {
            html.append("<ul>");
            for (CartItem item : cart) {
                html.append("<li>Menu Item ID: ")
                        .append(item.getMenuItemId())
                        .append(" | Quantity: ")
                        .append(item.getQuantity())
                        .append("</li>");
            }
            html.append("</ul>");

            html.append("<form action='/cart/checkout' method='post'>");
            html.append("<button type='submit'>Checkout</button>");
            html.append("</form>");
        }

        html.append("<br><a href='/menu.html'>Back to Menu</a>");
        html.append("</div></body></html>");

        byte[] response = html.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}