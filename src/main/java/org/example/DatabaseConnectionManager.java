package org.example;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.crypto.Cipher;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DatabaseConnectionManager {
    private static final byte[] secretKey = "aiajd9292UA7HK38381JSHA393JAKASt".getBytes(StandardCharsets.UTF_8);
    private DatabaseConfig dbConfig;

    public DatabaseConnectionManager(File configFile) {
        this.dbConfig = new DatabaseConfig();
        parseConfigFile(configFile);
    }

    private void parseConfigFile(File configFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            dbConfig.setDbType(xpath.evaluate("/configuration/database/type", document));
            dbConfig.setDbName(xpath.evaluate("/configuration/database/name", document));
            dbConfig.setHost(xpath.evaluate("/configuration/database/host", document));
            dbConfig.setPort(Integer.parseInt(xpath.evaluate("/configuration/database/port", document)));

            // Process username
            Node usernameNode = (Node) xpath.evaluate("/configuration/database/username", document, XPathConstants.NODE);
            if (usernameNode != null) {
                String username = usernameNode.getTextContent();
                String encryptedAttrUsername = ((Element) usernameNode).getAttribute("ENCRYPTED");
                if ("no".equalsIgnoreCase(encryptedAttrUsername)) {
                    username = encrypt(username);
                    ((Element) usernameNode).setTextContent(username);
                    ((Element) usernameNode).setAttribute("ENCRYPTED", "yes");
                }
                dbConfig.setUsername(username);
            }

            // Process password
            Node passwordNode = (Node) xpath.evaluate("/configuration/database/password", document, XPathConstants.NODE);
            if (passwordNode != null) {
                String password = passwordNode.getTextContent();
                String encryptedAttrPassword = ((Element) passwordNode).getAttribute("ENCRYPTED");
                if ("no".equalsIgnoreCase(encryptedAttrPassword)) {
                    password = encrypt(password);
                    ((Element) passwordNode).setTextContent(password);
                    ((Element) passwordNode).setAttribute("ENCRYPTED", "yes");
                }
                dbConfig.setPassword(password);
            }

            // Save changes back to the XML file
            saveDocumentToFile(document, configFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String decrypt(String encryptedData, byte[] secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveDocumentToFile(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    public Connection getConnection() throws SQLException {
        String connectionUrl = buildConnectionUrl();
        String decryptedUsername = decrypt(dbConfig.getUsername(), secretKey);
        String decryptedPassword = decrypt(dbConfig.getPassword(), secretKey);

        return DriverManager.getConnection(connectionUrl, decryptedUsername, decryptedPassword);
    }

    private String buildConnectionUrl() {
        switch (dbConfig.getDbType().toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDbName());
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDbName());
            case "mssql":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDbName());
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbConfig.getDbType());
        }
    }

    public void createTables(Connection connection) throws SQLException {
        String[] createTableCommands = getTableCreationCommands(dbConfig.getDbType());

        try (Statement statement = connection.createStatement()) {
            for (String sql : createTableCommands) {
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private String[] getTableCreationCommands(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return getMySQLTableCommands();
            case "postgresql":
                return getPostgreSQLTableCommands();
            case "mssql":
                return getMSSQLTableCommands();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    // Implement these methods to return the actual SQL commands for each database type
    private String[] getMySQLTableCommands() {
        return new String[] {
                "CREATE TABLE IF NOT EXISTS class (" +
                        "class_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "class_name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "UNIQUE INDEX unique_class_name (class_name))",

                "CREATE TABLE IF NOT EXISTS teachers (" +
                        "teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "teacher_name VARCHAR(255) NOT NULL, " +
                        "tsc_number INT, " +
                        "id_number VARCHAR(50), " +
                        "kra_pin VARCHAR(50), " +
                        "phone VARCHAR(50), " +
                        "email VARCHAR(50), " +
                        "date_of_birth DATE, " +
                        "class_id BIGINT, " +
                        "username VARCHAR(250), " +
                        "password VARCHAR(255), " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "UNIQUE INDEX unique_username (username), " +
                        "UNIQUE INDEX unique_email (email), " +
                        "UNIQUE INDEX unique_id_number  (id_number), " +
                        "UNIQUE INDEX unique_tsc_number  (tsc_number), " +
                        "UNIQUE INDEX unique_kra_pin  (kra_pin), " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id))",

                "CREATE TABLE IF NOT EXISTS pupils (" +
                        "pupils_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "pupil_name VARCHAR(255) NOT NULL, " +
                        "date_of_birth DATE, " +
                        "guardian_name VARCHAR(25), " +
                        "guardian_phone VARCHAR(50), " +
                        "username VARCHAR(50), " +
                        "reg_no VARCHAR(250), " +
                        "password VARCHAR(255), " +
                        "class_id BIGINT, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "UNIQUE INDEX unique_username (username), " +
                        "UNIQUE INDEX unique_reg_no (reg_no), " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id))",

                "CREATE TABLE IF NOT EXISTS subject (" +
                        "subject_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "subject_name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "UNIQUE INDEX unique_subject_name (subject_name))",

                "CREATE TABLE IF NOT EXISTS exam (" +
                        "exam_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "exam_name VARCHAR(50) NOT NULL, " +
                        "class_id BIGINT, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id)) ",

                "CREATE TABLE IF NOT EXISTS exam_subjects (" +
                        "exam_subject_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "exam_id BIGINT, " +
                        "subject_id BIGINT, " +
                        "teacher_id BIGINT, " +
                        "exam_date DATE NULL, " +
                        "exam_duration INT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "FOREIGN KEY (exam_id) REFERENCES exam(exam_id), " +
                        "FOREIGN KEY (subject_id) REFERENCES subject(subject_id), " +
                        "FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id))",

                "CREATE TABLE IF NOT EXISTS questions (" +
                        "questions_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "exam_subject_id BIGINT, " +
                        "question_no INT, " +
                        "description TEXT, " +
                        "marks INT, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "FOREIGN KEY (exam_subject_id) REFERENCES exam_subjects(exam_subject_id))",

                "CREATE TABLE IF NOT EXISTS choices (" +
                        "choices_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "questions_id BIGINT, " +
                        "option_label VARCHAR(2), " +
                        "option_value TEXT, " +
                        "correct BOOLEAN, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "FOREIGN KEY (questions_id) REFERENCES questions(questions_id))",

                "CREATE TABLE IF NOT EXISTS answers (" +
                        "answers_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "questions_id BIGINT, " +
                        "choices_id BIGINT, " +
                        "pupils_id BIGINT, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME NULL, " +
                        "FOREIGN KEY (questions_id) REFERENCES questions(questions_id), " +
                        "FOREIGN KEY (choices_id) REFERENCES choices(choices_id), " +
                        "FOREIGN KEY (pupils_id) REFERENCES pupils(pupils_id))"
        };
    }

    private String[] getPostgreSQLTableCommands() {
        return new String[] {
                "CREATE TABLE IF NOT EXISTS class (" +
                        "class_id SERIAL PRIMARY KEY, " +
                        "class_name VARCHAR(50) NOT NULL UNIQUE, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS teachers (" +
                        "teacher_id SERIAL PRIMARY KEY, " +
                        "teacher_name VARCHAR(255) NOT NULL, " +
                        "tsc_number INT UNIQUE, " +
                        "id_number VARCHAR(50) UNIQUE, " +
                        "kra_pin VARCHAR(50) UNIQUE, " +
                        "phone VARCHAR(50), " +
                        "email VARCHAR(50) UNIQUE, " +
                        "date_of_birth DATE, " +
                        "class_id BIGINT, " +
                        "username VARCHAR(250) UNIQUE, " +
                        "password VARCHAR(255), " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP, " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id))",

                "CREATE TABLE IF NOT EXISTS pupils (" +
                        "pupils_id SERIAL PRIMARY KEY, " +
                        "pupil_name VARCHAR(255) NOT NULL, " +
                        "date_of_birth DATE, " +
                        "guardian_name VARCHAR(255), " +
                        "guardian_phone VARCHAR(50), " +
                        "username VARCHAR(50) UNIQUE, " +
                        "reg_no VARCHAR(250) UNIQUE, " +
                        "password VARCHAR(255), " +
                        "class_id BIGINT, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP, " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id))",

                "CREATE TABLE IF NOT EXISTS subject (" +
                        "subject_id SERIAL PRIMARY KEY, " +
                        "subject_name VARCHAR(50) NOT NULL UNIQUE, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS exam (" +
                        "exam_id SERIAL PRIMARY KEY, " +
                        "exam_name VARCHAR(50) NOT NULL, " +
                        "class_id BIGINT, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP, " +
                        "FOREIGN KEY (class_id) REFERENCES class(class_id))",

                "CREATE TABLE IF NOT EXISTS exam_subjects (" +
                        "exam_subject_id SERIAL PRIMARY KEY, " +
                        "exam_id BIGINT, " +
                        "subject_id BIGINT, " +
                        "teacher_id BIGINT, " +
                        "exam_date DATE NULL, " +
                        "exam_duration INT NULL, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP NULL, " +
                        "FOREIGN KEY (exam_id) REFERENCES exam(exam_id), " +
                        "FOREIGN KEY (subject_id) REFERENCES subject(subject_id), " +
                        "FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id))",

                "CREATE TABLE IF NOT EXISTS questions (" +
                        "questions_id SERIAL PRIMARY KEY, " +
                        "exam_subject_id BIGINT, " +
                        "question_no INT, " +
                        "description TEXT, " +
                        "marks INT, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP NULL, " +
                        "FOREIGN KEY (exam_subject_id) REFERENCES exam_subjects(exam_subject_id))",

                "CREATE TABLE IF NOT EXISTS choices (" +
                        "choices_id SERIAL PRIMARY KEY, " +
                        "questions_id BIGINT, " +
                        "option_label VARCHAR(2), " +
                        "option_value TEXT, " +
                        "correct BOOLEAN, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP, " +
                        "FOREIGN KEY (questions_id) REFERENCES questions(questions_id))",

                "CREATE TABLE IF NOT EXISTS answers (" +
                        "answers_id SERIAL PRIMARY KEY, " +
                        "questions_id BIGINT, " +
                        "choices_id BIGINT, " +
                        "pupils_id BIGINT, " +
                        "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified TIMESTAMP, " +
                        "FOREIGN KEY (questions_id) REFERENCES questions(questions_id), " +
                        "FOREIGN KEY (choices_id) REFERENCES choices(choices_id), " +
                        "FOREIGN KEY (pupils_id) REFERENCES pupils(pupils_id))",

                // Indexes for teachers table
                "CREATE INDEX IF NOT EXISTS idx_username ON teachers (username)",
                "CREATE INDEX IF NOT EXISTS idx_teacher_name ON teachers (teacher_name)",
                "CREATE INDEX IF NOT EXISTS idx_email ON teachers (email)",
                "CREATE INDEX IF NOT EXISTS idx_id_number ON teachers (id_number)",
                "CREATE INDEX IF NOT EXISTS idx_tsc_number ON teachers (tsc_number)",
                "CREATE INDEX IF NOT EXISTS idx_kra_pin ON teachers (kra_pin)",

                // Indexes for pupils table
                "CREATE INDEX IF NOT EXISTS idx_username_pupils ON pupils (username)",
                "CREATE INDEX IF NOT EXISTS idx_pupil_name ON pupils (pupil_name)",
                "CREATE INDEX IF NOT EXISTS idx_reg_no ON pupils (reg_no)",

                // Index for exam table
                "CREATE INDEX IF NOT EXISTS idx_exam_name ON exam (exam_name)"
        };
    }



    private String[] getMSSQLTableCommands() {
        return new String[]{
                // 'class' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[class]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.class (" +
                        "class_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "class_name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "CONSTRAINT unique_class_name UNIQUE (class_name)" +
                        ") END",

                // 'teachers' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[teachers]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.teachers (" +
                        "teacher_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "teacher_name VARCHAR(255) NOT NULL, " +
                        "tsc_number INT UNIQUE, " +
                        "id_number VARCHAR(50) UNIQUE, " +
                        "kra_pin VARCHAR(50) UNIQUE, " +
                        "phone VARCHAR(50), " +
                        "email VARCHAR(50) UNIQUE, " +
                        "date_of_birth DATE, " +
                        "class_id INT, " +
                        "username VARCHAR(250) UNIQUE, " +
                        "password VARCHAR(255), " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (class_id) REFERENCES dbo.class(class_id)" +
                        ") " +
                        "CREATE NONCLUSTERED INDEX idx_teacher_name ON dbo.teachers (teacher_name);" +
                        "END",

                // 'pupils' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[pupils]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.pupils (" +
                        "pupils_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "pupil_name VARCHAR(255) NOT NULL, " +
                        "date_of_birth DATE, " +
                        "guardian_name VARCHAR(255), " +
                        "guardian_phone VARCHAR(50), " +
                        "username VARCHAR(50) UNIQUE, " +
                        "reg_no VARCHAR(250) UNIQUE, " +
                        "password VARCHAR(255), " +
                        "class_id INT, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (class_id) REFERENCES dbo.class(class_id)" +
                        ") " +
                        "CREATE NONCLUSTERED INDEX idx_pupil_name ON dbo.pupils (pupil_name);" +
                        "END",

                // 'subject' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[subject]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.subject (" +
                        "subject_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "subject_name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "CONSTRAINT unique_subject_name UNIQUE (subject_name)" +
                        ") END",

                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[exam]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.exam (" +
                        "exam_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "exam_name VARCHAR(50) NOT NULL, " +
                        "class_id INT, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (class_id) REFERENCES dbo.class(class_id)" +
                        ") " +
                        "CREATE NONCLUSTERED INDEX idx_exam_name ON dbo.exam (exam_name);" +
                        "END",

                // 'exam_subjects' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[exam_subjects]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.exam_subjects (" +
                        "exam_subject_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "exam_id INT, " +
                        "subject_id INT, " +
                        "teacher_id INT, " +
                        "exam_date DATE NULL, " +
                        "exam_duration INT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (exam_id) REFERENCES dbo.exam(exam_id), " +
                        "FOREIGN KEY (subject_id) REFERENCES dbo.subject(subject_id), " +
                        "FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id) " +
                        ") END",

// 'questions' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[questions]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.questions (" +
                        "questions_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "exam_subject_id INT, " +
                        "question_no INT, " +
                        "description TEXT, " +
                        "marks INT, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (exam_subject_id) REFERENCES dbo.exam_subjects(exam_subject_id)" +
                        ") END",

//'choices' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[choices]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.choices (" +
                        "choices_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "questions_id INT, " +
                        "option_label VARCHAR(2), " +
                        "option_value TEXT, " +
                        "correct BIT, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (questions_id) REFERENCES dbo.questions(questions_id)" +
                        ") END",

// 'answers' table
                "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[answers]') AND type in (N'U')) " +
                        "BEGIN " +
                        "CREATE TABLE dbo.answers (" +
                        "answers_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "questions_id INT, " +
                        "choices_id INT, " +
                        "pupils_id INT, " +
                        "date_created DATETIME NOT NULL DEFAULT GETDATE(), " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (questions_id) REFERENCES dbo.questions(questions_id), " +
                        "FOREIGN KEY (choices_id) REFERENCES dbo.choices(choices_id), " +
                        "FOREIGN KEY (pupils_id) REFERENCES dbo.pupils(pupils_id)" +
                        ") END"
        };
    }




        // DatabaseConfig inner class
    public static class DatabaseConfig {
        private static String dbType;
        private String dbName;
        private String host;
        private int port;
        private String username;
        private String password;

        public void setDbType(String dbType) { this.dbType = dbType; }
        public void setDbName(String dbName) { this.dbName = dbName; }
        public void setHost(String host) { this.host = host; }
        public void setPort(int port) { this.port = port; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }

        public static String getDbType() { return dbType; }
        public String getDbName() { return dbName; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}
