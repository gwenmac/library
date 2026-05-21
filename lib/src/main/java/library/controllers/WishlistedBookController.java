package library.controllers;

import library.entities.WishlistedBook;
import library.repositories.WishlistedBookRepository;
import library.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}