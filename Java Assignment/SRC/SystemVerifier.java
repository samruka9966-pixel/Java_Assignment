import java.sql.SQLException;
import java.util.List;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * SystemVerifier: Comprehensive End-to-End Verification of KEC R2024 Rules
 */
public class SystemVerifier {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   KONGU ENGINEERING COLLEGE (AUTONOMOUS) - R2024 SYSTEM VERIFIER         ");
        System.out.println("==========================================================================");

        StudentDAO studentDAO = new StudentDAO();
        AcademicDAO academicDAO = new AcademicDAO();

        try {
            DBConnection.getConnection();

            // -----------------------------------------------------------------
            // 1. VERIFY STUDENT & COURSE REGISTRATION
            // -----------------------------------------------------------------
            System.out.println("\n[TEST 1] Verifying Student & Course Registration...");
            Student testStudent = new Student("24ITR099", "Karthik Raja M", "IT", 3);
            studentDAO.addStudent(testStudent);
            Student retrievedStudent = studentDAO.getStudent("24ITR099");
            assert retrievedStudent != null && retrievedStudent.getName().equals("Karthik Raja M");
            System.out.println("  PASS: Student registered and retrieved -> " + retrievedStudent);

            Course testCourse = new Course("24ITR99", "Cloud Computing Architectures", 3.0, CourseType.THEORY);
            studentDAO.addCourse(testCourse);
            Course retrievedCourse = studentDAO.getCourse("24ITR99");
            assert retrievedCourse != null && retrievedCourse.getCiaWeightage() == 40.0 && retrievedCourse.getEseMinThreshold() == 27.0;
            System.out.println("  PASS: Course registered with R2024 weightages -> " + retrievedCourse);

            // -----------------------------------------------------------------
            // 2. VERIFY ENROLLMENT & ATTENDANCE FILTERS
            // -----------------------------------------------------------------
            System.out.println("\n[TEST 2] Verifying Attendance Filtering Rules (KEC R2024)...");
            int enrId = studentDAO.enrollStudent("24ITR099", "24ITR99", 3);
            
            // Case A: Below 65% attendance (e.g. 30/50 = 60%)
            studentDAO.updateAttendance(enrId, 50, 30);
            Enrollment enrA = studentDAO.getEnrollment(enrId);
            System.out.printf("  Case A (60.0%% Attendance): Status = %s (Expected: BARRED_SA)\n", enrA.getEligibilityStatus());
            assert "BARRED_SA".equals(enrA.getEligibilityStatus());

            // Case B: Between 65% and 74.9% (e.g. 35/50 = 70%) without condonation
            studentDAO.updateAttendance(enrId, 50, 35);
            Enrollment enrB = studentDAO.getEnrollment(enrId);
            System.out.printf("  Case B (70.0%% Attendance, No Condonation): Status = %s (Expected: CONDITIONAL)\n", enrB.getEligibilityStatus());
            assert "CONDITIONAL".equals(enrB.getEligibilityStatus());

            // Case C: Between 65% and 74.9% WITH Medical Condonation approved
            studentDAO.approveMedicalCondonation(enrId, true);
            Enrollment enrC = studentDAO.getEnrollment(enrId);
            System.out.printf("  Case C (70.0%% Attendance, Condonation Approved): Status = %s (Expected: CONDONED_ELIGIBLE)\n", enrC.getEligibilityStatus());
            assert "CONDONED_ELIGIBLE".equals(enrC.getEligibilityStatus());

            // Case D: Greater than or equal to 75% (e.g. 45/50 = 90%)
            studentDAO.updateAttendance(enrId, 50, 45);
            Enrollment enrD = studentDAO.getEnrollment(enrId);
            System.out.printf("  Case D (90.0%% Attendance): Status = %s (Expected: ELIGIBLE)\n", enrD.getEligibilityStatus());
            assert "ELIGIBLE".equals(enrD.getEligibilityStatus());

            // -----------------------------------------------------------------
            // 3. VERIFY INCREMENTAL MARKS & R2024 EVALUATION ENGINE
            // -----------------------------------------------------------------
            System.out.println("\n[TEST 3] Verifying Incremental Marks Entry & Weightages...");

            // Phase 1: CIA 1 = 80, CIA 2 = 90 (Avg = 85)
            academicDAO.recordPhase1Marks(enrId, 80.0, 90.0);
            AssessmentMark m1 = academicDAO.getAssessmentMark(enrId);
            System.out.printf("  Phase 1 Recorded: CIA1=%.1f, CIA2=%.1f\n", m1.getCia1Marks(), m1.getCia2Marks());

            // Phase 2: Assignment = 90
            // Raw CIA (100) = (85 * 0.6) + (90 * 0.4) = 51 + 36 = 87.0
            // Scaled CIA (Theory 40%) = 87.0 * 0.40 = 34.80 / 40
            academicDAO.recordPhase2Marks(enrId, 90.0);
            AssessmentMark m2 = academicDAO.getAssessmentMark(enrId);
            System.out.printf("  Phase 2 Recorded: Raw CIA=%.2f, Scaled CIA (40%%)=%.2f / 40\n", m2.getCiaRaw100(), m2.getScaledCia());
            assert Math.abs(m2.getScaledCia() - 34.80) < 0.01;

            // Phase 3 - Subcase 1: ESE Minimum Threshold Failure
            // Raw ESE = 40/100 -> Scaled ESE (Theory 60%) = 24.0 / 60 (Threshold is 27.0!)
            // Total = 34.8 + 24.0 = 58.8 >= 50, BUT scaled ESE < 27 -> FAILS (RA)!
            academicDAO.recordPhase3Marks(enrId, 40.0, false);
            AssessmentMark m3_fail = academicDAO.getAssessmentMark(enrId);
            System.out.printf("  Subcase 3A (ESE Minimum Threshold Fail: Scaled ESE=%.2f < 27): Grade=%s, Result=%s, GP=%d (Expected: RA, FAIL, 0)\n",
                    m3_fail.getScaledEse(), m3_fail.getGrade(), m3_fail.getResultStatus(), m3_fail.getGradePoint());
            assert "RA".equals(m3_fail.getGrade()) && "FAIL".equals(m3_fail.getResultStatus());

            // Phase 3 - Subcase 2: Successful ESE Completion
            // Raw ESE = 85/100 -> Scaled ESE (Theory 60%) = 51.0 / 60
            // Total = 34.8 + 51.0 = 85.8 -> A+ (81-90), GP: 9
            academicDAO.recordPhase3Marks(enrId, 85.0, false);
            AssessmentMark m3_pass = academicDAO.getAssessmentMark(enrId);
            System.out.printf("  Subcase 3B (Successful ESE: Total=%.2f): Grade=%s, Result=%s, GP=%d (Expected: A+, PASS, 9)\n",
                    m3_pass.getTotalMarks(), m3_pass.getGrade(), m3_pass.getResultStatus(), m3_pass.getGradePoint());
            assert "A+".equals(m3_pass.getGrade()) && "PASS".equals(m3_pass.getResultStatus()) && m3_pass.getGradePoint() == 9;

            // Phase 3 - Subcase 3: Absent in ESE (AB)
            academicDAO.recordPhase3Marks(enrId, null, true);
            AssessmentMark m3_ab = academicDAO.getAssessmentMark(enrId);
            System.out.printf("  Subcase 3C (Absent in ESE): Grade=%s, Result=%s, GP=%d (Expected: AB, AB, 0)\n",
                    m3_ab.getGrade(), m3_ab.getResultStatus(), m3_ab.getGradePoint());
            assert "AB".equals(m3_ab.getGrade()) && "AB".equals(m3_ab.getResultStatus());

            System.out.println("\n--------------------------------------------------------------------------");
            System.out.println("  ALL AUTOMATED UNIT & INTEGRATION TESTS PASSED WITH 100% SUCCESS!");
            System.out.println("==========================================================================");

        } catch (Exception e) {
            System.err.println("Verification Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
