/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * Enrollment Entity Model with Attendance Tracking & Eligibility Filtering
 */
public class Enrollment {
    private int enrollmentId;
    private String rollNo;
    private String courseCode;
    private int semester;
    private int classesHeld;
    private int classesAttended;
    private double attendancePct;
    private boolean condonationApproved;
    private String eligibilityStatus; // ELIGIBLE, CONDITIONAL, BARRED_SA

    // Joined auxiliary fields for reporting convenience
    private String studentName;
    private String department;
    private String courseTitle;
    private CourseType courseType;
    private double credits;

    public Enrollment() {
        this.classesHeld = 0;
        this.classesAttended = 0;
        this.attendancePct = 0.0;
        this.condonationApproved = false;
        this.eligibilityStatus = "ELIGIBLE";
    }

    public Enrollment(String rollNo, String courseCode, int semester) {
        this();
        this.rollNo = rollNo;
        this.courseCode = courseCode;
        this.semester = semester;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getClassesHeld() {
        return classesHeld;
    }

    public void setClassesHeld(int classesHeld) {
        this.classesHeld = classesHeld;
        recalculateAttendance();
    }

    public int getClassesAttended() {
        return classesAttended;
    }

    public void setClassesAttended(int classesAttended) {
        this.classesAttended = classesAttended;
        recalculateAttendance();
    }

    public double getAttendancePct() {
        return attendancePct;
    }

    public void setAttendancePct(double attendancePct) {
        this.attendancePct = attendancePct;
    }

    public boolean isCondonationApproved() {
        return condonationApproved;
    }

    public void setCondonationApproved(boolean condonationApproved) {
        this.condonationApproved = condonationApproved;
        recalculateAttendance();
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
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

    /**
     * KEC R2024 Attendance Eligibility Filter:
     * - >= 75%: Eligible for ESE
     * - 65% to 74.9%: Conditionally eligible (Requires Medical Condonation approval)
     * - < 65%: Award SA (Shortage of Attendance) grade (0 Grade Points). Barred from ESE.
     */
    public void recalculateAttendance() {
        if (classesHeld > 0) {
            this.attendancePct = Math.round(((double) classesAttended / classesHeld) * 10000.0) / 100.0;
        } else {
            this.attendancePct = 100.0;
        }

        if (attendancePct >= 75.0) {
            this.eligibilityStatus = "ELIGIBLE";
        } else if (attendancePct >= 65.0) {
            this.eligibilityStatus = condonationApproved ? "CONDONED_ELIGIBLE" : "CONDITIONAL";
        } else {
            this.eligibilityStatus = "BARRED_SA";
        }
    }

    public boolean isEligibleForESE() {
        recalculateAttendance();
        if (attendancePct >= 75.0) return true;
        return attendancePct >= 65.0 && condonationApproved;
    }

    @Override
    public String toString() {
        return String.format("Enrollment #%d | Student: %s (%s) | Course: %s | Attendance: %d/%d (%.1f%%) | Status: %s",
                enrollmentId, rollNo, studentName != null ? studentName : "", courseCode,
                classesAttended, classesHeld, attendancePct, eligibilityStatus);
    }
}
