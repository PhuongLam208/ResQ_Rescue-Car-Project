import React, { useState, useEffect } from "react";
import AddDiscountForm from "./AddDiscountForm";
import UpdateDiscount from "./UpdateDiscount";
import {
  getAllDiscount,
  searchDiscounts,
  deactivateDiscount
} from "../../../../../admin";
import "../../../../styles/admin/general.css";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";

const MainDiscount = () => {
  const [discounts, setDiscounts] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editDiscount, setEditDiscount] = useState(null);
  const [statusFilter, setStatusFilter] = useState("all");
  const [typeFilter, setTypeFilter] = useState("all");
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    loadAllDiscounts();
  }, []);

  const loadAllDiscounts = async () => {
    try {
      const data = await getAllDiscount();
      const mapped = data.map(d => ({
        ...d,
        typeDis: d.type_dis
      }));
      setDiscounts(mapped);
    } catch (error) {
      console.error("Failed to load discounts:", error);
    }
  };

  useEffect(() => {
    const delay = setTimeout(() => {
      handleSearch();
    }, 500);
    return () => clearTimeout(delay);
  }, [searchTerm]);

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      loadAllDiscounts();
      return;
    }
    try {
      const res = await searchDiscounts(searchTerm);
      if (res?.data) {
        const mapped = res.data.map(d => ({
          ...d,
          typeDis: d.type_dis
        }));
        setDiscounts(mapped);
      } else {
        setDiscounts([]);
      }
    } catch (error) {
      console.error("Error when searching:", error);
    }
  };

  const handleUpdateDiscount = (updated) => {
    setDiscounts(prev => prev.map(d => (d.id === updated.id ? updated : d)));
    setEditDiscount(null);
  };

  const handleDeactivate = async (id) => {
    const confirm = await Swal.fire({
      title: "Are you sure?",
      text: "Do you want to deactivate this discount?",
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#3085d6",
      confirmButtonText: "Yes, deactivate it!"
    });

    if (confirm.isConfirmed) {
      try {
        const res = await deactivateDiscount(id);
        if (res?.data) {
          setDiscounts(prev =>
            prev.map(d => (d.id === id ? { ...d, status: "Inactive" } : d))
          );
          Swal.fire("Deactivated!", "Discount has been deactivated.", "success");
        }
      } catch (error) {
        console.error("Error when deactivating:", error);
        Swal.fire("Error", "Failed to deactivate discount.", "error");
      }
    }
  };

  const filteredDiscounts = discounts.filter(item => {
    const matchStatus = statusFilter === "all" || (item.status?.toLowerCase() === statusFilter);
    const matchType = typeFilter === "all" || item.typeDis === typeFilter;
    return matchStatus && matchType;
  });

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {!showForm && !editDiscount && (
        <>
          <div className="relative mb-6">
            <h1 className="text-3xl font-bold text-center text-gray-800">
              Discount Code Management
            </h1>
            <button
              onClick={() => setShowForm(true)}
              className="absolute right-0 top-0 px-5 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
            >
              ➕ Add New Discount
            </button>

            <div className="allsearch flex items-center mt-4 justify-center">
              <input
                type="text"
                className="input-search border rounded px-3 py-1"
                placeholder="Search..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <img
                src="/images/icon-web/Search.png"
                className="click-search cursor-pointer ml-2"
                onClick={handleSearch}
                alt="Search"
              />
            </div>
          </div>
        </>
      )}

      {showForm ? (
        <AddDiscountForm
          onAdd={(newDiscount) => {
            setDiscounts(prev => [...prev, newDiscount]);
            setShowForm(false);
          }}
          onCancel={() => setShowForm(false)}
        />
      ) : editDiscount ? (
        <UpdateDiscount
          discount={editDiscount}
          onUpdate={handleUpdateDiscount}
          onCancel={() => setEditDiscount(null)}
        />
      ) : (
        <>
          <div className="mb-4 flex flex-wrap justify-center gap-4">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-1 border rounded text-sm"
            >
              <option value="all">All Status</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="px-3 py-1 border rounded text-sm"
            >
              <option value="all">All Types</option>
              <option value="toan_app">App-wide</option>
              <option value="hoi_vien">Member-only</option>
            </select>
          </div>

          <div className="border rounded-2xl shadow-lg p-4 bg-white overflow-x-auto">
            <h2 className="text-xl font-semibold mb-4 text-blue-600">All Discount Codes</h2>
            <table className="min-w-full text-sm border border-gray-200 shadow rounded-xl">
              <thead className="bg-blue-50">
                <tr>
                  <th className="p-3 border text-left">No.</th>
                  <th className="p-3 border text-left">Code</th>
                  <th className="p-3 border text-left">Name</th>
                  <th className="p-3 border text-left">Value</th>
                  <th className="p-3 border text-left">Type</th>
                  <th className="p-3 border text-left">Status</th>
                  <th className="p-3 border text-left">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredDiscounts.map((item, index) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="p-3 border">{index + 1}</td>
                    <td className="p-3 border">{item.code}</td>
                    <td className="p-3 border">{item.name}</td>
                    <td className="p-3 border">
                      {item.amount}{item.type === "Percent" ? "%" : "đ"}
                    </td>
                    <td className="p-3 border">
                      {item.typeDis === "toan_app" ? "App-wide" : "Member-only"}
                    </td>
                    <td className="p-3 border capitalize">{item.status || "--"}</td>
                    <td className="p-3 border space-x-2">
                      <button
                        onClick={() => setEditDiscount(item)}
                        className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs hover:bg-yellow-200"
                      >
                        Edit
                      </button>
                      {item.status?.toLowerCase() !== "inactive" && (
                        <button
                          onClick={() => handleDeactivate(item.id)}
                          className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs hover:bg-red-200"
                        >
                          Deactivate
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
};

export default MainDiscount;
