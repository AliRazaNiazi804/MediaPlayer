package com.mediaplayer.db;

import com.mediaplayer.model.User;
import com.mediaplayer.model.Media;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:mediaplayer.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)";
            stmt.execute(createUsersTable);

            // Create media table
            String createMediaTable = "CREATE TABLE IF NOT EXISTS media (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "file_path TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "user_id INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users (id))";
            stmt.execute(createMediaTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // User operations
    public static boolean registerUser(User user) {
        String sql = "INSERT INTO users(name, email, password) VALUES(?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Email already exists or other error
        }
    }

    public static User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Media operations
    public static void saveMedia(Media media) {
        String sql = "INSERT INTO media(title, file_path, type, user_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, media.getTitle());
            pstmt.setString(2, media.getFilePath());
            pstmt.setString(3, media.getType());
            pstmt.setInt(4, media.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Media> getUserMedia(int userId) {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Media media = new Media();
                media.setId(rs.getInt("id"));
                media.setTitle(rs.getString("title"));
                media.setFilePath(rs.getString("file_path"));
                media.setType(rs.getString("type"));
                media.setUserId(rs.getInt("user_id"));
                mediaList.add(media);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mediaList;
    }
}
