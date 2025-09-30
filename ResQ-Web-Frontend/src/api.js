import axios from "axios";

export const api = axios.create({
    baseURL: 'http://localhost:9090/api',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Auth
export const login = (user) => api.post('/resq/login', user);

// Message APIs
export const fetchConversationByUserId = (userId) =>
  api.get(`/messages/conversation/user/${userId}`);

export const fetchAllConversations = (userId) =>
  api.get(`/messages/conversation/user/all/${userId}`);

export const fetchConversationById = (conversationId) =>
  api.get(`/messages/conversation/${conversationId}`);

export const fetchMessages = (conversationId) =>
  api.get(`/messages/${conversationId}`);

export const markMessagesAsRead = (conversationId, readerId) =>
  api.post(`/messages/${conversationId}/mark-as-read?readerId=${readerId}`);

export const sendMessage = (params) =>
  api.post('/messages/send', null, { params });

export const closeConversation = (conversationId) =>
  api.post(`/messages/conversation/${conversationId}/close`);


// Notification Template APIs
export const fetchNotificationTemplates = () =>
  api.get('/resq/notification-templates');

export const createNotificationTemplate = (dto) =>
  api.post('/resq/notification-templates', dto);

export const updateNotificationTemplate = (id, dto) =>
  api.put(`/resq/notification-templates/${id}`, dto);

export const createConversation = (senderId, recipientId, subject) =>
  api.post("/messages/conversation/create", null, {
    params: { senderId, recipientId, subject },
  });
