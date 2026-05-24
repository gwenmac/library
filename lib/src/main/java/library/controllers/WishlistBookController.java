package library.controllers;

import library.entities.WishlistBook;
import library.repositories.WishlistBookRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class WishlistBookController {

    private final WishlistBookRepository wishlistBookRepository;

    public WishlistBookController(WishlistBookRepository wishlistBookRepository) {
        this.wishlistBookRepository = wishlistBookRepository;
    }

    @GetMapping("/wishlist/all")
    public List<WishlistBook> getAll() {
        return wishlistBookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.householdId());
    }

    @Transactional
    @DeleteMapping("/wishlist/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        WishlistBook book = wishlistBookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist book not found"));
        book.getAuthors().clear();
        wishlistBookRepository.delete(book);
    }
}