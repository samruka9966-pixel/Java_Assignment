/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * Student Entity Model
 */
public class Student {
    private String rollNo;
    private String name;
    private String department;
    private int semester;

    public Student() {}

    public Student(String rollNo, String name, String department, int semester) {
        this.rollNo = rollNo;
        this.name = name;
        this.department = department;
        this.semester = semester;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Dept: %-6s | Sem: %d", rollNo, name, department, semester);
    }
}
