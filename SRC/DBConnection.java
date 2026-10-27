import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024
 * DBConnection: Manages JDBC connections and schema initialization.
 * 
 * Supports:
 *   1. SQLite (Default, embedded zero-configuration database: 'kec_academic.db')
 *   2. MySQL (Optional enterprise database configurable via 'db.properties')
 */
public class DBConnection {

    private static Connection connection = null;
    private static String dbType = "SQLITE";
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:kec_academic.db";

    private DBConnection() {
        // Private constructor for singleton pattern
    }

    /**
     * Obtains or reuses an active JDBC Connection.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            Properties props = loadProperties();
            String configuredType = props.getProperty("db.type", "SQLITE").toUpperCase().trim();

            if ("MYSQL".equals(configuredType)) {
                dbType = "MYSQL";
                String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/kec_academic?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
                String user = props.getProperty("db.user", "root");
                String pass = props.getProperty("db.password", "root");

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("[DBConnection] Connected to MySQL Database successfully.");
            } else {
                dbType = "SQLITE";
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DEFAULT_SQLITE_URL);
                
                // Enable foreign key constraints in SQLite
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                // System.out.println("[DBConnection] Connected to SQLite Database (kec_academic.db).");
            }

            initializeSchema(connection);

        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage(), e);
        }

        return connection;
    }

    /**
     * Initializes required tables according to KEC Regulation 2024 specifications
     * if they do not already exist in the database.
     */
    public static void initializeSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            boolean isSQLite = "SQLITE".equalsIgnoreCase(dbType);
            String autoInc = isSQLite ? "INTEGER PRIMARY KEY AUTOINCREMENT" : "INT PRIMARY KEY AUTO_INCREMENT";

            // 1. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "roll_no VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "department VARCHAR(50) NOT NULL, " +
                    "semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 2. Courses Table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "course_code VARCHAR(20) PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "credits DECIMAL(3, 1) NOT NULL, " +
                    "course_type VARCHAR(20) NOT NULL, " +
                    "cia_weightage DECIMAL(5, 2) NOT NULL, " +
                    "ese_weightage DECIMAL(5, 2) NOT NULL, " +
                    "ese_min_threshold DECIMAL(5, 2) NOT NULL, " +
                    "pass_mark DECIMAL(5, 2) NOT NULL DEFAULT 50.0" +
                    ");");

            // 3. Enrollments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                    "enrollment_id " + autoInc + ", " +
                    "roll_no VARCHAR(20) NOT NULL, " +
                    "course_code VARCHAR(20) NOT NULL, " +
                    "semester INT NOT NULL, " +
                    "classes_held INT NOT NULL DEFAULT 0, " +
                    "classes_attended INT NOT NULL DEFAULT 0, " +
                    "attendance_pct DECIMAL(5, 2) DEFAULT 0.0, " +
                    "condonation_approved INT DEFAULT 0, " +
                    "eligibility_status VARCHAR(25) DEFAULT 'ELIGIBLE', " +
                    "FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                    "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE, " +
                    "UNIQUE (roll_no, course_code, semester)" +
                    ");");

            // 4. Assessment Marks Table
            stmt.execute("CREATE TABLE IF NOT EXISTS assessment_marks (" +
                    "mark_id " + autoInc + ", " +
                    "enrollment_id INT NOT NULL UNIQUE, " +
                    "cia1_marks DECIMAL(5, 2) DEFAULT NULL, " +
                    "cia2_marks DECIMAL(5, 2) DEFAULT NULL, " +
                    "assignment_marks DECIMAL(5, 2) DEFAULT NULL, " +
                    "cia_raw_100 DECIMAL(5, 2) DEFAULT NULL, " +
                    "scaled_cia DECIMAL(5, 2) DEFAULT NULL, " +
                    "is_absent_ese INT DEFAULT 0, " +
                    "ese_raw_marks DECIMAL(5, 2) DEFAULT NULL, " +
                    "scaled_ese DECIMAL(5, 2) DEFAULT NULL, " +
                    "total_marks DECIMAL(5, 2) DEFAULT NULL, " +
                    "grade VARCHAR(5) DEFAULT NULL, " +
                    "grade_point INT DEFAULT NULL, " +
                    "result_status VARCHAR(15) DEFAULT 'PENDING', " +
                    "evaluation_phase VARCHAR(25) DEFAULT 'NOT_STARTED', " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE" +
                    ");");

        } catch (SQLException e) {
            System.err.println("[DBConnection] Schema initialization error: " + e.getMessage());
        }
    }

    /**
     * Loads database configuration from db.properties if available.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("[DBConnection] Warning: Could not read db.properties, defaulting to SQLite.");
            }
        }
        return props;
    }

    public static String getDbType() {
        return dbType;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException ignored) {}
        }
    }
}
