import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import { getCustomerReports, blockUser } from "../../../../../admin";

const statusStyle = {
  "PENDING": "bg-yellow-200 text-yellow-800",
  "RESOLVED": "bg-green-200 text-green-800",
  "REJECTED": "bg-red-200 text-red-800",
};

const statusOptions = ["ALL", "PENDING", "RESOLVED", "REJECTED"];

const Violations = ({ customer }) => {
  const [reports, setReports] = useState([]);
  const [filterStatus, setFilterStatus] = useState("ALL");

  useEffect(() => {
    if (!customer || !customer.userId) return;

    const fetchReports = async () => {
      const response = await getCustomerReports(customer.userId);
      setReports(response.data.data);
    };

    fetchReports();
  }, [customer]);

  const filteredReports =
    filterStatus === "ALL"
      ? reports
      : reports.filter((r) => r.status === filterStatus);

  const handleBlockUser = async () => {
    const confirm = await Swal.fire({
      title: 'Are you sure?',
      text: `You are about to block user ${customer?.name || "this user"}.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Block',
      cancelButtonText: 'Cancel',
      customClass: {
        popup: 'rounded-xl p-4',
        confirmButton: 'bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded mr-2',
        cancelButton: 'bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded',
      }
    });

    if (confirm.isConfirmed) {
      try {
        const res = await blockUser(customer.userId);
        console.log('Block API response:', res);
        Swal.fire('Blocked!', 'User has been blocked.', 'success');
      } catch (error) {
        console.error('Failed to block user:', error);
        Swal.fire('Error!', 'Failed to block user.', 'error');
      }
    }
  };

  const handleViewDetails = async (report) => {
    const buttons = report.status === "PENDING"
      ? {
          confirmButtonText: "Resolve",
          denyButtonText: "Reject",
          showDenyButton: true,
          showCancelButton: true,
          cancelButtonText: "Close",
        }
      : {
          confirmButtonText: "Close",
          showCancelButton: false,
          showDenyButton: false,
        };

    const result = await Swal.fire({
      title: `Report #${report.reportId}`,
      html: `
        <div class="text-left">
          <p><strong>Name:</strong> ${report.name || "N/A"}</p>
          <p><strong>Description:</strong> ${report.description || "N/A"}</p>
          <p><strong>Status:</strong> ${report.status}</p>
          <p><strong>Created At:</strong> ${report.createdAt ? new Date(report.createdAt).toLocaleString("en-GB") : "N/A"}</p>
        </div>
      `,
      icon: "info",
      ...buttons,
      customClass: {
        popup: 'rounded-xl p-4',
        confirmButton: 'bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded mr-2',
        denyButton: 'bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded',
        cancelButton: 'bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded',
      },
    });

    if (result.isConfirmed && report.status === "PENDING") {
      const { value: response } = await Swal.fire({
        title: 'Resolve Report',
        input: 'textarea',
        inputLabel: 'Response to user',
        inputPlaceholder: 'Type your response here...',
        showCancelButton: true,
        confirmButtonText: "Submit",
        cancelButtonText: "Cancel",
        customClass: {
          popup: 'rounded-xl p-4',
          confirmButton: 'bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded mr-2',
          cancelButton: 'bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded',
        }
      });
      if (response) {
        console.log(`Resolved report #${report.reportId} with response: ${response}`);
        // TODO: Call API to update status + response
        Swal.fire("Success!", "Report has been resolved.", "success");
      }
    } else if (result.isDenied) {
      const { value: response } = await Swal.fire({
        title: 'Reject Report',
        input: 'textarea',
        inputLabel: 'Response to user',
        inputPlaceholder: 'Type your response here...',
        showCancelButton: true,
        confirmButtonText: "Submit",
        cancelButtonText: "Cancel",
        customClass: {
          popup: 'rounded-xl p-4',
          confirmButton: 'bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded mr-2',
          cancelButton: 'bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded',
        }
      });
      if (response) {
        console.log(`Rejected report #${report.reportId} with response: ${response}`);
        // TODO: Call API to update status + response
        Swal.fire("Success!", "Report has been rejected.", "success");
      }
    }
  };

  return (
    <div className="mt-4">
      <div className="flex items-center justify-between mb-3">
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="border rounded px-3 py-1 text-sm"
        >
          {statusOptions.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
        <button
          onClick={handleBlockUser}
          className="bg-red-500 hover:bg-red-600 text-white text-xs px-3 py-1 rounded"
        >
          Block User
        </button>
      </div>
      <div className="overflow-x-auto rounded-lg shadow-md">
        <table className="min-w-full text-sm text-left bg-white">
          <thead className="bg-blue-500 text-white">
            <tr>
              <th className="px-4 py-2">Date</th>
              <th className="px-4 py-2">Reason</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Note</th>
              <th className="px-4 py-2">Report Details</th>
            </tr>
          </thead>
          <tbody>
            {filteredReports.length === 0 ? (
              <tr>
                <td colSpan="5" className="text-center py-4">
                  No reports found.
                </td>
              </tr>
            ) : (
              filteredReports.map((report) => (
                <tr key={report.reportId} className="border-b">
                  <td className="px-4 py-2">
                    {report.createdAt
                      ? new Date(report.createdAt).toLocaleDateString("en-GB")
                      : "N/A"}
                  </td>
                  <td className="px-4 py-2">{report.name || "N/A"}</td>
                  <td className="px-4 py-2">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        statusStyle[report.status] || "bg-gray-200 text-gray-800"
                      }`}
                    >
                      {report.status || "Unknown"}
                    </span>
                  </td>
                  <td className="px-4 py-2">{report.description || "N/A"}</td>
                  <td className="px-4 py-2">
                    <button
                      onClick={() => handleViewDetails(report)}
                      className="bg-blue-400 text-white text-xs px-3 py-1 rounded hover:bg-blue-500"
                    >
                      View Details
                    </button>
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

export default Violations;
