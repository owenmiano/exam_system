package org.example;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class Main {


    public static void main(String[] args) {
        File configFile = getConfigFile();
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile);

            try (Connection connection = dbManager.getConnection()) {
                System.out.println("Connected successfully");

                // Call createTables after establishing the connection
                dbManager.createTables(connection);
                allPupilsScores(connection);
            } // The connection will be closed automatically here due to try-with-resources

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the database: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static File getConfigFile() {
        Path resourcesPath = Paths.get("src", "main", "resources", "config.xml");
        if (!Files.exists(resourcesPath)) {
            throw new IllegalArgumentException("Configuration file not found at " + resourcesPath);
        }

        return resourcesPath.toFile();
    }

    //Class section Start
    private static void addClass(Connection connection) {
        HashMap<String, Object> classData = new HashMap<>();
        classData.put("class_name", "class 4 ");

        ClassController.createClass(connection, classData);

    }
    //select class method
    private static void selectClass(Connection connection) {
        HashMap<String, Object> classData = new HashMap<>();
        classData.put("class_name", "class 2");

        String[] columns={"class_name"};

        ClassController.findClass(connection, classData,columns);

    }

    //update class method
    private static void renameClass(Connection connection) {
        HashMap<String, Object> classData = new HashMap<>();
        classData.put("class_name", "1Weslalaland000");

        String classId="4";
        ClassController.updateClass(connection, classData,classId);

    }
//Class section Start


//Teacher section Start
    //Add teacher method
    private static void addTeacher(Connection connection) {
        HashMap<String, Object> teacherData = new HashMap<>();
        teacherData.put("teacher_name", "Christopher martin");
        teacherData.put("date_of_birth", "2000-01-15");
        teacherData.put("id_number", "71616554");
        teacherData.put("phone", "0748507924");
        teacherData.put("email", "chris90@gmail.com");
        teacherData.put("username", "chris");
        teacherData.put("kra_pin", "A012G15338GAFH");
        teacherData.put("tsc_number", 2637389); // Replace with actual TSC number
        teacherData.put("password", "chris9000"); // Replace with actual password
        teacherData.put("class_id", 1); // Replace with actual class ID

        TeacherController.createTeacher(connection, teacherData);

    }
//    select teacher method
    private static void selectTeacher(Connection connection) {
        HashMap<String, Object> teacherData = new HashMap<>();
        teacherData.put("id_number", "80009000");

        String[] columns = {
    "teacher_id", "teacher_name", "tsc_number", "id_number", "kra_pin",
    "phone", "email", "date_of_birth", "class_id", "username",
    "date_created"
        };

        TeacherController.findTeacher(connection, teacherData,columns);

    }
    //rename teacher method
    private static void renameTeacher(Connection connection) {
        HashMap<String, Object> teacherData = new HashMap<>();
        teacherData.put("teacher_name", "Trevor Mutiso");

        String teacherId="4";
        TeacherController.updateTeacher(connection, teacherData,teacherId);

    }
//    Teacher section end

//Pupil section Start
private static void addPuPil(Connection connection) {
    HashMap<String, Object> pupilData = new HashMap<>();
    pupilData.put("pupil_name", "Sharon Mutua");
    pupilData.put("date_of_birth", "2002-01-15");
    pupilData.put("guardian_name", "Shelly Rogers");
    pupilData.put("guardian_phone", "0701562771");
    pupilData.put("username", "sharon");
    pupilData.put("reg_no", "P009");
    pupilData.put("password", "sharon9000");
    pupilData.put("class_id", 2);

    PupilController.createPupil(connection, pupilData);

}

    private static void selectPupil(Connection connection) {
        HashMap<String, Object> pupilData = new HashMap<>();
        pupilData.put("reg_no", "P011");

        String[] columns = {
                "pupils_id",
                "pupil_name",
                "date_of_birth",
                "guardian_name",
                "guardian_phone",
                "username",
                "reg_no",
                "class_id",
                "date_created",
                "date_modified"
        };

        PupilController.findPupil(connection, pupilData,columns);

    }
    //rename teacher method
    private static void renamePupil(Connection connection) {
        HashMap<String, Object> pupilData = new HashMap<>();
        pupilData.put("pupil_name", "Trevor Mutiso");

        String pupilId="4";
        PupilController.updatePupil(connection, pupilData,pupilId);

    }
//Pupil section end



//Subject section Start
private static void addSubject(Connection connection) {
    HashMap<String, Object> subjectData = new HashMap<>();
    subjectData.put("subject_name", "Science");

    SubjectController.createSubject(connection, subjectData);

}
    private static void selectSubject(Connection connection) {
        HashMap<String, Object> subjectData = new HashMap<>();
        subjectData.put("subject_name", "Science");

        String[] columns={""};

        SubjectController.findSubject(connection, subjectData,columns);

    }

    //update subject method
    private static void renameSubject(Connection connection) {
        HashMap<String, Object> subjectData = new HashMap<>();
        subjectData.put("subject_name", "Sciencee");

        String subjectId="4";
        SubjectController.updateSubject(connection, subjectData,subjectId);

    }
//Subject section end




//Exam section Start
private static void addExam(Connection connection) {
    HashMap<String, Object> examData = new HashMap<>();
    examData.put("exam_name", "MidTerm Exam");
    examData.put("subject_id", 3);
    examData.put("class_id", "");
    examData.put("teacher_id", "");
    examData.put("exam_date", "");
    examData.put("exam_duration", "");

    ExamController.createExam(connection, examData);
}
    private static void selectExam(Connection connection) {
        HashMap<String, Object> examData = new HashMap<>();
        examData.put("exam_name", "Science");

        String[] columns={""};

        ExamController.findExam(connection, examData,columns);

    }
    private static void renameExam(Connection connection) {
        HashMap<String, Object> examData = new HashMap<>();
        examData.put("exam_name", "Sciencee");

        String examId="4";
        ExamController.updateExam(connection, examData,examId);

    }
//Exam section end



//Questions section Start
private static void addQuestion(Connection connection) {
    HashMap<String, Object> questionData = new HashMap<>();
    questionData.put("exam_id", "");
    questionData.put("question_no", "");
    questionData.put("description", "");
    questionData.put("marks", "");

    QuestionController.createQuestion(connection, questionData);
}
    private static void selectQuestion(Connection connection) {
        HashMap<String, Object> questionData = new HashMap<>();
        questionData.put("question_no", "Science");

        String[] columns={""};

        QuestionController.findQuestion(connection, questionData,columns);

    }
    private static void renameQuestion(Connection connection) {
        HashMap<String, Object> questionData = new HashMap<>();
        questionData.put("description", "Sciencee");

        String questionId="4";
        QuestionController.updateQuestion(connection, questionData,questionId);

    }
//Questions section end


//choices section Start
private static void addChoice(Connection connection) {
    HashMap<String, Object> choiceData = new HashMap<>();
    choiceData.put("questions_id", "");
    choiceData.put("option_label", "");
    choiceData.put("option_value", "");
    choiceData.put("correct", "");


    boolean isInserted = GenericQueries.insertData(connection, "choices", choiceData); // Replace "class" with your actual table name

    if (isInserted) {
        System.out.println("Choices added successfully");
    } else {
        System.out.println("Failed to add choice");
    }
}

//    private static void renameChoice(Connection connection) {
//        HashMap<String, Object> choiceData = new HashMap<>();
//        choiceData.put("option_value", "Sciencee");
//
//        String choicesId="4";
//        QuestionController.updateQuestion(connection, choiceData,choicesId);
//
//    }
//choices section end


//answers section Start
private static void addAnswer(Connection connection) {
    HashMap<String, Object> answersData = new HashMap<>();
    answersData.put("questions_id", "");
    answersData.put("choices_id", "");
    answersData.put("pupils_id", "");

    boolean isInserted = GenericQueries.insertData(connection, "answers", answersData); // Replace "class" with your actual table name

    if (isInserted) {
        System.out.println("Answer added successfully");
    } else {
        System.out.println("Failed to add answer");
    }
}

//answers section end


//Report Section
private static void pupilAnswers(Connection connection) throws SQLException {
    // Define the columns to fetch from the joined tables

    int pupilId = 1; // Example pupilId
    int examSubject = 2; // Example examId

    // Fetch the report data
    ExamReport.generatePupilsAnswers(connection, pupilId, examSubject);

 }
    private static void displayExams(Connection connection) throws SQLException {

        int teacherId = 1;

        // Fetch the report data
        ExamReport.generateExamsByTeacher(connection, teacherId);

        // Print or process the report data
    }

    private static void topFivePupils(Connection connection) throws SQLException {

        int examSubject = 1;

        // Fetch the report data
        ExamReport.generateTopPupilsByScore(connection, examSubject);

        // Print or process the report data
    }
    private static void allPupilsScores(Connection connection) throws SQLException {

        int examId = 1;

        // Fetch the report data
        ExamReport.generatePupilScoreReport(connection, examId);

        // Print or process the report data
    }
}

