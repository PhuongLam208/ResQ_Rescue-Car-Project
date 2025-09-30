import React, { useEffect, useState } from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";
import { staffAPI } from "../../../../../admin";
import StaffDetail from "./StaffDetail";
import NewStaff from "./FormStaff";


const MainStaff = () => {
  {/* API SETUP */ }
  const [isLoading, setIsLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [staffs, setStaffs] = useState([]);
  const fetchStaffs = async () => {
    try {
      const response = await staffAPI.getStaffs();
      setStaffs(response.data);
      
      setIsLoading(false);
    } catch (err) {
      console.error("Cannot get staffs: " + err);
      setIsLoading(false);
    }
  };

  const searchStaffs = async () => {
    try {
      if (keyword.trim() == "") {
        fetchStaffs();
      } else {
        const response = await staffAPI.searchStaff(keyword);
        setStaffs(response.data);
        setIsLoading(false);
      }
    } catch (err) {
      console.error("Cannot search staffs: " + err);
      setIsLoading(false);
    }
  };

  {/* OTHER FUNC SETUP */ }
  const [isEdit, setIsEdit] = useState(false);
  const [selectedStaff, setSelectedStaff] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
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

  {/* FILETER & SORT SETUP */ }
  const [statusFilter, setStatusFilter] = useState("");
  const [sortField, setSortField] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");
  const toggleSort = (field) => {
    if (sortField === field) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortOrder("asc");
    }
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

  {/* PAGINATION SETUP */ }
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 15;
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
    <div>
      {isCreating ? (
        <NewStaff onBack={handleBack}
          staff={selectedStaff}
          isEdit={isEdit} />
      ) : (selectedStaff ? (
        <StaffDetail onBack={handleBack}
          staff={selectedStaff} />
      ) : (
        <div>
          <div className="flex">
            {/* <div className="items-center bg-[#013171] border border-gray-300 rounded-full mt-[2vh] h-[43px] w-[9%] ml-[5vh]">
              <button className="text-white mx-3 my-2 w-[100%] flex px-1" onClick={() => setIsCreating(true)}>
                New Staff
              </button>
            </div> */}
            <form onSubmit={(e) => e.preventDefault()}
              className="flex items-center border border-gray-300 rounded-full w-[75vh] ml-[7%] px-4 py-2 my-[2vh]">
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                type="text"
                placeholder="Search..."
                className="flex-grow outline-none bg-transparent" />
              <button type="submit">
                <img src="/images/icon-web/Search.png" className="h-6" alt="Search" />
              </button>
            </form>
            <div className="items-center border border-gray-300 rounded-full mt-[2vh] h-[43px] w-40 ml-[10%]">
              <select
                className="mx-4 my-3 w-32"
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
          <table className="w-[96%] mx-8 table-auto border rounded-2xl border-r-0 border-l-0">
            <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0">
              <tr>
                <th className="w-[5%]">ID</th>
                <th className="w-[15%]">Full Name</th>
                <th className="w-[15%]">Phone No.</th>
                <th className="w-[13%]">Email</th>
                <th className="w-[16%]">
                  Month Lates
                  <button onClick={() => toggleSort("lates")}>
                    {sortField === "lates" ? (
                      <img
                        src={`/images/icon-web/Chevron ${sortOrder === "asc" ? "Up" : "Down"}.png`}
                        className="h-3 ml-2 inline-block"
                      />
                    ) : (
                      <img
                        src={`/images/icon-web/sort.png`}
                        className="h-3 ml-2 inline-block"
                      />
                    )}
                  </button>
                </th>
                <th className="w-[10%]">Status</th>
                <th className="w-[16%]">
                  Response Time
                  <button onClick={() => toggleSort("response")}>
                    {sortField === "response" ? (
                      <img
                        src={`/images/icon-web/Chevron ${sortOrder === "asc" ? "Up" : "Down"}.png`}
                        className="h-3 ml-2 inline-block"
                      />
                    ) : (
                      <img
                        src={`/images/icon-web/sort.png`}
                        className="h-3 ml-2 inline-block"
                      />
                    )}
                  </button>
                </th>
                <th className="w-[15%]">Action</th>
              </tr>
            </thead>
            <tbody>
              {currentStaff.map((staff) => (
                <tr className="leading-9 h-12" key={staff.staffId}>
                  <td className="text-center">{staff.staffId}</td>
                  <td>{staff.fullName}</td>
                  <td>{staff.sdt}</td>
                  <td>{staff.email}</td>
                  <td className="text-center">{staff.monthLateCount}</td>
                  <td className="text-center ">
                    <span
                      className={`text-xs px-3 py-1 rounded-full ${getUserStatus(
                        staff.status
                      )}`}
                    >
                      {staff.status}
                    </span>
                  </td>
                  <td className="text-center">{staff.responseTime.toFixed(2)} min</td>
                  <td className="text-center">
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
          {/* Pagination */}
          {filteredAndSortedStaffs.length > itemsPerPage && (
            <div className="flex justify-center mt-4 space-x-2">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                <img
                  src={`/images/icon-web/${currentPage === 1 ? "Back To" : "Back To1"}.png`}
                  alt="Back"
                  className="w-9"
                />
              </button>
              <span className="px-3 py-5 font-semibold">
                {currentPage} / {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                disabled={currentPage >= totalPages}
              >
                <img
                  src={`/images/icon-web/${currentPage >= totalPages ? "Next page" : "Next page1"}.png`}
                  alt="Next"
                  className="w-9"
                />
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

export default MainStaff;
