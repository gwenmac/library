package library.driver;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class LonghornCsvToBookDriver {

    private static final long HOUSEHOLD_ID = 3;
    private static final long GWEN_USER_ID = 3;
    private static final long DALE_USER_ID = 4;

    private static long nextBookId = 9;
    private static long nextAuthorId = 7;
    private static long nextGenreId = 8;

    // Track already-created authors and genres to avoid duplicates
    private static final Map<String, Long> authorMap = new HashMap<>();
    private static final Map<String, Long> genreMap = new HashMap<>();

    public static void main(String[] args) {
        String fileLocation = args[0];
        String outputFile = args.length > 1 ? args[1] : "csv/longhorn_import.sql";

        System.out.println("fileLocation: " + fileLocation);
        System.out.println("outputFile: " + outputFile);

        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile));
             CSVReader reader = new CSVReader(new FileReader(fileLocation))) {

            out.println("-- Generated SQL import from LonghornCollection.csv");
            out.println("-- Run this against the library database");
            out.println();

            reader.readNext(); // skip header
            String[] line;
            int count = 0;
            while ((line = reader.readNext()) != null) {
                createBook(line, out);
                count++;
            }

            System.out.println("Done! Generated SQL for " + count + " books in: " + outputFile);

        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createBook(String[] cols, PrintWriter out) {
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

        long bookId = nextBookId++;
        String sortTitle = title.replaceFirst("(?i)^(the|a)\\s+", "");

        out.println("-- Book: " + title);

        // 1. Find or create author
        Long authorId = null;
        if (authorLast != null) {
            authorId = getOrCreateAuthor(out, authorFirst, authorLast);
        }

        // 2. Find or create genre
        Long genreId = null;
        if (genre != null && !genre.isEmpty() && !genre.equals("-")) {
            genreId = getOrCreateGenre(out, genre);
        }

        // 3. Insert book
        out.printf("INSERT INTO book (id, title, sort_title, page_count, year, household_id, created_at, updated_at) VALUES (%d, %s, %s, %s, %s, %d, NOW(), NOW());%n",
                bookId,
                sqlString(title),
                sqlString(sortTitle),
                pageNum != null ? pageNum.toString() : "NULL",
                year != null ? year.toString() : "NULL",
                HOUSEHOLD_ID);

        // 4. Link author to book
        if (authorId != null) {
            out.printf("INSERT INTO book_author (book_id, author_id) VALUES (%d, %d);%n", bookId, authorId);
        }

        // 5. Link genre to book
        if (genreId != null) {
            out.printf("INSERT INTO book_genre (book_id, genre_id) VALUES (%d, %d);%n", bookId, genreId);
        }

        // 6. Default to English language (language id = 1)
        out.printf("INSERT INTO book_language (book_id, language_id) VALUES (%d, 1);%n", bookId);

        // 7. Set Gwen's status
        if (gwenStatusId != null && gwenStatusId > 0) {
            out.printf("INSERT INTO book_status (book_id, status_id, user_id, updated_at) VALUES (%d, %d, %d, NOW());%n",
                    bookId, gwenStatusId, GWEN_USER_ID);
        }

        // 8. Set Dale's status
        if (daleStatusId != null && daleStatusId > 0) {
            out.printf("INSERT INTO book_status (book_id, status_id, user_id, updated_at) VALUES (%d, %d, %d, NOW());%n",
                    bookId, daleStatusId, DALE_USER_ID);
        }

        out.println();
    }

    private static Long getOrCreateAuthor(PrintWriter out, String firstName, String lastName) {
        String key = (firstName != null ? firstName : "NULL") + "|" + lastName;
        if (authorMap.containsKey(key)) {
            return authorMap.get(key);
        }

        long authorId = nextAuthorId++;
        authorMap.put(key, authorId);

        out.printf("INSERT INTO author (id, first_name, last_name, household_id) VALUES (%d, %s, %s, %d);%n",
                authorId,
                firstName != null ? sqlString(firstName) : "NULL",
                sqlString(lastName),
                HOUSEHOLD_ID);

        return authorId;
    }

    private static Long getOrCreateGenre(PrintWriter out, String name) {
        if (genreMap.containsKey(name)) {
            return genreMap.get(name);
        }

        long genreId = nextGenreId++;
        genreMap.put(name, genreId);

        out.printf("INSERT INTO genre (id, name) VALUES (%d, %s);%n", genreId, sqlString(name));

        return genreId;
    }

    private static String sqlString(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
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
