import React, { useEffect, useState } from "react";
import { Pencil, Plus, X } from "lucide-react";
import axios from "axios";

const NotificationTable = () => {
  const [templates, setTemplates] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editTemplateId, setEditTemplateId] = useState(null);
  const [formData, setFormData] = useState({ notiType: "", title: "" });
  const [searchTerm, setSearchTerm] = useState("");

  const handleSearchChange = (e) => {
  setSearchTerm(e.target.value);
};


  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = () => {
    const token = localStorage.getItem("token");
    axios
      .get("http://localhost:9090/api/resq/notification-templates", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => setTemplates(res.data))
      .catch((err) => console.error(err));
  };

  const openAddModal = () => {
    setFormData({ notiType: "", title: "" });
    setEditTemplateId(null);
    setIsEditing(false);
    setShowModal(true);
  };

  const openEditModal = (template) => {
    setFormData({
      notiType: template.notitype,
      title: template.title,
    });
    setEditTemplateId(template.notificationTemplateID);
    setIsEditing(true);
    setShowModal(true);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");

      const config = {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      };

      if (isEditing) {
        await axios.put(
          `http://localhost:9090/api/notification-templates/${editTemplateId}`,
          formData,
          config
        );
      } else {
        await axios.post(
          "http://localhost:9090/api/notification-templates",
          formData,
          config
        );
      }

      fetchTemplates(); // refetch list
      setShowModal(false);
      setFormData({ notiType: "", title: "" }); // reset form
    } catch (err) {
      console.error("Submit failed:", err);
    }
  };

  const filteredTemplates = templates.filter((item) =>
  item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
  item.notitype.toLowerCase().includes(searchTerm.toLowerCase())
);


  return (
    <div className="bg-white p-6 rounded-xl shadow-md">
      {/* Search + Add */}
      <div className="flex justify-between items-center mb-4 gap-4 flex-wrap">
        <div className="flex gap-4">
          <input
  type="text"
  placeholder="Tìm kiếm theo Type hoặc Title..."
  value={searchTerm}
  onChange={handleSearchChange}
  className="border px-4 py-2 rounded-lg w-full max-w-xs"
/>
        </div>
        <button
          onClick={openAddModal}
          className="bg-green-500 hover:bg-green-600 text-white flex items-center gap-2 px-4 py-2 rounded-lg"
        >
          <Plus size={16} /> Thêm Template
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto rounded-xl">
        <table className="min-w-full border-collapse border border-gray-200">
          <thead>
            <tr className="bg-[#68A2F0] text-white">
              <th className="px-4 py-2 text-left">ID</th>
              <th className="px-4 py-2 text-left">Type</th>
              <th className="px-4 py-2 text-left">Title</th>
              <th className="px-4 py-2 text-left">Action</th>
            </tr>
          </thead>
         <tbody>
  {filteredTemplates.map((item, index) => (
    <tr key={index} className="border-b hover:bg-gray-100">
      <td className="px-4 py-2">{item.notificationTemplateID}</td>
      <td className="px-4 py-2">{item.notitype}</td>
      <td className="px-4 py-2">{item.title}</td>
      <td className="px-4 py-2 flex gap-2">
        <button
          onClick={() => openEditModal(item)}
          className="p-2 hover:bg-gray-200 rounded-lg"
        >
          <Pencil size={16} />
        </button>
      </td>
    </tr>
  ))}
</tbody>

        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white p-6 rounded-xl w-full max-w-md shadow-xl relative">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-2 right-2 text-gray-600 hover:text-black"
            >
              <X size={20} />
            </button>
            <h2 className="text-xl font-bold mb-4">
              {isEditing ? "Chỉnh sửa Template" : "Thêm Template Mới"}
            </h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <input
                type="text"
                name="notiType"
                value={formData.notiType}
                onChange={handleChange}
                placeholder="Notification Type"
                className="border px-4 py-2 rounded-lg"
                required
              />
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Title"
                className="border px-4 py-2 rounded-lg"
                required
              />
              <button
                type="submit"
                className="bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600"
              >
                {isEditing ? "Cập nhật" : "Tạo mới"}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationTable;
