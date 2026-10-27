import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * AcademicDAO: Assessment Engine, Incremental Marks Processing,
 * Attendance Enforcement, and Formal Academic Performance Reports.
 */
public class AcademicDAO {

    private final StudentDAO studentDAO = new StudentDAO();

    // =========================================================================
    // 1. INCREMENTAL MARKS ENTRY ("Then and Now")
    // =========================================================================

    /**
     * Phase 1 (Mid-Semester):
     * Record CIA Test 1 (0-100) and CIA Test 2 (0-100).
     */
    public boolean recordPhase1Marks(int enrollmentId, double cia1, double cia2) throws SQLException {
        if (cia1 < 0.0 || cia1 > 100.0 || cia2 < 0.0 || cia2 > 100.0) {
            throw new IllegalArgumentException("CIA Test marks must be between 0 and 100. Entered: CIA1=" + cia1 + ", CIA2=" + cia2);
        }

        Enrollment enrollment = studentDAO.getEnrollment(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment ID #" + enrollmentId + " does not exist.");
        }

        String sql = "UPDATE assessment_marks SET cia1_marks = ?, cia2_marks = ?, evaluation_phase = 'PHASE_1_ENTERED' " +
                "WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, Math.round(cia1 * 100.0) / 100.0);
            pstmt.setDouble(2, Math.round(cia2 * 100.0) / 100.0);
            pstmt.setInt(3, enrollmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Phase 2 (End of Semester - CIA):
     * Record Assignments / Quizzes / Lab Evaluations (0-100) and compute scaled CIA.
     */
    public boolean recordPhase2Marks(int enrollmentId, double assignmentMarks) throws SQLException {
        if (assignmentMarks < 0.0 || assignmentMarks > 100.0) {
            throw new IllegalArgumentException("Assignment/Quiz marks must be between 0 and 100. Entered: " + assignmentMarks);
        }

        AssessmentMark existing = getAssessmentMark(enrollmentId);
        if (existing == null || existing.getCia1Marks() == null || existing.getCia2Marks() == null) {
            throw new IllegalStateException("Phase 1 marks (CIA 1 & CIA 2) must be recorded before Phase 2 CIA entry.");
        }

        Enrollment enrollment = studentDAO.getEnrollment(enrollmentId);
        Course course = studentDAO.getCourse(enrollment.getCourseCode());

        // KEC R2024 CIA Formula: Tests Avg 60% + Assignment/Lab 40%
        double testAvg = (existing.getCia1Marks() + existing.getCia2Marks()) / 2.0;
        double ciaRaw100 = (testAvg * 0.60) + (assignmentMarks * 0.40);
        ciaRaw100 = Math.round(ciaRaw100 * 100.0) / 100.0;

        // Scaled to Course Type Weightage:
        // Theory: 40%, Practical: 60%, Embedded: 50%
        double scaledCia = Math.round((ciaRaw100 * (course.getCiaWeightage() / 100.0)) * 100.0) / 100.0;

        String sql = "UPDATE assessment_marks SET assignment_marks = ?, cia_raw_100 = ?, scaled_cia = ?, " +
                "evaluation_phase = 'PHASE_2_ENTERED' WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, Math.round(assignmentMarks * 100.0) / 100.0);
            pstmt.setDouble(2, ciaRaw100);
            pstmt.setDouble(3, scaledCia);
            pstmt.setInt(4, enrollmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Phase 3 (Post Semester):
     * Record End Semester Examination (ESE) Marks (0-100) or Mark Absence (AB).
     * Enforces KEC R2024 Attendance Filter & 10-Point Grading System.
     */
    public AssessmentMark recordPhase3Marks(int enrollmentId, Double eseRawMarks, boolean isAbsent) throws SQLException {
        Enrollment enrollment = studentDAO.getEnrollment(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment ID #" + enrollmentId + " does not exist.");
        }

        Course course = studentDAO.getCourse(enrollment.getCourseCode());
        AssessmentMark mark = getAssessmentMark(enrollmentId);
        if (mark == null || mark.getScaledCia() == null) {
            throw new IllegalStateException("Cannot enter ESE marks: Phase 2 (Continuous Internal Assessment) has not been completed.");
        }

        // ---------------------------------------------------------------------
        // ATTENDANCE FILTER ENFORCEMENT (KEC R2024)
        // ---------------------------------------------------------------------
        enrollment.recalculateAttendance();
        double attPct = enrollment.getAttendancePct();

        if (attPct < 65.0) {
            // Shortage of Attendance: Barred from ESE, Grade SA, Grade Point 0
            return applyShortageOfAttendance(enrollmentId, mark);
        }

        if (attPct < 75.0 && !enrollment.isCondonationApproved()) {
            throw new IllegalStateException(String.format(
                    "Student attendance is %.2f%% (65%% - 74.9%%). Requires Medical Condonation approval before appearing for ESE.",
                    attPct));
        }

        // ---------------------------------------------------------------------
        // ABSENCE / REGULAR MARKS EVALUATION
        // ---------------------------------------------------------------------
        double scaledCia = mark.getScaledCia();
        double scaledEse = 0.0;
        double totalMarks = 0.0;
        String grade;
        int gradePoint;
        String resultStatus;

        if (isAbsent) {
            scaledEse = 0.0;
            totalMarks = scaledCia;
            grade = "AB";
            gradePoint = 0;
            resultStatus = "AB";
        } else {
            if (eseRawMarks == null || eseRawMarks < 0.0 || eseRawMarks > 100.0) {
                throw new IllegalArgumentException("ESE marks must be between 0 and 100. Entered: " + eseRawMarks);
            }

            scaledEse = Math.round((eseRawMarks * (course.getEseWeightage() / 100.0)) * 100.0) / 100.0;
            totalMarks = Math.round((scaledCia + scaledEse) * 100.0) / 100.0;

            // KEC R2024 Passing Criteria:
            // 1. Scaled ESE must meet course threshold (Theory: 27/60, Practical: 20/40, Embedded: 22.5/50)
            // 2. Overall aggregate marks must be >= 50.0
            if (scaledEse < course.getEseMinThreshold()) {
                grade = "RA";
                gradePoint = 0;
                resultStatus = "FAIL";
            } else if (totalMarks < course.getPassMark()) {
                grade = "RA";
                gradePoint = 0;
                resultStatus = "FAIL";
            } else {
                // 10-Point Grading System
                // O (91 - 100) -> 10
                // A+ (81 - 90) -> 9
                // A (71 - 80) -> 8
                // B+ (61 - 70) -> 7
                // B (50 - 60) -> 6
                long roundedTotal = Math.round(totalMarks);
                if (roundedTotal >= 91) {
                    grade = "O";
                    gradePoint = 10;
                } else if (roundedTotal >= 81) {
                    grade = "A+";
                    gradePoint = 9;
                } else if (roundedTotal >= 71) {
                    grade = "A";
                    gradePoint = 8;
                } else if (roundedTotal >= 61) {
                    grade = "B+";
                    gradePoint = 7;
                } else {
                    grade = "B";
                    gradePoint = 6;
                }
                resultStatus = "PASS";
            }
        }

        String sql = "UPDATE assessment_marks SET is_absent_ese = ?, ese_raw_marks = ?, scaled_ese = ?, " +
                "total_marks = ?, grade = ?, grade_point = ?, result_status = ?, evaluation_phase = 'EVALUATED_FINAL' " +
                "WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isAbsent ? 1 : 0);
            if (isAbsent || eseRawMarks == null) {
                pstmt.setNull(2, java.sql.Types.DECIMAL);
            } else {
                pstmt.setDouble(2, eseRawMarks);
            }
            pstmt.setDouble(3, scaledEse);
            pstmt.setDouble(4, totalMarks);
            pstmt.setString(5, grade);
            pstmt.setInt(6, gradePoint);
            pstmt.setString(7, resultStatus);
            pstmt.setInt(8, enrollmentId);
            pstmt.executeUpdate();
        }

        return getAssessmentMark(enrollmentId);
    }

    private AssessmentMark applyShortageOfAttendance(int enrollmentId, AssessmentMark mark) throws SQLException {
        String sql = "UPDATE assessment_marks SET is_absent_ese = 0, ese_raw_marks = 0.0, scaled_ese = 0.0, " +
                "total_marks = COALESCE(scaled_cia, 0.0), grade = 'SA', grade_point = 0, result_status = 'SA', " +
                "evaluation_phase = 'EVALUATED_FINAL' WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.executeUpdate();
        }
        return getAssessmentMark(enrollmentId);
    }

    // =========================================================================
    // 2. QUERY METHODS FOR ASSESSMENT MARKS
    // =========================================================================

    public AssessmentMark getAssessmentMark(int enrollmentId) throws SQLException {
        String sql = "SELECT m.*, e.roll_no, e.course_code, e.attendance_pct, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM assessment_marks m " +
                "JOIN enrollments e ON m.enrollment_id = e.enrollment_id " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE m.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAssessmentMark(rs);
                }
            }
        }
        return null;
    }

    public List<AssessmentMark> getAssessmentMarksForStudent(String rollNo, int semester) throws SQLException {
        List<AssessmentMark> list = new ArrayList<>();
        String sql = "SELECT m.*, e.roll_no, e.course_code, e.attendance_pct, e.eligibility_status, " +
                "s.name AS student_name, s.department, c.title AS course_title, c.course_type, c.credits " +
                "FROM assessment_marks m " +
                "JOIN enrollments e ON m.enrollment_id = e.enrollment_id " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.roll_no = ? AND e.semester = ? " +
                "ORDER BY e.course_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim().toUpperCase());
            pstmt.setInt(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAssessmentMark(rs));
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 3. EXPECTED REPORTING OUTPUTS
    // =========================================================================

    /**
     * Report 1: Student Semester Mark Sheet
     * Displays CIA, ESE, Total Marks, Grade, Grade Point, Result, SGPA, and Equivalent %.
     */
    public String generateStudentSemesterMarkSheet(String rollNo, int semester) throws SQLException {
        Student student = studentDAO.getStudent(rollNo);
        if (student == null) {
            return "Error: Student with Roll Number '" + rollNo + "' not found.";
        }

        List<AssessmentMark> marks = getAssessmentMarksForStudent(rollNo, semester);
        if (marks.isEmpty()) {
            return "No enrolled courses found for Student: " + student.getName() + " (" + rollNo + ") in Semester " + semester;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================================\n");
        sb.append("                                KONGU ENGINEERING COLLEGE (AUTONOMOUS)                                              \n");
        sb.append("                                     PERUNDURAI, ERODE - 638060                                                     \n");
        sb.append("                             OFFICE OF THE CONTROLLER OF EXAMINATIONS                                               \n");
        sb.append("                            SEMESTER GRADE SHEET - REGULATION 2024 (R2024)                                          \n");
        sb.append("====================================================================================================================\n");
        sb.append(String.format(" Roll Number : %-20s  Student Name : %-30s\n", student.getRollNo(), student.getName()));
        sb.append(String.format(" Department  : %-20s  Semester     : %d\n", student.getDepartment(), semester));
        sb.append("--------------------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-9s | %-28s | %-9s | %-4s | %-5s | %-6s | %-6s | %-5s | %-5s | %-3s | %-6s \n",
                "Course", "Course Title", "Type", "Cred", "Att%", "CIA", "ESE", "Total", "Grade", "GP", "Result"));
        sb.append("--------------------------------------------------------------------------------------------------------------------\n");

        double totalCreditsEnrolled = 0.0;
        double totalCreditsEarned = 0.0;
        double totalWeightedGradePoints = 0.0;
        boolean allPassed = true;
        boolean allEvaluated = true;

        for (AssessmentMark m : marks) {
            Course course = studentDAO.getCourse(m.getCourseCode());
            double cred = m.getCredits();
            totalCreditsEnrolled += cred;

            String ciaStr = m.getScaledCia() != null ? String.format("%.1f/%.0f", m.getScaledCia(), course.getCiaWeightage()) : "--";
            String eseStr;
            if (m.isAbsentEse()) {
                eseStr = "AB";
            } else if (m.getScaledEse() != null) {
                eseStr = String.format("%.1f/%.0f", m.getScaledEse(), course.getEseWeightage());
            } else {
                eseStr = "--";
            }

            String totStr = m.getTotalMarks() != null ? String.format("%.1f", m.getTotalMarks()) : "--";
            String gradeStr = m.getGrade() != null ? m.getGrade() : "PENDING";
            String gpStr = m.getGradePoint() != null ? String.valueOf(m.getGradePoint()) : "--";
            String resStr = m.getResultStatus() != null ? m.getResultStatus() : "PENDING";

            if ("PASS".equalsIgnoreCase(m.getResultStatus())) {
                totalCreditsEarned += cred;
            } else {
                allPassed = false;
            }

            if (!"EVALUATED_FINAL".equalsIgnoreCase(m.getEvaluationPhase())) {
                allEvaluated = false;
            }

            if (m.getGradePoint() != null) {
                totalWeightedGradePoints += (cred * m.getGradePoint());
            }

            sb.append(String.format(" %-9s | %-28.28s | %-9s | %4.1f | %5.1f | %-6s | %-6s | %5s | %-5s | %3s | %-6s \n",
                    m.getCourseCode(),
                    m.getCourseTitle(),
                    m.getCourseType().name(),
                    cred,
                    m.getAttendancePct(),
                    ciaStr,
                    eseStr,
                    totStr,
                    gradeStr,
                    gpStr,
                    resStr));
        }

        sb.append("--------------------------------------------------------------------------------------------------------------------\n");

        double sgpa = (totalCreditsEnrolled > 0) ? (totalWeightedGradePoints / totalCreditsEnrolled) : 0.0;
        sgpa = Math.round(sgpa * 100.0) / 100.0;
        double percentage = Math.round(sgpa * 10.0 * 100.0) / 100.0;

        String standing;
        if (!allEvaluated) {
            standing = "EVALUATION IN PROGRESS";
        } else if (allPassed) {
            if (sgpa >= 8.5) standing = "FIRST CLASS WITH DISTINCTION";
            else if (sgpa >= 6.5) standing = "FIRST CLASS";
            else standing = "SECOND CLASS";
        } else {
            standing = "RE-APPEARANCE / SHORTAGE REQUIRED";
        }

        sb.append(String.format(" Total Credits Registered : %-6.1f       Total Credits Earned : %-6.1f\n", totalCreditsEnrolled, totalCreditsEarned));
        sb.append(String.format(" SGPA (R2024 Formula)     : %-6.2f       Equivalent Percentage: %.2f %%\n", sgpa, percentage));
        sb.append(String.format(" Academic Standing        : %s\n", standing));
        sb.append("====================================================================================================================\n");
        sb.append(" * Grading Legend: O (91-100: 10 GP) | A+ (81-90: 9 GP) | A (71-80: 8 GP) | B+ (61-70: 7 GP) | B (50-60: 6 GP)\n");
        sb.append("   RA: Re-Appearance (0 GP) | SA: Shortage of Attendance (0 GP) | AB: Absent (0 GP)\n");
        sb.append("====================================================================================================================\n");

        return sb.toString();
    }

    /**
     * Report 2: Attendance Defaulter Report
     * Lists students with attendance < 65% (Barred - SA) and 65%-74.9% (Conditional).
     */
    public String generateAttendanceDefaulterReport() throws SQLException {
        List<Enrollment> defaulters = studentDAO.getAttendanceDefaulters();

        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================================\n");
        sb.append("                                KONGU ENGINEERING COLLEGE (AUTONOMOUS)                                              \n");
        sb.append("                              ATTENDANCE DEFAULTER & ELIGIBILITY REPORT                                             \n");
        sb.append("                                        REGULATION 2024 (R2024)                                                     \n");
        sb.append("====================================================================================================================\n");
        sb.append(String.format(" %-10s | %-20s | %-5s | %-9s | %-6s | %-8s | %-6s | %-12s | %-18s \n",
                "Roll No", "Student Name", "Dept", "Course", "Held", "Attended", "Att%", "Condonation", "Eligibility Status"));
        sb.append("--------------------------------------------------------------------------------------------------------------------\n");

        if (defaulters.isEmpty()) {
            sb.append(" No attendance defaulters found. All students meet the >= 75% attendance criterion.\n");
        } else {
            int saCount = 0;
            int condCount = 0;
            for (Enrollment e : defaulters) {
                String condStr = e.isCondonationApproved() ? "APPROVED" : "NOT_APPROVED";
                String statusStr = e.getAttendancePct() < 65.0 ? "BARRED (GRADE SA)" : (e.isCondonationApproved() ? "CONDONED (ELIGIBLE)" : "CONDITIONAL (PENDING)");
                if (e.getAttendancePct() < 65.0) saCount++;
                else condCount++;

                sb.append(String.format(" %-10s | %-20.20s | %-5s | %-9s | %-6d | %-8d | %5.1f%% | %-12s | %-18s \n",
                        e.getRollNo(), e.getStudentName(), e.getDepartment(), e.getCourseCode(),
                        e.getClassesHeld(), e.getClassesAttended(), e.getAttendancePct(), condStr, statusStr));
            }
            sb.append("--------------------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format(" Summary: Total Defaulters = %d | Barred from ESE (Grade SA) = %d | Conditional Medical Condonation = %d\n",
                    defaulters.size(), saCount, condCount));
        }
        sb.append("====================================================================================================================\n");
        return sb.toString();
    }

    /**
     * Report 3: Class Performance & Result Summary
     * Department-wise pass/fail counts, course pass percentages, and class average SGPA.
     */
    public String generateClassPerformanceSummary(String department, int semester) throws SQLException {
        List<Student> students = studentDAO.getStudentsByDepartment(department, semester);
        if (students.isEmpty()) {
            return "No students found for Department: " + department + ", Semester: " + semester;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================================\n");
        sb.append(String.format("                     KONGU ENGINEERING COLLEGE (AUTONOMOUS) - R2024 RESULT SUMMARY\n"));
        sb.append(String.format("                                 DEPARTMENT: %-6s | SEMESTER: %d\n", department.toUpperCase(), semester));
        sb.append("====================================================================================================================\n");
        sb.append(String.format(" %-10s | %-22s | %-8s | %-7s | %-6s | %-8s | %-12s \n",
                "Roll No", "Student Name", "Enrolled", "Passed", "Failed", "SGPA", "Overall Result"));
        sb.append("--------------------------------------------------------------------------------------------------------------------\n");

        int totalClassStudents = students.size();
        int totalPassedAll = 0;
        int totalWithFailures = 0;
        double sumSGPA = 0.0;
        int evaluatedStudents = 0;

        Map<String, int[]> coursePassMap = new HashMap<>(); // courseCode -> [passed, total]

        for (Student s : students) {
            List<AssessmentMark> marks = getAssessmentMarksForStudent(s.getRollNo(), semester);
            if (marks.isEmpty()) continue;

            int courseCount = marks.size();
            int passedCount = 0;
            int failedCount = 0;
            double studentPoints = 0.0;
            double studentCredits = 0.0;
            boolean anyUnevaluated = false;

            for (AssessmentMark m : marks) {
                studentCredits += m.getCredits();
                if (m.getGradePoint() != null) {
                    studentPoints += (m.getCredits() * m.getGradePoint());
                }

                int[] stat = coursePassMap.computeIfAbsent(m.getCourseCode(), k -> new int[2]);
                stat[1]++; // Total enrolled

                if ("PASS".equalsIgnoreCase(m.getResultStatus())) {
                    passedCount++;
                    stat[0]++; // Passed count
                } else if ("FAIL".equalsIgnoreCase(m.getResultStatus()) || "SA".equalsIgnoreCase(m.getResultStatus()) || "AB".equalsIgnoreCase(m.getResultStatus())) {
                    failedCount++;
                } else {
                    anyUnevaluated = true;
                }
            }

            double studentSGPA = (studentCredits > 0) ? (studentPoints / studentCredits) : 0.0;
            studentSGPA = Math.round(studentSGPA * 100.0) / 100.0;

            String resultLabel;
            if (anyUnevaluated) {
                resultLabel = "IN PROGRESS";
            } else if (failedCount == 0) {
                resultLabel = "ALL CLEAR";
                totalPassedAll++;
                evaluatedStudents++;
                sumSGPA += studentSGPA;
            } else {
                resultLabel = "RE-APPEAR";
                totalWithFailures++;
                evaluatedStudents++;
                sumSGPA += studentSGPA;
            }

            sb.append(String.format(" %-10s | %-22.22s | %-8d | %-7d | %-6d | %-8.2f | %-12s \n",
                    s.getRollNo(), s.getName(), courseCount, passedCount, failedCount, studentSGPA, resultLabel));
        }

        sb.append("--------------------------------------------------------------------------------------------------------------------\n");
        double classAvgSGPA = (evaluatedStudents > 0) ? (sumSGPA / evaluatedStudents) : 0.0;
        double passPct = (evaluatedStudents > 0) ? ((double) totalPassedAll / evaluatedStudents) * 100.0 : 0.0;

        sb.append(String.format(" Total Students: %d | Evaluated: %d | All Clear (Pass): %d | With Arrears: %d | Pass Rate: %.1f%%\n",
                totalClassStudents, evaluatedStudents, totalPassedAll, totalWithFailures, passPct));
        sb.append(String.format(" Class Average SGPA: %.2f | Equivalent Average Percentage: %.2f %%\n",
                classAvgSGPA, classAvgSGPA * 10.0));
        sb.append("--------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" COURSE-WISE PERFORMANCE BREAKDOWN:\n");
        for (Map.Entry<String, int[]> entry : coursePassMap.entrySet()) {
            Course c = studentDAO.getCourse(entry.getKey());
            int p = entry.getValue()[0];
            int tot = entry.getValue()[1];
            double cPct = (tot > 0) ? ((double) p / tot) * 100.0 : 0.0;
            sb.append(String.format("  [%s] %-30s : %d/%d Passed (%.1f%%)\n",
                    entry.getKey(), c != null ? c.getTitle() : "", p, tot, cPct));
        }
        sb.append("====================================================================================================================\n");

        return sb.toString();
    }

    private AssessmentMark mapAssessmentMark(ResultSet rs) throws SQLException {
        AssessmentMark m = new AssessmentMark();
        m.setMarkId(rs.getInt("mark_id"));
        m.setEnrollmentId(rs.getInt("enrollment_id"));

        double cia1 = rs.getDouble("cia1_marks");
        if (!rs.wasNull()) m.setCia1Marks(cia1);

        double cia2 = rs.getDouble("cia2_marks");
        if (!rs.wasNull()) m.setCia2Marks(cia2);

        double assign = rs.getDouble("assignment_marks");
        if (!rs.wasNull()) m.setAssignmentMarks(assign);

        double ciaRaw = rs.getDouble("cia_raw_100");
        if (!rs.wasNull()) m.setCiaRaw100(ciaRaw);

        double scCia = rs.getDouble("scaled_cia");
        if (!rs.wasNull()) m.setScaledCia(scCia);

        m.setAbsentEse(rs.getInt("is_absent_ese") == 1);

        double eseRaw = rs.getDouble("ese_raw_marks");
        if (!rs.wasNull()) m.setEseRawMarks(eseRaw);

        double scEse = rs.getDouble("scaled_ese");
        if (!rs.wasNull()) m.setScaledEse(scEse);

        double tot = rs.getDouble("total_marks");
        if (!rs.wasNull()) m.setTotalMarks(tot);

        m.setGrade(rs.getString("grade"));

        int gp = rs.getInt("grade_point");
        if (!rs.wasNull()) m.setGradePoint(gp);

        m.setResultStatus(rs.getString("result_status"));
        m.setEvaluationPhase(rs.getString("evaluation_phase"));

        // Joined metadata
        m.setRollNo(rs.getString("roll_no"));
        m.setStudentName(rs.getString("student_name"));
        m.setDepartment(rs.getString("department"));
        m.setCourseCode(rs.getString("course_code"));
        m.setCourseTitle(rs.getString("course_title"));
        m.setCourseType(CourseType.fromString(rs.getString("course_type")));
        m.setCredits(rs.getDouble("credits"));
        m.setAttendancePct(rs.getDouble("attendance_pct"));
        m.setEligibilityStatus(rs.getString("eligibility_status"));

        return m;
    }
}
