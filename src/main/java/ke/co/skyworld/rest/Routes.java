package ke.co.skyworld.rest;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import ke.co.skyworld.handlers.answers.*;
import ke.co.skyworld.handlers.authentication.*;
import ke.co.skyworld.handlers.classes.*;
import ke.co.skyworld.handlers.exam.*;
import ke.co.skyworld.handlers.pupils.*;
import ke.co.skyworld.handlers.report.GenerateExamsByTeacher;
import ke.co.skyworld.handlers.report.GeneratePupilScoreReport;
import ke.co.skyworld.handlers.report.GeneratePupilsAnswers;
import ke.co.skyworld.handlers.report.GenerateTopFivePupils;
import ke.co.skyworld.handlers.teachers.*;
import ke.co.skyworld.handlers.subjects.*;
import ke.co.skyworld.handlers.examSchedules.*;
import ke.co.skyworld.handlers.questions.*;
import ke.co.skyworld.handlers.choices.*;
import ke.co.skyworld.middleware.Authentication;
import ke.co.skyworld.rest.base.*;

public class Routes {
    public static RoutingHandler Class() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",new Dispatcher(new BlockingHandler(new CreateClass())))
                .put( "/{classId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateClass()))))
                .get( "/{classId}",authentication.authenticateUser( new Dispatcher(new GetClass())))
                .get("",authentication.authenticateUser( new Dispatcher(new GetClasses())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Pupil() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreatePupil())))
                .put( "/{pupilId}",authentication.authenticateUser(new Dispatcher(new BlockingHandler(new UpdatePupil()))))
                .get( "/{pupilId}",authentication.authenticateUser( new Dispatcher(new GetPupil())))
                .get("",authentication.authenticateUser(new Dispatcher(new GetPupils())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Teacher() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "", authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new CreateTeacher()))))
                .put( "/{teacherId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateTeacher()))))
                .get( "/{teacherId}", authentication.authenticateUser(new Dispatcher(new GetTeacher())))
                .get("",authentication.authenticateUser(new Dispatcher(new GetTeachers())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
    public static RoutingHandler Auth() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "/login", new Dispatcher(new BlockingHandler(new LoginUser())))
                .post( "/refresh-token", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new RefreshToken()))));
    }

    public static RoutingHandler Exam() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new CreateExam()))))
                .put( "/{examId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateExam()))))
                .get( "/{examId}",authentication.authenticateUser( new Dispatcher(new GetExam())))
                .get("",authentication.authenticateUser(new Dispatcher(new GetExams())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler ExamSchedule() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new CreateExamSchedules()))))
                .put( "/{examScheduleId}",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new UpdateExamSchedule()))))
                .get( "/{examScheduleId}",authentication.authenticateUser(new Dispatcher(new GetExamSchedule())))
                .get("",authentication.authenticateUser(new Dispatcher(new GetExamSchedules())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Subject() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new CreateSubject()))))
                .put( "/{subjectId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateSubject()))))
                .get( "/{subjectId}",authentication.authenticateUser( new Dispatcher(new GetSubject())))
                .get("",authentication.authenticateUser( new Dispatcher(new GetSubjects())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Answers() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authenticateUser( new Dispatcher(new BlockingHandler(new CreateAnswer()))));
    }

    public static RoutingHandler Question() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new CreateQuestion()))))
                .put( "/{questionId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateQuestion()))))
                .get( "/{examSubjectId}/{questionId}",authentication.authenticateUser( new Dispatcher(new GetQuestion())))
                .get("",authentication.authenticateUser(new Dispatcher(new GetQuestions())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Choice() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .post( "",authentication.authorizeTeacher( new Dispatcher(new BlockingHandler(new CreateChoice()))))
                .put( "/{choiceId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new UpdateChoice()))))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Report() {
        Authentication authentication=new Authentication();
        return Handlers.routing()
                .get( "/exams-by-teacher/{teacherId}", authentication.authenticateUser(new Dispatcher(new BlockingHandler(new GenerateExamsByTeacher()))))
                .get( "/generate-answers/{examSubjectId}/{pupilId}",authentication.authorizeTeacher(new Dispatcher(new BlockingHandler(new GeneratePupilsAnswers()))))
                .get( "/top-five-results/{examSubjectId}",authentication.authenticateUser( new Dispatcher(new GenerateTopFivePupils())))
                .get("/pupils-score/{examId}",authentication.authenticateUser(new Dispatcher(new GeneratePupilScoreReport())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
}
