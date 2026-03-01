package ru.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.ewm.stats.proto.*;

/**
 * gRPC контроллер для обработки запросов рекомендаций.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final AnalyzerService analyzerService;

    /**
     * Возвращает персональные рекомендации для пользователя.
     *
     * @param request          запрос с ID пользователя
     * @param responseObserver наблюдатель ответа
     */
    @Override
    public void getRecommendationsForUser(UserRecommendationsRequestProto request,
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

    /**
     * Возвращает похожие мероприятия.
     *
     * @param request          запрос с ID мероприятия
     * @param responseObserver наблюдатель ответа
     */
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

    /**
     * Возвращает статистику взаимодействий.
     *
     * @param request          запрос со списком мероприятий
     * @param responseObserver наблюдатель ответа
     */
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