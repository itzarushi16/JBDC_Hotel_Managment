import hotel_reservation.DatabaseConnection;

import java.sql.*;
import java.util.Scanner;

public class HotelReservationSystem {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to the Hotel Reservation System");
            System.out.println("1. Make a Reservation");
            System.out.println("2. View Available Rooms");
            System.out.println("3. Exit");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    makeReservation(conn, scanner);
                    break;
                case 2:
                    viewAvailableRooms(conn);
                    break;
                default:
                    System.out.println("Goodbye!");
                    System.exit(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();


        }
    }
    public static void makeReservation(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter your first name:");
        String firstName = scanner.next();
        System.out.println("Enter your last name:");
        String lastName = scanner.next();
        System.out.println("Enter your email:");
        String email = scanner.next();
        System.out.println("Enter your phone:");
        String phone = scanner.next();

        String customerQuery = "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        PreparedStatement customerStmt = conn.prepareStatement(customerQuery, Statement.RETURN_GENERATED_KEYS);
        customerStmt.setString(1, firstName);
        customerStmt.setString(2, lastName);
        customerStmt.setString(3, email);
        customerStmt.setString(4, phone);
        customerStmt.executeUpdate();

        ResultSet generatedKeys = customerStmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            int customerId = generatedKeys.getInt(1);

            System.out.println("Available rooms:");
            viewAvailableRooms(conn);

            System.out.println("Enter room ID to reserve:");
            int roomId = scanner.nextInt();
            System.out.println("Enter check-in date (YYYY-MM-DD):");
            String checkInDate = scanner.next();
            System.out.println("Enter check-out date (YYYY-MM-DD):");
            String checkOutDate = scanner.next();

            // Insert booking data
            String roomPriceQuery = "SELECT price_per_night FROM rooms WHERE room_id = ?";
            PreparedStatement roomPriceStmt = conn.prepareStatement(roomPriceQuery);
            roomPriceStmt.setInt(1, roomId);
            ResultSet roomPriceResult = roomPriceStmt.executeQuery();
            double pricePerNight = 0;
            if (roomPriceResult.next()) {
                pricePerNight = roomPriceResult.getDouble(1);
            }

            long numNights = java.sql.Date.valueOf(checkOutDate).toLocalDate().toEpochDay() - java.sql.Date.valueOf(checkInDate).toLocalDate().toEpochDay();
            double totalPrice = numNights * pricePerNight;

            String bookingQuery = "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, total_price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery);
            bookingStmt.setInt(1, customerId);
            bookingStmt.setInt(2, roomId);
            bookingStmt.setString(3, checkInDate);
            bookingStmt.setString(4, checkOutDate);
            bookingStmt.setDouble(5, totalPrice);
            bookingStmt.executeUpdate();

            System.out.println("Reservation successful! Total price: $" + totalPrice);
        }
    }

    public static void viewAvailableRooms(Connection conn) throws SQLException {
        String query = "SELECT * FROM rooms";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            System.out.println("Room ID: " + rs.getInt("room_id"));
            System.out.println("Room Number: " + rs.getInt("room_number"));
            System.out.println("Room Type: " + rs.getString("room_type"));
            System.out.println("Price per Night: $" + rs.getDouble("price_per_night"));
            System.out.println("------------------------");
        }
    }
}
