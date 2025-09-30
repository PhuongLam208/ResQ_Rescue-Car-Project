import React, { useEffect, useState, useRef } from "react";
import { Modal, Button } from "react-bootstrap";
import * as api from "../../../../api";

const ChatBoxModal = ({ show, handleClose, conversationId }) => {
  const [messages, setMessages] = useState([]);
  const messageEndRef = useRef(null);

  useEffect(() => {
    if (conversationId && show) {
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
    }
  }, [conversationId, show]);

  useEffect(() => {
    if (messageEndRef.current) {
      messageEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  return (
    <Modal show={show} onHide={handleClose} centered size="lg">
      <Modal.Header closeButton>
        <Modal.Title>Detailed Conversation</Modal.Title>
      </Modal.Header>
      <Modal.Body style={{ maxHeight: "70vh", overflowY: "auto" }}>
        <div className="space-y-4 px-3 py-2">
          {messages.map((msg, index) => {
            const isOwn = msg.senderRole === "staff";
            const messageDate = new Date(msg.createdAt).toLocaleDateString();
            const showDate =
              index === 0 ||
              messageDate !==
                new Date(messages[index - 1].createdAt).toLocaleDateString();

            return (
              <div key={index}>
                {showDate && (
                  <div className="text-center text-xs text-gray-400 mb-2">
                    {messageDate}
                  </div>
                )}
                <div className={`flex ${isOwn ? "justify-end" : "justify-start"}`}>
                  <div className="flex flex-col max-w-[70%]">
                    <p
                      className={`text-xs mb-1 font-semibold ${
                        isOwn ? "text-blue-700 text-right" : "text-gray-600"
                      }`}
                    >
                      {msg.senderName}
                    </p>
                    <div
                      className={`rounded-2xl px-5 py-3 text-sm shadow-sm ${
                        isOwn
                          ? "bg-blue-500 text-white rounded-br-md self-end"
                          : "bg-gray-100 text-black rounded-bl-md"
                      }`}
                    >
                      <p>{msg.content}</p>
                      <p className="text-[11px] mt-1 text-right">
                        {msg.createdAt &&
                          new Date(msg.createdAt).toLocaleTimeString([], {
                            hour: "2-digit",
                            minute: "2-digit",
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
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Đóng
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ChatBoxModal;
