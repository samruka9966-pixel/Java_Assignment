-- ============================================================================
-- KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE - 638060
-- ACADEMIC REGULATION 2024 (R2024)
-- DATABASE SCHEMA SCRIPT: kec_schema.sql
-- Compatible with SQLite 3 & MySQL 8.x
-- ============================================================================

-- Drop existing tables (in reverse dependency order)
DROP TABLE IF EXISTS assessment_marks;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;

-- ----------------------------------------------------------------------------
-- 1. STUDENTS TABLE
-- Stores student profile information including Roll Number, Name, Department,
-- and Current Semester.
-- ----------------------------------------------------------------------------
CREATE TABLE students (
    roll_no VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. COURSES TABLE
-- Stores course catalog with course code, title, credits, course type,
-- and regulation-mandated weightage thresholds.
-- Course Types: THEORY, PRACTICAL, EMBEDDED
-- ----------------------------------------------------------------------------
CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    credits DECIMAL(3, 1) NOT NULL CHECK (credits > 0),
    course_type VARCHAR(20) NOT NULL CHECK (course_type IN ('THEORY', 'PRACTICAL', 'EMBEDDED')),
    cia_weightage DECIMAL(5, 2) NOT NULL,       -- Theory: 40, Practical: 60, Embedded: 50
    ese_weightage DECIMAL(5, 2) NOT NULL,       -- Theory: 60, Practical: 40, Embedded: 50
    ese_min_threshold DECIMAL(5, 2) NOT NULL,   -- Theory: 27 (45% of 60), Practical: 20 (50% of 40), Embedded: 22.5 (45% of 50)
    pass_mark DECIMAL(5, 2) NOT NULL DEFAULT 50.0
);

-- ----------------------------------------------------------------------------
-- 3. ENROLLMENTS TABLE
-- Maps students to registered courses per semester and tracks continuous
-- attendance (classes held vs. classes attended).
-- Enforces Attendance Filter:
--   >= 75%: Eligible
--   65% - 74.9%: Conditionally eligible (Medical Condonation required)
--   < 65%: Shortage of Attendance (SA - Barred from ESE)
-- ----------------------------------------------------------------------------
CREATE TABLE enrollments (
    enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    roll_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    classes_held INT NOT NULL DEFAULT 0 CHECK (classes_held >= 0),
    classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0),
    attendance_pct DECIMAL(5, 2) DEFAULT 0.00,
    condonation_approved INT DEFAULT 0, -- 1 = Approved medical condonation
    eligibility_status VARCHAR(25) DEFAULT 'ELIGIBLE', -- ELIGIBLE, CONDITIONAL, BARRED_SA
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE (roll_no, course_code, semester)
);

-- ----------------------------------------------------------------------------
-- 4. ASSESSMENT_MARKS TABLE
-- Supports phased incremental marks entry:
--   Phase 1 (Mid-Semester): CIA Test 1 (0-100), CIA Test 2 (0-100)
--   Phase 2 (End-of-Semester CIA): Assignments / Quizzes / Lab Evaluations (0-100)
--   Phase 3 (Post-Semester): End Semester Examination (ESE 0-100) & Absence (AB)
-- Stores computed grades, grade points, and final pass/fail results.
-- ----------------------------------------------------------------------------
CREATE TABLE assessment_marks (
    mark_id INTEGER PRIMARY KEY AUTOINCREMENT,
    enrollment_id INT NOT NULL UNIQUE,
    -- Phase 1: Mid-Semester Tests
    cia1_marks DECIMAL(5, 2) DEFAULT NULL CHECK (cia1_marks IS NULL OR (cia1_marks BETWEEN 0 AND 100)),
    cia2_marks DECIMAL(5, 2) DEFAULT NULL CHECK (cia2_marks IS NULL OR (cia2_marks BETWEEN 0 AND 100)),
    -- Phase 2: End-of-Semester CIA
    assignment_marks DECIMAL(5, 2) DEFAULT NULL CHECK (assignment_marks IS NULL OR (assignment_marks BETWEEN 0 AND 100)),
    cia_raw_100 DECIMAL(5, 2) DEFAULT NULL,      -- Tests Avg (60%) + Assignment (40%)
    scaled_cia DECIMAL(5, 2) DEFAULT NULL,       -- Scaled to course CIA weightage
    -- Phase 3: End Semester Examination
    is_absent_ese INT DEFAULT 0,                 -- 1 if Absent
    ese_raw_marks DECIMAL(5, 2) DEFAULT NULL CHECK (ese_raw_marks IS NULL OR (ese_raw_marks BETWEEN 0 AND 100)),
    scaled_ese DECIMAL(5, 2) DEFAULT NULL,       -- Scaled to course ESE weightage
    -- Final Results & 10-Point Grading
    total_marks DECIMAL(5, 2) DEFAULT NULL,      -- Scaled CIA + Scaled ESE
    grade VARCHAR(5) DEFAULT NULL,               -- O, A+, A, B+, B, RA, SA, AB
    grade_point INT DEFAULT NULL,                -- 10, 9, 8, 7, 6, 0
    result_status VARCHAR(15) DEFAULT 'PENDING', -- PASS, FAIL, SA, AB, PENDING
    evaluation_phase VARCHAR(25) DEFAULT 'NOT_STARTED', -- PHASE_1_ENTERED, PHASE_2_ENTERED, EVALUATED_FINAL
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

-- Indices for rapid querying
CREATE INDEX idx_student_dept ON students(department, semester);
CREATE INDEX idx_enroll_student ON enrollments(roll_no, semester);
CREATE INDEX idx_enroll_course ON enrollments(course_code);
CREATE INDEX idx_marks_enroll ON assessment_marks(enrollment_id);

-- ----------------------------------------------------------------------------
-- INITIAL SAMPLE DATA SEEDING (KEC R2024 Curriculum Sample)
-- ----------------------------------------------------------------------------

-- Sample Courses for IT & CSE Semester 3 (R2024)
INSERT INTO courses (course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, pass_mark) VALUES
('24ITR31', 'Object Oriented Programming using Java', 3.0, 'THEORY', 40.0, 60.0, 27.0, 50.0),
('24ITR32', 'Data Structures and Algorithms', 4.0, 'EMBEDDED', 50.0, 50.0, 22.5, 50.0),
('24ITR33', 'Database Management Systems', 3.0, 'THEORY', 40.0, 60.0, 27.0, 50.0),
('24ITL31', 'Java Programming Laboratory', 1.5, 'PRACTICAL', 60.0, 40.0, 20.0, 50.0),
('24ITL32', 'Data Structures Laboratory', 1.5, 'PRACTICAL', 60.0, 40.0, 20.0, 50.0);

-- Sample Students
INSERT INTO students (roll_no, name, department, semester) VALUES
('24ITR001', 'Aadhithya K', 'IT', 3),
('24ITR002', 'Bavani S', 'IT', 3),
('24ITR003', 'Chandran M', 'IT', 3),
('24ITR004', 'Deepika R', 'IT', 3),
('24CSR015', 'Gokulnath P', 'CSE', 3);
