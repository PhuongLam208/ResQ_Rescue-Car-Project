import React, { useState } from "react";
import Swal from "sweetalert2";
import { updateDiscount } from '../../../../../admin';

const UpdateDiscount = ({ discount, onUpdate, onCancel }) => {
  const [form, setForm] = useState({
    name: discount.name || "",
    code: discount.code || "",
    amount: discount.amount || "",
    type: discount.type || "Percent",          // Percent hoặc Money
    type_dis: discount.type_dis || "",
    applyDate: discount.applyDate ? discount.applyDate.split("T")[0] : "",
    quantity: discount.quantity || "",
    status: discount.status || "Active"
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    try {
      const today = new Date().toISOString().split("T")[0];
      if (form.applyDate < today) {
        Swal.fire("Error", "Ngày phát hành không được ở quá khứ.", "error");
        setLoading(false);
        return;
      }

      const res = await updateDiscount(discount.id, form);

      if (res.success) {
        Swal.fire({
          icon: "success",
          title: "Success",
          text: "Discount updated successfully!",
          timer: 1500,
          showConfirmButton: false,
        });
        onUpdate(res.data);
      } else if (Array.isArray(res.errors) && res.errors.length) {
        const fieldErrors = {};
        res.errors.forEach(msg => {
          const lower = msg.toLowerCase();
          if (lower.includes("name")) fieldErrors.name = msg;
          else if (lower.includes("code")) fieldErrors.code = msg;
          else if (lower.includes("amount")) fieldErrors.amount = msg;
          else if (lower.includes("type_dis")) fieldErrors.type_dis = msg;
          else if (lower.includes("type")) fieldErrors.type = msg;
          else if (lower.includes("apply date")) fieldErrors.applyDate = msg;
          else if (lower.includes("quantity")) fieldErrors.quantity = msg;
          else if (lower.includes("status")) fieldErrors.status = msg;
          else fieldErrors._other = msg; // lỗi chung không rõ field
        });
        setErrors(fieldErrors);

        if (fieldErrors._other) {
          Swal.fire({
            icon: "error",
            title: "Validation Error",
            text: fieldErrors._other,
          });
        }
      } else {
        Swal.fire({
          icon: "error",
          title: "Error",
          text: res.message || "Có lỗi xảy ra!",
        });
      }
    } catch (err) {
      console.error("Unexpected error:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "Unexpected error happened!",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto bg-white p-6 border border-gray-300 rounded-xl shadow">
      <h2 className="text-center text-2xl font-semibold mb-6 text-blue-800">
        Update Discount Code
      </h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block font-medium">Name:</label>
          <input
            type="text"
            name="name"
            value={form.name}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          />
          {errors.name && <p className="text-red-600 text-sm">{errors.name}</p>}
        </div>

        <div>
          <label className="block font-medium">Code:</label>
          <input
            type="text"
            name="code"
            value={form.code}
            disabled
            className="w-full border border-gray-300 rounded-full px-4 py-2 bg-gray-100 cursor-not-allowed"
          />
          {errors.code && <p className="text-red-600 text-sm">{errors.code}</p>}
        </div>

        <div>
          <label className="block font-medium">Amount:</label>
          <input
            type="number"
            name="amount"
            value={form.amount}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          />
          {errors.amount && <p className="text-red-600 text-sm">{errors.amount}</p>}
        </div>

        <div>
          <label className="block font-medium">Value Type:</label>
          <select
            name="type"
            value={form.type}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          >
            <option value="Percent">%</option>
            <option value="Money">VNĐ</option>
          </select>
          {errors.type && <p className="text-red-600 text-sm">{errors.type}</p>}
        </div>

        <div>
          <label className="block font-medium">Discount Type:</label>
          <select
            name="type_dis"
            value={form.type_dis}
            disabled
            className="w-full border border-gray-300 rounded-full px-4 py-2 bg-gray-100 cursor-not-allowed"
          >
            <option value="toan_app">App-wide</option>
            <option value="hoi_vien">Member-only</option>
          </select>
          {errors.type_dis && <p className="text-red-600 text-sm">{errors.type_dis}</p>}
        </div>

        <div>
          <label className="block font-medium">Apply Date:</label>
          <input
            type="date"
            name="applyDate"
            value={form.applyDate}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          />
          {errors.applyDate && <p className="text-red-600 text-sm">{errors.applyDate}</p>}
        </div>

        <div>
          <label className="block font-medium">Quantity:</label>
          <input
            type="number"
            name="quantity"
            value={form.quantity}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          />
          {errors.quantity && <p className="text-red-600 text-sm">{errors.quantity}</p>}
        </div>

        <div>
          <label className="block font-medium">Status:</label>
          <select
            name="status"
            value={form.status}
            onChange={handleChange}
            className="w-full border border-gray-300 rounded-full px-4 py-2"
          >
            <option value="Active">Active</option>
            <option value="Inactive">Inactive</option>
          </select>
          {errors.status && <p className="text-red-600 text-sm">{errors.status}</p>}
        </div>

        <div className="flex justify-between">
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="bg-gray-500 text-white px-6 py-2 rounded-full hover:bg-gray-600"
          >
            Back
          </button>
          <button
            type="submit"
            disabled={loading}
            className={`px-6 py-2 rounded-full text-white ${loading ? "bg-gray-400 cursor-not-allowed" : "bg-blue-800 hover:bg-blue-900"}`}
          >
            {loading ? "Updating..." : "Update"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default UpdateDiscount;
