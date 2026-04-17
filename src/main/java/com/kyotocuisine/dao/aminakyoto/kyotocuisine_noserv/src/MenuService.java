public class MenuService {

    public static void addItem(String name, double price) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Item name is required.");
        }
        if (price < 0) {
            throw new RuntimeException("Price cannot be negative.");
        }
        MenuItemDAO.insert(name.trim(), price);
    }
}
