package library.controllers;

import library.entities.Author;
import library.entities.Book;
import library.entities.WishlistBook;
import library.repositories.AuthorRepository;
import library.repositories.WishlistBookRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class WishlistBookController {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final WishlistBookRepository wishlistBookRepository;
    private final AuthorRepository authorRepository;

    public WishlistBookController(WishlistBookRepository wishlistBookRepository, AuthorRepository authorRepository) {
        this.wishlistBookRepository = wishlistBookRepository;
        this.authorRepository = authorRepository;
    }

    @GetMapping("/wishlist/all")
    public List<WishlistBook> getAll() {
        return wishlistBookRepository.findAllByUserIdOrderBySortTitleAsc(CurrentUser.get().getId());
    }

    @GetMapping("/wishlist/{id}")
    public WishlistBook getById(@PathVariable Long id) {
        return wishlistBookRepository.findByIdAndUserId(id, CurrentUser.get().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @Transactional
    @DeleteMapping("/wishlist/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        WishlistBook book = wishlistBookRepository.findByIdAndUserId(id, CurrentUser.get().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist book not found"));
        book.getAuthors().clear();
        wishlistBookRepository.delete(book);
    }

    @Transactional
    @PostMapping("/wishlist")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistBook create(@RequestBody Map<String, Object> body) {
        WishlistBook book = new WishlistBook();
        book.setUser(CurrentUser.get());
        book.setTitle((String) body.get("title"));
        if (body.get("authorIds") != null) {
            book.setAuthors(resolveAuthorIds(body.get("authorIds")));
        }
        if (body.get("releaseDate") != null) {
            book.setReleaseDate(LocalDate.parse(body.get("releaseDate").toString(), formatter));
        }
        book.setNotes((String) body.get("notes"));
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        return wishlistBookRepository.save(book);
    }

    @Transactional
    @PutMapping("/wishlist/{id}")
    public WishlistBook update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        WishlistBook book = wishlistBookRepository.findByIdAndUserId(id, CurrentUser.get().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        book.setTitle((String) body.get("title"));
        if (body.get("authorIds") != null) {
            book.setAuthors(resolveAuthorIds(body.get("authorIds")));
        } else {
            book.setAuthors(new HashSet<>());
        }
        if (body.get("releaseDate") != null) {
            book.setReleaseDate(LocalDate.parse(body.get("releaseDate").toString(), formatter));
        } else {
            book.setReleaseDate(null);
        }
        book.setNotes((String) body.get("notes"));
        book.setUpdatedAt(LocalDateTime.now());
        return wishlistBookRepository.save(book);
    }

    // ── Helpers ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Set<Author> resolveAuthorIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(ids));
        if (authors.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more author IDs not found");
        }
        return authors;
    }
}