import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/login.html";
        }

        File baseDir = new File(System.getProperty("user.dir"));
        File webDir = new File(baseDir, "kyotocuisine_noserv/web");
        File file = new File(webDir, path.substring(1));

        System.out.println("user.dir = " + baseDir.getAbsolutePath());
        System.out.println("Requested path = " + path);
        System.out.println("Resolved file = " + file.getAbsolutePath());

        if (!file.exists() || file.isDirectory()) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        if (path.endsWith(".html")) {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        } else if (path.endsWith(".css")) {
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
        } else if (path.endsWith(".js")) {
            exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
        }

        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}