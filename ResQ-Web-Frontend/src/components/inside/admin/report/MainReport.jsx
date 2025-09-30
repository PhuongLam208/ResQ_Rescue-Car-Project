import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import { getAllReport, resolveReport } from "../../../../../admin";

const MainReport = ({ onShowDetails }) => {
  const [complaints, setComplaints] = useState([]);
  const [statusFilter, setStatusFilter] = useState("all");
  const [within24HFilter, setWithin24HFilter] = useState("all");

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    const reports = await getAllReport();
    setComplaints(reports.data.data);
  };

  const handleActionClick = async (item, action) => {
    const status = action === "Resolve" ? "resolved" : "rejected";
    const resolverId = parseInt(localStorage.getItem("userId"), 10);

    const { value: responseText } = await Swal.fire({
      title: `${action} Report`,
      input: "textarea",
      inputLabel: "Response to complainant",
      inputPlaceholder: "Type your response here...",
      showCancelButton: true,
      confirmButtonText: action,
      cancelButtonText: "Cancel",
      inputValidator: (value) => {
        if (!value) return "Response is required!";
      },
    });

    if (responseText) {
      try {
        const payload = {
          status,
          resolverId,
          responseToComplainant: responseText,
        };
        await resolveReport(item.reportId, payload);
        Swal.fire("Success!", `Report ${status.toUpperCase()} successfully.`, "success");
        fetchReports();
      } catch (error) {
        Swal.fire("Error", "Failed to update the report.", error.message || "Unknown error");
      }
    }
  };

  const getActions = (item) => {
    const actions = ["View Complaint"];
    if (item.status?.toLowerCase() === "pending") {
      actions.push("Resolve", "Reject");
    }
    return actions;
  };

  const getStatusBadgeStyle = (status) => {
    switch (status.toLowerCase()) {
      case "pending":
        return "bg-yellow-100 text-yellow-700";
      case "resolved":
        return "bg-green-100 text-green-700";
      case "rejected":
        return "bg-red-100 text-red-700";
      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  // Áp dụng filter
  const filteredComplaints = complaints.filter((item) => {
    const statusMatch =
      statusFilter === "all" || (item.status && item.status.toLowerCase() === statusFilter);
    const within24HMatch =
      within24HFilter === "all" ||
      (within24HFilter === "yes" && item.within24H) ||
      (within24HFilter === "no" && !item.within24H);
    return statusMatch && within24HMatch;
  });

  // Sắp xếp ưu tiên
  const sortedComplaints = filteredComplaints.sort((a, b) => {
    if (a.within24H && !b.within24H) return -1;
    if (!a.within24H && b.within24H) return 1;
    if (a.status === "pending" && b.status !== "pending") return -1;
    if (a.status !== "pending" && b.status === "pending") return 1;
    return 0;
  });

  return (
    <div className="bg-white p-4 rounded-lg shadow">
      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-4 text-xs">
        <div>
          <label className="mr-2 text-gray-600">Filter Status:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="border rounded px-2 py-1"
          >
            <option value="all">All</option>
            <option value="pending">Pending</option>
            <option value="resolved">Resolved</option>
            <option value="rejected">Rejected</option>
          </select>
        </div>
        <div>
          <label className="mr-2 text-gray-600">Within 24h:</label>
          <select
            value={within24HFilter}
            onChange={(e) => setWithin24HFilter(e.target.value)}
            className="border rounded px-2 py-1"
          >
            <option value="all">All</option>
            <option value="yes">Yes</option>
            <option value="no">No</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full text-xs text-left">
          <thead className="bg-blue-600 text-white">
            <tr>
              <th className="px-3 py-2">#</th>
              <th className="px-3 py-2">Complainant</th>
              <th className="px-3 py-2">Defendant</th>
              <th className="px-3 py-2">Type</th>
              <th className="px-3 py-2">Staff</th>
              <th className="px-3 py-2">Resolver</th>
              <th className="px-3 py-2">Reason</th>
              <th className="px-3 py-2">Within 24h</th>
              <th className="px-3 py-2">Status</th>
              <th className="px-3 py-2">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {sortedComplaints.map((item, index) => (
              <tr key={index} className="hover:bg-gray-50">
                <td className="px-3 py-2">{index + 1}</td>
                <td className="px-3 py-2">{item.complainantName || "--"}</td>
                <td className="px-3 py-2">{item.defendantName || "--"}</td>
                <td className="px-3 py-2 uppercase">{item.complainantType || "--"}</td>
                <td className="px-3 py-2">{item.staffName || "--"}</td>
                <td className="px-3 py-2">{item.resolverName || "--"}</td>
                <td className="px-3 py-2 text-gray-700">{item.reason || "--"}</td>
                <td className="px-3 py-2">
                  {item.within24H ? (
                    <span className="text-red-600 font-semibold">Yes</span>
                  ) : (
                    <span className="text-gray-500">No</span>
                  )}
                </td>
                <td className="px-3 py-2">
                  <span
                    className={`px-2 py-1 rounded-full font-medium ${getStatusBadgeStyle(
                      item.status || "unknown"
                    )}`}
                  >
                    {(item.status || "UNKNOWN").toUpperCase()}
                  </span>
                </td>
                <td className="px-3 py-2 flex flex-wrap gap-1">
                  {getActions(item).map((action, i) => {
                    const base = "px-2 py-1 text-xs rounded font-semibold";
                    const style =
                      action === "View Complaint"
                        ? "bg-blue-100 text-blue-700 hover:bg-blue-200"
                        : action === "Resolve"
                        ? "bg-green-100 text-green-700 hover:bg-green-200"
                        : "bg-red-100 text-red-700 hover:bg-red-200";
                    return (
                      <button
                        key={i}
                        className={`${base} ${style}`}
                        onClick={() =>
                          action === "View Complaint"
                            ? onShowDetails(item)
                            : handleActionClick(item, action)
                        }
                      >
                        {action}
                      </button>
                    );
                  })}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default MainReport;
