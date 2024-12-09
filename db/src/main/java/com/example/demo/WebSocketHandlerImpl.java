package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    // 사용자 세션 관리 (username -> session)
    private static ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 방 관리 (roomId -> username 리스트)
    private static ConcurrentHashMap<String, List<String>> roomMap = new ConcurrentHashMap<>();

    // 사용자 카운트 (순차 매칭을 위해 사용)
    private static int userCount = 0;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientMessage = message.getPayload();
        JSONObject jsonMessage = new JSONObject(clientMessage);
        String type = jsonMessage.getString("type");

        if ("check".equals(type)) { // 사용자 등록 및 방 배정
            String username = jsonMessage.getString("message");
            sessionMap.put(username, session);
            assignUserToRoom(username);
        } else if ("chat".equals(type)) { // 메시지 전송
            String username = jsonMessage.getString("username");
            String chatMessage = jsonMessage.getString("message");
            sendMessageToRoom(username, chatMessage);
        } else if ("out".equals(type)) { // 사용자 연결 해제
            String username = jsonMessage.getString("message");
            removeUserFromRoom(username);
            sessionMap.remove(username);
        }
    }

    /**
     * 사용자를 방에 배정 (홀수/짝수 순서 기반)
     */
    private void assignUserToRoom(String username) {
        userCount++; // 사용자 수 증가

        if (userCount % 2 == 1) { // 홀수 번째 사용자: 새로운 방 생성
            String newRoomId = "room" + ((userCount / 2) + 1); // 예: room1, room2...
            List<String> newRoom = new ArrayList<>();
            newRoom.add(username);
            roomMap.put(newRoomId, newRoom);
            System.out.println("Created new room: " + newRoomId + " with user: " + username);
        } else { // 짝수 번째 사용자: 이전 방에 입장
            String lastRoomId = "room" + (userCount / 2); // 마지막 생성된 방에 추가
            List<String> usersInRoom = roomMap.get(lastRoomId);
            if (usersInRoom != null) {
                usersInRoom.add(username);
                System.out.println("User " + username + " joined room " + lastRoomId);
            } else {
                System.out.println("Error: Room not found for user " + username);
            }
        }
    }

    /**
     * 방의 모든 사용자에게 메시지 전송
     */
    private void sendMessageToRoom(String username, String message) {
        String userRoomId = getUserRoomId(username);
        if (userRoomId == null) {
            System.out.println("User " + username + " is not in any room.");
            return;
        }

        List<String> usersInRoom = roomMap.get(userRoomId);
        for (String user : usersInRoom) {
            WebSocketSession session = sessionMap.get(user);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(username + " : " + message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 사용자가 속한 방 ID를 반환
     */
    private String getUserRoomId(String username) {
        for (String roomId : roomMap.keySet()) {
            if (roomMap.get(roomId).contains(username)) {
                return roomId;
            }
        }
        return null;
    }

    /**
     * 사용자를 방에서 제거
     */
    private void removeUserFromRoom(String username) {
        String userRoomId = getUserRoomId(username);
        if (userRoomId != null) {
            List<String> usersInRoom = roomMap.get(userRoomId);
            usersInRoom.remove(username);
            if (usersInRoom.isEmpty()) {
                roomMap.remove(userRoomId);
                System.out.println("Room " + userRoomId + " is empty and removed.");
            } else {
                System.out.println("User " + username + " left room " + userRoomId);
            }
        }
    }
}
