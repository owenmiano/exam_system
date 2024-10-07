package ke.co.skyworld.rest;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import ke.co.skyworld.handlers.admin.CreateAdmin;
import ke.co.skyworld.handlers.answers.CreateAnswer;
import ke.co.skyworld.handlers.authentication.ChangePassword;
import ke.co.skyworld.handlers.authentication.LoginUser;
import ke.co.skyworld.handlers.authentication.LogoutUser;
import ke.co.skyworld.handlers.authentication.RefreshToken;
import ke.co.skyworld.handlers.choices.CreateChoice;
import ke.co.skyworld.handlers.choices.UpdateChoice;
import ke.co.skyworld.handlers.classes.CreateClass;
import ke.co.skyworld.handlers.classes.GetClass;
import ke.co.skyworld.handlers.classes.GetClasses;
import ke.co.skyworld.handlers.classes.UpdateClass;
import ke.co.skyworld.handlers.exam.CreateExam;
import ke.co.skyworld.handlers.exam.GetExam;
import ke.co.skyworld.handlers.exam.GetExams;
import ke.co.skyworld.handlers.exam.UpdateExam;
import ke.co.skyworld.handlers.examSchedules.CreateExamSchedules;
import ke.co.skyworld.handlers.examSchedules.GetExamSchedule;
import ke.co.skyworld.handlers.examSchedules.GetExamSchedules;
import ke.co.skyworld.handlers.examSchedules.UpdateExamSchedule;
import ke.co.skyworld.handlers.pupils.CreatePupil;
import ke.co.skyworld.handlers.pupils.GetPupil;
import ke.co.skyworld.handlers.pupils.GetPupils;
import ke.co.skyworld.handlers.pupils.UpdatePupil;
import ke.co.skyworld.handlers.questions.CreateQuestion;
import ke.co.skyworld.handlers.questions.GetQuestion;
import ke.co.skyworld.handlers.questions.GetQuestions;
import ke.co.skyworld.handlers.questions.UpdateQuestion;
import ke.co.skyworld.handlers.report.GenerateExamsByTeacher;
import ke.co.skyworld.handlers.report.GeneratePupilScoreReport;
import ke.co.skyworld.handlers.report.GeneratePupilsAnswers;
import ke.co.skyworld.handlers.report.GenerateTopFivePupils;
import ke.co.skyworld.handlers.subjects.CreateSubject;
import ke.co.skyworld.handlers.subjects.GetSubject;
import ke.co.skyworld.handlers.subjects.GetSubjects;
import ke.co.skyworld.handlers.subjects.UpdateSubject;
import ke.co.skyworld.handlers.teachers.CreateTeacher;
import ke.co.skyworld.handlers.teachers.GetTeacher;
import ke.co.skyworld.handlers.teachers.GetTeachers;
import ke.co.skyworld.handlers.teachers.UpdateTeacher;
import ke.co.skyworld.middleware.AuthenticationMiddleware;
import ke.co.skyworld.rest.base.Dispatcher;
import ke.co.skyworld.rest.base.FallBack;
import ke.co.skyworld.rest.base.InvalidMethod;

public class Routes {
    public static RoutingHandler Class() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new CreateClass())),"admin"))
                .put("/{classId}", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateClass())), "admin"))
                .get("/{classId}", authentication.authenticateUser(new Dispatcher(new GetClass()), "admin", "teacher", "pupil"))
                .get("",authentication.authenticateUser( new Dispatcher(new GetClasses()),"admin", "teacher", "pupil"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Pupil() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreatePupil())))
                .put( "/{pupilId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdatePupil())),"admin", "pupil"))
                .get( "/{pupilId}",authentication.authenticateUser( new Dispatcher(new GetPupil()),"admin","teacher","pupil"))
                .get("",authentication.authenticateUser(new Dispatcher(new GetPupils()),"admin","teacher"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Teacher() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateTeacher())))
                .put( "/{teacherId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateTeacher())),"admin","teacher"))
                .get( "/{teacherId}", authentication.authenticateUser(new Dispatcher(new GetTeacher()),"admin","teacher"))
                .get("",authentication.authenticateUser(new Dispatcher(new GetTeachers()),"admin","teacher"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
    public static RoutingHandler Admin() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateAdmin())),"admin"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
    public static RoutingHandler Auth() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "/login", new Dispatcher(new BlockingHandler(new LoginUser())))
                .put( "/refresh-token", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new RefreshToken())),"admin","teacher","pupil"))
                .post( "/change-password", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new ChangePassword())),"admin","teacher","pupil"))
                .put( "/logout", new Dispatcher(new BlockingHandler(new LogoutUser())));
    }

    public static RoutingHandler Exam() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateExam())),"teacher"))
                .put( "/{examId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateExam())),"teacher"))
                .get( "/{examId}",authentication.authenticateUser( new Dispatcher(new GetExam()),"admin","teacher","pupil"))
                .get("",authentication.authenticateUser(new Dispatcher(new GetExams()),"teacher"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler ExamSchedule() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateExamSchedules())),"teacher"))
                .put( "/{examScheduleId}",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new UpdateExamSchedule())),"teacher"))
                .get( "/{examScheduleId}",authentication.authenticateUser(new Dispatcher(new GetExamSchedule()),"teacher","pupil"))
                .get("",authentication.authenticateUser(new Dispatcher(new GetExamSchedules()),"teacher"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Subject() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateSubject())),"admin","teacher"))
                .put( "/{subjectId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateSubject())),"admin","teacher"))
                .get( "/{subjectId}",authentication.authenticateUser( new Dispatcher(new GetSubject()),"admin","teacher","pupil"))
                .get("",authentication.authenticateUser( new Dispatcher(new GetSubjects()),"admin","teacher","pupil"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Answers() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateAnswer())),"pupil"));
    }

    public static RoutingHandler Question() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateQuestion())),"admin","teacher"))
                .put( "/{questionId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateQuestion())),"admin","teacher"))
                .get( "/{examSubjectId}/{questionId}",authentication.authenticateUser( new Dispatcher(new GetQuestion()),"admin","teacher","pupil"))
                .get("",authentication.authenticateUser(new Dispatcher(new GetQuestions()),"admin","teacher","pupil"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Choice() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateChoice())),"admin","teacher"))
                .put( "/{choiceId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdateChoice())),"admin","teacher"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Report() {
        AuthenticationMiddleware authentication=new AuthenticationMiddleware();
        return Handlers.routing()
                .get( "/exams-by-teacher/{teacherId}", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new GenerateExamsByTeacher())),"admin","teacher"))
                .get( "/generate-answers/{examSubjectId}/{pupilId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new GeneratePupilsAnswers())),"teacher"))
                .get( "/top-five-results/{examSubjectId}",authentication.authenticateUser( new Dispatcher(new GenerateTopFivePupils()),"admin","teacher","pupil"))
                .get("/pupils-score/{examId}",authentication.authenticateUser(new Dispatcher(new GeneratePupilScoreReport()),"admin","teacher","pupil"))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
}
