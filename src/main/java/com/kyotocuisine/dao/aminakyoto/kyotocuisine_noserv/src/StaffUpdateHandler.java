import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StaffUpdateHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = FormUtil.parseFormData(formData);
        int orderId = Integer.parseInt(params.getOrDefault("orderId", "0"));

        try {
            OrderService.advanceStatus(orderId);
            exchange.getResponseHeaders().add("Location", "/staff.html?updated=1");
        } catch (Exception e) {
            exchange.getResponseHeaders().add("Location", "/staff.html?error=1");
        }
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
