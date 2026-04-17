import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class MainServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/auth/login", new LoginHandler());
        server.createContext("/auth/logout", new LogoutHandler());

        server.createContext("/cart/add", new CartAddHandler());
        server.createContext("/cart/view", new CartViewHandler());
        server.createContext("/cart/checkout", new CartCheckoutHandler());

        server.createContext("/reservations/create", new ReservationHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Kyoto Cuisine server running at http://localhost:8080/login.html");
    }
}