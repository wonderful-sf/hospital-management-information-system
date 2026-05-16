package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
    private final JdbcTemplate jdbcTemplate;

    public StatisticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> dashboard(Long userId, String role, Long linkedId) {
        Map<String, Object> result = new HashMap<>();
        result.put("departmentCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM departments", Long.class));
        result.put("doctorCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM doctors", Long.class));
        result.put("todayRegistrations", jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM registrations WHERE DATE(registered_at) = CURDATE()", Long.class));

        if ("PATIENT".equals(role) && linkedId != null) {
            BigDecimal pending = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = 'UNPAID' AND patient_id = ?",
                BigDecimal.class, linkedId);
            result.put("pendingBillAmount", pending);
            Long activeAdmissions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admissions WHERE patient_id = ? AND status = 'ACTIVE'", Long.class, linkedId);
            result.put("activeAdmissionCount", activeAdmissions);
        } else if ("DOCTOR".equals(role) && linkedId != null) {
            Long todayVisits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM registrations WHERE doctor_id = ? AND status = 'REGISTERED' AND DATE(registered_at) = CURDATE()",
                Long.class, linkedId);
            result.put("todayPendingVisits", todayVisits);
            Long activeAdmissions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admissions WHERE attending_doctor_id = ? AND status = 'ACTIVE'", Long.class, linkedId);
            result.put("activeAdmissionCount", activeAdmissions);
        } else {
            BigDecimal pending = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = 'UNPAID'", BigDecimal.class);
            result.put("pendingBillAmount", pending);
            Long activeAdmissions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admissions WHERE status = 'ACTIVE'", Long.class);
            result.put("activeAdmissionCount", activeAdmissions);
        }
        return result;
    }

    public List<Map<String, Object>> departmentScheduleStats(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(
            "SELECT d.name AS dept_name, " +
            "COALESCE(SUM(CASE WHEN ds.schedule_type='OUTPATIENT' THEN 1 ELSE 0 END),0) AS outpatient, " +
            "COALESCE(SUM(CASE WHEN ds.schedule_type='INPATIENT_ROUND' THEN 1 ELSE 0 END),0) AS inpatient, " +
            "COUNT(ds.id) AS total " +
            "FROM departments d LEFT JOIN doctor_schedules ds ON d.id=ds.department_id " +
            "AND ds.start_time BETWEEN ? AND ? GROUP BY d.id,d.name ORDER BY total DESC",
            (rs, n) -> Map.of("departmentName", rs.getString("dept_name"),
                "outpatientCount", rs.getLong("outpatient"),
                "inpatientCount", rs.getLong("inpatient"),
                "totalCount", rs.getLong("total")),
            startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    public List<Map<String, Object>> departmentScheduleDetail(Long deptId, LocalDate start, LocalDate end) {
        return jdbcTemplate.query(
            "SELECT doc.name AS doc_name, ds.schedule_type, ds.start_time, ds.end_time, ds.room " +
            "FROM doctor_schedules ds JOIN doctors doc ON ds.doctor_id=doc.id " +
            "WHERE ds.department_id=? AND ds.start_time BETWEEN ? AND ? ORDER BY doc.name, ds.start_time",
            (rs, n) -> Map.of("doctorName", rs.getString("doc_name"),
                "scheduleType", rs.getString("schedule_type"),
                "startTime", rs.getTimestamp("start_time").toLocalDateTime().toString(),
                "endTime", rs.getTimestamp("end_time").toLocalDateTime().toString(),
                "room", rs.getString("room")),
            deptId, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    public List<Map<String, Object>> doctorWorkloadStats(Long doctorId, Long deptId, LocalDate start, LocalDate end) {
        StringBuilder sql = new StringBuilder(
            "SELECT doc.name AS dname, dep.name AS pname, " +
            "(SELECT COUNT(*) FROM outpatient_visits ov WHERE ov.doctor_id=doc.id AND ov.visited_at BETWEEN ? AND ?) AS visits, " +
            "(SELECT COUNT(*) FROM admissions adm WHERE adm.attending_doctor_id=doc.id AND adm.status='ACTIVE') AS admits, " +
            "(SELECT COUNT(*) FROM prescriptions p WHERE p.doctor_id=doc.id AND p.created_at BETWEEN ? AND ?) AS rxs, " +
            "(SELECT COALESCE(SUM(p.total_amount),0) FROM prescriptions p WHERE p.doctor_id=doc.id AND p.created_at BETWEEN ? AND ?) AS rx_total " +
            "FROM doctors doc JOIN departments dep ON doc.department_id=dep.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        params.add(start.atStartOfDay()); params.add(end.plusDays(1).atStartOfDay());
        params.add(start.atStartOfDay()); params.add(end.plusDays(1).atStartOfDay());
        params.add(start.atStartOfDay()); params.add(end.plusDays(1).atStartOfDay());
        if (doctorId != null) { sql.append("AND doc.id=?"); params.add(doctorId); }
        if (deptId != null) { sql.append("AND doc.department_id=?"); params.add(deptId); }
        sql.append(" ORDER BY visits DESC");
        return jdbcTemplate.query(sql.toString(), (rs, n) -> Map.of(
            "doctorName", rs.getString("dname"), "departmentName", rs.getString("pname"),
            "visitCount", rs.getLong("visits"), "admissionCount", rs.getLong("admits"),
            "prescriptionCount", rs.getLong("rxs"), "totalPrescriptionAmount", rs.getBigDecimal("rx_total")),
            params.toArray());
    }

    public Map<String, Object> outpatientSummary(LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();
        result.put("registrations", jdbcTemplate.queryForMap(
            "SELECT COUNT(*) AS t, COALESCE(SUM(CASE WHEN status='REGISTERED' THEN 1 ELSE 0 END),0) AS r, " +
            "COALESCE(SUM(CASE WHEN status='VISITED' THEN 1 ELSE 0 END),0) AS v, " +
            "COALESCE(SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END),0) AS c " +
            "FROM registrations WHERE registered_at BETWEEN ? AND ?", start.atStartOfDay(), end.plusDays(1).atStartOfDay()));
        result.put("prescriptions", jdbcTemplate.queryForMap(
            "SELECT COUNT(*) AS t, COALESCE(SUM(CASE WHEN status='PAID' THEN 1 ELSE 0 END),0) AS p, " +
            "COALESCE(SUM(CASE WHEN status='UNPAID' THEN 1 ELSE 0 END),0) AS u, " +
            "COALESCE(SUM(total_amount),0) AS amt FROM prescriptions WHERE created_at BETWEEN ? AND ?",
            start.atStartOfDay(), end.plusDays(1).atStartOfDay()));
        return result;
    }

    public Map<String, Object> inpatientSummary() {
        return jdbcTemplate.queryForMap(
            "SELECT COALESCE(SUM(CASE WHEN status='ACTIVE' THEN 1 ELSE 0 END),0) AS a, " +
            "COALESCE(SUM(CASE WHEN status='DISCHARGED' THEN 1 ELSE 0 END),0) AS d, " +
            "COALESCE(SUM(CASE WHEN status='SUSPENDED' THEN 1 ELSE 0 END),0) AS s, COUNT(*) AS t FROM admissions");
    }

    public Map<String, Object> inpatientRecordSummary(LocalDate start, LocalDate end) {
        Map<String, Object> r = new HashMap<>();
        r.put("recordCount", jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM inpatient_records ir JOIN admissions a ON ir.admission_id=a.id WHERE ir.record_date BETWEEN ? AND ?",
            Long.class, start, end));
        r.put("totalCost", jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(b.total_amount),0) FROM bills b WHERE b.source_type='INPATIENT' AND b.created_at BETWEEN ? AND ?",
            BigDecimal.class, start.atStartOfDay(), end.plusDays(1).atStartOfDay()));
        return r;
    }

    public List<Map<String, Object>> patientTreatmentDetail(LocalDate start, LocalDate end) {
        return jdbcTemplate.query(
            "SELECT p.name, p.medical_record_no, COUNT(DISTINCT r.id) AS op, COUNT(DISTINCT a.id) AS adm, " +
            "COALESCE(SUM(b.total_amount),0) AS cost FROM patients p " +
            "LEFT JOIN registrations r ON p.id=r.patient_id AND r.registered_at BETWEEN ? AND ? " +
            "LEFT JOIN admissions a ON p.id=a.patient_id AND a.admitted_at BETWEEN ? AND ? " +
            "LEFT JOIN bills b ON p.id=b.patient_id AND b.status='PAID' AND b.created_at BETWEEN ? AND ? " +
            "GROUP BY p.id ORDER BY cost DESC",
            (rs, n) -> Map.of("name", rs.getString("name"), "medicalRecordNo", rs.getString("medical_record_no"),
                "outpatientCount", rs.getLong("op"), "admissionCount", rs.getLong("adm"),
                "totalCost", rs.getBigDecimal("cost")),
            start.atStartOfDay(), end.plusDays(1).atStartOfDay(), start.atStartOfDay(), end.plusDays(1).atStartOfDay(),
            start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }
}
