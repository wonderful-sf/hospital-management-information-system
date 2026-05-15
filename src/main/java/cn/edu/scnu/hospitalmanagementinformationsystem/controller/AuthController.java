package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.LoginRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.MenuItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.SessionUser;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.AuthService;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.InvalidCredentialsException;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SessionUser>> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "账号和密码不能为空"));
        }
        try {
            SessionUser user = authService.login(request.username().trim(), request.password());
            session.setAttribute(AuthService.LOGIN_USER, user);
            return ResponseEntity.ok(ApiResponse.ok(user));
        } catch (InvalidCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionUser>> me(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(AuthService.LOGIN_USER);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "未登录"));
        }
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<MenuItem>>> menus(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(AuthService.LOGIN_USER);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "未登录"));
        }
        return ResponseEntity.ok(ApiResponse.ok(authService.menusFor(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
