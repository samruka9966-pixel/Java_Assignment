import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * StudentDAO: Handles database operations for Students, Courses, Enrollments,
 * and Continuous Attendance Tracking.
 */
public class StudentDAO {

    // =========================================================================
    // 1. STUDENT MANAGEMENT
    // =========================================================================

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, department, semester) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getRollNo().trim().toUpperCase());
            pstmt.setString(2, student.getName().trim());
            pstmt.setString(3, student.getDepartment().trim().toUpperCase());
            pstmt.setInt(4, student.getSemester());
            return pstmt.executeUpdate() > 0;
        }
    }

    public Student getStudent(String rollNo) throws SQLException {
        String sql = "SELECT roll_no, name, department, semester FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim().toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    );
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, semester FROM students ORDER BY roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("semester")
                ));
            }
        }
        return list;
    }

    public List<Student> getStudentsByDepartment(String dept, int semester) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, semester FROM students WHERE department = ? AND semester = ? ORDER BY roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dept.trim().toUpperCase());
            pstmt.setInt(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    ));
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 2. COURSE CATALOG MANAGEMENT
    // =========================================================================

    public boolean addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, title, credits, course_type, " +
                "cia_weightage, ese_weightage, ese_min_threshold, pass_mark) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourseCode().trim().toUpperCase());
            pstmt.setString(2, course.getTitle().trim());
            pstmt.setDouble(3, course.getCredits());
            pstmt.setString(4, course.getCourseType().name());
            pstmt.setDouble(5, course.getCiaWeightage());
            pstmt.setDouble(6, course.getEseWeightage());
            pstmt.setDouble(7, course.getEseMinThreshold());
            pstmt.setDouble(8, course.getPassMark());
            return pstmt.executeUpdate() > 0;
        }
    }

    public Course getCourse(String courseCode) throws SQLException {
        String sql = "SELECT course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, pass_mark " +
                "FROM courses WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CourseType type = CourseType.fromString(rs.getString("course_type"));
                    return new Course(
                            rs.getString("course_code"),
                            rs.getString("title"),
                            rs.getDouble("credits"),
                            type,
                            rs.getDouble("cia_weightage"),
                            rs.getDouble("ese_weightage"),
                            rs.getDouble("ese_min_threshold"),
                            rs.getDouble("pass_mark")
                    );
                }
            }
        }
        return null;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, pass_mark " +
                "FROM courses ORDER BY course_code ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getDouble("credits"),
                        CourseType.fromString(rs.getString("course_type")),
                        rs.getDouble("cia_weightage"),
                        rs.getDouble("ese_weightage"),
                        rs.getDouble("ese_min_threshold"),
                        rs.getDouble("pass_mark")
                ));
            }
        }
        return list;
    }

    // =========================================================================
    // 3. COURSE ENROLLMENT & ATTENDANCE TRACKING
    // =========================================================================

    public int enrollStudent(String rollNo, String courseCode, int semester) throws SQLException {
        String checkSql = "SELECT enrollment_id FROM enrollments WHERE roll_no = ? AND course_code = ? AND semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, rollNo.trim().toUpperCase());
            checkStmt.setString(2, courseCode.trim().toUpperCase());
            checkStmt.setInt(3, semester);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id"); // Already enrolled
                }
            }
        }

        String insertSql = "INSERT INTO enrollments (roll_no, course_code, semester, classes_held, classes_attended, attendance_pct, condonation_approved, eligibility_status) " +
                "VALUES (?, ?, ?, 0, 0, 0.0, 0, 'ELIGIBLE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, rollNo.trim().toUpperCase());
            pstmt.setString(2, courseCode.trim().toUpperCase());
            pstmt.setInt(3, semester);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int enrollmentId = generatedKeys.getInt(1);
                    // Initialize empty record in assessment_marks
                    initAssessmentRecord(conn, enrollmentId);
                    return enrollmentId;
                }
            }
        }
        return -1;
    }

    private void initAssessmentRecord(Connection conn, int enrollmentId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO assessment_marks (enrollment_id, evaluation_phase, result_status) VALUES (?, 'NOT_STARTED', 'PENDING')";
        if ("MYSQL".equalsIgnoreCase(DBConnection.getDbType())) {
            sql = "INSERT IGNORE INTO assessment_marks (enrollment_id, evaluation_phase, result_status) VALUES (?, 'NOT_STARTED', 'PENDING')";
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates attendance continuously and recalculates KEC R2024 eligibility:
     * - >= 75%: ELIGIBLE
     * - 65% - 74.9%: CONDITIONAL (Requires Condonation)
     * - < 65%: BARRED_SA (Award SA Grade, Barred from ESE)
     */
    public boolean updateAttendance(int enrollmentId, int classesHeld, int classesAttended) throws SQLException {
        if (classesHeld < 0 || classesAttended < 0 || classesAttended > classesHeld) {
            throw new IllegalArgumentException("Invalid attendance counts: Held=" + classesHeld + ", Attended=" + classesAttended);
        }

        Enrollment enrollment = getEnrollment(enrollmentId);
        if (enrollment == null) return false;

        enrollment.setClassesHeld(classesHeld);
        enrollment.setClassesAttended(classesAttended);
        enrollment.recalculateAttendance();

        String sql = "UPDATE enrollments SET classes_held = ?, classes_attended = ?, attendance_pct = ?, eligibility_status = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classesHeld);
            pstmt.setInt(2, classesAttended);
            pstmt.setDouble(3, enrollment.getAttendancePct());
            pstmt.setString(4, enrollment.getEligibilityStatus());
            pstmt.setInt(5, enrollmentId);
            int rows = pstmt.executeUpdate();

            // If attendance is < 65%, immediately flag SA in assessment_marks
            if (enrollment.getAttendancePct() < 65.0) {
                applySAGrade(conn, enrollmentId);
            }

            return rows > 0;
        }
    }

    /**
     * Approves Medical Condonation for students in the 65% - 74.9% bracket.
     */
    public boolean approveMedicalCondonation(int enrollmentId, boolean approved) throws SQLException {
        Enrollment enrollment = getEnrollment(enrollmentId);
        if (enrollment == null) return false;

        enrollment.setCondonationApproved(approved);
        enrollment.recalculateAttendance();

        String sql = "UPDATE enrollments SET condonation_approved = ?, eligibility_status = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, approved ? 1 : 0);
            pstmt.setString(2, enrollment.getEligibilityStatus());
            pstmt.setInt(3, enrollmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private void applySAGrade(Connection conn, int enrollmentId) throws SQLException {
        String sql = "UPDATE assessment_marks SET grade = 'SA', grade_point = 0, result_status = 'SA', evaluation_phase = 'EVALUATED_FINAL' WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.executeUpdate();
        }
    }

    public Enrollment getEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, " +
                "e.attendance_pct, e.condonation_approved, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        }
        return null;
    }

    public Enrollment getEnrollmentByRollAndCourse(String rollNo, String courseCode, int semester) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, " +
                "e.attendance_pct, e.condonation_approved, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.roll_no = ? AND e.course_code = ? AND e.semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim().toUpperCase());
            pstmt.setString(2, courseCode.trim().toUpperCase());
            pstmt.setInt(3, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        }
        return null;
    }

    public List<Enrollment> getEnrollmentsForStudent(String rollNo, int semester) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, " +
                "e.attendance_pct, e.condonation_approved, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.roll_no = ? AND e.semester = ? ORDER BY e.course_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim().toUpperCase());
            pstmt.setInt(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        }
        return list;
    }

    public List<Enrollment> getAttendanceDefaulters() throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, " +
                "e.attendance_pct, e.condonation_approved, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.attendance_pct < 75.0 AND e.classes_held > 0 " +
                "ORDER BY e.attendance_pct ASC, e.roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEnrollment(rs));
            }
        }
        return list;
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setRollNo(rs.getString("roll_no"));
        e.setCourseCode(rs.getString("course_code"));
        e.setSemester(rs.getInt("semester"));
        e.setClassesHeld(rs.getInt("classes_held"));
        e.setClassesAttended(rs.getInt("classes_attended"));
        e.setAttendancePct(rs.getDouble("attendance_pct"));
        e.setCondonationApproved(rs.getInt("condonation_approved") == 1);
        e.setEligibilityStatus(rs.getString("eligibility_status"));
        e.setStudentName(rs.getString("student_name"));
        e.setDepartment(rs.getString("department"));
        e.setCourseTitle(rs.getString("course_title"));
        e.setCourseType(CourseType.fromString(rs.getString("course_type")));
        e.setCredits(rs.getDouble("credits"));
        return e;
    }
}
