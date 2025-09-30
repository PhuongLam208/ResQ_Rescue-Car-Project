import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import * as adminApi from "../../../../../admin";
import { toast } from "react-toastify";
import ChatBoxModal from "./ChatBoxModal";

const AdminRefund = () => {
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [showRefundModal, setShowRefundModal] = useState(false);
  const [refundMessage, setRefundMessage] = useState("");
  const [modalMode, setModalMode] = useState("REJECT"); // "REJECT" or "RECEIVE"
  const [selectedRefundId, setSelectedRefundId] = useState(null);
  const [selectedRefundStatus, setSelectedRefundStatus] = useState("");
  const [showChatModal, setShowChatModal] = useState(false);
  const [selectedConversationId, setSelectedConversationId] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    document.title = "ResQ - Refund";
    adminApi.fetchNavigate(navigate);
    fetchRefunds();
  }, [navigate]);

  const fetchRefunds = async () => {
    try {
      setLoading(true);
      const response = await adminApi.getAllRefunds();
      let result = response.data;
      for(const item of result){
        try{
          const payment = await adminApi.UserPaymentOfRefund(item.refundId);
          item.userPayment = payment.data;
        }catch(err){
          console.log("Check payment failed: "+err);
          item.userPayment = false; 
        }
      }
      const sorted = [...result].sort((a, b) => {
        // Sorts "PENDING" and "REQUESTED" statuses first
        const statusOrder = { "PENDING": 1, "REQUESTED": 2, "APPROVED": 3, "REJECT": 4 };
        const statusA = statusOrder[a.status] || 99;
        const statusB = statusOrder[b.status] || 99;

        if (statusA !== statusB) {
          return statusA - statusB;
        }
        // If statuses are the same, sort by refundId for consistent order
        return a.refundId - b.refundId;
      });
      setRefunds(sorted);
          console.log(sorted);
    } catch (error) {
      console.error("Error fetching refunds:", error);
      toast.error("Failed to fetch refunds.");
    } finally {
      setLoading(false);
    }
  };


  const openChatModal = (conversationId) => {
    setSelectedConversationId(conversationId);
    setShowChatModal(true);
  };

  const openModal = (id, status, mode) => {
    setSelectedRefundId(id);
    setSelectedRefundStatus(status);
    setModalMode(mode);
    setRefundMessage(""); // Clear message on opening
    setShowRefundModal(true);
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (keyword.trim() === '') {
      fetchRefunds();
    } else {
      try {
        setLoading(true);
        const response = await adminApi.searchRefundsByName(keyword);
        setRefunds(response.data);
      } catch (error) {
        console.error("Error searching refunds:", error);
        toast.error("Failed to search refunds.");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleRefundConfirm = async () => {
    try {
      setLoading(true);
      let response;
      if (modalMode === "REJECT") {
        response = await adminApi.RejectRefund(selectedRefundId, { message: refundMessage });
      } else { // modalMode === "RECEIVE"
        response = await adminApi.ReceivedRefund(selectedRefundId, { message: refundMessage });
      }

      if (response.status === 200 || response.status === 201) {
        fetchRefunds(); // Re-fetch data to update table
        toast.success(
          modalMode === "REJECT"
            ? "Refund rejected successfully!"
            : "Refund accepted successfully!"
        );
      } else {
        toast.error(`Operation failed: ${response.data?.message || 'Unknown error'}`);
      }

      setShowRefundModal(false);
    } catch (error) {
      console.error("Error handling refund:", error);
      toast.error("Something went wrong while processing the refund.");
    } finally {
      setLoading(false);
    }
  };

  // Helper function for status styling
  const getStatusClasses = (status) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800";
      case "REQUESTED":
        return "bg-blue-100 text-blue-800";
      case "RECIEVED":
        return "bg-green-100 text-green-800";
      case "REJECT":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="p-4 sm:p-6 w-100 min-h-screen">
      <h1 className="text-2xl sm:text-3xl font-bold text-gray-800 mb-6">Refund Management</h1>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="mb-6">
        <input
          type="text"
          placeholder="Search by staff/user name..."
          className="border border-gray-300 rounded-full px-4 py-2 w-full max-w-sm focus:outline-none focus:ring-2 focus:ring-[#013171] focus:border-transparent transition-all duration-200"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
      </form>

      {/* Refund Table */}
      {loading ? (
        <div className="text-center py-8 text-lg text-gray-600">Loading refunds...</div>
      ) : (
        // Thêm div với overflow-x-auto để bảng có thanh cuộn ngang
        <div className="overflow-x-auto rounded-lg border border-gray-200 shadow-sm">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-[#68A2F0] text-white">
              <tr>
                {/* Điều chỉnh padding và loại bỏ pl-[xxpx] cứng nhắc */}
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider rounded-tl-lg">ID</th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Submitted By</th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider whitespace-nowrap">Amount</th> {/* Đổi Request Amount thành Amount */}
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider whitespace-nowrap">Pay To</th> {/* Đổi Pay To Account thành Pay To */}
                {/* <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Reason</th> Đổi Refund Reason thành Reason */}
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Recipient</th>
                <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider">Status</th>
                <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider rounded-tr-lg">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {refunds.length > 0 ? (
                refunds.map((ref, index) => {
                  const isDisabled = ["REJECT", "APPROVED"].includes(ref.status);
                  const isApproved = ref.status === "APPROVED";
                  const isRejected = ref.status === "REJECT";
                  const showReject = ref.status === "PENDING";
                  const showAccept = ref.userPayment && !isDisabled && ref.status === "PENDING"; // Show accept only if payment account exists and not already processed

                  return (
                    <tr key={ref.refundId} className={`${isDisabled ? "bg-gray-50 text-gray-500" : "hover:bg-gray-100"} transition-colors duration-150`}>
                      <td className="px-2 py-3 text-center text-sm">{index + 1}</td>
                      <td className="px-2 py-3 text-sm whitespace-nowrap">{ref.staffName}</td>
                      <td className="px-2 py-3 text-sm whitespace-nowrap">
  {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ref.amount)}
</td>

                      <td className="px-2 py-3 text-sm whitespace-nowrap">{ref.userName}</td>
                      {/* <td className="px-4 py-3 text-sm max-w-[150px] overflow-hidden text-ellipsis">{ref.reason}</td> Thêm max-w và overflow để tránh tràn */}
                      <td className="px-2 py-3 text-sm whitespace-nowrap">{ref.recipientName}</td>
                      <td className="px-2 py-3 text-center">
                        <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusClasses(ref.status)}`}>
                          {ref.status}
                        </span>
                      </td>
                      <td className="px-2 py-3 text-center">
                        {/* Thay đổi flex-row trên breakpoint sm, mặc định flex-col để responsive tốt hơn */}
                        <div className="flex flex-col space-y-2 sm:flex-row sm:space-x-2 sm:space-y-0 items-center justify-center">
                          {/* Detailed Conversation Button */}
                          <button
                            onClick={() => openChatModal(ref.conversationId)}
                            className={`px-3 py-1 text-xs rounded-full border transition duration-200
                              ${isDisabled ? "text-gray-400 border-gray-300 bg-gray-100 cursor-not-allowed" : "text-gray-700 border-gray-700 hover:bg-gray-100 active:scale-95"}`}
                            disabled={isDisabled}
                          >
                            Conversation
                          </button>

                          {/* Accept Button (Conditional) */}
                          {showAccept && (
                            <button
                              onClick={() => openModal(ref.refundId, ref.status, "RECEIVE")}
                              className={`px-3 py-1 text-xs rounded-full transition duration-200
                                bg-[#013171] text-white hover:bg-blue-700 active:scale-95`}
                            >
                              Accept
                            </button>
                          )}
                           {/* Display status if already accepted and userPayment exists */}
                           {isApproved && ref.userPayment && ref.status === "PENDING" && (
                                <span className="px-3 py-1 text-xs rounded-full border border-green-600 text-green-700 bg-green-50">Accepted</span>
                           )}
                           {isApproved && !ref.userPayment && ref.status === "PENDING" && (
                                <span className="px-3 py-1 text-xs rounded-full border border-green-600 text-green-700 bg-green-50">Accepted (No Pmt Account)</span>
                           )}


                          {/* Reject Button (Conditional) */}
                          {!isRejected && showReject && ( // Only show Reject button if not already rejected
                            <button
                                onClick={() => openModal(ref.refundId, ref.status, "REJECT")}
                                className={`px-3 py-1 text-xs rounded-full transition duration-200
                                ${isDisabled ? "text-gray-400 border-gray-300 bg-gray-100 cursor-not-allowed" : "bg-red-600 text-white hover:bg-red-700 active:scale-95"}`}
                                disabled={isDisabled}
                            >
                                Reject
                            </button>
                          )}
                          {isRejected && ( // Show rejected status if already rejected
                             <span className="px-3 py-1 text-xs rounded-full border border-red-600 text-red-700 bg-red-50">Rejected</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan="8" className="px-4 py-6 text-center text-gray-500">
                    No refunds found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* ChatBoxModal - Reused without changes */}
      <ChatBoxModal
        show={showChatModal}
        handleClose={() => setShowChatModal(false)}
        conversationId={selectedConversationId}
      />

      {/* Refund Confirmation Modal (Tailwind CSS) */}
      {showRefundModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6 relative">
            <button
              className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
              onClick={() => setShowRefundModal(false)}
              aria-label="Close modal"
            >
              &times;
            </button>
            <h2 className="text-xl font-semibold mb-4 text-gray-800">
              {modalMode === "REJECT" ? "Reject Refund" : "Confirm Refund Reception"}
            </h2>
            <div className="mb-4">
              <label htmlFor="refundMessage" className="block text-gray-700 text-sm font-medium mb-2">
                {modalMode === "REJECT" ? "Reason for Rejection:" : "Message to User (optional):"}
              </label>
              <textarea
                id="refundMessage"
                rows="3"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#013171] focus:border-transparent resize-y"
                value={refundMessage}
                onChange={(e) => setRefundMessage(e.target.value)}
                placeholder={
                  modalMode === "REJECT"
                    ? "Explain why this refund is rejected..."
                    : "Thank the user or leave a note..."
                }
              ></textarea>
            </div>
            <div className="flex justify-end gap-3">
              <button
                className="bg-gray-300 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-400 transition-colors duration-200"
                onClick={() => setShowRefundModal(false)}
              >
                Cancel
              </button>
              <button
                className={`px-4 py-2 rounded-md transition-colors duration-200
                  ${modalMode === "REJECT" ? "bg-red-600 text-white hover:bg-red-700" : "bg-green-600 text-white hover:bg-green-700"}`}
                onClick={handleRefundConfirm}
              >
                {modalMode === "REJECT" ? "Reject" : "Confirm"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminRefund;