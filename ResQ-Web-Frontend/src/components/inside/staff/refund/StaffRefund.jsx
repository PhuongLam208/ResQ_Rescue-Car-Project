import React from "react";
import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import * as staffApi from "../../../../../staff";
import { fetchConversationByUserId } from "../../../../api";

const StaffRefund = () => {

    const [userName, setUserName] = useState("");
    const [userID, setUserID] = useState("");
    const [userType, setUserType] = useState("customer");
    const [userList, setUserList] = useState([]);
    const [RRID, setRRID] = useState("");
    const [reason, setReason] = useState("");
    const [amount, setAmount] = useState(0);
    const [senderId] = useState(localStorage.getItem("userId"));
    const [rescueList, setRescueList] = useState([]);
    const [conversations, setConversations] = useState([]);
    const [errors, setErrors] = useState({});

    useEffect(() => {
        const fetchUsers = async () => {
        try {
            const res = userType === "customer"
            ? await staffApi.getAllCustomers()
            : await staffApi.getAllPartners();

            if (res.status === 200) {
            setUserList(res.data);

            }
        } catch (err) {
            console.error("Failed to fetch users:", err);
        }
        };
        fetchUsers();
    }, [userType]);

    useEffect(() => {
        const fetchRescueTrips = async () => {
            if (!userID) return;

            try {
                const res = userType === "customer"
                    ? await staffApi.searchRRByUser(userID)
                    : await staffApi.searchRRByPartner(userID);

                if (res.status === 200) {
                    const filtered = res.data.filter(trip => trip.endTime); 
                    setRescueList(filtered);
                }
            } catch (error) {
            console.error(" Error fetching rescue trips:", error);
            }
        };

        fetchRescueTrips();
    }, [userID, userType]);

    useEffect(() => {
    
        if (userID ) {
            fetchConversationByUserId(userID)
            .then((res) => {
                if (res.status === 200 && res.data) {
                    setConversations([res.data]); 
                } else {
                    setConversations([]);
                }
            })
            .catch((err) => {
                console.error("Error fetching conversation:", err);
                setConversations([]);
            });
        } else {
            setConversations([]);
        }
    }, [userID]);

      const handleSend = async (e) => {
        e.preventDefault();

        const newErrors = {};

        if (!userID) newErrors.userID = "Please select a user.";
        if (!RRID) newErrors.RRID = "Please select a rescue trip.";
        if (!reason.trim()) newErrors.reason = "Reason is required.";
        if (!amount || isNaN(amount) || parseFloat(amount) <= 0) newErrors.amount = "Amount must be greater than 0.";
        if (!conversations.length) newErrors.conversations = "Please select a conversation.";

        setErrors(newErrors);
        if (Object.keys(newErrors).length > 0) {
            return; 
        }

    setErrors(newErrors);
        try {
            const payload = {
                senderId: parseInt(senderId),
                rrid: parseInt(RRID),
                userId: parseInt(userID),
                reason: reason.trim(),
                amount: parseFloat(amount),
            };

            const response = await staffApi.createRefund(payload);

        // console.log("Response:", response);
        // console.log("Payload:", payload);

            if (response.status === 201) {
                alert("Refund request sent successfully");
            } else {
                alert("Failed to send refund request");
            }
        } catch (error) {
            console.error("Error sending refund request:", error);
        }
    };

    return (
        <div className="mx-auto mt-10 px-[10px] py-6 bg-white rounded">
            <h1 className="text-center text-2xl font-bold mb-6">Refund Request</h1>

            <div className="mb-4 w-[800px] ml-[250px] text-left">
                {/* Staff info */}
                <label className="block font-semibold pb-3 mb-1">
                    Staff :
                    <input
                        type="text"
                        id="staffname"
                        className="ml-6"
                        value={localStorage.getItem("fullName")}
                        disabled
                    />
                </label>

                {/* User Type Selection */}
                <label className="block font-semibold pb-3 mb-1">
                    User Type :
                    <select
                        value={userType}
                        onChange={(e) => {
                            setUserType(e.target.value);
                            setUserName("");
                            setUserID();
                            setRescueList([]);
                            setRRID("");
                        }}
                        className="ml-6 border-2 border-gray-300 rounded-xl px-4 py-1"
                    >
                        <option value="customer">Customer</option>
                        <option value="partner">Partner</option>
                    </select>
                </label>

                {/* User Dropdown */}
                <label className="block font-semibold pb-3 mb-1">
                    Select {userType} :
                    <select
                        value={userID}
                        onChange={(e) => {
                            setUserID(e.target.value);
                            const selectedUser = userList.find(u => u.userId === parseInt(e.target.value));
                            setUserName(selectedUser?.fullName || "");
                        }}
                        className="ml-6 border-2 border-gray-300 rounded-xl px-4 py-1"
                    >
                        <option value="">-- Select --</option>
                        {userList.map((u) => (
                        <option key={u.userId} value={u.userId}>
                            {u.fullName || `#${u.userId}`}
                        </option>
                        ))}
                    </select>
                    {errors.userID && <p className="text-red-500 ml-6 mt-1 text-sm">{errors.userID}</p>}
                </label>

                {/* Rescue Trip ID */}
                <label className="block font-semibold pb-3 mb-1">Rescue trip:
                    <select className="ml-6 border px-2 py-1" value={RRID} onChange={(e) => setRRID(e.target.value)}>
                        <option value="">-- Select Rescue Trip --</option>
                        {rescueList.map((r) => (
                        <option key={r.rrid} value={r.rrid}>
                            #{r.rrid} - Ended at: {new Date(r.endTime).toLocaleString()}
                        </option>
                        ))}
                    </select>
                    {errors.RRID && <p className="text-red-500 ml-6 text-sm">{errors.RRID}</p>}
                </label>

                {/* Reason */}
                <label className="block font-semibold pb-3 mb-1">Reason:</label>
                    <textarea
                    id="reason"
                    className="border-2 border-gray-300 rounded-xl px-[10px] h-[130px] w-[680px]"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    ></textarea>
                {errors.reason && <p className="text-red-500 text-sm mt-1">{errors.reason}</p>}

                {/* Amount */}
                <label className="block font-semibold py-3 mb-1">
                    Amount :
                    <input
                        type="number"
                        id="amount"
                        className="ml-6 border-2 border-gray-300 rounded-xl px-[10px]"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                    />
                </label>
                {errors.amount && <p className="text-red-500 ml-6 text-sm">{errors.amount}</p>}

                {/* Link Conversation */}
                <label className="block font-semibold pb-3 mb-1">
                    Detailed Conversation:
                    <select
                        className="ml-6 border-2 border-gray-300 rounded-xl px-2 py-1"
                        disabled={!conversations.length}
                    >
                        <option value="">-- Select conversation --</option>
                        {conversations.map((conv) => (
                        <option key={conv.conversationId} value={conv.conversationId}>
                            {conv.subject || `Conversation #${conv.conversationId}`} - {new Date(conv.updatedAt).toLocaleString()}
                        </option>
                        ))}
                    </select>
                    {errors.conversations && <p className="text-red-500 ml-6 text-sm">{errors.conversations}</p>}
                </label>

                {/* Send button */}
                <div className="flex justify-end pr-[140px] mt-4">
                <button
                    onClick={handleSend}
                    type="submit"
                    className="bg-[#013171] text-white px-8 py-2 rounded-3xl shadow-md hover:font-semibold hover:shadow-lg active:scale-95 duration-200"
                >
                    Send
                </button>
                </div>
            </div>
            </div>
        );
    };

export default StaffRefund;