package ru.practicum.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionService {
    void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver);
}