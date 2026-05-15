USE hospital;

SET NAMES utf8mb4;

DELETE FROM bill_items;
DELETE FROM bills;
DELETE FROM inpatient_record_items;
DELETE FROM inpatient_records;
DELETE FROM admissions;
DELETE FROM beds;
DELETE FROM wards;
DELETE FROM prescription_items;
DELETE FROM prescriptions;
DELETE FROM outpatient_visits;
DELETE FROM registrations;
DELETE FROM doctor_schedules;
DELETE FROM medicines;
DELETE FROM patients;
DELETE FROM doctors;
DELETE FROM doctor_titles;
DELETE FROM departments;
DELETE FROM users;

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE departments AUTO_INCREMENT = 1;
ALTER TABLE doctor_titles AUTO_INCREMENT = 1;
ALTER TABLE doctors AUTO_INCREMENT = 1;
ALTER TABLE patients AUTO_INCREMENT = 1;
ALTER TABLE medicines AUTO_INCREMENT = 1;
ALTER TABLE doctor_schedules AUTO_INCREMENT = 1;
ALTER TABLE registrations AUTO_INCREMENT = 1;
ALTER TABLE outpatient_visits AUTO_INCREMENT = 1;
ALTER TABLE prescriptions AUTO_INCREMENT = 1;
ALTER TABLE prescription_items AUTO_INCREMENT = 1;
ALTER TABLE wards AUTO_INCREMENT = 1;
ALTER TABLE beds AUTO_INCREMENT = 1;
ALTER TABLE admissions AUTO_INCREMENT = 1;
ALTER TABLE inpatient_records AUTO_INCREMENT = 1;
ALTER TABLE inpatient_record_items AUTO_INCREMENT = 1;
ALTER TABLE bills AUTO_INCREMENT = 1;
ALTER TABLE bill_items AUTO_INCREMENT = 1;

INSERT INTO users (id, username, password, role, status) VALUES
(1, 'admin', '123456', 'ADMIN', 'ACTIVE'),
(2, 'doc_zhang', '123456', 'DOCTOR', 'ACTIVE'),
(3, 'doc_li', '123456', 'DOCTOR', 'ACTIVE'),
(4, 'doc_wang', '123456', 'DOCTOR', 'ACTIVE'),
(5, 'doc_chen', '123456', 'DOCTOR', 'ACTIVE'),
(6, 'doc_liu', '123456', 'DOCTOR', 'ACTIVE'),
(7, 'doc_zhao', '123456', 'DOCTOR', 'ACTIVE'),
(8, 'pat_lin', '123456', 'PATIENT', 'ACTIVE'),
(9, 'pat_huang', '123456', 'PATIENT', 'ACTIVE'),
(10, 'pat_zhou', '123456', 'PATIENT', 'ACTIVE'),
(11, 'pat_wu', '123456', 'PATIENT', 'ACTIVE'),
(12, 'pat_xu', '123456', 'PATIENT', 'ACTIVE'),
(13, 'pat_he', '123456', 'PATIENT', 'ACTIVE');

INSERT INTO departments (id, name, location) VALUES
(1, '内科', '门诊楼2层A区'),
(2, '外科', '门诊楼3层B区'),
(3, '儿科', '门诊楼2层C区'),
(4, '妇产科', '住院楼5层'),
(5, '骨科', '住院楼6层');

INSERT INTO doctor_titles (id, name, consultation_fee) VALUES
(1, '主任医师', 80.00),
(2, '副主任医师', 60.00),
(3, '主治医师', 40.00),
(4, '住院医师', 25.00);

INSERT INTO doctors (id, user_id, department_id, title_id, employee_no, name, gender, phone) VALUES
(1, 2, 1, 1, 'D2026001', '张明', 'MALE', '13800010001'),
(2, 3, 1, 3, 'D2026002', '李敏', 'FEMALE', '13800010002'),
(3, 4, 2, 2, 'D2026003', '王强', 'MALE', '13800010003'),
(4, 5, 3, 3, 'D2026004', '陈静', 'FEMALE', '13800010004'),
(5, 6, 4, 1, 'D2026005', '刘芳', 'FEMALE', '13800010005'),
(6, 7, 5, 2, 'D2026006', '赵磊', 'MALE', '13800010006');

