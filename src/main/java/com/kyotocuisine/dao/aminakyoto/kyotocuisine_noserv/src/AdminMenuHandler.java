import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AdminMenuHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = FormUtil.parseFormData(formData);

        String name = params.get("name");
        double price = Double.parseDouble(params.getOrDefault("price", "0"));

        try {
            MenuService.addItem(name, price);
            exchange.getResponseHeaders().add("Location", "/admin.html?success=1");
        } catch (Exception e) {
            exchange.getResponseHeaders().add("Location", "/admin.html?error=1");
        }
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
