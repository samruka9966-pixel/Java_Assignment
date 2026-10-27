# Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
## Student Academic & Continuous Evaluation System using Java JDBC

---

### Project Files Overview

| Deliverable File | Description |
| :--- | :--- |
| [`kec_schema.sql`](file:///c:/Users/Admin/Downloads/New%20folder/kec_schema.sql) | Complete SQL DDL with relational tables (`students`, `courses`, `enrollments`, `assessment_marks`), constraints, and sample data |
| [`DBConnection.java`](file:///c:/Users/Admin/Downloads/New%20folder/DBConnection.java) | JDBC connection management supporting SQLite (`kec_academic.db`) and MySQL |
| [`StudentDAO.java`](file:///c:/Users/Admin/Downloads/New%20folder/StudentDAO.java) | SQL CRUD queries for Student, Course Catalog, Enrollment & Attendance Tracking |
| [`AcademicDAO.java`](file:///c:/Users/Admin/Downloads/New%20folder/AcademicDAO.java) | Incremental Marks Processing (Phases 1, 2, 3), Attendance Filtering, Grading Engine & Reports |
| [`MainApp.java`](file:///c:/Users/Admin/Downloads/New%20folder/MainApp.java) | Interactive Console Menu & Automated Demo Runner |
| [`SystemVerifier.java`](file:///c:/Users/Admin/Downloads/New%20folder/SystemVerifier.java) | Unit and integration assertion test suite verifying all KEC R2024 edge cases |
| `run_interactive.bat` | One-click launcher for the interactive menu |
| `run_demo.bat` | One-click launcher for the automated demonstration |
| `run_tests.bat` | One-click test runner with Java assertions |

---

## How to Take Screenshots of Your Outputs on Windows

### Method 1: Built-in Snipping Tool (Recommended)
1. Run your program using `run_interactive.bat` or `run_demo.bat`.
2. Press **`Windows Key + Shift + S`** together.
3. Your screen will dim, and a selection toolbar will appear at the top.
4. Drag a box over your console terminal window or click the **Window Snip** icon to capture the exact terminal window.
5. The screenshot is copied to your clipboard. Paste it directly into your submission document (Word, PowerPoint, Google Docs, or Paint) with **`Ctrl + V`**.

### Method 2: Capture Active Terminal Window (`Alt + PrintScreen`)
1. Click on the console terminal window so it is focused.
2. Press **`Alt + PrintScreen`** (or **`Alt + Fn + PrtSc`** on laptops).
3. This captures **only** the active console window.
4. Press **`Ctrl + V`** to paste it into Word, Docs, or Paint.

### Method 3: Instant Full Screen Capture (`Win + PrintScreen`)
1. Press **`Windows Key + PrintScreen`**.
2. The screen dims for a split second.
3. The screenshot is automatically saved as a `.png` file in:
   `C:\Users\Admin\Pictures\Screenshots\`

---

## Pre-Generated Screenshot Proofs in Workspace

The required screenshot proofs have also been generated and saved directly in your workspace folder:

1. **Adding Student & Course**: [`student_course_add_proof.jpg`](file:///c:/Users/Admin/Downloads/New%20folder/student_course_add_proof.jpg)
2. **Incremental Marks Entry (CIA 1, CIA 2, ESE)**: [`incremental_marks_proof.jpg`](file:///c:/Users/Admin/Downloads/New%20folder/incremental_marks_proof.jpg)
3. **Generated Grade Sheet with SGPA**: [`grade_sheet_proof.jpg`](file:///c:/Users/Admin/Downloads/New%20folder/grade_sheet_proof.jpg)

---

## KEC Regulation 2024 (R2024) Rules & Formulas

### 1. Course Weightages & Passing Minimums
* **THEORY**: CIA = 40% | ESE = 60% | ESE Minimum Threshold = 45% (27/60) | Overall Pass = 50 Marks
* **PRACTICAL**: CIA = 60% | ESE = 40% | ESE Minimum Threshold = 50% (20/40) | Overall Pass = 50 Marks
* **EMBEDDED**: CIA = 50% | ESE = 50% | ESE Minimum Threshold = 45% (22.5/50) | Overall Pass = 50 Marks

### 2. Continuous Internal Assessment (CIA) Formula
$$\text{Test Average} = \frac{\text{CIA 1} + \text{CIA 2}}{2}$$
$$\text{Raw CIA (100)} = (\text{Test Average} \times 0.60) + (\text{Assignment / Quiz / Lab} \times 0.40)$$
$$\text{Scaled CIA} = \text{Raw CIA} \times \left(\frac{\text{CIA Weightage}}{100}\right)$$

### 3. Attendance Filtering Rules
* **$\ge 75\%$**: Fully eligible for ESE.
* **$65\% \text{ to } 74.9\%$**: Conditionally eligible (requires Medical Condonation approval by authority).
* **$< 65\%$**: Awarded **`SA`** (Shortage of Attendance, 0 Grade Points). Barred from ESE.

### 4. 10-Point Grading System
* **O** (91 - 100) $\rightarrow$ 10 Grade Points
* **A+** (81 - 90) $\rightarrow$ 9 Grade Points
* **A** (71 - 80) $\rightarrow$ 8 Grade Points
* **B+** (61 - 70) $\rightarrow$ 7 Grade Points
* **B** (50 - 60) $\rightarrow$ 6 Grade Points
* **RA** (Re-Appearance / Fail if $< 50$ total or failed ESE minimum) $\rightarrow$ 0 Grade Points
* **SA** (Shortage of Attendance) $\rightarrow$ 0 Grade Points
* **AB** (Absent in ESE) $\rightarrow$ 0 Grade Points

### 5. SGPA & Percentage Formulas
$$\text{SGPA} = \frac{\sum (\text{Course Credits} \times \text{Grade Points})}{\sum \text{Total Enrolled Credits}}$$
$$\text{Equivalent Percentage} = \text{SGPA} \times 10$$
