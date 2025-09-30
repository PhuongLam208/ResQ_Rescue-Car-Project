import React, { useEffect, useState } from "react";
import { getCustomerTransactions } from "../../../../../admin";

const statusStyle = {
  "COMPLETED": "bg-green-200 text-green-800",
  "PAID": "bg-green-200 text-green-800",
  "FAILED": "bg-red-200 text-red-800"
};

const Transactions = ({ customer }) => {
  const [transactions, setTransactions] = useState([]);
  const [filterStatus, setFilterStatus] = useState("ALL");

  useEffect(() => {
    if (!customer || !customer.userId) return;

    const fetchData = async () => {
      const response = await getCustomerTransactions(customer.userId);
      setTransactions(response.data.data)
    };

    fetchData();
  }, [customer]);

  // Lọc transactions dựa trên filterStatus
  const filteredTransactions = filterStatus === "ALL"
    ? transactions
    : transactions.filter(txn => txn.status === filterStatus);

  return (
    <div className="mt-4">
      {/* Filter dropdown */}
      <div className="mb-3 flex items-center gap-2">
        <label htmlFor="statusFilter" className="text-sm">Filter by Status:</label>
        <select
          id="statusFilter"
          className="border rounded px-2 py-1 text-sm"
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
        >
          <option value="ALL">All</option>
          <option value="COMPLETED">Completed</option>
          <option value="PAID">Paid</option>
          <option value="FAILED">Failed</option>
        </select>
      </div>

      <div className="overflow-x-auto rounded-lg shadow-md">
        <table className="min-w-full text-xs text-left bg-white">
          <thead className="bg-blue-500 text-white">
            <tr>
              <th className="px-4 py-2">Bill ID</th>
              <th className="px-4 py-2">Rescue Type</th>
              <th className="px-4 py-2">Total Before Discount</th>
              <th className="px-4 py-2">Total After Discount</th>
              <th className="px-4 py-2">Payment Method</th>
              <th className="px-4 py-2">Created At</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {filteredTransactions.length === 0 ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  No transactions found.
                </td>
              </tr>
            ) : (
              filteredTransactions.map((txn) => (
                <tr key={txn.billId} className="border-b">
                  <td className="px-4 py-2">{txn.billId}</td>
                  <td className="px-4 py-2">{txn.rescueType || "N/A"}</td>
                  <td className="px-4 py-2">
                    {txn.totalPrice?.toLocaleString() || "N/A"} VND
                  </td>
                  <td className="px-4 py-2">
                    {txn.total?.toLocaleString() || "N/A"} VND
                  </td>
                  <td className="px-4 py-2">{txn.paymentMethod || "N/A"}</td>
                  <td className="px-4 py-2">
                    {txn.createdAt
                      ? new Date(txn.createdAt).toLocaleString("en-GB", {
                          day: "2-digit",
                          month: "2-digit",
                          year: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                          second: "2-digit"
                        })
                      : "N/A"}
                  </td>
                  <td className="px-4 py-2">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        statusStyle[txn.status] || "bg-gray-200 text-gray-800"
                      }`}
                    >
                      {txn.status || "Unknown"}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Transactions;
