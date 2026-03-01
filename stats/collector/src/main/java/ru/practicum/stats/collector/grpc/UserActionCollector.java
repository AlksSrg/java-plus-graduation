package ru.practicum.stats.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.stats.collector.config.CollectorConfig;
import ru.practicum.stats.collector.service.UserActionService;

@GrpcService
@RequiredArgsConstructor
public class UserActionCollector extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionService service;
    private final CollectorConfig config;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        service.collectUserAction(request, responseObserver);
    }
}