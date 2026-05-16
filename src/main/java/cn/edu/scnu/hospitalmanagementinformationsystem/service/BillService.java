package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bill;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.BillItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final JdbcTemplate jdbc;
    private final Object lock = new Object();

    public BillService(JdbcTemplate j) { this.jdbc = j; }

    public List<Bill> findAll(Long patientId, String status) {
        StringBuilder s = new StringBuilder("SELECT id,bill_no,patient_id,source_type,source_id,total_amount,status,paid_at,created_at FROM bills WHERE 1=1");
        if (patientId != null) s.append(" AND patient_id=").append(patientId);
        if (status != null && !status.isBlank()) s.append(" AND status='").append(status).append("'");
        s.append(" ORDER BY created_at DESC");
        return jdbc.query(s.toString(), (r, n) -> new Bill(r.getLong("id"), r.getString("bill_no"),
            r.getLong("patient_id"), r.getString("source_type"), r.getLong("source_id"),
            r.getBigDecimal("total_amount"), r.getString("status"),
            r.getTimestamp("paid_at") != null ? r.getTimestamp("paid_at").toLocalDateTime() : null,
            r.getTimestamp("created_at").toLocalDateTime()));
    }

    public Bill findById(Long id) {
        return jdbc.queryForObject("SELECT * FROM bills WHERE id=?", (r, n) -> new Bill(r.getLong("id"),
            r.getString("bill_no"), r.getLong("patient_id"), r.getString("source_type"),
            r.getLong("source_id"), r.getBigDecimal("total_amount"), r.getString("status"),
            r.getTimestamp("paid_at") != null ? r.getTimestamp("paid_at").toLocalDateTime() : null,
            r.getTimestamp("created_at").toLocalDateTime()), id);
    }

    public List<BillItem> findItems(Long billId) {
        return jdbc.query("SELECT * FROM bill_items WHERE bill_id=? ORDER BY id", (r, n) ->
            new BillItem(r.getLong("id"), r.getLong("bill_id"), r.getString("item_type"),
                r.getString("item_name"), r.getBigDecimal("unit_price"),
                r.getBigDecimal("quantity"), r.getBigDecimal("amount")), billId);
    }

    public Map<String, Object> stats(Long pid) {
        Map<String, Object> m = new HashMap<>();
        String pw = pid != null ? " WHERE patient_id=" + pid : "";
        String uw = (pid != null ? " AND" : " WHERE") + " status='UNPAID'";
        String aw = (pid != null ? " AND" : " WHERE") + " status='PAID'";
        m.put("pendingAmount", jdbc.queryForObject("SELECT COALESCE(SUM(total_amount),0) FROM bills"+pw+uw, BigDecimal.class));
        m.put("paidAmount", jdbc.queryForObject("SELECT COALESCE(SUM(total_amount),0) FROM bills"+pw+aw, BigDecimal.class));
        m.put("totalCount", jdbc.queryForObject("SELECT COUNT(*) FROM bills"+pw, Long.class));
        return m;
    }

    @Transactional
    public Payment pay(Long billId) {
        Bill bill = findById(billId);
        if (!"UNPAID".equals(bill.status())) throw new RuntimeException("账单已支付");
        String pn = genPayNo();
        jdbc.update("INSERT INTO payments(payment_no,bill_id,patient_id,amount,payment_method,paid_at) VALUES(?,?,?,?,'ONLINE',NOW())",
            pn, billId, bill.patientId(), bill.totalAmount());
        Long pid = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        jdbc.update("UPDATE bills SET status='PAID',paid_at=NOW() WHERE id=?", billId);
        if ("OUTPATIENT".equals(bill.sourceType()) && bill.sourceId() != null)
            jdbc.update("UPDATE prescriptions SET status='PAID' WHERE id=?", bill.sourceId());
        return new Payment(pid, pn, billId, bill.patientId(), bill.totalAmount(), "ONLINE", LocalDateTime.now());
    }

    public List<Payment> findPayments(Long pid) {
        String s = "SELECT * FROM payments" + (pid != null ? " WHERE patient_id=" + pid : "") + " ORDER BY paid_at DESC";
        return jdbc.query(s, (r, n) -> new Payment(r.getLong("id"), r.getString("payment_no"),
            r.getLong("bill_id"), r.getLong("patient_id"), r.getBigDecimal("amount"),
            r.getString("payment_method"), r.getTimestamp("paid_at").toLocalDateTime()));
    }

    private String genPayNo() {
        synchronized (lock) {
            String p = "PAY" + LocalDate.now().format(FMT);
            String mx = jdbc.queryForObject("SELECT MAX(payment_no) FROM payments WHERE payment_no LIKE ?", String.class, p + "%");
            int nx = mx != null && mx.length() >= p.length() ? Integer.parseInt(mx.substring(p.length())) + 1 : 1;
            return p + String.format("%04d", nx);
        }
    }
}
