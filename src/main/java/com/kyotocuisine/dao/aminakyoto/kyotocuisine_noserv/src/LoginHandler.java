import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = FormUtil.parseFormData(formData);

        String email = params.get("email");
        String password = params.get("password");

        User user = UserService.login(email, password);

        if (user != null) {
            String location;
            if ("CUSTOMER".equals(user.getRole())) {
                location = "/menu.html";
            } else if ("STAFF".equals(user.getRole())) {
                location = "/staff.html";
            } else {
                location = "/admin.html";
            }
            exchange.getResponseHeaders().add("Location", location);
            exchange.sendResponseHeaders(302, -1);
        } else {
            exchange.getResponseHeaders().add("Location", "/login.html?error=1");
            exchange.sendResponseHeaders(302, -1);
        }
        exchange.close();
    }
}
