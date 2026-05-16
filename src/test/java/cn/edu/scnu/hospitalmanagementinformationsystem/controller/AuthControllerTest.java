package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.SessionUser;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.User;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.UserMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.AuthService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private final FakeAuthService authService = new FakeAuthService();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService), new IndexController()).build();

    @Test
    @DisplayName("POST /api/auth/login stores user in session")
    void login_validCredentials_storesUserInSession() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))
            .andExpect(request().sessionAttribute(AuthService.LOGIN_USER, new SessionUser(1L, "admin", "ADMIN", List.of("admin:*"))));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 when username is blank")
    void login_blankUsername_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"   \",\"password\":\"123456\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("GET /api/auth/me returns 401 when session is missing")
    void me_missingSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/logout clears session")
    void logout_existingSession_clearsSession() throws Exception {
        var sessionUser = new SessionUser(1L, "admin", "ADMIN", List.of("admin:*"));

        mockMvc.perform(post("/api/auth/logout").sessionAttr("LOGIN_USER", sessionUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /admin forwards for admin user")
    void admin_adminUser_forwardsAdminHome() throws Exception {
        var sessionUser = new SessionUser(1L, "admin", "ADMIN", List.of("admin:*"));

        mockMvc.perform(get("/admin").sessionAttr(AuthService.LOGIN_USER, sessionUser))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/admin.html"));
    }

    @Test
    @DisplayName("GET /admin redirects doctor to doctor home")
    void admin_doctorUser_redirectsDoctorHome() throws Exception {
        var sessionUser = new SessionUser(2L, "doc_zhang", "DOCTOR", List.of("doctor:schedule:view"));

        mockMvc.perform(get("/admin").sessionAttr(AuthService.LOGIN_USER, sessionUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/doctor"));
    }

    @Test
    @DisplayName("GET /patient redirects missing session to login")
    void patient_missingSession_redirectsLogin() throws Exception {
        mockMvc.perform(get("/patient"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
    private static class FakeAuthService extends AuthService {
        FakeAuthService() {
            super(new EmptyUserMapper());
        }

        @Override
        public SessionUser login(String username, String password) {
            return new SessionUser(1L, username, "ADMIN", List.of("admin:*"));
        }

        @Override
        public void logout(HttpSession session) {
            session.invalidate();
        }
    }

    private static class EmptyUserMapper implements UserMapper {
        @Override
        public Optional<User> findActiveByUsername(String username) {
            return Optional.empty();
        }
    }
}
