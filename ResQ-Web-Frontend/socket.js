import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

let stompClient = null;

export const connectSocket = (conversationId, onMessageReceived) => {
  const socket = new SockJS('http://localhost:9090/ws');
  stompClient = new Client({
    webSocketFactory: () => socket,
    onConnect: () => {
      console.log('✅ WebSocket connected');
      if (conversationId) {
        stompClient.subscribe(`/topic/conversations/${conversationId}`, (message) => {
          const newMsg = JSON.parse(message.body);
          onMessageReceived(newMsg);
        });
      }
    },
    onDisconnect: () => {
      console.log('❌ WebSocket disconnected');
    },
    onWebSocketError: (error) => {
      console.error('🚫 WebSocket error', error);
    },
    debug: (str) => console.log(str),
  });

  stompClient.activate();
};

export const disconnectSocket = () => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
  }
};

export const sendMessage = (message) => {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message),
    });
  } else {
    console.warn('⚠️ STOMP client chưa kết nối!');
  }
};