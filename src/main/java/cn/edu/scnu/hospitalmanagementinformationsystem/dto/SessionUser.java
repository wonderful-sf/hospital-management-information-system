package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import java.util.List;

public record SessionUser(Long id, String username, String role, List<String> permissions) {
    public boolean hasPermission(String permission) {
        return permissions.contains("admin:*") || permissions.contains(permission);
    }
}
