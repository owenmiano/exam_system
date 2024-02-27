package ke.co.skyworld.rest;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import ke.co.skyworld.handlers.answers.*;
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
import ke.co.skyworld.rest.base.*;

public class Routes {
    public static RoutingHandler Class() {
        return Handlers.routing()
                .post( "",new Dispatcher(new BlockingHandler(new CreateClass())))
                .put( "/{classId}",new Dispatcher(new BlockingHandler(new UpdateClass())))
                .get( "/{classId}", new Dispatcher(new GetClass()))
                .get("", new Dispatcher(new GetClasses()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Pupil() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreatePupil())))
                .put( "/{pupilId}",new Dispatcher(new BlockingHandler(new UpdatePupil())))
                .get( "/{pupilId}", new Dispatcher(new GetPupil()))
                .get("",new Dispatcher(new GetPupils()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Teacher() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateTeacher())))
                .put( "/{teacherId}",new Dispatcher(new BlockingHandler(new UpdateTeacher())))
                .get( "/{teacherId}", new Dispatcher(new GetTeacher()))
                .get("",new Dispatcher(new GetTeachers()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Exam() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateExam())))
                .put( "/{examId}",new Dispatcher(new BlockingHandler(new UpdateExam())))
                .get( "/{examId}", new Dispatcher(new GetExam()))
                .get("",new Dispatcher(new GetExams()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler ExamSchedule() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateExamSchedules())))
                .put( "/{examScheduleId}",new Dispatcher(new BlockingHandler(new UpdateExamSchedule())))
                .get( "/{examScheduleId}", new Dispatcher(new GetExamSchedule()))
                .get("",new Dispatcher(new GetExamSchedules()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Subject() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateSubject())))
                .put( "/{subjectId}",new Dispatcher(new BlockingHandler(new UpdateSubject())))
                .get( "/{subjectId}", new Dispatcher(new GetSubject()))
                .get("",new Dispatcher(new GetSubjects()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Answers() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateAnswer())));
    }

    public static RoutingHandler Question() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateQuestion())))
                .put( "/{questionId}",new Dispatcher(new BlockingHandler(new UpdateQuestion())))
                .get( "/{examSubjectId}/{questionId}", new Dispatcher(new GetQuestion()))
                .get("/{examSubjectId}",new Dispatcher(new GetQuestions()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Choice() {
        return Handlers.routing()
                .post( "", new Dispatcher(new BlockingHandler(new CreateChoice())))
                .put( "/{choiceId}",new Dispatcher(new BlockingHandler(new UpdateChoice())))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }

    public static RoutingHandler Report() {
        return Handlers.routing()
                .get( "/exams-by-teacher/{teacherId}", new Dispatcher(new BlockingHandler(new GenerateExamsByTeacher())))
                .get( "/generate-answers/{examSubjectId}/{pupilId}",new Dispatcher(new BlockingHandler(new GeneratePupilsAnswers())))
                .get( "/top-five-results/{examSubjectId}", new Dispatcher(new GenerateTopFivePupils()))
                .get("/pupils-score/{examId}",new Dispatcher(new GeneratePupilScoreReport()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()))
                .setFallbackHandler(new Dispatcher(new FallBack()));
    }
}
