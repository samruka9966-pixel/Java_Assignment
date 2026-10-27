# Kongu Engineering College (Autonomous) - R2024
## Student Academic & Continuous Evaluation System using Java JDBC

A database-driven Java application utilizing JDBC to manage student records, execute phased/incremental marks entry (CIA1, CIA2, Assignments, ESE), enforce attendance eligibility thresholds, and generate formal academic performance reports adhering to KEC Regulation 2024.

---

### Quick Launch Instructions (Windows)

1. **Compile**: Double-click `compile.bat` or run:
   ```cmd
   javac -cp ".;lib/*" *.java
   ```
2. **Launch Interactive Menu**: Double-click `run_interactive.bat` or run:
   ```cmd
   java -cp ".;lib/*" MainApp
   ```
3. **Run Automated Demonstration (All Scenarios)**: Double-click `run_demo.bat` or run:
   ```cmd
   java -cp ".;lib/*" MainApp --demo
   ```
4. **Run Unit & Integration Assertions**: Double-click `run_tests.bat` or run:
   ```cmd
   java -ea -cp ".;lib/*" SystemVerifier
   ```

---

### Key Deliverables in this Repository

* **`kec_schema.sql`**: Relational database schema with constraints and foreign keys.
* **`DBConnection.java`**: JDBC connection manager for SQLite and MySQL with auto-initialization.
* **`StudentDAO.java`**: CRUD operations for Students, Course Catalog, Enrollment & Attendance.
* **`AcademicDAO.java`**: Assessment engine for incremental marks (Phases 1-3), attendance filters, and reports.
* **`MainApp.java`**: Menu-driven interface and automated demo runner.
* **`EXECUTION_DOCUMENTATION.md`**: Complete documentation with formulas, screenshots, and guidelines.
* **`student_course_add_proof.jpg`**: Screenshot proof 1 (Student & Course entry).
* **`incremental_marks_proof.jpg`**: Screenshot proof 2 (Phased marks entry).
* **`grade_sheet_proof.jpg`**: Screenshot proof 3 (Semester Grade Sheet with SGPA).
