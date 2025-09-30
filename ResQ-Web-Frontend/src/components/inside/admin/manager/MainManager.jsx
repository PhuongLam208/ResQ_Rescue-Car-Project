import React, { useEffect, useState } from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";
import { managerAPI } from "../../../../../admin";
import ManagerDetail from "./ManagerDetail";
import NewManager from "./FormManager";

const MainManager = () => {
  const [managers, setManagers] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [selectedManager, setSelectManager] = useState(null);
  const [isEdit, setIsEdit] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [statusFilter, setStatusFilter] = useState("");
  const [sortField, setSortField] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 15;

  const fetchManagers = async () => {
    try {
      const response = await managerAPI.getManagers();
      setManagers(response.data);
    } catch (err) {
      console.error("Cannot get managers: " + err);
    }
  };

  const searchManagers = async () => {
    try {
      if (keyword.trim() === "") {
        fetchManagers();
      } else {
        const response = await managerAPI.searchManager(keyword);
        setManagers(response.data);
      }
    } catch (err) {
      console.error("Cannot search managers: " + err);
    }
  };

  const handleViewDetail = (manager) => {
    setSelectManager(manager);
  };

  const handleEdit = (manager) => {
    setSelectManager(manager);
    setIsCreating(true);
    setIsEdit(true);
  };

  const handleBack = () => {
    setSelectManager(null);
    setIsEdit(false);
    setIsCreating(false);
    fetchManagers();
  };

  const toggleSort = (field) => {
    if (sortField === field) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortOrder("asc");
    }
  };

  const filteredAndSortedManagers = [...managers]
    .filter((manager) => {
      return statusFilter ? manager.status.toLowerCase() === statusFilter.toLowerCase() : true;
    })
    .sort((a, b) => {
      if (!sortField) return 0;
      const valueA = sortField === "response" ? a.responseTime || 0 : sortField === "lates" ? a.monthLateCount || 0 : 0;
      const valueB = sortField === "response" ? b.responseTime || 0 : sortField === "lates" ? b.monthLateCount || 0 : 0;
      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });

  const currentManager = filteredAndSortedManagers.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );
  const totalPages = Math.ceil(filteredAndSortedManagers.length / itemsPerPage);

  useEffect(() => {
    fetchManagers();
  }, []);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      searchManagers();
    }, 20);
    return () => clearTimeout(delayDebounce);
  }, [keyword]);

  return (
    <div className="p-6">
      {isCreating ? (
        <NewManager onBack={handleBack} manager={selectedManager} isEdit={isEdit} />
      ) : selectedManager ? (
        <ManagerDetail manager={selectedManager} onBack={handleBack} />
      ) : (
        <div className="space-y-6">
          <div className="flex flex-wrap justify-between gap-4 items-center">
            <button
              className="bg-[#013171] text-white rounded-full px-5 py-2 text-sm"
              onClick={() => setIsCreating(true)}
            >
              + New Manager
            </button>
            <form
              onSubmit={(e) => e.preventDefault()}
              className="flex items-center border border-gray-300 rounded-full px-4 py-2 w-full max-w-md"
            >
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                type="text"
                placeholder="Search..."
                className="flex-grow outline-none bg-transparent"
              />
              <button type="submit">
                <img src="/images/icon-web/Search.png" className="h-5" alt="Search" />
              </button>
            </form>
            <div className="border border-gray-300 rounded-full px-3 py-2">
              <select
                className="bg-transparent outline-none"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="">--- Status ---</option>
                <option value="Waiting">Waiting</option>
                <option value="Active">Active</option>
                <option value="Deactive">Deactive</option>
                <option value="24h">24h</option>
                <option value="Blocked">Blocked</option>
              </select>
            </div>
          </div>

          <div className="overflow-x-auto">
           <table className="w-full table-auto border border-gray-200 rounded overflow-hidden shadow-sm text-xs">
              <thead className="bg-[#68A2F0] text-white">
  <tr>
    <th className="px-4 py-3 text-left w-[50px]">ID</th>
    <th className="px-4 py-3 text-left max-w-[150px] truncate">Full Name</th>
    <th className="px-4 py-3 text-left max-w-[120px] truncate">Phone No.</th>
    <th className="px-4 py-3 text-left max-w-[180px] truncate">Email</th>
    <th className="px-4 py-3 text-center w-[120px]">
      Monthly Lates
      <button onClick={() => toggleSort("lates")} className="ml-1 text-sm">⬍</button>
    </th>
    <th className="px-4 py-3 text-center w-[100px]">Status</th>
    <th className="px-4 py-3 text-center w-[150px]">
      Response Time
      <button onClick={() => toggleSort("response")} className="ml-1 text-sm">⬍</button>
    </th>
    <th className="px-4 py-3 text-center w-[130px]">Action</th>
  </tr>
</thead>
<tbody>
  {currentManager.map((manager, index) => (
    <tr key={manager.staffId} className="border-t hover:bg-gray-50">
      <td className="px-4 py-3">{index + 1}</td>
      <td className="px-4 py-3 max-w-[150px] truncate">{manager.fullName}</td>
      <td className="px-4 py-3 max-w-[120px] truncate">{manager.sdt}</td>
      <td className="px-4 py-3 max-w-[180px] truncate">{manager.email}</td>
      <td className="px-4 py-3 text-center">{manager.monthLateCount}</td>
      <td className="px-4 py-3 text-center">
        <span className={`text-xs px-3 py-1 rounded-full ${getUserStatus(manager.status)}`}>
          {manager.status}
        </span>
      </td>
      <td className="px-4 py-3 text-center">{manager.responseTime?.toFixed(2)} min</td>
      <td className="px-4 py-3 text-center">
        <div className="flex gap-2 justify-center">
          <button
            className="flex items-center gap-1 border border-blue-500 text-blue-500 px-3 h-7 rounded-full hover:bg-blue-50"
            onClick={() => handleViewDetail(manager)}
          >
           View
          </button>
          <button
            className="flex items-center gap-1 border border-gray-400 text-gray-600 px-3 h-7 rounded-full hover:bg-gray-100"
            onClick={() => handleEdit(manager)}
          >
            <img src="/images/icon-web/edit.png" className="w-4 h-4" alt="edit" /> Edit
          </button>
        </div>
      </td>
    </tr>
  ))}
</tbody>

            </table>
          </div>

          {filteredAndSortedManagers.length > itemsPerPage && (
            <div className="flex justify-center items-center gap-4 mt-4">
              <button onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={currentPage === 1}>
                <img
                  src={`/images/icon-web/${currentPage === 1 ? "Back To" : "Back To1"}.png`}
                  alt="Back"
                  className="w-8"
                />
              </button>
              <span className="font-semibold text-sm">
                Page {currentPage} of {totalPages}
              </span>
              <button onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))} disabled={currentPage >= totalPages}>
                <img
                  src={`/images/icon-web/${currentPage >= totalPages ? "Next page" : "Next page1"}.png`}
                  alt="Next"
                  className="w-8"
                />
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MainManager;
