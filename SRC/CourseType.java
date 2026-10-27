/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * CourseType Enumeration defining assessment weightages & ESE threshold
 */
public enum CourseType {
    THEORY(40.0, 60.0, 27.0, 50.0),      // 40% CIA, 60% ESE, 27/60 Min ESE, 50 Pass Min
    PRACTICAL(60.0, 40.0, 20.0, 50.0),   // 60% CIA, 40% ESE, 20/40 Min ESE, 50 Pass Min
    EMBEDDED(50.0, 50.0, 22.5, 50.0);    // 50% CIA, 50% ESE, 22.5/50 Min ESE, 50 Pass Min

    private final double ciaWeightage;
    private final double eseWeightage;
    private final double eseMinThreshold;
    private final double passMinimum;

    CourseType(double ciaWeightage, double eseWeightage, double eseMinThreshold, double passMinimum) {
        this.ciaWeightage = ciaWeightage;
        this.eseWeightage = eseWeightage;
        this.eseMinThreshold = eseMinThreshold;
        this.passMinimum = passMinimum;
    }

    public double getCiaWeightage() {
        return ciaWeightage;
    }

    public double getEseWeightage() {
        return eseWeightage;
    }

    public double getEseMinThreshold() {
        return eseMinThreshold;
    }

    public double getPassMinimum() {
        return passMinimum;
    }

    public static CourseType fromString(String type) {
        if (type == null) return THEORY;
        switch (type.trim().toUpperCase()) {
            case "PRACTICAL":
            case "LAB":
                return PRACTICAL;
            case "EMBEDDED":
            case "INTEGRATED":
                return EMBEDDED;
            case "THEORY":
            default:
                return THEORY;
        }
    }
}
