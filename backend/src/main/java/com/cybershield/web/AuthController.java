package com.cybershield.web;

import com.cybershield.model.User;
import com.cybershield.service.AuthenticationService;
import com.cybershield.web.dto.LoginRequest;
import com.cybershield.web.dto.RegisterRequest;
import com.cybershield.web.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationService authenticationService,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationService = authenticationService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        User user = authenticationService.register(
                request.getUsername(), request.getEmail(), request.getFullName(), request.getPassword());
        establishSession(user, httpRequest, httpResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        User user = authenticationService.login(request.getUsername(), request.getPassword());
        establishSession(user, httpRequest, httpResponse);
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return UserResponse.from(authenticationService.requireUser(userId));
    }

    private void establishSession(User user, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        session.setAttribute(SessionUser.USER_ID_ATTR, user.getUserId());
    }
}
