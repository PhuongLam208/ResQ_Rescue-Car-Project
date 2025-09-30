import React, { useState } from "react";
import Swal from "sweetalert2";
import { addDiscount } from "../../../../../admin";

const AddDiscountForm = ({ onAdd, onCancel }) => {
  const [form, setForm] = useState({
    name: "",
    code: "",
    amount: "",
    type: "Percent",      // Percent hoặc Money
    type_dis: "",         // đúng tên backend
    applyDate: "",        // yyyy-MM-dd
    quantity: "",
    status: "Active",
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    try {
      const res = await addDiscount(form);

      if (res.success) {
        Swal.fire({
          icon: "success",
          title: "Success",
          text: "Discount added successfully!",
          timer: 1500,
          showConfirmButton: false,
        });
        onAdd(res.data);
      } else if (Array.isArray(res.errors) && res.errors.length) {
        // Mapping lỗi từ backend
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
    <div className="max-w-2xl mx-auto bg-white p-6">
      <h2 className="text-center text-2xl font-semibold mb-6 text-blue-800">
        Add New Discount Code
      </h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name */}
        <InputField
          label="Name"
          name="name"
          value={form.name}
          onChange={handleChange}
          error={errors.name}
        />

        {/* Code */}
        <InputField
          label="Code"
          name="code"
          value={form.code}
          onChange={handleChange}
          error={errors.code}
        />

        {/* Amount */}
        <InputField
          label="Amount"
          name="amount"
          type="number"
          value={form.amount}
          onChange={handleChange}
          error={errors.amount}
        />

        {/* Value Type */}
        <SelectField
          label="Value Type"
          name="type"
          value={form.type}
          onChange={handleChange}
          options={[
            { value: "Percent", label: "%" },
            { value: "Money", label: "VNĐ" }
          ]}
          error={errors.type}
        />

        {/* Discount Type */}
        <SelectField
          label="Discount Type"
          name="type_dis"
          value={form.type_dis}
          onChange={handleChange}
          options={[
            { value: "", label: "-- Select Type --" },
            { value: "toan_app", label: "App-wide" },
            { value: "hoi_vien", label: "Member-only" }
          ]}
          error={errors.type_dis}
        />

        {/* Apply Date */}
        <InputField
          label="Apply Date"
          name="applyDate"
          type="date"
          value={form.applyDate}
          onChange={handleChange}
          error={errors.applyDate}
        />

        {/* Quantity */}
        <InputField
          label="Quantity"
          name="quantity"
          type="number"
          value={form.quantity}
          onChange={handleChange}
          error={errors.quantity}
        />

        {/* Status */}
        <SelectField
          label="Status"
          name="status"
          value={form.status}
          onChange={handleChange}
          options={[
            { value: "Active", label: "Active" },
            { value: "Inactive", label: "Inactive" }
          ]}
          error={errors.status}
        />

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
            {loading ? "Saving..." : "Save"}
          </button>
        </div>
      </form>
    </div>
  );
};

// InputField component
const InputField = ({ label, name, type="text", value, onChange, error }) => (
  <div>
    <label className="block font-medium">{label}:</label>
    <input
      type={type}
      name={name}
      value={value}
      onChange={onChange}
      className="w-full border border-gray-300 rounded-full px-4 py-2"
    />
    {error && <p className="text-red-600 text-sm">{error}</p>}
  </div>
);

// SelectField component
const SelectField = ({ label, name, value, onChange, options, error }) => (
  <div>
    <label className="block font-medium">{label}:</label>
    <select
      name={name}
      value={value}
      onChange={onChange}
      className="w-full border border-gray-300 rounded-full px-4 py-2"
    >
      {options.map(opt => (
        <option key={opt.value} value={opt.value}>{opt.label}</option>
      ))}
    </select>
    {error && <p className="text-red-600 text-sm">{error}</p>}
  </div>
);

export default AddDiscountForm;
