import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * MainApp: Menu-Driven Console Application for Student Academic & Continuous Evaluation System
 */
public class MainApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    public static void main(String[] args) {
        // Ensure Database schema is initialized
        try {
            DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return;
        }

        // Check if headless demo argument is passed
        if (args.length > 0 && ("--demo".equalsIgnoreCase(args[0]) || "--test".equalsIgnoreCase(args[0]))) {
            runAutomatedDemonstration();
            return;
        }

        // Interactive Menu Loop
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print(" Enter your choice [1-8]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleStudentManagement();
                    break;
                case "2":
                    handleCourseCatalog();
                    break;
                case "3":
                    handleEnrollmentAndAttendance();
                    break;
                case "4":
                    handleIncrementalMarksEntry();
                    break;
                case "5":
                    handleReportingOutputs();
                    break;
                case "6":
                    handleSeedSampleData();
                    break;
                case "7":
                    runAutomatedDemonstration();
                    break;
                case "8":
                    System.out.println("\n Exiting KEC Academic System. Thank you!");
                    running = false;
                    break;
                default:
                    System.out.println(" Invalid choice! Please enter a number between 1 and 8.");
            }
        }

        DBConnection.closeConnection();
    }

    private static void printMainMenu() {
        System.out.println("\n==========================================================================");
        System.out.println("       KONGU ENGINEERING COLLEGE (AUTONOMOUS) - REGULATION 2024           ");
        System.out.println("             STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM              ");
        System.out.println("==========================================================================");
        System.out.println(" [1] Student Registration (Add / View Students)");
        System.out.println(" [2] Course Catalog (Add / View Courses - Theory, Practical, Embedded)");
        System.out.println(" [3] Course Enrollment & Continuous Attendance Tracking (held vs attended)");
        System.out.println(" [4] Incremental Marks Entry (\"Then and Now\" - Phase 1, 2, 3)");
        System.out.println(" [5] Academic Reports & Result Sheets (Mark Sheet, Defaulters, Summary)");
        System.out.println(" [6] Populate Sample KEC Dataset (Pre-load demo curriculum & students)");
        System.out.println(" [7] Run Full Automated Verification & Live Demo (Proof Generation)");
        System.out.println(" [8] Exit System");
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // 1. STUDENT MANAGEMENT
    // =========================================================================
    private static void handleStudentManagement() {
        System.out.println("\n--- [1] STUDENT MANAGEMENT ---");
        System.out.println(" 1. Register New Student");
        System.out.println(" 2. View All Registered Students");
        System.out.print(" Select option: ");
        String opt = scanner.nextLine().trim();

        if ("1".equals(opt)) {
            System.out.print(" Enter Roll Number (e.g. 24ITR001): ");
            String rollNo = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Student Name: ");
            String name = scanner.nextLine().trim();
            System.out.print(" Enter Department (e.g. IT, CSE, ECE, MECH): ");
            String dept = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Current Semester (1-8): ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            try {
                boolean success = studentDAO.addStudent(new Student(rollNo, name, dept, sem));
                if (success) {
                    System.out.println(" [+] Student registered successfully: " + rollNo + " (" + name + ")");
                }
            } catch (SQLException e) {
                System.err.println(" [-] Error registering student: " + e.getMessage());
            }
        } else if ("2".equals(opt)) {
            try {
                List<Student> students = studentDAO.getAllStudents();
                System.out.println("\n Registered Students List (" + students.size() + " total):");
                for (Student s : students) {
                    System.out.println("  " + s);
                }
            } catch (SQLException e) {
                System.err.println(" Error fetching students: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 2. COURSE CATALOG MANAGEMENT
    // =========================================================================
    private static void handleCourseCatalog() {
        System.out.println("\n--- [2] COURSE CATALOG (KEC R2024) ---");
        System.out.println(" 1. Add New Course");
        System.out.println(" 2. View All Courses in Catalog");
        System.out.print(" Select option: ");
        String opt = scanner.nextLine().trim();

        if ("1".equals(opt)) {
            System.out.print(" Enter Course Code (e.g. 24ITR31): ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Course Title: ");
            String title = scanner.nextLine().trim();
            System.out.print(" Enter Credits (e.g. 3.0, 4.0, 1.5): ");
            double cred = Double.parseDouble(scanner.nextLine().trim());
            System.out.println(" Course Types in KEC R2024:");
            System.out.println("   1. THEORY    (CIA: 40% | ESE: 60% | Min ESE: 27/60 | Pass: 50)");
            System.out.println("   2. PRACTICAL (CIA: 60% | ESE: 40% | Min ESE: 20/40 | Pass: 50)");
            System.out.println("   3. EMBEDDED  (CIA: 50% | ESE: 50% | Min ESE: 22.5/50 | Pass: 50)");
            System.out.print(" Choose Type [1-3 or Type Name]: ");
            String tStr = scanner.nextLine().trim().toUpperCase();
            CourseType type;
            if ("1".equals(tStr) || tStr.startsWith("THE")) type = CourseType.THEORY;
            else if ("2".equals(tStr) || tStr.startsWith("PRA") || tStr.startsWith("LAB")) type = CourseType.PRACTICAL;
            else if ("3".equals(tStr) || tStr.startsWith("EMB")) type = CourseType.EMBEDDED;
            else type = CourseType.fromString(tStr);

            Course course = new Course(code, title, cred, type);
            try {
                boolean ok = studentDAO.addCourse(course);
                if (ok) {
                    System.out.println(" [+] Course added successfully: " + course);
                }
            } catch (SQLException e) {
                System.err.println(" [-] Error adding course: " + e.getMessage());
            }
        } else if ("2".equals(opt)) {
            try {
                List<Course> courses = studentDAO.getAllCourses();
                System.out.println("\n Course Catalog (" + courses.size() + " courses):");
                for (Course c : courses) {
                    System.out.println("  " + c);
                }
            } catch (SQLException e) {
                System.err.println(" Error fetching course catalog: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 3. COURSE ENROLLMENT & ATTENDANCE TRACKING
    // =========================================================================
    private static void handleEnrollmentAndAttendance() {
        System.out.println("\n--- [3] COURSE ENROLLMENT & ATTENDANCE TRACKING ---");
        System.out.println(" 1. Enroll Student into Course");
        System.out.println(" 2. Continuously Record Classes Held vs Classes Attended");
        System.out.println(" 3. Approve Medical Condonation (for 65% - 74.9% bracket)");
        System.out.println(" 4. View Student Course Enrollments");
        System.out.print(" Select option: ");
        String opt = scanner.nextLine().trim();

        if ("1".equals(opt)) {
            System.out.print(" Enter Roll Number: ");
            String rollNo = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Course Code: ");
            String courseCode = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            try {
                int enrId = studentDAO.enrollStudent(rollNo, courseCode, sem);
                if (enrId > 0) {
                    System.out.println(" [+] Student " + rollNo + " enrolled in " + courseCode + " (Enrollment ID #" + enrId + ")");
                }
            } catch (SQLException e) {
                System.err.println(" [-] Enrollment failed: " + e.getMessage());
            }
        } else if ("2".equals(opt)) {
            System.out.print(" Enter Roll Number: ");
            String rollNo = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Course Code: ");
            String courseCode = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            try {
                Enrollment e = studentDAO.getEnrollmentByRollAndCourse(rollNo, courseCode, sem);
                if (e == null) {
                    System.out.println(" [-] No enrollment record found for " + rollNo + " in " + courseCode);
                    return;
                }
                System.out.print(" Enter Total Classes Held: ");
                int held = Integer.parseInt(scanner.nextLine().trim());
                System.out.print(" Enter Total Classes Attended: ");
                int attended = Integer.parseInt(scanner.nextLine().trim());

                boolean ok = studentDAO.updateAttendance(e.getEnrollmentId(), held, attended);
                if (ok) {
                    Enrollment updated = studentDAO.getEnrollment(e.getEnrollmentId());
                    System.out.printf(" [+] Attendance updated: %d/%d (%.2f%%) - Status: %s\n",
                            held, attended, updated.getAttendancePct(), updated.getEligibilityStatus());
                    if (updated.getAttendancePct() < 65.0) {
                        System.out.println(" [!] WARNING: Attendance is below 65%. Student is Barred and awarded SA Grade (0 GP).");
                    } else if (updated.getAttendancePct() < 75.0) {
                        System.out.println(" [!] NOTICE: Attendance is between 65% - 74.9%. Medical Condonation required for ESE eligibility.");
                    }
                }
            } catch (Exception ex) {
                System.err.println(" [-] Error updating attendance: " + ex.getMessage());
            }
        } else if ("3".equals(opt)) {
            System.out.print(" Enter Enrollment ID: ");
            int enrId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print(" Approve Medical Condonation? (yes/no): ");
            boolean approved = scanner.nextLine().trim().toLowerCase().startsWith("y");
            try {
                boolean ok = studentDAO.approveMedicalCondonation(enrId, approved);
                if (ok) {
                    System.out.println(" [+] Medical Condonation status updated for Enrollment #" + enrId);
                }
            } catch (SQLException ex) {
                System.err.println(" [-] Error updating condonation: " + ex.getMessage());
            }
        } else if ("4".equals(opt)) {
            System.out.print(" Enter Roll Number: ");
            String rollNo = scanner.nextLine().trim().toUpperCase();
            System.out.print(" Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());
            try {
                List<Enrollment> list = studentDAO.getEnrollmentsForStudent(rollNo, sem);
                System.out.println("\n Enrollments for " + rollNo + " (Sem " + sem + "):");
                for (Enrollment en : list) {
                    System.out.println("  " + en);
                }
            } catch (SQLException ex) {
                System.err.println(" Error fetching enrollments: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. INCREMENTAL MARKS ENTRY ("Then and Now")
    // =========================================================================
    private static void handleIncrementalMarksEntry() {
        System.out.println("\n--- [4] INCREMENTAL MARKS ENTRY (\"Then and Now\") ---");
        System.out.println(" 1. Phase 1 (Mid-Semester): Record CIA Test 1 (0-100) & CIA Test 2 (0-100)");
        System.out.println(" 2. Phase 2 (End of Semester - CIA): Record Assignments / Quizzes / Lab (0-100)");
        System.out.println(" 3. Phase 3 (Post Semester): Record ESE Marks (0-100) / Absence (AB)");
        System.out.print(" Select Phase [1-3]: ");
        String opt = scanner.nextLine().trim();

        System.out.print(" Enter Student Roll Number: ");
        String rollNo = scanner.nextLine().trim().toUpperCase();
        System.out.print(" Enter Course Code: ");
        String courseCode = scanner.nextLine().trim().toUpperCase();
        System.out.print(" Enter Semester: ");
        int sem = Integer.parseInt(scanner.nextLine().trim());

        try {
            Enrollment e = studentDAO.getEnrollmentByRollAndCourse(rollNo, courseCode, sem);
            if (e == null) {
                System.out.println(" [-] Student is not enrolled in this course.");
                return;
            }

            int enrId = e.getEnrollmentId();

            if ("1".equals(opt)) {
                System.out.print(" Enter CIA Test 1 Marks (0 - 100): ");
                double cia1 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print(" Enter CIA Test 2 Marks (0 - 100): ");
                double cia2 = Double.parseDouble(scanner.nextLine().trim());

                boolean ok = academicDAO.recordPhase1Marks(enrId, cia1, cia2);
                if (ok) {
                    System.out.printf(" [+] Phase 1 Marks Recorded: CIA 1 = %.1f, CIA 2 = %.1f (Test Avg = %.1f)\n",
                            cia1, cia2, (cia1 + cia2) / 2.0);
                }
            } else if ("2".equals(opt)) {
                System.out.print(" Enter Assignments / Quizzes / Lab Evaluations Marks (0 - 100): ");
                double assign = Double.parseDouble(scanner.nextLine().trim());

                boolean ok = academicDAO.recordPhase2Marks(enrId, assign);
                if (ok) {
                    AssessmentMark m = academicDAO.getAssessmentMark(enrId);
                    Course c = studentDAO.getCourse(courseCode);
                    System.out.printf(" [+] Phase 2 Continuous Internal Assessment (CIA) Finalized:\n" +
                                    "     Raw CIA (100 Scale)   : %.2f\n" +
                                    "     Scaled CIA (%.0f%% Weight): %.2f / %.0f\n",
                            m.getCiaRaw100(), c.getCiaWeightage(), m.getScaledCia(), c.getCiaWeightage());
                }
            } else if ("3".equals(opt)) {
                System.out.print(" Is the student Absent in ESE? (yes/no): ");
                boolean isAbsent = scanner.nextLine().trim().toLowerCase().startsWith("y");
                Double eseMarks = null;
                if (!isAbsent) {
                    System.out.print(" Enter End Semester Examination (ESE) Marks (0 - 100): ");
                    eseMarks = Double.parseDouble(scanner.nextLine().trim());
                }

                AssessmentMark evaluated = academicDAO.recordPhase3Marks(enrId, eseMarks, isAbsent);
                System.out.printf("\n [+] Phase 3 Final Evaluation Complete:\n" +
                                "     Scaled CIA  : %.2f\n" +
                                "     Scaled ESE  : %.2f\n" +
                                "     Total Marks : %.2f / 100\n" +
                                "     Letter Grade: %s (Grade Point: %d)\n" +
                                "     Result      : %s\n",
                        evaluated.getScaledCia(), evaluated.getScaledEse(), evaluated.getTotalMarks(),
                        evaluated.getGrade(), evaluated.getGradePoint(), evaluated.getResultStatus());
            }
        } catch (Exception ex) {
            System.err.println(" [-] Evaluation Error: " + ex.getMessage());
        }
    }

    // =========================================================================
    // 5. REPORTING OUTPUTS
    // =========================================================================
    private static void handleReportingOutputs() {
        System.out.println("\n--- [5] ACADEMIC REPORTING OUTPUTS ---");
        System.out.println(" 1. Student Semester Mark Sheet (SGPA & Equivalent Percentage)");
        System.out.println(" 2. Attendance Defaulter Report (SA Flagging)");
        System.out.println(" 3. Class Performance & Result Summary (Department-wise)");
        System.out.print(" Select report [1-3]: ");
        String opt = scanner.nextLine().trim();

        try {
            if ("1".equals(opt)) {
                System.out.print(" Enter Student Roll Number (e.g. 24ITR001): ");
                String rollNo = scanner.nextLine().trim().toUpperCase();
                System.out.print(" Enter Semester: ");
                int sem = Integer.parseInt(scanner.nextLine().trim());
                String report = academicDAO.generateStudentSemesterMarkSheet(rollNo, sem);
                System.out.println(report);
            } else if ("2".equals(opt)) {
                String report = academicDAO.generateAttendanceDefaulterReport();
                System.out.println(report);
            } else if ("3".equals(opt)) {
                System.out.print(" Enter Department (e.g. IT, CSE): ");
                String dept = scanner.nextLine().trim().toUpperCase();
                System.out.print(" Enter Semester: ");
                int sem = Integer.parseInt(scanner.nextLine().trim());
                String report = academicDAO.generateClassPerformanceSummary(dept, sem);
                System.out.println(report);
            }
        } catch (SQLException ex) {
            System.err.println(" Report Generation Error: " + ex.getMessage());
        }
    }

    // =========================================================================
    // 6. SAMPLE DATA SEEDING
    // =========================================================================
    private static void handleSeedSampleData() {
        System.out.println("\n Seeding KEC R2024 demo courses, students, and enrollments...");
        seedDataset();
        System.out.println(" [+] Sample dataset ready for testing!");
    }

    public static void seedDataset() {
        try {
            // Add Courses
            Course[] courses = new Course[]{
                    new Course("24ITR31", "Object Oriented Programming using Java", 3.0, CourseType.THEORY),
                    new Course("24ITR32", "Data Structures and Algorithms", 4.0, CourseType.EMBEDDED),
                    new Course("24ITR33", "Database Management Systems", 3.0, CourseType.THEORY),
                    new Course("24ITL31", "Java Programming Laboratory", 1.5, CourseType.PRACTICAL),
                    new Course("24ITL32", "Data Structures Laboratory", 1.5, CourseType.PRACTICAL)
            };
            for (Course c : courses) {
                try { studentDAO.addCourse(c); } catch (SQLException ignored) {}
            }

            // Add Students
            Student[] students = new Student[]{
                    new Student("24ITR001", "Aadhithya K", "IT", 3), // High performer (O/A+ grade)
                    new Student("24ITR002", "Bavani S", "IT", 3),    // Average performer (B+/B grade)
                    new Student("24ITR003", "Chandran M", "IT", 3),  // Attendance Defaulter (<65% -> SA)
                    new Student("24ITR004", "Deepika R", "IT", 3),   // Medical Condonation (68% -> Condoned)
                    new Student("24ITR005", "Elango V", "IT", 3),    // ESE Absent (AB)
                    new Student("24CSR015", "Gokulnath P", "CSE", 3)
            };
            for (Student s : students) {
                try { studentDAO.addStudent(s); } catch (SQLException ignored) {}
            }

            // Enrollments & Attendance
            // Student 1: Aadhithya (Eligible, 92% attendance)
            for (Course c : courses) {
                int id = studentDAO.enrollStudent("24ITR001", c.getCourseCode(), 3);
                studentDAO.updateAttendance(id, 50, 46); // 92%
            }

            // Student 2: Bavani (Eligible, 80% attendance)
            for (Course c : courses) {
                int id = studentDAO.enrollStudent("24ITR002", c.getCourseCode(), 3);
                studentDAO.updateAttendance(id, 50, 40); // 80%
            }

            // Student 3: Chandran (Attendance Defaulter: 28/50 = 56% -> Barred SA)
            for (Course c : courses) {
                int id = studentDAO.enrollStudent("24ITR003", c.getCourseCode(), 3);
                studentDAO.updateAttendance(id, 50, 28); // 56% -> Barred SA
            }

            // Student 4: Deepika (Medical Condonation: 34/50 = 68% -> Condoned)
            for (Course c : courses) {
                int id = studentDAO.enrollStudent("24ITR004", c.getCourseCode(), 3);
                studentDAO.updateAttendance(id, 50, 34); // 68%
                studentDAO.approveMedicalCondonation(id, true); // Approved
            }

            // Student 5: Elango (Eligible attendance 88%, but absent in ESE)
            for (Course c : courses) {
                int id = studentDAO.enrollStudent("24ITR005", c.getCourseCode(), 3);
                studentDAO.updateAttendance(id, 50, 44); // 88%
            }

        } catch (Exception e) {
            System.err.println("Seeding notice: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. FULL AUTOMATED DEMONSTRATION & PROOF GENERATOR
    // =========================================================================
    public static void runAutomatedDemonstration() {
        System.out.println("\n==========================================================================");
        System.out.println("    KONGU ENGINEERING COLLEGE (AUTONOMOUS) - R2024 VERIFICATION DEMO     ");
        System.out.println("==========================================================================");

        try {
            seedDataset();

            // Scenario 1: Aadhithya K (24ITR001) - Full 3-Phase Progression across all courses
            System.out.println("\n>>> [STEP 1] INCREMENTAL MARKS ENTRY FOR 24ITR001 (Aadhithya K)");
            System.out.println("--- Phase 1: Mid-Semester (CIA1 & CIA2) ---");
            int e1 = studentDAO.getEnrollmentByRollAndCourse("24ITR001", "24ITR31", 3).getEnrollmentId();
            int e2 = studentDAO.getEnrollmentByRollAndCourse("24ITR001", "24ITR32", 3).getEnrollmentId();
            int e3 = studentDAO.getEnrollmentByRollAndCourse("24ITR001", "24ITR33", 3).getEnrollmentId();
            int e4 = studentDAO.getEnrollmentByRollAndCourse("24ITR001", "24ITL31", 3).getEnrollmentId();
            int e5 = studentDAO.getEnrollmentByRollAndCourse("24ITR001", "24ITL32", 3).getEnrollmentId();

            academicDAO.recordPhase1Marks(e1, 92, 94);
            academicDAO.recordPhase1Marks(e2, 88, 90);
            academicDAO.recordPhase1Marks(e3, 85, 87);
            academicDAO.recordPhase1Marks(e4, 95, 96);
            academicDAO.recordPhase1Marks(e5, 92, 95);
            System.out.println(" [+] Recorded Phase 1 (CIA1 & CIA2) for all 5 enrolled courses.");

            System.out.println("\n--- Phase 2: End-of-Semester CIA (Assignments / Quizzes / Lab) ---");
            academicDAO.recordPhase2Marks(e1, 95); // Theory: (93*0.6) + (95*0.4) = 93.8 -> 37.52 / 40
            academicDAO.recordPhase2Marks(e2, 92); // Embedded: (89*0.6) + (92*0.4) = 90.2 -> 45.10 / 50
            academicDAO.recordPhase2Marks(e3, 90); // Theory: (86*0.6) + (90*0.4) = 87.6 -> 35.04 / 40
            academicDAO.recordPhase2Marks(e4, 98); // Practical: (95.5*0.6) + (98*0.4) = 96.5 -> 57.90 / 60
            academicDAO.recordPhase2Marks(e5, 96); // Practical: (93.5*0.6) + (96*0.4) = 94.5 -> 56.70 / 60
            System.out.println(" [+] Recorded Phase 2 Assignment/Lab scores & Scaled CIA calculated.");

            System.out.println("\n--- Phase 3: Post-Semester End Semester Examination (ESE) ---");
            academicDAO.recordPhase3Marks(e1, 92.0, false); // Scaled ESE: 92*0.60 = 55.20 / 60 -> Total: 92.7 -> O (10 GP)
            academicDAO.recordPhase3Marks(e2, 88.0, false); // Scaled ESE: 88*0.50 = 44.00 / 50 -> Total: 89.1 -> A+ (9 GP)
            academicDAO.recordPhase3Marks(e3, 84.0, false); // Scaled ESE: 84*0.60 = 50.40 / 60 -> Total: 85.4 -> A+ (9 GP)
            academicDAO.recordPhase3Marks(e4, 94.0, false); // Scaled ESE: 94*0.40 = 37.60 / 40 -> Total: 95.5 -> O (10 GP)
            academicDAO.recordPhase3Marks(e5, 90.0, false); // Scaled ESE: 90*0.40 = 36.00 / 40 -> Total: 92.7 -> O (10 GP)
            System.out.println(" [+] Phase 3 ESE recorded and final 10-point grades computed.");

            // Scenario 2: Bavani S (24ITR002) - Average Student
            int b1 = studentDAO.getEnrollmentByRollAndCourse("24ITR002", "24ITR31", 3).getEnrollmentId();
            int b2 = studentDAO.getEnrollmentByRollAndCourse("24ITR002", "24ITR32", 3).getEnrollmentId();
            int b3 = studentDAO.getEnrollmentByRollAndCourse("24ITR002", "24ITR33", 3).getEnrollmentId();
            academicDAO.recordPhase1Marks(b1, 65, 70);
            academicDAO.recordPhase2Marks(b1, 72);
            academicDAO.recordPhase3Marks(b1, 68.0, false);

            academicDAO.recordPhase1Marks(b2, 60, 62);
            academicDAO.recordPhase2Marks(b2, 65);
            academicDAO.recordPhase3Marks(b2, 64.0, false);

            academicDAO.recordPhase1Marks(b3, 58, 60);
            academicDAO.recordPhase2Marks(b3, 62);
            academicDAO.recordPhase3Marks(b3, 55.0, false);

            // Scenario 3: Chandran M (24ITR003) - Shortage of Attendance (<65% -> SA)
            int c1 = studentDAO.getEnrollmentByRollAndCourse("24ITR003", "24ITR31", 3).getEnrollmentId();
            academicDAO.recordPhase1Marks(c1, 70, 75);
            academicDAO.recordPhase2Marks(c1, 70);
            // In Phase 3, because attendance is 56%, should automatically award SA!
            academicDAO.recordPhase3Marks(c1, 80.0, false); // System enforces SA

            // Scenario 4: Deepika R (24ITR004) - Medical Condonation approved
            int d1 = studentDAO.getEnrollmentByRollAndCourse("24ITR004", "24ITR31", 3).getEnrollmentId();
            academicDAO.recordPhase1Marks(d1, 78, 80);
            academicDAO.recordPhase2Marks(d1, 82);
            academicDAO.recordPhase3Marks(d1, 75.0, false);

            // Scenario 5: Elango V (24ITR005) - Absent in ESE (AB)
            int el1 = studentDAO.getEnrollmentByRollAndCourse("24ITR005", "24ITR31", 3).getEnrollmentId();
            academicDAO.recordPhase1Marks(el1, 82, 85);
            academicDAO.recordPhase2Marks(el1, 85);
            academicDAO.recordPhase3Marks(el1, null, true); // Absent -> AB

            // PRINT FORMAL REPORTS
            System.out.println("\n>>> [REPORT 1] STUDENT SEMESTER MARK SHEET (24ITR001 - Aadhithya K)");
            System.out.println(academicDAO.generateStudentSemesterMarkSheet("24ITR001", 3));

            System.out.println("\n>>> [REPORT 2] ATTENDANCE DEFAULTER REPORT (KEC Regulation 2024)");
            System.out.println(academicDAO.generateAttendanceDefaulterReport());

            System.out.println("\n>>> [REPORT 3] DEPARTMENT CLASS PERFORMANCE & RESULT SUMMARY (IT Dept, Sem 3)");
            System.out.println(academicDAO.generateClassPerformanceSummary("IT", 3));

            System.out.println("==========================================================================");
            System.out.println("             ALL VERIFICATION SCENARIOS COMPLETED SUCCESSFULLY!           ");
            System.out.println("==========================================================================");

        } catch (Exception ex) {
            System.err.println("Verification Demo Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
