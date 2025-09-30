import React, { useState, useEffect } from "react";
import Swal from "sweetalert2";
import withReactContent from "sweetalert2-react-content";
import { getReportedPartner } from "../../../../../manager"; // Đảm bảo đúng đường dẫn

const MySwal = withReactContent(Swal);

// Chuẩn hóa trạng thái từ backend (in hoa) về format đẹp
const normalizeStatus = (status) => {
  switch (status?.toUpperCase()) {
    case "PENDING":
      return "Pending";
    case "RESOLVED":
      return "Resolved";
    case "REJECTED":
      return "Rejected";
    default:
      return "Unknown";
  }
};

// Gán màu nền + chữ tương ứng với status
const getStatusStyle = (status) => {
  switch (status) {
    case "Pending":
      return "bg-yellow-100 text-yellow-700";
    case "Resolved":
      return "bg-green-100 text-green-700";
    case "Rejected":
      return "bg-red-100 text-red-700";
    default:
      return "bg-gray-100 text-gray-700";
  }
};

const Violations = ({ partner }) => {
  const [violations, setViolations] = useState([]);

  const handleStatusChange = (violation, newStatus) => {
    const updated = violations.map((v) =>
      v === violation ? { ...v, status: normalizeStatus(newStatus) } : v
    );
    setViolations(updated);
  };

  const handleDetailClick = (violation) => {
    const isFinal =
      violation.status === "Resolved" || violation.status === "Rejected";

    MySwal.fire({
      title: "Violation Details",
      html: (
        <div className="text-left space-y-2">
          <p><strong>📅 Date:</strong> {violation.date}</p>
          <p><strong>❌ Reason:</strong> {violation.reason}</p>
          <p><strong>📋 Description:</strong> {violation.report}</p>
          <p>
            <strong>📌 Status:</strong>{" "}
            <span className={`px-2 py-1 rounded ${getStatusStyle(violation.status)}`}>
              {violation.status}
            </span>
          </p>
        </div>
      ),
      showCancelButton: true,
      showDenyButton: !isFinal,
      showConfirmButton: !isFinal,
      confirmButtonText: "✅ Resolve",
      denyButtonText: "❌ Reject",
      cancelButtonText: "Close",
      customClass: {
        popup: "rounded-xl",
      },
    }).then((result) => {
      if (result.isConfirmed) {
        handleStatusChange(violation, "RESOLVED");
        Swal.fire("✔️ Resolved!", "", "success");
      } else if (result.isDenied) {
        handleStatusChange(violation, "REJECTED");
        Swal.fire("🚫 Rejected!", "", "info");
      }
    });
  };

  useEffect(() => {
    const fetchViolations = async () => {
      const data = await getReportedPartner(partner.partnerId);
      const mapped = data.data.data.map((item) => ({
        date: item.createdAt?.slice(0, 10),
        reason: item.name,
        status: normalizeStatus(item.status),
        report: item.description,
      }));
      setViolations(mapped);
    };

    if (partner.partnerId) fetchViolations();
  }, [partner]);

  return (
    <div className="p-6 relative min-h-screen">
      <h1 className="text-xl font-bold mb-4 text-blue-700">
        Violation Report List
      </h1>

      <div className="overflow-x-auto bg-white rounded-lg shadow-md">
        <table className="w-full table-auto">
          <thead className="bg-blue-400 text-white">
            <tr>
              <th className="px-4 py-3 text-left">Date</th>
              <th className="px-4 py-3 text-left">Violation Reason</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Description</th>
              <th className="px-4 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody>
            {violations.length === 0 ? (
              <tr>
                <td colSpan="5" className="text-center py-6 text-gray-500">
                  No violation reports found.
                </td>
              </tr>
            ) : (
              violations.map((item, index) => (
                <tr key={index} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3">{item.date}</td>
                  <td className="px-4 py-3">{item.reason}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`text-xs px-3 py-1 rounded-full font-medium ${getStatusStyle(
                        item.status
                      )}`}
                    >
                      {item.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">{item.report}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => handleDetailClick(item)}
                      className="text-xs font-medium px-3 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200"
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