INSERT INTO patients (id, user_id, medical_record_no, name, gender, phone, address) VALUES
(1, 8, 'P20260001', '林浩', 'MALE', '13900020001', '广州市天河区华师路1号'),
(2, 9, 'P20260002', '黄丽', 'FEMALE', '13900020002', '广州市越秀区中山路18号'),
(3, 10, 'P20260003', '周杰', 'MALE', '13900020003', '广州市海珠区新港西路20号'),
(4, 11, 'P20260004', '吴婷', 'FEMALE', '13900020004', '广州市白云区机场路88号'),
(5, 12, 'P20260005', '徐乐', 'MALE', '13900020005', '广州市番禺区大学城外环路12号'),
(6, 13, 'P20260006', '何欣', 'FEMALE', '13900020006', '广州市黄埔区科学大道66号');

INSERT INTO medicines (id, code, name, specification, unit, unit_price, stock_quantity, status) VALUES
(1, 'M001', '阿莫西林胶囊', '0.25g*24粒', '盒', 18.50, 120, 'ACTIVE'),
(2, 'M002', '布洛芬片', '0.2g*20片', '盒', 12.00, 80, 'ACTIVE'),
(3, 'M003', '复方感冒灵颗粒', '10g*9袋', '盒', 16.80, 100, 'ACTIVE'),
(4, 'M004', '盐酸氨溴索口服液', '100ml', '瓶', 22.50, 60, 'ACTIVE'),
(5, 'M005', '头孢克肟分散片', '50mg*12片', '盒', 35.00, 45, 'ACTIVE'),
(6, 'M006', '葡萄糖注射液', '500ml', '瓶', 8.00, 200, 'ACTIVE'),
(7, 'M007', '云南白药气雾剂', '85g+30g', '盒', 68.00, 35, 'ACTIVE'),
(8, 'M008', '碳酸钙D3片', '60片', '瓶', 42.00, 50, 'ACTIVE');

INSERT INTO doctor_schedules (id, doctor_id, department_id, schedule_type, start_time, end_time, room) VALUES
(1, 1, 1, 'OUTPATIENT', '2026-05-15 08:00:00', '2026-05-15 12:00:00', '内科201'),
(2, 2, 1, 'OUTPATIENT', '2026-05-15 14:00:00', '2026-05-15 18:00:00', '内科202'),
(3, 3, 2, 'OUTPATIENT', '2026-05-15 08:00:00', '2026-05-15 12:00:00', '外科301'),
(4, 4, 3, 'OUTPATIENT', '2026-05-15 08:00:00', '2026-05-15 12:00:00', '儿科205'),
(5, 5, 4, 'INPATIENT_ROUND', '2026-05-15 09:00:00', '2026-05-15 11:00:00', '住院楼5层'),
(6, 6, 5, 'INPATIENT_ROUND', '2026-05-15 15:00:00', '2026-05-15 17:00:00', '住院楼6层'),
(7, 1, 1, 'OUTPATIENT', '2026-05-16 08:00:00', '2026-05-16 12:00:00', '内科201'),
(8, 3, 2, 'OUTPATIENT', '2026-05-16 14:00:00', '2026-05-16 18:00:00', '外科301');

INSERT INTO registrations (id, patient_id, doctor_id, department_id, schedule_id, visit_type, registered_at, status) VALUES
(1, 1, 1, 1, 1, 'FIRST', '2026-05-15 08:15:00', 'VISITED'),
(2, 2, 2, 1, 2, 'FOLLOW_UP', '2026-05-15 14:10:00', 'VISITED'),
(3, 3, 3, 2, 3, 'FIRST', '2026-05-15 08:35:00', 'VISITED'),
(4, 4, 4, 3, 4, 'FIRST', '2026-05-15 09:00:00', 'REGISTERED'),
(5, 5, 1, 1, 7, 'FOLLOW_UP', '2026-05-16 08:20:00', 'REGISTERED'),
(6, 6, 3, 2, 8, 'FIRST', '2026-05-16 14:30:00', 'CANCELLED');

