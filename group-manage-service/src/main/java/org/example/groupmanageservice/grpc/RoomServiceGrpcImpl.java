package org.example.groupmanageservice.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.groupmanageservice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class RoomServiceGrpcImpl extends RoomServiceGrpc.RoomServiceImplBase{
    @Autowired
    private RoomService roomService;

    @Override
    public void createRoom(CreateRoomRequest request,
                           StreamObserver<CreateRoomResponse> responseObserver) {
        try {
            var room = roomService.createRoom(request.getHosterUserId());
            CreateRoomResponse response = CreateRoomResponse.newBuilder()
                    .setRoomId(room.getRoomId())
                    .setJoinPassword(room.getJoinPassword())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void closeRoom(CloseRoomRequest request,
                          StreamObserver<CloseRoomResponse> responseObserver) {
        try {
            var room = roomService.closeRoom(request.getRoomId(), request.getHosterUserId());
            CloseRoomResponse response = CloseRoomResponse.newBuilder()
                    .setMessage("Room closed successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getRoom(GetRoomRequest request,
                        StreamObserver<GetRoomResponse> responseObserver) {
        try {
            var room = roomService.getRoom(request.getRoomId());
            if (room == null) {
                responseObserver.onError(new Exception("Room not found"));
            } else {
                GetRoomResponse response = GetRoomResponse.newBuilder()
                        .setRoomId(room.getRoomId())
                        .setHosterUserId(room.getHosterUserId())
                        .setJoinPassword(room.getJoinPassword())
                        .setStatus(room.getStatus().name())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void joinRoom(JoinRoomRequest request,
                         StreamObserver<JoinRoomResponse> responseObserver) {
        try {
            String message = roomService.joinRoom(request.getRoomId(), request.getPassword(), request.getUserId());
            JoinRoomResponse response = JoinRoomResponse.newBuilder()
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void leaveRoom(LeaveRoomRequest request,
                          StreamObserver<LeaveRoomResponse> responseObserver) {
        try {
            String message = roomService.leaveRoom(request.getRoomId(), request.getUserId());
            LeaveRoomResponse response = LeaveRoomResponse.newBuilder()
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
