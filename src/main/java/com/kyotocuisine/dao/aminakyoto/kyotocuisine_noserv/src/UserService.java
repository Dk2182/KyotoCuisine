public class UserService {

    public static User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        email = email.trim();
        password = password.trim();

        try {
            User dbUser = UserDAO.findByEmailAndPassword(email, password);
            if (dbUser != null) {
                return dbUser;
            }
        } catch (Exception e) {
            // fallback demo users below
        }

        if (email.equals("customer@kyoto.com") && password.equals("123")) {
            User u = new User();
            u.setId(1);
            u.setEmail(email);
            u.setRole("CUSTOMER");
            return u;
        }

        if (email.equals("staff@kyoto.com") && password.equals("123")) {
            User u = new User();
            u.setId(2);
            u.setEmail(email);
            u.setRole("STAFF");
            return u;
        }

        if (email.equals("admin@kyoto.com") && password.equals("123")) {
            User u = new User();
            u.setId(3);
            u.setEmail(email);
            u.setRole("ADMIN");
            return u;
        }

        return null;
    }
}
