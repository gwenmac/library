package library.driver;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class LonghornCsvToBookDriver {

    private static final long HOUSEHOLD_ID = 1;
    private static final long GWEN_USER_ID = 1;
    private static final long DALE_USER_ID = 2; // adjust if different

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3333/library";
        String username = "root";
        String password = "root";
        String fileLocation = args[0];

        System.out.println("Connecting database ...");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            System.out.println("fileLocation: " + fileLocation);

            try (CSVReader reader = new CSVReader(new FileReader(fileLocation))) {
                reader.readNext(); // skip header
                String[] line;
                int count = 0;
                while ((line = reader.readNext()) != null) {
                    createBook(line, connection);
                    count++;
                }
                System.out.println("Done! Processed " + count + " books.");
            } catch (CsvValidationException | IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private static void createBook(String[] cols, Connection conn) throws SQLException {
        String title = cols[0];
        String authorFirst = Objects.equals(cols[1], "-") ? null : cols[1];
        String authorLast = Objects.equals(cols[2], "-") ? null : cols[2];
        Integer year = cols[3].isEmpty() || Objects.equals(cols[3], "-") ? null : Integer.parseInt(cols[3]);
        String genre = cols[4];
        Integer gwenStatusId = getStatus(cols[5]);
        Integer daleStatusId = getStatus(cols[6]);
        String location = cols[7]; // TODO: add as tag?
        String format = cols[8];
        String specialEdition = cols[9];
        Integer pageNum = cols[10].isEmpty() ? null : Integer.parseInt(cols[10]);

        // 1. Find or create author
        Long authorId = null;
        if (authorLast != null) {
            authorId = findOrCreateAuthor(conn, authorFirst, authorLast);
        }

        // 2. Find or create genre
        Long genreId = null;
        if (genre != null && !genre.isEmpty() && !genre.equals("-")) {
            genreId = findOrCreateGenre(conn, genre);
        }

        // 3. Insert book
        String sortTitle = title.replaceFirst("(?i)^(the|a)\\s+", "");
        long bookId;
        String insertBook = "INSERT INTO book (title, sort_title, page_count, year, household_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, sortTitle);
            if (pageNum != null) ps.setInt(3, pageNum); else ps.setNull(3, Types.INTEGER);
            if (year != null) ps.setInt(4, year); else ps.setNull(4, Types.INTEGER);
            ps.setLong(5, HOUSEHOLD_ID);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                bookId = keys.getLong(1);
            }
        }

        // 4. Link author to book
        if (authorId != null) {
            String insertBookAuthor = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBookAuthor)) {
                ps.setLong(1, bookId);
                ps.setLong(2, authorId);
                ps.executeUpdate();
            }
        }

        // 5. Link genre to book
        if (genreId != null) {
            String insertBookGenre = "INSERT INTO book_genre (book_id, genre_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBookGenre)) {
                ps.setLong(1, bookId);
                ps.setLong(2, genreId);
                ps.executeUpdate();
            }
        }

        // 6. Default to English language (language id = 1)
        String insertBookLang = "INSERT INTO book_language (book_id, language_id) VALUES (?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(insertBookLang)) {
            ps.setLong(1, bookId);
            ps.executeUpdate();
        }

        // 7. Set Gwen's status
        if (gwenStatusId != null && gwenStatusId > 0) {
            insertBookStatus(conn, bookId, gwenStatusId, GWEN_USER_ID);
        }

        // 8. Set Dale's status
        if (daleStatusId != null && daleStatusId > 0) {
            insertBookStatus(conn, bookId, daleStatusId, DALE_USER_ID);
        }

        System.out.println("Created book: " + title + " (id=" + bookId + ")");
    }

    private static Long findOrCreateAuthor(Connection conn, String firstName, String lastName) throws SQLException {
        // Try to find existing author
        String select = "SELECT id FROM author WHERE last_name = ? AND (first_name = ? OR (first_name IS NULL AND ? IS NULL)) AND household_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setString(3, firstName);
            ps.setLong(4, HOUSEHOLD_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }

        // Create new author
        String insert = "INSERT INTO author (first_name, last_name, household_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            if (firstName != null) ps.setString(1, firstName); else ps.setNull(1, Types.VARCHAR);
            ps.setString(2, lastName);
            ps.setLong(3, HOUSEHOLD_ID);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private static Long findOrCreateGenre(Connection conn, String name) throws SQLException {
        String select = "SELECT id FROM genre WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }

        String insert = "INSERT INTO genre (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private static void insertBookStatus(Connection conn, long bookId, int statusId, long userId) throws SQLException {
        String insert = "INSERT INTO book_status (book_id, status_id, user_id, updated_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setLong(1, bookId);
            ps.setInt(2, statusId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    private static Integer getStatus(String col) {
        return switch (col) {
            case "Y" -> 3;   // Completed
            case "N" -> 1;   // To Be Read
            case "DNF" -> 4; // Did Not Finish
            case "I" -> 2;   // In Progress
            case "N/A" -> 6; // Not Applicable
            case "N/I" -> 7; // Not Interested
            default -> null;
        };
    }
}
