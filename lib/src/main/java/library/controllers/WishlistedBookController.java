package library.controllers;

import library.entities.WishlistedBook;
import library.repositories.WishlistedBookRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class WishlistedBookController {

    private final WishlistedBookRepository wishlistedBookRepository;

    public WishlistedBookController(WishlistedBookRepository wishlistedBookRepository) {
        this.wishlistedBookRepository = wishlistedBookRepository;
    }

    @GetMapping("/wishlist/all")
    public List<WishlistedBook> getAll() {
        return wishlistedBookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.householdId());
    }

    @Transactional
    @DeleteMapping("/wishlist/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        WishlistedBook book = wishlistedBookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlisted book not found"));
        book.getAuthors().clear();
        wishlistedBookRepository.delete(book);
    }
}