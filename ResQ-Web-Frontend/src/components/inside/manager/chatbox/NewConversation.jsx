import React, { useEffect, useState } from "react";
import { Modal, Button, Form } from "react-bootstrap";
import * as api from "../../../../api.js";
import { partnerAPI, customerAPI } from "../../../../../admin.js"; 

const NewConversationModal = ({ show, handleClose, onCreated }) => {
  const [type, setType] = useState("customer"); // customer | partner
  const [customerList, setCustomerList] = useState([]);
  const [partnerList, setPartnerList] = useState([]);
  const [selectedId, setSelectedId] = useState("");

  useEffect(() => {
    if (show) {
      if (type === "customer") {
        customerAPI.getAllCustomers()
          .then(res => {
            if (res.status === 200) setCustomerList(res.data);
            else console.error("❌ Lỗi khi lấy danh sách khách hàng");
          })
          .catch(err => console.error("❌ Error fetching customers:", err));
      } else if (type === "partner") {
        partnerAPI.getAllPartners()
          .then(res => {
            if (res.status === 200) setPartnerList(res.data);
            else console.error("❌ Lỗi khi lấy danh sách đối tác");
          })
          .catch(err => console.error("❌ Error fetching partners:", err));
      }
    }
  }, [show, type]);

  const handleCreate = async () => {
    const recipientId = parseInt(localStorage.getItem("userId"));
    const senderId = parseInt(selectedId);

    if (!recipientId || recipientId === senderId) {
      alert("Vui lòng chọn người nhận hợp lệ.");
      return;
    }

    try {
      const response = await api.createConversation(senderId, recipientId, "Cuộc trò chuyện mới");

      if (response.status === 200) {
        alert("✅ Tạo cuộc trò chuyện thành công!");
        onCreated(response.data);
        handleClose();
        setSelectedId("");
      } else {
        alert(`❌ Tạo cuộc trò chuyện thất bại. Status: ${response.status}`);
      }
    } catch (err) {
      console.error("❌ Lỗi tạo cuộc trò chuyện:", err);
      alert("❌ Đã xảy ra lỗi khi tạo cuộc trò chuyện.");
    }
  };

  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>Tạo Cuộc Trò Chuyện Mới</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="mb-3">
          <Form.Check
            inline
            label="Customer"
            name="type"
            type="radio"
            checked={type === "customer"}
            onChange={() => {
              setType("customer");
              setSelectedId("");
            }}
          />
          <Form.Check
            inline
            label="Partner"
            name="type"
            type="radio"
            checked={type === "partner"}
            onChange={() => {
              setType("partner");
              setSelectedId("");
            }}
          />
        </div>

        {type === "customer" && (
          <Form.Select value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
            <option value="">-- Chọn khách hàng --</option>
            {customerList.map((user) => (
              <option key={user.userId} value={user.userId}>
                {user.fullName}
              </option>
            ))}
          </Form.Select>
        )}

        {type === "partner" && (
          <Form.Select value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
            <option value="">-- Chọn đối tác --</option>
            {partnerList.map((partner) => (
              <option key={partner.partnerId} value={partner.userId}>
                {partner.fullName}
              </option>
            ))}
          </Form.Select>
        )}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>Huỷ</Button>
        <Button variant="primary" onClick={handleCreate}>Tạo</Button>
      </Modal.Footer>
    </Modal>
  );
};

export default NewConversationModal;