INSERT INTO outpatient_visits (id, registration_id, patient_id, doctor_id, symptom_description, diagnosis, visited_at) VALUES
(1, 1, 1, 1, '发热、咳嗽两天，伴有咽痛。', '上呼吸道感染。', '2026-05-15 08:45:00'),
(2, 2, 2, 2, '胃部不适，饭后胀痛。', '慢性胃炎复诊，建议继续观察。', '2026-05-15 14:40:00'),
(3, 3, 3, 3, '右脚踝扭伤，局部肿胀。', '踝关节软组织损伤。', '2026-05-15 09:20:00');

INSERT INTO prescriptions (id, visit_id, doctor_id, patient_id, consultation_fee, medicine_amount, total_amount, status, created_at) VALUES
(1, 1, 1, 1, 80.00, 65.80, 145.80, 'PAID', '2026-05-15 08:55:00'),
(2, 2, 2, 2, 40.00, 22.50, 62.50, 'UNPAID', '2026-05-15 14:50:00'),
(3, 3, 3, 3, 60.00, 68.00, 128.00, 'PAID', '2026-05-15 09:30:00');

INSERT INTO prescription_items (id, prescription_id, medicine_id, medicine_name, unit_price, quantity, usage_instruction, amount) VALUES
(1, 1, 3, '复方感冒灵颗粒', 16.80, 2, '每日三次，每次一袋，温水冲服。', 33.60),
(2, 1, 2, '布洛芬片', 12.00, 1, '发热或疼痛时服用，每次一片。', 12.00),
(3, 1, 1, '阿莫西林胶囊', 18.50, 1, '每日三次，每次两粒。', 18.50),
(4, 2, 4, '盐酸氨溴索口服液', 22.50, 1, '每日三次，每次10ml。', 22.50),
(5, 3, 7, '云南白药气雾剂', 68.00, 1, '外用，每日两次，避免接触伤口。', 68.00);

INSERT INTO wards (id, department_id, ward_no, location, daily_charge) VALUES
(1, 1, 'N501', '住院楼5层东区', 120.00),
(2, 4, 'O502', '住院楼5层西区', 160.00),
(3, 5, 'B601', '住院楼6层东区', 150.00);

INSERT INTO beds (id, ward_id, bed_no, status) VALUES
(1, 1, '01', 'OCCUPIED'),
(2, 1, '02', 'AVAILABLE'),
(3, 1, '03', 'AVAILABLE'),
(4, 2, '01', 'OCCUPIED'),
(5, 2, '02', 'AVAILABLE'),
(6, 3, '01', 'OCCUPIED'),
(7, 3, '02', 'AVAILABLE'),
(8, 3, '03', 'MAINTENANCE');

INSERT INTO admissions (id, admission_no, patient_id, department_id, attending_doctor_id, bed_id, admitted_at, discharged_at, status) VALUES
(1, 'A20260001', 2, 1, 2, 1, '2026-05-13 10:00:00', NULL, 'ACTIVE'),
(2, 'A20260002', 4, 4, 5, 4, '2026-05-14 09:30:00', NULL, 'ACTIVE'),
(3, 'A20260003', 5, 5, 6, 6, '2026-05-10 16:20:00', '2026-05-15 10:00:00', 'DISCHARGED');

