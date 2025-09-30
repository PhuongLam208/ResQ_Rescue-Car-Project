import React, { useState, useEffect } from "react";
import { feedbackAPI, partnerAPI } from "../../../../../admin";
import SidebarPartner from "./SidebarPartner";
import Performance from "./Performance";
import RescueCalls from "./RescueCalls";
import Revenue from "./Revenue";
import Violations from "./Violations";
import Documents from "./Documents";
import Vehicles from "./Vehicles";
import "../../../../styles/admin/partner.css";
import { getUserStatus } from "../../../../utils/StatusStyle";

const MainPartner = () => {
  const [countWaiting, setCountWaiting] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [partners, setPartners] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [waitingPartners, setWaitingPartners] = useState([]);

  const fetchPartners = async () => {
    try {
      const response = await partnerAPI.getAllPartners();
      let result = response.data;
      setCountWaiting(result.filter(p => p.verificationStatus === false || p.resTow == 2 || p.resDrive == 2 || p.resFix == 2).length);
      setIsLoading(false);
      setPartners(result);
      return result;
    } catch (error) {
      console.error('Error fetching partners', error);
      setIsLoading(false);
      return [];
    }
  };

  const searchPartners = async (e) => {
    e.preventDefault();
    try {
      if (keyword.trim() === '') {
        if (approving) {
          const waitingList = partners.filter(p => p.verificationStatus === false || p.resTow == 2 || p.resDrive == 2 || p.resFix == 2);
          setWaitingPartners(waitingList);
        } else {
          await fetchPartners();
        }
      } else {
        const response = await partnerAPI.search(keyword);
        const searchResults = response.data;
        if (approving) {
          const waitingList = searchResults.filter(p => p.verificationStatus === false || p.resTow == 2 || p.resDrive == 2 || p.resFix == 2);
          setWaitingPartners(waitingList);
        } else {
          setPartners(searchResults);
        }
        setIsLoading(false);
      }
    } catch (err) {
      console.error("Cannot find any partner: " + err)
      setIsLoading(false);
    }
  };

  const [sortField, setSortField] = useState(null);
  const [statusFilter, setStatusFilter] = useState("");
  const [serviceFilter, setServiceFilter] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");

  const [approving, setApproving] = useState(false);
  const filteredPartners = (approving ? waitingPartners : partners)
    .filter((partner) => {
      const statusMatch = statusFilter ? partner.status.toLowerCase() === statusFilter.toLowerCase() : true;
     const serviceMatch = serviceFilter
        ? (
          (serviceFilter.toLowerCase() === 'restow' && partner.resTow) ||
          (serviceFilter.toLowerCase() === 'resfix' && partner.resFix) ||
          (serviceFilter.toLowerCase() === 'resdrive' && partner.resDrive)
        )
        : true;
      return statusMatch && serviceMatch;
    })
    .sort((a, b) => {
      if (!sortField) return 0;
      let valueA, valueB;
      if (sortField === "rate") {
        valueA = a.avgRate ?? 0;
        valueB = b.avgRate ?? 0;
      } else if (sortField === "joined") {
        valueA = new Date(a.createdAt).getTime();
        valueB = new Date(b.createdAt).getTime();
      }
      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });

  const toggleSort = (field) => {
    setSortOrder(sortField === field && sortOrder === "asc" ? "desc" : "asc");
    setSortField(field);
  };

  const handleApproval = async () => {
    setApproving(true);
    setKeyword('');
    setStatusFilter('');
    setServiceFilter('');
    const result = await fetchPartners();
    const waitingList = result.filter((p) => p.verificationStatus === false || p.resTow == 2 || p.resDrive == 2 || p.resFix == 2);
    setWaitingPartners(waitingList);
  };

  const handleBack = () => {
    setApproving(false);
    setSelectedPartner(null);
    setKeyword('');
    setStatusFilter('');
    setServiceFilter('');
    fetchPartners();
  };

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentPartners = filteredPartners.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredPartners.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  const [selectedTab, setSelectedTab] = useState("performance");
  const [selectedPartner, setSelectedPartner] = useState(null);

  const renderContent = () => {
    switch (selectedTab) {
      case "performance":
        return <Performance partner={selectedPartner} />;
      case "rescues":
        return <RescueCalls partner={selectedPartner} />;
      case "revenue":
        return <Revenue  partner={selectedPartner}/>;
      case "violations":
        return <Violations partner={selectedPartner}/>;
      case "documents":
        return <Documents partner={selectedPartner}/>;
      case "vehicles":
        return <Vehicles partner={selectedPartner}/>;
      default:
        return <div>Chọn chức năng từ sidebar</div>;
    }
  };

  const renderTableHeaders = () => (
    <thead className="bg-blue-500 text-white text-sm h-12">
      <tr>
        <th className="px-2 w-[4%]">ID</th>
        <th className="px-4 w-[14%]">Username</th>
        <th className="px-2 w-[11%]">Phone No.</th>
        <th className="px-4 w-[16%]">Email</th>
        <th className="px-2 w-[6%]">
          Rate
          <button onClick={() => toggleSort("rate")}>
            <img src={`/images/icon-web/${sortField === "rate" ? `Chevron ${sortOrder === "asc" ? "Up" : "Down"}` : "sort"}.png`} className="inline-block h-3 ml-2" />
          </button>
        </th>
        <th className="px-4 w-[13%]">Service</th>
        <th className="px-2 w-[8%]">Status</th>
        <th className="px-2 w-[14%]">
          Joined Date
          <button onClick={() => toggleSort("joined")}>
            <img src={`/images/icon-web/${sortField === "joined" ? `Chevron ${sortOrder === "asc" ? "Up" : "Down"}` : "sort"}.png`} className="inline-block h-3 ml-2" />
          </button>
        </th>
        <th className="px-2 w-[6%]">Action</th>
      </tr>
    </thead>
  );

  const renderTableBody = () => (
    <tbody className="text-sm">
      {currentPartners.map((part, index) => (
        <tr key={index} className="h-14 border-b">
          <td className="text-center">{index + 1}</td>
          <td>{part.fullName}</td>
          <td>{part.sdt}</td>
          <td>{part.email}</td>
          <td className="text-center">{part.avgRate ? part.avgRate.toFixed(2) : "0.0"}</td>
          <td>{[part.resTow && "resTow", part.resFix && "resFix", part.resDrive && "resDrive"].filter(Boolean).join(" || ")}</td>
          <td>
            <p className={`text-xs py-1 w-20 h-6 rounded-2xl text-center mx-auto ${getUserStatus(part.status)}`}>
              {part.status}
            </p>
          </td>
          <td className="text-center">{new Date(part.createdAt).toLocaleString('vi-VN')}</td>
          <td>
            <button className="bg-blue-100 text-blue-600 text-xs px-3 py-1 rounded-full" onClick={() => setSelectedPartner(part)}>Detail</button>
          </td>
        </tr>
      ))}
    </tbody>
  );

  useEffect(() => {
    fetchPartners();
    setCurrentPage(1);
  }, [statusFilter]);

  return (
    <div className="p-4">
      {!selectedPartner ? (
        <div>
          <div className="flex flex-wrap items-center gap-4 mb-4">
            {approving ? (
              <button onClick={handleBack} className="w-12 h-12 border border-blue-400 rounded-full flex items-center justify-center">
                <img src="/images/icon-web/Reply Arrow1.png" className="w-6" />
              </button>
            ) : (
              <button onClick={handleApproval} className="bg-blue-800 text-white px-4 py-2 rounded-full flex items-center gap-2">
                Approve Partner
                <span className="bg-red-500 rounded-full px-2 text-sm">{countWaiting}</span>
              </button>
            )}
            <form onSubmit={searchPartners} onChange={searchPartners} className="flex items-center border border-gray-300 rounded-full px-4 py-2 w-[30vw]">
              <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Search..." className="flex-grow outline-none bg-transparent" />
              <button type="submit"><img src="/images/icon-web/Search.png" className="h-5" /></button>
            </form>
            {!approving && (
              <>
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="border border-gray-300 rounded-full px-4 py-2 text-sm">
                  <option value="">--- Status ---</option>
                  <option value="Waiting">Waiting</option>
                  <option value="Active">Active</option>
                  <option value="Deactive">Deactive</option>
                  <option value="24h">24h</option>
                  <option value="Blocked">Blocked</option>
                </select>
              </>
            )}
            <select value={serviceFilter} onChange={(e) => setServiceFilter(e.target.value)} className="border border-gray-300 rounded-full px-4 py-2 text-sm">
              <option value="">--- Service ---</option>
              <option value="resTow">resTow</option>
              <option value="resFix">resFix</option>
              <option value="resDrive">resDrive</option>
            </select>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full border rounded-xl">
              {renderTableHeaders()}
              {renderTableBody()}
            </table>
          </div>
          {filteredPartners.length > itemsPerPage && (
            <div className="flex justify-center items-center gap-4 mt-4">
              <button onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={isPrevDisabled}>
                <img src={isPrevDisabled ? "/images/icon-web/Back To.png" : "/images/icon-web/Back To1.png"} className="w-8" />
              </button>
              <span>{currentPage} / {totalPages}</span>
              <button onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))} disabled={isNextDisabled}>
                <img src={isNextDisabled ? "/images/icon-web/Next page.png" : "/images/icon-web/Next page1.png"} className="w-8" />
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className="flex gap-4">
          <SidebarPartner
            onBack={handleBack}
            onSelect={setSelectedTab}
            activeKey={selectedTab}
            selectedPartner={selectedPartner}
            setSelectedPartner={setSelectedPartner}
            onReload={fetchPartners}
          />
          <div className="flex-1 rounded-xl shadow p-4">
            {renderContent()}
          </div>
        </div>
      )}
    </div>
  );
};

export default MainPartner;
