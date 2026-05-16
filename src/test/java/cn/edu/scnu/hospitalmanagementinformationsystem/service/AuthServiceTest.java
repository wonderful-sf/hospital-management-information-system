package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.MenuItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.User;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.UserRole;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.UserStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    @DisplayName("login returns session user when username and password match active user")
    void login_activeUserWithValidPassword_returnsSessionUser() {
        var service = new AuthService(new StubUserMapper(List.of(activeAdmin())));

        var result = service.login("admin", "123456");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.permissions()).containsExactly("admin:*");
    }

    @Test
    @DisplayName("login rejects invalid password")
    void login_invalidPassword_throws() {
        var service = new AuthService(new StubUserMapper(List.of(activeAdmin())));

        assertThatThrownBy(() -> service.login("admin", "wrong"))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    @DisplayName("menus include admin-only management entries for admin")
    void menus_admin_returnsAdminManagementMenus() {
        var service = new AuthService(new StubUserMapper(List.of(activeAdmin())));
        var user = service.login("admin", "123456");

        List<MenuItem> menus = service.menusFor(user);

        assertThat(menus).extracting(MenuItem::title)
            .contains("管理员首页", "科室管理", "医生管理", "病人管理", "药品管理", "病房管理", "病床管理", "统计分析");
    }

    @Test
    @DisplayName("menus hide admin-only entries for patient")
    void menus_patient_hidesAdminManagementMenus() {
        var service = new AuthService(new StubUserMapper(List.of(activePatient())));
        var user = service.login("pat_lin", "123456");

        List<MenuItem> menus = service.menusFor(user);

        assertThat(menus).extracting(MenuItem::title)
            .contains("病人首页", "挂号管理", "我的住院档案", "预缴管理", "账单管理");
        assertThat(menus).extracting(MenuItem::title)
            .doesNotContain("科室管理", "医生管理", "药品管理");
    }

    private static User activeAdmin() {
        return new User(1L, "admin", "123456", UserRole.ADMIN, "[\"admin:*\"]", UserStatus.ACTIVE);
    }

    private static User activePatient() {
        return new User(
            8L,
            "pat_lin",
            "123456",
            UserRole.PATIENT,
            "[\"patient:registration:manage\",\"patient:bill:view\",\"patient:prepaid:manage\",\"patient:admission:view\"]",
            UserStatus.ACTIVE);
    }

    private record StubUserMapper(List<User> users) implements UserMapper {
        @Override
        public Optional<User> findActiveByUsername(String username) {
            return users.stream()
                .filter(user -> user.username().equals(username))
                .filter(user -> user.status() == UserStatus.ACTIVE)
                .findFirst();
        }
    }
}
