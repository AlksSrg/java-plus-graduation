package ru.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.grpc.stats.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

/**
 * gRPC контроллер для обработки запросов рекомендаций.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Запрос рекомендаций для пользователя: {}", request.getUserId());
        try {
            analyzerService.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка getRecommendationsForUser", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Запрос похожих мероприятий для: {}", request.getEventId());
        try {
            analyzerService.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка getSimilarEvents", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Запрос статистики для {} мероприятий", request.getEventIdCount());
        try {
            analyzerService.getInteractionsCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка getInteractionsCount", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}