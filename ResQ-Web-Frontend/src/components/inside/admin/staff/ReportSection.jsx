import React, { useEffect, useState } from "react";
import AsyncSelect from "react-select/async";
import Swal from "sweetalert2";
import {
  createReport,
  getStaffById,
  getRequestRescue4Report
} from "../../../../../admin";

const ReportSection = () => {
  const [type, setType] = useState("CUSTOMER");
  const [staffId, setStaffId] = useState(null);
  const [staffName, setStaffName] = useState("Loading...");
  const [rridOptions, setRridOptions] = useState([]);
  const [selectedRrid, setSelectedRrid] = useState(null);
  const [form, setForm] = useState({ reason: "", request: "" });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    const fetchData = async () => {
      const id = localStorage.getItem("userId");
      if (id) {
        setStaffId(id);
        try {
          const staff = await getStaffById(id);
          setStaffName(staff ? `${staff.fullName} (${staff.username})` : "Không xác định");
        } catch (err) {
          console.error("Error fetching staff info:", err);
          setStaffName("Lỗi khi lấy thông tin nhân viên");
        }
      } else {
        setStaffName("Chưa đăng nhập");
      }

      try {
        const data = await getRequestRescue4Report();
        if (Array.isArray(data)) {
          const options = data.map((item) => ({
            value: item.rrid,
            label: `${item.rrid}: ${item.customerName} ⇄ ${item.partnerName}`,
            customer: { id: item.userId, name: item.customerName },
            partner: { id: item.partnerId, name: item.partnerName }
          }));
          setRridOptions(options);
        } else {
          console.warn("Data is not an array:", data);
          setRridOptions([]);
        }
      } catch (err) {
        console.error("Failed to load RRID list:", err);
        setRridOptions([]);
      }
    };

    fetchData();
  }, []);

  const handleSubmit = async (urgent = false) => {
    // Kiểm tra bắt buộc
    if (!selectedRrid || !staffId) {
      Swal.fire({
        icon: "warning",
        title: "Thiếu thông tin",
        text: "⚠ Vui lòng chọn RRID và đảm bảo đã đăng nhập!"
      });
      return;
    }

    const complainant = type === "CUSTOMER" ? selectedRrid.customer : selectedRrid.partner;
    const defendant = type === "CUSTOMER" ? selectedRrid.partner : selectedRrid.customer;

    const formData = new FormData();
    formData.append("complainantType", type);
    formData.append("complainantId", complainant.id);
    formData.append("defendantType", type === "CUSTOMER" ? "PARTNER" : "CUSTOMER");
    formData.append("defendantId", defendant.id);
    formData.append("reason", form.reason);
    formData.append("request", form.request);
    formData.append("staffid", staffId);
    formData.append("requestRescueId", selectedRrid.value);
    formData.append("within24H", urgent);

    try {
      const res = await createReport(formData);

      if (res.success) {
        Swal.fire({
          icon: "success",
          title: "✅ Gửi báo cáo thành công!",
          showConfirmButton: false,
          timer: 2000
        });
        setForm({ reason: "", request: "" });
        setSelectedRrid(null);
        setErrors({});
      } else {
        if (Array.isArray(res.errors) && res.errors.length > 0) {
          // Convert dạng ["field: message"] -> { field: message }
          const errObj = {};
          res.errors.forEach((msg) => {
            const [field, ...rest] = msg.split(":");
            errObj[field.trim()] = rest.join(":").trim();
          });
          setErrors(errObj);
        } else {
          Swal.fire({
            icon: "error",
            title: "❌ Gửi báo cáo thất bại",
            text: res.message || "Lỗi không xác định"
          });
        }
      }
    } catch (error) {
      console.error("Error submitting report:", error);
      Swal.fire({
        icon: "error",
        title: "❌ Lỗi khi gửi báo cáo",
        text: "Vui lòng thử lại sau!"
      });
    }
  };

  return (
    <div className="max-w-2xl mx-auto border p-6 rounded-lg shadow">
      <h2 className="text-xl font-bold text-center mb-6">Complaint Report Form</h2>

      <div className="mb-3">
        <label className="font-semibold">Staff:</label>{" "}
        <span className="ml-2">{staffName}</span>
      </div>

      <div className="mb-4">
        <label className="block font-semibold mb-1">Select RRID:</label>
        <AsyncSelect
          cacheOptions
          defaultOptions={rridOptions}
          loadOptions={(inputValue) =>
            Promise.resolve(
              rridOptions.filter((opt) =>
                opt.label.toLowerCase().includes(inputValue.toLowerCase())
              )
            )
          }
          value={selectedRrid}
          onChange={setSelectedRrid}
          placeholder="Select RRID..."
        />
        {errors.requestRescueId && (
          <div className="text-red-600 text-sm mt-1">{errors.requestRescueId}</div>
        )}
      </div>

      <div className="mb-4">
        <label className="block font-semibold mb-1">Complainant Type:</label>
        <select
          value={type}
          onChange={(e) => setType(e.target.value)}
          className="border rounded px-2 py-1 w-full"
        >
          <option value="CUSTOMER">CUSTOMER</option>
          <option value="PARTNER">PARTNER</option>
        </select>
        {errors.complainantType && (
          <div className="text-red-600 text-sm mt-1">{errors.complainantType}</div>
        )}
      </div>

      {selectedRrid && (
        <div className="mb-4">
          <div><strong>Complainant:</strong> {type === "CUSTOMER" ? selectedRrid.customer.name : selectedRrid.partner.name}</div>
          <div><strong>Defendant:</strong> {type === "CUSTOMER" ? selectedRrid.partner.name : selectedRrid.customer.name}</div>
        </div>
      )}

      <div className="mb-4">
        <label className="block font-semibold mb-1">Reason:</label>
        <textarea
          name="reason"
          value={form.reason}
          onChange={(e) => setForm({ ...form, reason: e.target.value })}
          className="w-full border rounded px-2 py-2 min-h-[60px]"
        />
        {errors.reason && (
          <div className="text-red-600 text-sm mt-1">{errors.reason}</div>
        )}
      </div>

      <div className="mb-6">
        <label className="block font-semibold mb-1">Request:</label>
        <textarea
          name="request"
          value={form.request}
          onChange={(e) => setForm({ ...form, request: e.target.value })}
          className="w-full border rounded px-2 py-2 min-h-[60px]"
        />
        {errors.request && (
          <div className="text-red-600 text-sm mt-1">{errors.request}</div>
        )}
      </div>

      <div className="flex justify-center gap-4">
        <button
          onClick={() => handleSubmit(true)}
          className="bg-red-600 hover:bg-red-700 text-white px-6 py-2 rounded"
        >
          Submit Urgent (24h)
        </button>
        <button
          onClick={() => handleSubmit(false)}
          className="bg-blue-800 hover:bg-blue-900 text-white px-6 py-2 rounded"
        >
          Submit Normally
        </button>
      </div>
    </div>
  );
};

export default ReportSection;
