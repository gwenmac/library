package library.controllers;

import library.entities.User;
import library.repositories.UserRepository;
import library.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "role", user.getRole().name()
            )
        );
    }

    @PostMapping("/bootstrap")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> bootstrap(@RequestBody Map<String, String> body) {
        if (userRepository.existsByRole(User.Role.admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin already exists");
        }

        User admin = new User();
        admin.setEmail(body.get("email"));
        admin.setPasswordHash(passwordEncoder.encode(body.get("password")));
        admin.setDisplayName(body.get("displayName"));
        admin.setRole(User.Role.admin);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole().name());

        return Map.of(
            "token", token,
            "user", Map.of(
                "id", admin.getId(),
                "email", admin.getEmail(),
                "displayName", admin.getDisplayName(),
                "role", admin.getRole().name()
            )
        );
    }
}
