package library.controllers;

import library.entities.Household;
import library.entities.User;
import library.repositories.HouseholdRepository;
import library.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserRepository userRepository,
            HouseholdRepository householdRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(body.get("password")));
        user.setDisplayName(body.get("displayName"));
        user.setRole(User.Role.user);
        user.setCreatedAt(LocalDateTime.now());

        Long householdId = Long.parseLong(body.get("householdId"));
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Household not found"));
        user.setHousehold(household);

        return userRepository.save(user);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin user");
        }

        userRepository.delete(user);
    }

    // --- Household CRUD ---

    @GetMapping("/admin/households")
    public List<Household> listHouseholds() {
        return householdRepository.findAll();
    }

    @PostMapping("/admin/households")
    @ResponseStatus(HttpStatus.CREATED)
    public Household createHousehold(@RequestBody Map<String, String> body) {
        Household household = new Household();
        household.setName(body.get("name"));
        household.setCreatedAt(LocalDateTime.now());
        return householdRepository.save(household);
    }

    @PutMapping("/admin/households/{id}")
    public Household updateHousehold(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));
        household.setName(body.get("name"));
        return householdRepository.save(household);
    }

// --- Assign user to household ---

    @PutMapping("/admin/users/{id}/household")
    public User assignHousehold(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Household household = householdRepository.findById(body.get("householdId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));
        user.setHousehold(household);
        return userRepository.save(user);
    }
}
