import React, { useEffect, useState } from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";
import { staffAPI } from "../../../../../admin";
import StaffDetail from "./StaffDetail";
import NewStaff from "./FormStaff";

const MainStaff = () => {
  const [keyword, setKeyword] = useState('');
  const [staffs, setStaffs] = useState([]);
  const [isEdit, setIsEdit] = useState(false);
  const [selectedStaff, setSelectedStaff] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [statusFilter, setStatusFilter] = useState("");
  const [sortField, setSortField] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);

  const itemsPerPage = 15;

  const fetchStaffs = async () => {
    try {
      const response = await staffAPI.getStaffs();
      setStaffs(response.data);
    } catch (err) {
      console.error("Cannot get staffs: " + err);
    }
  };

  const searchStaffs = async () => {
    try {
      if (keyword.trim() === "") {
        fetchStaffs();
      } else {
        const response = await staffAPI.searchStaff(keyword);
        setStaffs(response.data);
      }
    } catch (err) {
      console.error("Cannot search staffs: " + err);
    }
  };

  const toggleSort = (field) => {
    if (sortField === field) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortOrder("asc");
    }
  };

  const handleViewDetail = (staff) => {
    setSelectedStaff(staff);
  };

  const handleEdit = (staff) => {
    setSelectedStaff(staff);
    setIsCreating(true);
    setIsEdit(true);
  };

  const handleBack = () => {
    setSelectedStaff(null);
    setIsEdit(false);
    setIsCreating(false);
    fetchStaffs();
  };

  const filteredAndSortedStaffs = [...staffs]
    .filter((staff) => {
      return statusFilter ? staff.status.toLowerCase() === statusFilter.toLowerCase() : true;
    })
    .sort((a, b) => {
      if (!sortField) return 0;
      const valueA = sortField === "response" ? a.responseTime || 0 : 0;
      const valueB = sortField === "response" ? b.responseTime || 0 : 0;
      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });

  const currentStaff = filteredAndSortedStaffs.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const totalPages = Math.ceil(filteredAndSortedStaffs.length / itemsPerPage);

  useEffect(() => {
    fetchStaffs();
  }, []);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      searchStaffs();
    }, 20);
    return () => clearTimeout(delayDebounce);
  }, [keyword]);

  return (
    <div className="p-4">
      {isCreating ? (
        <NewStaff onBack={handleBack} staff={selectedStaff} isEdit={isEdit} />
      ) : selectedStaff ? (
        <StaffDetail onBack={handleBack} staff={selectedStaff} />
      ) : (
        <div>
          <div className="flex flex-wrap items-center gap-4 mb-4">
            <button
              className="bg-blue-900 text-white rounded-full px-6 py-2"
              onClick={() => setIsCreating(true)}
            >
              New Staff
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
                className="flex-grow bg-transparent outline-none text-sm"
              />
              <button type="submit">
                <img src="/images/icon-web/Search.png" className="h-5" alt="Search" />
              </button>
            </form>
            <select
              className="border border-gray-300 rounded-full px-4 py-2 text-sm"
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

          <table className="w-full table-auto border border-gray-200 rounded overflow-hidden shadow-sm text-xs">
            <thead className="bg-blue-400 text-white">
              <tr>
                <th className="px-4 py-2 text-left">ID</th>
                <th className="px-4 py-2 text-left">Full Name</th>
                <th className="px-4 py-2 text-left">Phone No.</th>
                <th className="px-4 py-2 text-left">Email</th>
                <th className="px-4 py-2 text-left">Month Lates <button onClick={() => toggleSort("lates")}>⬍</button></th>
                <th className="px-4 py-2 text-left">Status</th>
                <th className="px-4 py-2 text-left">Response Time <button onClick={() => toggleSort("response")}>⬍</button></th>
                <th className="px-4 py-2 text-left">Action</th>
              </tr>
            </thead>
            <tbody className="bg-white">
              {currentStaff.map((staff, index) => (
                <tr key={staff.staffId} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-2">{index + 1}</td>
                  <td className="px-4 py-2">{staff.fullName}</td>
                  <td className="px-4 py-2">{staff.sdt}</td>
                  <td className="px-4 py-2">{staff.email}</td>
                  <td className="px-4 py-2 text-center">{staff.monthLateCount}</td>
                  <td className="px-4 py-2 text-center">
                    <span className={`text-xs px-3 py-1 rounded-full ${getUserStatus(staff.status)}`}>
                      {staff.status}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-center">{staff.responseTime.toFixed(2)} min</td>
                  <td className="px-4 py-2 text-center">
                    <div className="flex justify-center gap-2">
                      <button
                        className="bg-blue-100 text-blue-600 text-xs border border-blue-300 rounded-full px-3 h-6"
                        onClick={() => handleViewDetail(staff)}
                      >
                        Detail
                      </button>
                      <button
                        className="bg-white border border-gray-400 rounded-full h-6 w-8 flex items-center justify-center"
                        onClick={() => handleEdit(staff)}
                      >
                        <img src="/images/icon-web/edit.png" className="w-4 h-4" alt="edit" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {filteredAndSortedStaffs.length > itemsPerPage && (
            <div className="flex justify-center mt-4 items-center gap-4 text-sm">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                <img
                  src={`/images/icon-web/${currentPage === 1 ? "Back To" : "Back To1"}.png`}
                  alt="Back"
                  className="w-8"
                />
              </button>
              <span className="font-semibold">
                {currentPage} / {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                disabled={currentPage >= totalPages}
              >
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

export default MainStaff;
