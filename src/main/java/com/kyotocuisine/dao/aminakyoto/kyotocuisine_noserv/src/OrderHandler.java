import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OrderHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseFormData(formData);

        int menuItemId = Integer.parseInt(params.get("menuItemId"));
        int quantity = Integer.parseInt(params.get("quantity"));

        try {
            int customerId = 1; // demo customer

            OrderService.createSingleItemOrder(customerId, menuItemId, quantity);

            exchange.getResponseHeaders().add("Location", "/menu.html?added=1");
            exchange.sendResponseHeaders(302, -1);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponseHeaders().add("Location", "/menu.html?error=1");
            exchange.sendResponseHeaders(302, -1);
        }

        exchange.close();
    }

    private Map<String, String> parseFormData(String formData) throws IOException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                map.put(key, value);
            }
        }

        return map;
    }

}