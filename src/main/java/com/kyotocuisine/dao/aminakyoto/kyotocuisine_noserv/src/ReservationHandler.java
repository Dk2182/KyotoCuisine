import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ReservationHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = FormUtil.parseFormData(formData);

        String date = params.get("date");
        String time = params.get("time");
        int guests = Integer.parseInt(params.getOrDefault("guests", "0"));

        boolean ok = ReservationService.createReservation(1, date + " " + time, guests);
        if (ok) {
            exchange.getResponseHeaders().add("Location", "/reservation.html?success=1");
        } else {
            exchange.getResponseHeaders().add("Location", "/reservation.html?error=1");
        }

        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
