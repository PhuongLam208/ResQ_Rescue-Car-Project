import React, { useState, useEffect } from "react";
import TopbarCustomer from "./TopbarCustomer";
import Information from "./Information";
import History from "./History";
import Transactions from "./Transactions";
import Violations from "./Violations";
import Documents from "./Documents";
import "../../../../styles/admin/general.css";
import { customerAPI } from "../../../../../admin";
import { getUserStatus } from "../../../../utils/StatusStyle";

const MainCustomer = () => {
  const [customers, setCustomers] = useState([]);
  const [waitingCustomers, setWaitingCustomers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [approving, setApproving] = useState(false);
  const [countWaiting, setCountWaiting] = useState(0);
  const [sortField, setSortField] = useState(null);
  const [sortOrder, setSortOrder] = useState("asc");
  const [statusFilter, setStatusFilter] = useState("");
  const [selectedTab, setSelectedTab] = useState("information");
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;

  useEffect(() => {
    fetchCustomers();
  }, [statusFilter]);

  useEffect(() => {
    const delay = setTimeout(() => searchCustomers(), 20);
    return () => clearTimeout(delay);
  }, [keyword]);

  useEffect(() => {
    if (approving) handleApproval();
  }, [approving]);

  const fetchCustomers = async () => {
    try {
      const res = await customerAPI.getAllCustomers();
      const data = res.data;
      setCustomers(data);
      setCountWaiting(data.filter((c) => c.pdStatus?.toLowerCase() === "pending").length);
      setIsLoading(false);
    } catch (err) {
      console.error("Error fetching customers", err);
      setIsLoading(false);
    }
  };

  const searchCustomers = async () => {
    try {
      if (!keyword.trim()) {
        approving
          ? setWaitingCustomers(customers.filter((c) => c.pdStatus?.toLowerCase() === "pending"))
          : fetchCustomers();
      } else {
        const res = await customerAPI.search(keyword.trim());
        const results = res.data;
        approving
          ? setWaitingCustomers(results.filter((c) => c.pdStatus?.toLowerCase() === "pending"))
          : setCustomers(results);
      }
    } catch (err) {
      console.error("Search error", err);
      setIsLoading(false);
    }
  };

  const handleApproval = async () => {
    setKeyword("");
    setStatusFilter("");
    const result = await customerAPI.getAllCustomers();
    const waitingList = result.data.filter((c) => c.pdStatus?.toLowerCase() === "pending");
    setWaitingCustomers(waitingList);
  };

  const handleBack = () => {
    setApproving(false);
    setSelectedCustomer(null);
    setKeyword("");
    fetchCustomers();
  };

  const toggleSort = (field) => {
    if (sortField === field) {
      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortField(field);
      setSortOrder("asc");
    }
  };

  const filteredCustomers = (approving ? waitingCustomers : customers)
    .filter((c) =>
      statusFilter ? c.status?.toLowerCase() === statusFilter.toLowerCase() : true
    )
    .sort((a, b) => {
      if (!sortField) return 0;
      const fields = {
        point: [a.loyaltyPoint ?? 0, b.loyaltyPoint ?? 0],
        total: [a.totalRescues ?? 0, b.totalRescues ?? 0],
        joined: [new Date(a.createdAt).getTime(), new Date(b.createdAt).getTime()],
      };
      const [valA, valB] = fields[sortField] || [0, 0];
      return sortOrder === "asc" ? valA - valB : valB - valA;
    });

  const startIdx = (currentPage - 1) * itemsPerPage;
  const currentCustomers = filteredCustomers.slice(startIdx, startIdx + itemsPerPage);
  const totalPages = Math.ceil(filteredCustomers.length / itemsPerPage);

  const renderTabContent = () => {
    const tabs = {
      information: Information,
      history: History,
      transactions: Transactions,
      violations: Violations,
      documents: Documents,
    };
    const Component = tabs[selectedTab] || (() => <div>Choose tab</div>);
    return <Component customer={selectedCustomer} />;
  };

  return (
    <div className="main-customer px-6 py-8 bg-gray-50 min-h-screen overflow-x-hidden text-sm">
      {!selectedCustomer ? (
        <div className="space-y-6">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            {approving && (
              <button
                onClick={handleBack}
                className="bg-white border border-blue-500 text-blue-600 rounded-full px-4 py-2 text-sm hover:bg-blue-50"
              >
                <img src="/images/icon-web/Reply Arrow1.png" alt="Back" className="w-4 inline mr-1" />
                Quay lại
              </button>
            )}
            <form
              onSubmit={(e) => e.preventDefault()}
              className="flex items-center border border-gray-300 rounded-full w-full md:w-[30vw] px-4 py-2 text-sm bg-white shadow-sm"
            >
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Search customer..."
                className="flex-grow outline-none bg-transparent"
              />
              <button type="submit">
                <img src="/images/icon-web/Search.png" className="h-5" alt="Search" />
              </button>
            </form>
            {!approving && (
              <button
                onClick={() => setApproving(true)}
                className="bg-blue-700 hover:bg-blue-800 text-white rounded-full px-4 py-2 text-sm flex items-center shadow"
              >
                Approved customer
                <span className="ml-2 bg-red-500 text-white rounded-full px-2 py-0.5 text-xs">
                  {countWaiting}
                </span>
              </button>
            )}
          </div>

          <div className="overflow-x-auto bg-white rounded-xl shadow">
            <table className="w-full text-sm table-auto">
              <thead className="bg-blue-600 text-white text-left text-xs">
                <tr>
                  {["#", "Fullname", "Phone", "Email", "Participant date", "Rescue", "Point", "Status", ""].map((col, i) => (
                    <th key={i} className="px-4 py-3 font-medium whitespace-nowrap text-center">{col}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 text-xs">
                {currentCustomers.map((cus, i) => (
                  <tr key={i} className="hover:bg-gray-50">
                    <td className="text-center py-3">{i + 1}</td>
                    <td className="px-4 py-3 whitespace-nowrap">{cus.fullName}</td>
                    <td className="px-4 py-3 text-center">{cus.sdt}</td>
                    <td className="px-4 py-3 text-center">{cus.email}</td>
                    <td className="px-4 py-3 text-center">{new Date(cus.createdAt).toLocaleDateString("vi-VN")}</td>
                    <td className="px-4 py-3 text-center">{cus.totalRescues || 0}</td>
                    <td className="px-4 py-3 text-center">{cus.loyaltyPoint}</td>
                    <td className="text-center">
                      <span className={`text-xs px-3 py-1 rounded-full ${getUserStatus(cus.status)}`}>
                        {cus.status}
                      </span>
                    </td>
                    <td className="text-center">
                      <button
                        onClick={() => setSelectedCustomer(cus)}
                        className="bg-blue-100 hover:bg-blue-200 text-blue-600 text-[11px] rounded-full px-2 py-[2px]"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredCustomers.length > itemsPerPage && (
            <div className="flex justify-center items-center gap-4 mt-4">
              <button
                onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                disabled={currentPage === 1}
              >
                <img
                  src={currentPage === 1 ? "/images/icon-web/Back To.png" : "/images/icon-web/Back To1.png"}
                  className="w-5"
                  alt="Previous"
                />
              </button>
              <span className="text-xs font-medium">{currentPage} / {totalPages}</span>
              <button
                onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
                disabled={currentPage >= totalPages}
              >
                <img
                  src={currentPage >= totalPages ? "/images/icon-web/Next page.png" : "/images/icon-web/Next page1.png"}
                  className="w-5"
                  alt="Next"
                />
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className="space-y-6">
          <TopbarCustomer
            onBack={handleBack}
            selectedCustomer={selectedCustomer}
            setSelectedCustomer={setSelectedCustomer}
            onSelect={setSelectedTab}
            activeKey={selectedTab}
          />
          <div className="rounded-xl shadow p-6 bg-white text-sm">
            {renderTabContent()}
          </div>
        </div>
      )}
    </div>
  );
};

export default MainCustomer;
