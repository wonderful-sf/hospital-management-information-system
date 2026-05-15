CREATE DATABASE IF NOT EXISTS hospital DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital;

DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS inpatient_record_items;
DROP TABLE IF EXISTS inpatient_records;
DROP TABLE IF EXISTS prepaid_records;
DROP TABLE IF EXISTS admissions;
DROP TABLE IF EXISTS beds;
DROP TABLE IF EXISTS wards;
DROP TABLE IF EXISTS prescription_items;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS outpatient_visits;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS doctor_schedules;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS doctor_titles;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'DOCTOR', 'PATIENT') NOT NULL,
    permissions VARCHAR(500) NOT NULL DEFAULT '[]',
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doctor_titles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    consultation_fee DECIMAL(10, 2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    department_id BIGINT NOT NULL,
    title_id BIGINT NOT NULL,
    employee_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    phone VARCHAR(30),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctors_department_id (department_id),
    INDEX idx_doctors_title_id (title_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    medical_record_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    specification VARCHAR(100),
    unit VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_medicines_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doctor_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    schedule_type ENUM('OUTPATIENT', 'INPATIENT_ROUND') NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    room VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doctor_schedules_doctor_time (doctor_id, start_time, end_time),
    INDEX idx_doctor_schedules_department_time (department_id, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE registrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    schedule_id BIGINT,
    visit_type ENUM('FIRST', 'FOLLOW_UP') NOT NULL,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('REGISTERED', 'VISITED', 'CANCELLED') NOT NULL DEFAULT 'REGISTERED',
    INDEX idx_registrations_patient_time (patient_id, registered_at),
    INDEX idx_registrations_doctor_time (doctor_id, registered_at),
    INDEX idx_registrations_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE outpatient_visits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_id BIGINT NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    symptom_description TEXT,
    diagnosis TEXT,
    visited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_outpatient_visits_patient_time (patient_id, visited_at),
    INDEX idx_outpatient_visits_doctor_time (doctor_id, visited_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visit_id BIGINT NOT NULL UNIQUE,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    consultation_fee DECIMAL(10, 2) NOT NULL,
    medicine_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('UNPAID', 'PAID') NOT NULL DEFAULT 'UNPAID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prescriptions_patient_id (patient_id),
    INDEX idx_prescriptions_doctor_id (doctor_id),
    INDEX idx_prescriptions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescription_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    medicine_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    usage_instruction VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    INDEX idx_prescription_items_prescription_id (prescription_id),
    INDEX idx_prescription_items_medicine_id (medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    ward_no VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    daily_charge DECIMAL(10, 2) NOT NULL,
    INDEX idx_wards_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE beds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ward_id BIGINT NOT NULL,
    bed_no VARCHAR(50) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    UNIQUE KEY uk_beds_ward_bed_no (ward_id, bed_no),
    INDEX idx_beds_ward_id (ward_id),
    INDEX idx_beds_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admission_no VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    attending_doctor_id BIGINT NOT NULL,
    bed_id BIGINT DEFAULT NULL,
    admitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discharged_at DATETIME,
    discharge_type ENUM('NORMAL', 'AGAINST_ADVICE', 'TRANSFERRED'),
    prepaid_balance DECIMAL(10, 2) NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'DISCHARGED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    INDEX idx_admissions_patient_time (patient_id, admitted_at),
    INDEX idx_admissions_doctor_status (attending_doctor_id, status),
    INDEX idx_admissions_bed_status (bed_id, status),
    INDEX idx_admissions_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inpatient_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admission_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    condition_description TEXT NOT NULL,
    treatment_summary TEXT NOT NULL,
    treatment_fee DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inpatient_records_admission_date (admission_id, record_date),
    INDEX idx_inpatient_records_admission_id (admission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inpatient_record_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inpatient_record_id BIGINT NOT NULL,
    item_type ENUM('MEDICINE', 'TREATMENT') NOT NULL,
    medicine_id BIGINT,
    item_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    usage_instruction VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    INDEX idx_inpatient_record_items_record_id (inpatient_record_id),
    INDEX idx_inpatient_record_items_medicine_id (medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_no VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    source_type ENUM('OUTPATIENT', 'INPATIENT') NOT NULL,
    source_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('UNPAID', 'PAID') NOT NULL DEFAULT 'UNPAID',
    paid_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bills_patient_time (patient_id, created_at),
    INDEX idx_bills_source (source_type, source_id),
    INDEX idx_bills_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bill_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_id BIGINT NOT NULL,
    item_type ENUM('CONSULTATION', 'MEDICINE', 'BED', 'TREATMENT') NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    INDEX idx_bill_items_bill_id (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    bill_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'ONLINE') NOT NULL,
    paid_at DATETIME NOT NULL,
    INDEX idx_payments_bill_id (bill_id),
    INDEX idx_payments_patient_time (patient_id, paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prepaid_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_no VARCHAR(50) NOT NULL UNIQUE,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type ENUM('DEPOSIT', 'DEDUCTION', 'REFUND') NOT NULL,
    balance_after DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prepaid_records_admission_id (admission_id),
    INDEX idx_prepaid_records_patient_time (patient_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
