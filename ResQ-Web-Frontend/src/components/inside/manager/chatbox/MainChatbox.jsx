import React, { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import * as api from "../../../../api.js";
import NewConversationModal from "./NewConversation";
import { Button } from "react-bootstrap";

const ChatBox = () => {
  const [userId, setUserId] = useState(null);
  const [conversationId, setConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const messageEndRef = useRef(null);
  const [conversation, setConversation] = useState(null);
  const [conversations, setConversations] = useState([]);
  const [selectedPartner, setSelectedPartner] = useState(null);
  const [stompClient, setStompClient] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const id = parseInt(localStorage.getItem("userId"));
    setUserId(id);

    if (id) {
      api
        .fetchConversationByUserId(id)
        .then((res) => {
          if (res.status === 200) {
            setConversation(res.data);
            setConversationId(res.data.conversationId);
          } else {
            console.error("⚠️ fetchConversationByUserId status:", res.status);
          }
        })
        .catch((err) => console.error("❌ Error getting conversation:", err));

      api
        .fetchAllConversations(id)
        .then((res) => {
          if (res.status === 200) {
            setConversations(res.data);
          } else {
            console.error("⚠️ fetchAllConversations status:", res.status);
          }
        })
        .catch((err) => console.error("❌ Error getting all conversations:", err));
    }
  }, []);

  useEffect(() => {
    if (!conversationId) return;

    api
      .fetchMessages(conversationId)
      .then((res) => {
        if (res.status === 200) {
          setMessages(res.data);
        } else {
          console.error("⚠️ fetchMessages status:", res.status);
        }
      })
      .catch((err) => console.error("❌ Error fetching messages:", err));
  }, [conversationId]);

  useEffect(() => {
    const socket = new SockJS("http://localhost:9090/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log("✅ WebSocket connected");
        client.subscribe(`/topic/conversations/${conversationId}`, (message) => {
          const newMsg = JSON.parse(message.body);
          setMessages((prev) => [...prev, newMsg]);
          api.fetchMessages(conversationId)
            .then((res) => {
              if (res.status === 200) setMessages(res.data);
              else console.error("⚠️ fetchMessages (socket) status:", res.status);
            })
            .catch((err) => console.error("❌ Error fetching messages (socket):", err));
        });
      },
      debug: (str) => console.log(str),
    });
    client.activate();
    setStompClient(client);

    return () => client.deactivate();
  }, [conversationId]);

  useEffect(() => {
    if (messageEndRef.current) {
      messageEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  const handleSendMessage = () => {
    const MAX_LENGTH = 1000;
    if (!messageInput.trim()) return;
    if (messageInput.length > MAX_LENGTH) {
      alert(`Your message is too long. Maximum allowed is ${MAX_LENGTH} characters.`);
      return;
    }
    if (!stompClient || !stompClient.connected) {
      console.warn("🚫 STOMP client chưa sẵn sàng");
      return;
    }

    const message = {
      conversationId,
      senderId: userId,
      recipientId: conversation.recipientId,
      content: messageInput,
    };

    stompClient.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify(message),
    });

    setMessageInput("");
  };

  const handleCloseConversation = async () => {
    try {
      const res = await api.closeConversation(conversationId);
      if (res.status === 200) {
        alert("✅ The conversation has been closed.");

        // Clear the current conversation panel
        setConversation(null);
        setConversationId(null);
        setMessages([]);
        setSelectedPartner(null);

        // Reload conversation list
        const convRes = await api.fetchAllConversations(userId);
        if (convRes.status === 200) {
          setConversations(convRes.data);
        } else {
          console.error("⚠️ Failed to reload conversation list, status:", convRes.status);
        }

      } else {
        console.error("⚠️ Failed to close conversation, status:", res.status);
        alert("❌ Failed to close the conversation.");
      }
    } catch (error) {
      console.error("❌ Error closing conversation:", error);
      alert("❌ An error occurred while closing the conversation.");
    }
  };

  const filteredConversations = Array.isArray(conversations)
  ? conversations.filter(
      (conv) =>
        !conv.isClosed && // ✅ lọc ra những conversation chưa bị đóng
        (conv.partnerName || "").toLowerCase().includes(searchTerm.toLowerCase())
    )
  : [];

  return (
    <div className="flex h-[730px] bg-white font-sans">
      <div className="w-72 border-r bg-[#FAFAFA] p-4 overflow-y-auto">
        <Button variant="success" onClick={() => setShowModal(true)}>
          Create New Conversation
        </Button>
        <NewConversationModal
          show={showModal}
          handleClose={() => setShowModal(false)}
          onCreated={(newConv) => {
            setConversations(prev => [...prev, newConv]);
            setConversationId(newConv.conversationId);
          }}
        />
        <input
          type="text"
          placeholder="Tìm kiếm đối tác..."
          className="w-full px-4 py-2 border rounded-lg text-sm mb-4 focus:outline-none"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        {filteredConversations.map((conv) => {
          const partnerName = conv.partnerName || (conv.senderId === userId ? `Staff #${conv.recipientId}` : `User #${conv.senderId}`);
          const partnerAvatar = conv.partnerAvatar || "images/icon-web/avatar.jpg";

          return (
            <div
              key={conv.conversationId}
              className="flex items-center gap-3 p-3 rounded-lg cursor-pointer hover:bg-gray-100"
              onClick={async () => {
                try {
                  const res = await api.fetchConversationById(conv.conversationId);
                  setConversation(res);
                  setConversationId(conv.conversationId);
                  setSelectedPartner({
                    name: partnerName,
                    avatar: partnerAvatar,
                    online: conv.partnerOnline === true
                  });
                } catch (error) {
                  console.error('❌ Lỗi khi tải hội thoại:', error);
                }
              }}
            >
              <img
                src={conv.partnerAvatar && conv.partnerAvatar !== 'uploads/null' ? `http://localhost:9090/${conv.partnerAvatar}` : '/images/icon-web/avatar.jpg'}
                alt="avatar"
                className="w-9 h-9 rounded-full"
              />
              <div className="flex-1">
                <p className="text-sm font-semibold text-blue-700">{partnerName}</p>
                <p className="text-xs text-gray-500">{conv.subject}</p>
              </div>
              <p className="text-xs text-gray-400">
                {conv.updatedAt && new Date(conv.updatedAt).toLocaleTimeString([], {
                  hour: '2-digit', minute: '2-digit'
                })}
              </p>
            </div>
          );
        })}
      </div>

      <div className="flex-1 flex flex-col bg-white">
        {selectedPartner ? (
          <>
            <div className="flex items-center gap-3 border-b px-6 py-4">
              <img
                src={selectedPartner.avatar && selectedPartner.avatar !== 'uploads/null' ? `http://localhost:9090/${selectedPartner.avatar}` : '/images/avatar.jpg'}
                alt="avatar"
                className="w-10 h-10 rounded-full"
              />
              <div>
                <p className="font-bold text-blue-800 text-lg">{selectedPartner.name}</p>
                <p className={`text-sm ${selectedPartner.online === true ? 'text-green-500' : 'text-gray-400'}`}>
                  ● {selectedPartner.online === true ? 'Đang hoạt động' : 'Không hoạt động'}
                </p>
              </div>
            </div>

            <div className="flex-1 px-6 py-6 overflow-auto space-y-4">
              {messages.map((msg, index) => {
                const isOwn = msg.senderId === userId;
                const messageDate = new Date(msg.createdAt).toLocaleDateString();
                const showDate = index === 0 || messageDate !== new Date(messages[index - 1].createdAt).toLocaleDateString();

                return (
                  <div key={index}>
                    {showDate && <div className="text-center text-xs text-gray-400 mb-4">{messageDate}</div>}
                    <div className={`flex ${isOwn ? 'justify-end' : 'justify-start'} items-start`}>
                      {!isOwn && (
                        <img
                          src={selectedPartner.avatar && selectedPartner.avatar !== 'uploads/null' ? `http://localhost:9090/${selectedPartner.avatar}` : '/images/avatar.jpg'}
                          alt="avatar"
                          className="w-10 h-10 rounded-full"
                        />
                      )}
                      <div className="flex flex-col max-w-sm">
                        <p className={`text-xs mb-1 font-semibold ${isOwn ? 'text-blue-700 text-right' : 'text-gray-600'}`}>
                          {msg.senderName}{msg.senderRole === 'staff' ? ' - staff' : ''}
                        </p>
                        <div className={`rounded-2xl px-5 py-3 text-sm shadow-sm ${isOwn ? 'bg-blue-500 text-white rounded-br-md self-end' : 'bg-gray-100 text-black rounded-bl-md'}`}>
                          <p>{msg.content}</p>
                          <p className="text-[11px] mt-1 text-right">
                            {msg.createdAt && new Date(msg.createdAt).toLocaleTimeString([], {
                              hour: '2-digit', minute: '2-digit'
                            })}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
              <div ref={messageEndRef} />
            </div>

            <div className="px-6 py-4 border-t bg-white flex items-center shrink-0">
              {conversation?.isClosed ? (
                <div className="w-full text-center text-gray-400 italic">
                  Cuộc trò chuyện này đã kết thúc. Không thể gửi thêm tin nhắn.
                </div>
              ) : (
                <>
                  <input
                    type="text"
                    placeholder="Viết tin nhắn ở đây"
                    className="flex-1 border rounded-full px-5 py-3 text-sm border-gray-300 focus:outline-none"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleSendMessage();
                      }
                    }}
                  />
                  <button onClick={handleSendMessage} className="ml-3 bg-blue-500 hover:bg-blue-600 text-white p-3 rounded-full">
                    <img src="/images/Up11.png" alt="Gửi" className="w-5 h-5 transform -rotate-45" />
                  </button>
                  <button onClick={handleCloseConversation} className="ml-2 bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-full">
                    Đóng hội thoại
                  </button>
                </>
              )}
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-400">
            <p className="text-xl">Hãy chọn một cuộc trò chuyện bên trái</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatBox;
