/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * Course Entity Model
 */
public class Course {
    private String courseCode;
    private String title;
    private double credits;
    private CourseType courseType;
    private double ciaWeightage;
    private double eseWeightage;
    private double eseMinThreshold;
    private double passMark;

    public Course() {
        this.passMark = 50.0;
    }

    public Course(String courseCode, String title, double credits, CourseType courseType) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.courseType = courseType;
        this.ciaWeightage = courseType.getCiaWeightage();
        this.eseWeightage = courseType.getEseWeightage();
        this.eseMinThreshold = courseType.getEseMinThreshold();
        this.passMark = courseType.getPassMinimum();
    }

    public Course(String courseCode, String title, double credits, CourseType courseType,
                  double ciaWeightage, double eseWeightage, double eseMinThreshold, double passMark) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.courseType = courseType;
        this.ciaWeightage = ciaWeightage;
        this.eseWeightage = eseWeightage;
        this.eseMinThreshold = eseMinThreshold;
        this.passMark = passMark;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public double getCiaWeightage() {
        return ciaWeightage;
    }

    public void setCiaWeightage(double ciaWeightage) {
        this.ciaWeightage = ciaWeightage;
    }

    public double getEseWeightage() {
        return eseWeightage;
    }

    public void setEseWeightage(double eseWeightage) {
        this.eseWeightage = eseWeightage;
    }

    public double getEseMinThreshold() {
        return eseMinThreshold;
    }

    public void setEseMinThreshold(double eseMinThreshold) {
        this.eseMinThreshold = eseMinThreshold;
    }

    public double getPassMark() {
        return passMark;
    }

    public void setPassMark(double passMark) {
        this.passMark = passMark;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-32s | Credits: %.1f | Type: %-9s | CIA: %.0f%% | ESE: %.0f%% (Min: %.1f)",
                courseCode, title, credits, courseType.name(), ciaWeightage, eseWeightage, eseMinThreshold);
    }
}