INSERT INTO inpatient_records (id, admission_id, record_date, condition_description, treatment_summary, treatment_fee, created_at) VALUES
(1, 1, '2026-05-14', '胃痛减轻，仍有轻微反酸。', '继续护胃治疗，观察饮食反应。', 45.00, '2026-05-14 09:20:00'),
(2, 1, '2026-05-15', '夜间睡眠改善，腹痛明显缓解。', '调整用药剂量，继续观察。', 40.00, '2026-05-15 09:10:00'),
(3, 2, '2026-05-15', '术前观察，生命体征平稳。', '常规护理，补液支持。', 80.00, '2026-05-15 10:30:00'),
(4, 3, '2026-05-14', '骨折术后恢复良好，疼痛可耐受。', '继续固定保护，补钙治疗。', 120.00, '2026-05-14 15:00:00');

INSERT INTO inpatient_record_items (id, inpatient_record_id, item_type, medicine_id, item_name, unit_price, quantity, usage_instruction, amount) VALUES
(1, 1, 'MEDICINE', 4, '盐酸氨溴索口服液', 22.50, 1.00, '每日三次，每次10ml。', 22.50),
(2, 1, 'TREATMENT', NULL, '住院常规护理', 22.50, 1.00, '每日护理。', 22.50),
(3, 2, 'TREATMENT', NULL, '内科查房诊疗', 40.00, 1.00, '主治医生查房。', 40.00),
(4, 3, 'MEDICINE', 6, '葡萄糖注射液', 8.00, 2.00, '静脉滴注。', 16.00),
(5, 3, 'TREATMENT', NULL, '术前护理', 64.00, 1.00, '术前观察与护理。', 64.00),
(6, 4, 'MEDICINE', 8, '碳酸钙D3片', 42.00, 1.00, '每日一次，每次一片。', 42.00),
(7, 4, 'TREATMENT', NULL, '骨科康复指导', 78.00, 1.00, '术后康复训练指导。', 78.00);

INSERT INTO bills (id, bill_no, patient_id, source_type, source_id, total_amount, status, created_at) VALUES
(1, 'B20260001', 1, 'OUTPATIENT', 1, 145.80, 'PAID', '2026-05-15 09:00:00'),
(2, 'B20260002', 2, 'OUTPATIENT', 2, 62.50, 'UNPAID', '2026-05-15 14:55:00'),
(3, 'B20260003', 3, 'OUTPATIENT', 3, 128.00, 'PAID', '2026-05-15 09:35:00'),
(4, 'B20260004', 2, 'INPATIENT', 1, 285.00, 'UNPAID', '2026-05-15 18:00:00'),
(5, 'B20260005', 4, 'INPATIENT', 2, 240.00, 'UNPAID', '2026-05-15 18:10:00'),
(6, 'B20260006', 5, 'INPATIENT', 3, 270.00, 'PAID', '2026-05-15 10:30:00');

INSERT INTO bill_items (id, bill_id, item_type, item_name, unit_price, quantity, amount) VALUES
(1, 1, 'CONSULTATION', '主任医师门诊诊疗费', 80.00, 1.00, 80.00),
(2, 1, 'MEDICINE', '门诊处方药品费', 65.80, 1.00, 65.80),
(3, 2, 'CONSULTATION', '主治医师门诊诊疗费', 40.00, 1.00, 40.00),
(4, 2, 'MEDICINE', '门诊处方药品费', 22.50, 1.00, 22.50),
(5, 3, 'CONSULTATION', '副主任医师门诊诊疗费', 60.00, 1.00, 60.00),
(6, 3, 'MEDICINE', '门诊处方药品费', 68.00, 1.00, 68.00),
(7, 4, 'BED', '内科病房床位费', 120.00, 2.00, 240.00),
(8, 4, 'TREATMENT', '住院治疗费', 45.00, 1.00, 45.00),
(9, 5, 'BED', '妇产科病房床位费', 160.00, 1.00, 160.00),
(10, 5, 'TREATMENT', '住院治疗费', 80.00, 1.00, 80.00),
(11, 6, 'BED', '骨科病房床位费', 150.00, 1.00, 150.00),
(12, 6, 'TREATMENT', '住院治疗费', 120.00, 1.00, 120.00);
