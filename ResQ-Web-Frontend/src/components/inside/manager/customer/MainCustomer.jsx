import React, { useState, useEffect } from "react";
import TopbarCustomer from "./TopbarCustomer";
import Information from "./Information";
import History from "./History";
import Violations from "./Violations";
import Documents from "./Documents";
import "../../../../styles/admin/customer.css";
import { customerAPI } from "../../../../../manager";
import { getUserStatus } from "../../../../utils/StatusStyle";


const MainCustomer = () => {
  /*API*/
  const [countWaiting, setCountWaiting] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [customers, setCustomers] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [waitingCustomers, setWaitingCustomers] = useState([]);
  // Get Customers 
  const fetchCustomers = async () => {
    try {
      const response = await customerAPI.getAllCustomers();
      let result = response.data;
      setIsLoading(false);
      setCustomers(result);
      setCountWaiting(result.filter(c => c.pdStatus?.toLowerCase() === "pending").length);
      return result;
    } catch (error) {
      console.error('Error fetching customers', error);
      setIsLoading(false);
      return [];
    }
  };

  const searchCustomers = async () => {
    try {
      if (keyword.trim() === '') {
        if (approving) {
          const waitingList = customers.filter((c) => c.pdStatus?.toLowerCase() === "pending");
          setWaitingCustomers(waitingList);
        } else {
          fetchCustomers();
        }
      } else {
        const response = await customerAPI.search(keyword.trim());
        const searchResults = response.data;
        if (approving) {
          const waitingList = searchResults.filter((c) => c.pdStatus?.toLowerCase() === "pending");
          setWaitingCustomers(waitingList);
        } else {
          setCustomers(searchResults);
        }
      }
    } catch (err) {
      console.error("Cannot find any customer: " + err)
      setIsLoading(false);
    }
  }

  /*SETUP FILTER & SORT*/
  const [sortField, setSortField] = useState(null);
  const [statusFilter, setStatusFilter] = useState("");
  const [serviceFilter, setServiceFilter] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");

  // Handle Filter
  const [approving, setApproving] = useState(false);
  const filteredCustomers = (approving ? waitingCustomers : customers)
    .filter((customer) => {
      const statusMatch = statusFilter
        ? customer.status?.toLowerCase() === statusFilter.toLowerCase()
        : true;

      const serviceMatch = serviceFilter
        ? customer.serviceType?.toLowerCase() === serviceFilter.toLowerCase()
        : true;

      return statusMatch && serviceMatch;
    })
    .sort((a, b) => {
      if (!sortField) return 0;

      const fields = {
        point: [a.loyaltyPoint ?? 0, b.loyaltyPoint ?? 0],
        total: [a.totalRescues ?? 0, b.totalRescues ?? 0],
        joined: [new Date(a.createdAt).getTime(), new Date(b.createdAt).getTime()],
      };

      const [valueA, valueB] = fields[sortField] || [0, 0];
      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });


  const toggleSort = (field) => {
    if (sortField === field) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortField(field);
      setSortOrder("asc");
    }
  };

  {/* OTHER FUNC */ }
  // Check Approving
  const handleApproval = async () => {
    setApproving(true);
    setKeyword('');
    setStatusFilter('');
    const result = await fetchCustomers();
    const waitingList = result.filter((c) => c.pdStatus?.toLowerCase() === "pending");
    setWaitingCustomers(waitingList);
  };

  const handleBack = () => {
    setApproving(false);
    setSelectedCustomer(null);
    setKeyword('');
    fetchCustomers();
  }

  /* SETUP PAGINATION */
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentCustomers = filteredCustomers.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredCustomers.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  {/* RENDER SECTION */ }
  const [selectedTab, setSelectedTab] = useState("information");
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const renderTabContent = () => {
    const components = {
      information: Information,
      history: History,
      violations: Violations,
      documents: Documents,
    };
    const Component = components[selectedTab] || (() => <div>Chọn tab</div>);
    return <Component customer={selectedCustomer} />;
  };

  const renderTableHeaders = () => (
    <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0">
      <tr>
        <th>ID</th>
        <th>Full Name</th>
        <th>Phone No.</th>
        <th>
          Joined Date
          <button onClick={() => toggleSort("joined")}>
            {sortField == "joined" ?
              <img
                src={`../../../../../public/images/icon-web/Chevron ${sortOrder === "asc" ? "Up" : "Down"}.png`}
                className="h-3 ml-2"
              /> : <img
                src={`../../../../../public/images/icon-web/sort.png`}
                className="h-3 ml-2"
              />
            }
          </button>
        </th>
        <th>
          Total Rescue
          <button onClick={() => toggleSort("total")}>
            {sortField == "total" ?
              <img
                src={`../../../../../public/images/icon-web/Chevron ${sortOrder === "asc" ? "Up" : "Down"}.png`}
                className="h-3 ml-2"
              /> : <img
                src={`../../../../../public/images/icon-web/sort.png`}
                className="h-3 ml-2"
              />
            }
          </button>
        </th>
        <th>
          Total Point
          <button onClick={() => toggleSort("point")}>
            {sortField == "point" ?
              <img
                src={`../../../../../public/images/icon-web/Chevron ${sortOrder === "asc" ? "Up" : "Down"}.png`}
                className="h-3 ml-2"
              /> : <img
                src={`../../../../../public/images/icon-web/sort.png`}
                className="h-3 ml-2"
              />
            }
          </button>
        </th>
        <th>Status</th>
        <th>Action</th>
      </tr>
    </thead>
  );

  const renderTableBody = () => (
    <tbody className="font-lexend text-[14px]">
      {currentCustomers.map((cus, index) => (
        <tr key={index} className="shadow h-14 font-lexend">
          <td className="text-center">{index + 1}</td>
          <td>{cus.fullName}</td>
          <td>{cus.sdt}</td>
          <td className="text-center">
            {new Date(cus.createdAt).toLocaleString('vi-VN')}
          </td>
          <td className="text-center">{cus.totalRescues || 0}</td>
          <td className="text-center">{cus.loyaltyPoint}</td>
          <td>
            <p className={`text-xs py-1 w-20 h-6 rounded-3xl text-center mx-auto ${getUserStatus(cus.status)}`}>
              {cus.status}
            </p>
          </td>
          <td>
            <button
              className="bg-blue-200 text-blue-600 text-xs border rounded-full px-3 h-6 w-18 text-center align-center"
              onClick={() => setSelectedCustomer(cus)}
            >
              Detail
            </button>
          </td>
        </tr>
      ))}
    </tbody>
  );

  {/* LOAD PAGE */ }
  useEffect(() => {
    fetchCustomers();
    setCurrentPage(1);
  }, [statusFilter]);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      searchCustomers();
    }, 20);
    return () => clearTimeout(delayDebounce);
  }, [keyword]);

  useEffect(() => {
    if (approving) {
      handleApproval();
    }
  }, [approving]);

  return (
    <div>
      {!selectedCustomer ? (
        <div>
          {approving ? (
            <div>
              <div className="flex">
                {/* Back Button */}
                <div className="pt-5 pl-10">
                  <button onClick={handleBack}
                    className="border border-[#68A2F0] rounded-full w-16 h-[43px]"
                  >
                    <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-7 m-auto" />
                  </button>
                </div>
                {/* Search */}
                <form
                  onSubmit={searchCustomers}
                  onChange={searchCustomers}
                  className="flex items-center border border-gray-300 rounded-full w-[40vw] ml-[15vw] px-4 py-2 my-[2vh]">
                  <input
                    type="text"
                    value={keyword} onChange={(e) => setKeyword(e.target.value)}
                    placeholder="Search..."
                    className="flex-grow outline-none bg-transparent"
                  />
                  <button type="submit">
                    <img src="/images/icon-web/Search.png" className="h-6" />
                  </button>
                </form>
              </div>
              {/* Customer Not Approved List */}
              <table className="w-[96%] mx-8 table-auto border rounded-2xl border-r-0 border-l-0">
                {renderTableHeaders()}
                {renderTableBody()}
              </table>
            </div>
          ) : (
            <div>
              <div className="flex">
                {/* Approved List */}
                {/* <div className="items-center bg-[#013171] border border-gray-300 rounded-full mt-[2vh] h-[43px] w-48 ml-[5vh]">
                  <button className="text-white mx-3 my-2 w-48 flex px-1" onClick={handleApproval}>
                    Approve Customer
                    <div className="bg-red-500 border-red-500 rounded-full min-w-6 px-1 ml-2">{countWaiting}</div>
                  </button>
                </div> */}
                {/* Search */}
                <form
                  onSubmit={searchCustomers}
                  onChange={searchCustomers}
                  className="flex items-center border border-gray-300 rounded-full w-[40vw] ml-[6vw] px-4 py-2 my-[2vh]">
                  <input
                    type="text"
                    value={keyword} onChange={(e) => setKeyword(e.target.value)}
                    placeholder="Search..."
                    className="flex-grow outline-none bg-transparent"
                  />
                  <button type="submit">
                    <img src="/images/icon-web/Search.png" className="h-6" />
                  </button>
                </form>
                {/* Filter Status */}
                <div className="items-center border border-gray-300 rounded-full mt-[2vh] h-[4.5vh] w-36 ml-16">
                  <select className="mx-3 mt-2" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                    <option value="">--- Status ---</option>
                    <option value="Waiting">Waiting</option>
                    <option value="Active">Active</option>
                    <option value="Deactive">Deactive</option>
                    <option value="24h">24H</option>
                    <option value="Blocked">Blocked</option>
                  </select>
                </div>
              </div>
              {/* List Customer */}
              <table className="w-[96%] mx-8 table-auto border rounded-2xl border-r-0 border-l-0">
                {renderTableHeaders()}
                {renderTableBody()}
              </table>
            </div>
          )}
          {/* Pagination */}
          {filteredCustomers.length > itemsPerPage && (
            <div className="flex justify-center mt-4 space-x-2">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                <img src={isPrevDisabled ? "/images/icon-web/Back To.png" : "/images/icon-web/Back To1.png"} alt="Back" className="w-9" />
              </button>
              <span className="px-3 py-5 font-semibold">{currentPage} / {totalPages}</span>
              <button
                onClick={() =>
                  setCurrentPage((prev) => Math.min(prev + 1, totalPages))
                }
                disabled={currentPage >= totalPages}
              >
                <img src={isNextDisabled ? "/images//icon-web/Next page.png" : "/images/icon-web/Next page1.png"} alt="Next" className="w-9" />
              </button>
            </div>
          )}
        </div>
      ) : (
        //Show Detail
        <div className="inline gap-4 maincontent-customer">
          <TopbarCustomer
            onBack={handleBack}
            selectedCustomer={selectedCustomer}
            setSelectedCustomer={setSelectedCustomer}
            onSelect={setSelectedTab}
            activeKey={selectedTab}
            className="topbar-customer"
          />
          <div className="flex-1 h-full rounded-xl shadow p-4 main-customer">
            {renderTabContent()}
          </div>
        </div>
      )}
    </div>
  );
};

export default MainCustomer;
