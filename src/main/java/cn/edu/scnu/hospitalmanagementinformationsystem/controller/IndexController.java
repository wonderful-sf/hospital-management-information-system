package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.SessionUser;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        return roleHome(session, "ADMIN", "/admin.html");
    }

    @GetMapping("/doctor")
    public String doctor(HttpSession session) {
        return roleHome(session, "DOCTOR", "/doctor.html");
    }

    @GetMapping("/patient")
    public String patient(HttpSession session) {
        return roleHome(session, "PATIENT", "/patient.html");
    }

    @GetMapping("/patients")
    public String patients() {
        return "forward:/patients.html";
    }

    @GetMapping("/departments")
    public String departments() {
        return "forward:/departments.html";
    }

    @GetMapping("/doctor-titles")
    public String doctorTitles() {
        return "forward:/doctor-titles.html";
    }

    @GetMapping("/doctors")
    public String doctors() {
        return "forward:/doctors.html";
    }

    @GetMapping("/medicines")
    public String medicines() {
        return "forward:/medicines.html";
    }

    @GetMapping("/wards")
    public String wards() {
        return "forward:/wards.html";
    }

    @GetMapping("/beds")
    public String beds() {
        return "forward:/beds.html";
    }

    @GetMapping("/schedules")
    public String schedules() {
        return "forward:/schedules.html";
    }

    @GetMapping("/registrations")
    public String registrations() {
        return "forward:/registrations.html";
    }

    @GetMapping("/visits")
    public String visits() {
        return "forward:/visits.html";
    }

    @GetMapping("/prescriptions")
    public String prescriptions() {
        return "forward:/prescriptions.html";
    }

    @GetMapping("/admissions")
    public String admissions() {
        return "forward:/admissions.html";
    }

    @GetMapping("/prepaid")
    public String prepaid() {
        return "forward:/prepaid.html";
    }

    @GetMapping("/inpatient-records")
    public String inpatientRecords() {
        return "forward:/inpatient-records.html";
    }

    @GetMapping("/bills")
    public String bills() {
        return "forward:/bills.html";
    }

    @GetMapping("/statistics")
    public String statistics() {
        return "forward:/statistics.html";
    }

    private String roleHome(HttpSession session, String role, String page) {
        SessionUser user = (SessionUser) session.getAttribute(AuthService.LOGIN_USER);
        if (user == null) {
            return "redirect:/login";
        }
        if (!role.equals(user.role())) {
            return "redirect:" + homePathForRole(user.role());
        }
        return "forward:" + page;
    }

    private String homePathForRole(String role) {
        return switch (role) {
            case "ADMIN" -> "/admin";
            case "DOCTOR" -> "/doctor";
            case "PATIENT" -> "/patient";
            default -> "/index";
        };
    }
}
