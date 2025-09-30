import React, { useState, useEffect } from "react";
import { feedbackAPI, partnerAPI } from "../../../../../manager";
import SidebarPartner from "./SidebarPartner";
import Performance from "./Performance";
import RescueCalls from "./RescueCalls";
import Violations from "./Violations";
import Documents from "./Documents";
import Vehicles from "./Vehicles";
import "../../../../styles/admin/partner.css";
import { getUserStatus } from "../../../../utils/StatusStyle";


const MainPartner = () => {
  /*API*/
  const [countWaiting, setCountWaiting] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [partners, setPartners] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [waitingPartners, setWaitingPartners] = useState([]);

  const fetchPartnersWithRate = async () => {
    const response = await partnerAPI.getAllPartners();
    const result = await Promise.all(response.data.map(async (item) => {
      try {
        const rateRes = await feedbackAPI.averageRate(item.partnerId);
        item.avgRate = rateRes.data;
      } catch {
        item.avgRate = null;
      }
      return item;
    }));
    return result;
  };

  // Get Partners 
  const fetchPartners = async () => {
    try {
      const response = await partnerAPI.getAllPartners();
      let result = response.data;
      setCountWaiting(response.data.filter(p => p.verificationStatus === false || p.resTow == 2 || p.resDrive == 2 || p.resFix == 2).length);
      setIsLoading(false);
      for (const item of result) {
        try {
          const rateRes = await feedbackAPI.averageRate(item.partnerId);
          item.avgRate = rateRes.data;
        } catch (err) {
          console.error(`Cannot get average rate for partner with id ${item.partnerId}:`, err);
          item.avgRate = null;
        }
      }
      setPartners(result.filter(r => r.verificationStatus === true));
      return result;
    } catch (error) {
      console.error('Error fetching partners', error);
      setIsLoading(false);
      return [];
    }
  };

  //Search
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
          setPartners(searchResults.filter(r => r.verificationStatus === true));
        }
        setIsLoading(false);
      }
    } catch (err) {
      console.error("Cannot find any partner: " + err)
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
  const filteredPartners = (approving ? waitingPartners : partners)
    .filter((partner) => {
      const statusMatch = statusFilter
        ? partner.status?.toLowerCase() === statusFilter.toLowerCase()
        : true;
      const serviceMatch = serviceFilter
        ? partner[serviceFilter] === true || partner[serviceFilter] === 1
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

  {/* OTHER FUNC */ }
  // Check Approving
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
  }

  /* SETUP PAGINATION */
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentPartners = filteredPartners.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredPartners.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  {/* RENDER SECTION */ }
  const [selectedTab, setSelectedTab] = useState("performance");
  const [selectedPartner, setSelectedPartner] = useState(null);
  const renderContent = () => {
    switch (selectedTab) {
      case "performance":
        return <Performance partner={selectedPartner} />;
      case "rescues":
        return <RescueCalls partner={selectedPartner} />;
      case "violations":
        return <Violations partner={selectedPartner} />;
      case "documents":
        return <Documents partner={selectedPartner} />;
      case "vehicles":
        return <Vehicles partner={selectedPartner} />;
      default:
        return <div>Chọn chức năng từ sidebar</div>;
    }
  };

  const renderTableHeaders = () => (
    <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0 text-sm">
      <tr>
        <th className="w-[4%] px-2 whitespace-nowrap">ID</th>
        <th className="w-[14%] px-4">Username</th>
        <th className="w-[11%] px-2 whitespace-nowrap">Phone No.</th>
        <th className="w-[16%] px-4">Email</th>
        <th className="w-[6%] px-2 whitespace-nowrap">
          Rate
          <button onClick={() => toggleSort("rate")}>
            {sortField === "rate" ? (
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
        <th className="w-[13%] px-4">Service</th>
        <th className="w-[8%] px-2 whitespace-nowrap">Status</th>
        <th className="w-[14%] px-2 whitespace-nowrap">
          Joined Date
          <button onClick={() => toggleSort("joined")}>
            {sortField === "joined" ? (
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
        <th className="w-[6%] px-2">Action</th>
      </tr>
    </thead>
  );

  const renderTableBody = () => (
    <tbody className="font-lexend text-[14px]">
      {currentPartners.map((part, index) => (
        <tr key={index} className="shadow h-14 font-lexend">
          <td className="text-center">{index + 1}</td>
          <td>{part.fullName}</td>
          <td>{part.sdt}</td>
          <td>{part.email}</td>
          <td className="text-center">
            {part.avgRate ? part.avgRate.toFixed(2) : "0.0"}
          </td>
          <td className="pl-2">
            {[part.resTow == 1 && "resTow", part.resFix == 1 && "resFix", part.resDrive == 1 && "resDrive"]
              .filter(Boolean)
              .join(" || ")
            }
          </td>
          <td>
            <p className={`text-xs py-1 w-20 h-6 rounded-3xl text-center mx-auto ${getUserStatus(part.status)}`}>
              {part.status}
            </p>
          </td>
          <td className="text-center">
            {new Date(part.createdAt).toLocaleString('vi-VN')}
          </td>
          <td>
            <button
              className="bg-blue-200 text-blue-600 text-xs border rounded-full px-3 h-6 w-18 text-center align-center"
              onClick={() => setSelectedPartner(part)}
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
    fetchPartners();
    setCurrentPage(1);
  }, [statusFilter]);

  return (
    <div>
      {!selectedPartner ? (
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
                  onSubmit={searchPartners}
                  onChange={searchPartners}
                  className="flex items-center border border-gray-300 rounded-full w-[30vw] ml-[25vh] px-4 py-2 my-[2vh]">
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
                {/* Filter Service */}
                <div className="items-center border border-gray-300 rounded-full mt-[2.1vh] h-[5vh] ml-48 w-36">
                  <select className="mx-4 mt-2"
                    value={serviceFilter} onChange={(e) => setServiceFilter(e.target.value)}>
                    <option value="">--- Service ---</option>
                    <option value="resTow">ResTow</option>
                    <option value="resFix">ResFix</option>
                    <option value="resDrive">ResDrive</option>
                  </select>
                </div>
              </div>
              {/* Partner Not Approved List */}
              <table className="w-[96%] mx-8 border rounded-2xl border-r-0 border-l-0">
                {renderTableHeaders()}
                {renderTableBody()}
              </table>
            </div>
          ) : (
            <div>
              <div className="flex">
                {/* Approved List */}
                {/* <div className="items-center bg-[#013171] border border-gray-300 rounded-full mt-[2vh] h-[43px] w-44 ml-[5vh]">
                  <button className="text-white mx-3 my-2 w-48 flex px-1" onClick={handleApproval}>
                    Approve Partner
                    <div className="bg-red-500 border-red-500 rounded-full min-w-6 px-1 ml-2">{countWaiting}</div>
                  </button>
                </div> */}
                {/* Search */}
                <form
                  onSubmit={searchPartners}
                  onChange={searchPartners}
                  className="flex items-center border border-gray-300 rounded-full w-[30vw] ml-[6vw] px-4 py-2 my-[2vh]">
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
                <div className="items-center border border-gray-300 rounded-full mt-[2vh] h-[4.5vh] w-36 ml-8">
                  <select className="mx-3 mt-2" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                    <option value="">--- Status ---</option>
                    <option value="Waiting">Waiting</option>
                    <option value="Active">Active</option>
                    <option value="Deactive">Deactive</option>
                    <option value="24h">24H</option>
                    <option value="Blocked">Blocked</option>
                  </select>
                </div>
                {/* Filter Service */}
                <div className="items-center border border-gray-300 rounded-full mt-[2vh] h-[4.5vh] w-36 ml-8">
                  <select className="mx-3 mt-2" value={serviceFilter} onChange={(e) => setServiceFilter(e.target.value)}>
                    <option value="">--- Service ---</option>
                    <option value="resTow">ResTow</option>
                    <option value="resFix">ResFix</option>
                    <option value="resDrive">ResDrive</option>
                  </select>
                </div>
              </div>
              {/* List Partner */}
              <table className="w-[96%] mx-8 table-auto border rounded-2xl border-r-0 border-l-0">
                {renderTableHeaders()}
                {renderTableBody()}
              </table>
            </div>
          )}
          {/* Pagination */}
          {filteredPartners.length > itemsPerPage && (
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
        <div className="flex gap-4 maincontent-partner">
          <SidebarPartner
            onBack={handleBack}
            onSelect={setSelectedTab}
            activeKey={selectedTab}
            selectedPartner={selectedPartner}
            setSelectedPartner={setSelectedPartner}
            onReload={fetchPartners}
            className="sidebar-partner"
          />
          <div className="flex-1 rounded-xl shadow p-4 main-partner">
            {renderContent()}
          </div>
        </div>
      )}
    </div>
  );
};

export default MainPartner;
