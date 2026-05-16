package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.MenuItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.SessionUser;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.User;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public static final String LOGIN_USER = "LOGIN_USER";

    private static final List<MenuItem> ALL_MENUS = List.of(
        new MenuItem("管理员首页", "/admin", "admin:*"),
        new MenuItem("医生首页", "/doctor", "doctor:schedule:view"),
        new MenuItem("病人首页", "/patient", "patient:registration:manage"),
        new MenuItem("科室管理", "/departments", "admin:*"),
        new MenuItem("医生管理", "/doctors", "admin:*"),
        new MenuItem("病人管理", "/patients", "admin:*"),
        new MenuItem("药品管理", "/medicines", "admin:*"),
        new MenuItem("病房管理", "/wards", "admin:*"),
        new MenuItem("病床管理", "/beds", "admin:*"),
        new MenuItem("排班日历", "/schedules", "doctor:schedule:view"),
        new MenuItem("挂号管理", "/registrations", "patient:registration:manage"),
        new MenuItem("接诊管理", "/visits", "doctor:visit:manage"),
        new MenuItem("处方管理", "/prescriptions", "doctor:prescription:manage"),
        new MenuItem("住院记录", "/inpatient-records", "doctor:inpatient:manage"),
        new MenuItem("我的住院档案", "/admissions", "patient:admission:view"),
        new MenuItem("预缴管理", "/prepaid", "patient:prepaid:manage"),
        new MenuItem("账单管理", "/bills", "patient:bill:view"),
        new MenuItem("统计分析", "/statistics", "admin:*")
    );

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SessionUser login(String username, String password) {
        User user = userMapper.findActiveByUsername(username).orElseThrow(InvalidCredentialsException::new);
        if (!user.password().equals(password)) {
            throw new InvalidCredentialsException();
        }
        return new SessionUser(user.id(), user.username(), user.role().name(), parsePermissions(user.permissions()));
    }

    public List<MenuItem> menusFor(SessionUser user) {
        return ALL_MENUS.stream()
            .filter(menu -> menu.permission().isBlank() || user.hasPermission(menu.permission()))
            .toList();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private List<String> parsePermissions(String rawPermissions) {
        if (rawPermissions == null || rawPermissions.isBlank() || "[]".equals(rawPermissions)) {
            return List.of();
        }
        String body = rawPermissions.trim();
        if (body.startsWith("[") && body.endsWith("]")) {
            body = body.substring(1, body.length() - 1);
        }
        if (body.isBlank()) {
            return List.of();
        }
        List<String> permissions = new ArrayList<>();
        for (String item : body.split(",")) {
            String permission = item.trim().replace("\"", "");
            if (!permission.isBlank()) {
                permissions.add(permission);
            }
        }
        return List.copyOf(permissions);
    }
}
