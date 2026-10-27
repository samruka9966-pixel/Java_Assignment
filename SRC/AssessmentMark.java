/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * AssessmentMark Entity Model
 * Encapsulates Phased Incremental Marks:
 *   Phase 1: CIA 1 & CIA 2 (0-100)
 *   Phase 2: Assignment / Quiz (0-100) -> Scaled CIA
 *   Phase 3: ESE (0-100) / Absence (AB) -> Scaled ESE, Total Marks, Grade, Grade Point, Result
 */
public class AssessmentMark {
    private int markId;
    private int enrollmentId;

    // Phase 1: Mid-Semester Tests (0-100)
    private Double cia1Marks;
    private Double cia2Marks;

    // Phase 2: End-of-Semester CIA (0-100)
    private Double assignmentMarks;
    private Double ciaRaw100;
    private Double scaledCia;

    // Phase 3: End Semester Examination (0-100)
    private boolean isAbsentEse;
    private Double eseRawMarks;
    private Double scaledEse;

    // Final Academic Evaluation Results
    private Double totalMarks;
    private String grade;       // O, A+, A, B+, B, RA, SA, AB
    private Integer gradePoint; // 10, 9, 8, 7, 6, 0
    private String resultStatus; // PASS, FAIL, SA, AB, PENDING
    private String evaluationPhase; // NOT_STARTED, PHASE_1_ENTERED, PHASE_2_ENTERED, EVALUATED_FINAL

    // Joined reporting helpers
    private String rollNo;
    private String studentName;
    private String department;
    private String courseCode;
    private String courseTitle;
    private CourseType courseType;
    private double credits;
    private double attendancePct;
    private String eligibilityStatus;

    public AssessmentMark() {
        this.isAbsentEse = false;
        this.resultStatus = "PENDING";
        this.evaluationPhase = "NOT_STARTED";
    }

    public AssessmentMark(int enrollmentId) {
        this();
        this.enrollmentId = enrollmentId;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getCia1Marks() {
        return cia1Marks;
    }

    public void setCia1Marks(Double cia1Marks) {
        this.cia1Marks = cia1Marks;
    }

    public Double getCia2Marks() {
        return cia2Marks;
    }

    public void setCia2Marks(Double cia2Marks) {
        this.cia2Marks = cia2Marks;
    }

    public Double getAssignmentMarks() {
        return assignmentMarks;
    }

    public void setAssignmentMarks(Double assignmentMarks) {
        this.assignmentMarks = assignmentMarks;
    }

    public Double getCiaRaw100() {
        return ciaRaw100;
    }

    public void setCiaRaw100(Double ciaRaw100) {
        this.ciaRaw100 = ciaRaw100;
    }

    public Double getScaledCia() {
        return scaledCia;
    }

    public void setScaledCia(Double scaledCia) {
        this.scaledCia = scaledCia;
    }

    public boolean isAbsentEse() {
        return isAbsentEse;
    }

    public void setAbsentEse(boolean absentEse) {
        isAbsentEse = absentEse;
    }

    public Double getEseRawMarks() {
        return eseRawMarks;
    }

    public void setEseRawMarks(Double eseRawMarks) {
        this.eseRawMarks = eseRawMarks;
    }

    public Double getScaledEse() {
        return scaledEse;
    }

    public void setScaledEse(Double scaledEse) {
        this.scaledEse = scaledEse;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Integer gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getEvaluationPhase() {
        return evaluationPhase;
    }

    public void setEvaluationPhase(String evaluationPhase) {
        this.evaluationPhase = evaluationPhase;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public double getAttendancePct() {
        return attendancePct;
    }

    public void setAttendancePct(double attendancePct) {
        this.attendancePct = attendancePct;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }
}
