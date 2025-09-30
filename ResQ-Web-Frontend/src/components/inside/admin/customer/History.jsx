import React, { useState, useEffect } from "react";
import * as adminApi from "../../../../../admin.js";
import { getReqStatus, } from "../../../../utils/StatusStyle";
import FeedbackDetail from "../feedback/FeedbackDetail";
import "../../../../styles/admin/feedback.css";

const History = ({ customer }) => {

  {/* SETUP API */ }
  const [isLoading, setIsLoading] = useState(true);
  const [history, setHistory] = useState([]);
  const [keyword, setKeyword] = useState("");

  //Get All
  const fetchHistory = async () => {
    try {
      const response = await adminApi.reqResQsAPI.findReqResQsByUser(customer.userId);
      setHistory(response.data);
      setIsLoading(false);
    } catch (err) {
      console.error("Cannot fetch history: " + err);
      setIsLoading(false);
    }
  }

  //Search
  const searchHistory = async () => {
    try {
      if (keyword.trim() == "") {
        fetchHistory();
      } else {
        const response = await adminApi.reqResQsAPI.searchCustomer(customer.userId, keyword);
        setHistory(response.data);
        setIsLoading(false);
      }
    } catch (err) {
      console.error("Cannot search: " + err);
      setIsLoading(false);
    }
  }

  {/* SETUP FILTER & SORT */ }
  const [statusFilter, setStatusFilter] = useState("");
  const [serviceFilter, setServiceFilter] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");
  const [sortField, setSortField] = useState("");
  const [recordStatus, setRecordStatus] = useState(null);
  const filteredHistory = history
    .filter((his) => {
      const statusMatch = statusFilter ? his.reqStatus.toLowerCase() === statusFilter.toLowerCase() : true;
      const serviceMatch = serviceFilter ? his.rescueType.toLowerCase() === serviceFilter.toLowerCase() : true;
      return statusMatch && serviceMatch;
    })
    .sort((a, b) => {
      if (!sortField) return 0;
      let valueA, valueB;

      if (sortField === "price") {
        valueA = a.total ?? 0;
        valueB = b.total ?? 0;
      } else if (sortField === "date") {
        valueA = new Date(a.startTime).getTime();
        valueB = new Date(b.startTime).getTime();
      }

      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });

  const toggleSort = (field) => {
    setSortOrder(sortField === field && sortOrder === "asc" ? "desc" : "asc");
    setSortField(field);
  };

  {/* SETUP PAGINATION */ }
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentHistory = filteredHistory.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredHistory.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;


  {/* OTHER FUNC */ }
  const [detail, setDetail] = useState(null);
  const [extraSrv, setExtraSrv] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [showFeedback, setShowFeedback] = useState(false);

  const handleViewDetail = async (his) => {
    setDetail(his);
    try {
      const response = await adminApi.reqResQsAPI.relatedRecordCheck(his.rrid);
      console.log(response.data);
      setRecordStatus(response.data);
    } catch (err) {
      console.error("Cannot check related records status: " + err);
      setIsLoading(false);
    }
    try {
      const response = await adminApi.extraSrvAPI.findExtrasByResResQ(his.rrid);
      setExtraSrv(response.data);
    } catch (err) {
      console.error("Cannot find extra service: " + err);
    }
    setIsLoading(false);
  };

  const handleViewFeedback = async (his) => {
    try {
      const response = await adminApi.feedbackAPI.findFeedbacksByReqResQ(his.rrid);
      setFeedback(response.data);
      setShowFeedback(true);
    } catch (err) {
      console.error("Cannot find feedback: " + err);
    }
  }

  const handleCloseDetail = () => {
    setDetail(null);
  };

  useEffect(() => {
    fetchHistory();
    if (currentPage !== 1) {
      setCurrentPage(1);
    }
    const delayDebounce = setTimeout(() => {
      searchHistory();
    }, 20);
    return () => clearTimeout(delayDebounce);
  }, [keyword, statusFilter, serviceFilter]);

  return (
    <div>
      {/* Search and Filter */}
      <div className="flex ml-[40vh]">
        {/* Search */}
        <form
          onSubmit={(e) => e.preventDefault()}
          className="flex items-center border border-gray-300 rounded-full w-full max-w-xl px-4 py-2 my-[20px]"
        >
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            type="text"
            placeholder="Search..."
            className="flex-grow outline-none bg-transparent"
          />
          <button type="submit">
            <img src="/images/icon-web/Search.png" className="h-6" alt="Search" />
          </button>
        </form>

        {/* Filter */}
        <div className="items-center border border-gray-300 rounded-full mt-[20px] h-[43px] w-36 ml-[10vh]">
          <select
            className="m-3 w-28"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">--- Status ---</option>
            <option value="Pending">Pending</option>
            <option value="On trip">On Trip</option>
            <option value="Completed">Completed</option>
            <option value="Cancelled">Cancelled</option>
          </select>
        </div>
        <div className="items-center border border-gray-300 rounded-full mt-[20px] h-[43px] w-36 ml-[2vw]">
          <select
            className="m-3 w-28"
            value={serviceFilter}
            onChange={(e) => setServiceFilter(e.target.value)}
          >
            <option value="">--- Service ---</option>
            <option value="ResTow">ResTow</option>
            <option value="ResFix">ResFix</option>
            <option value="ResDrive">ResDrive</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <table className="w-full table-auto border rounded-2xl border-r-0 border-l-0">
        <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0">
          <tr>
            <th className="w-[10%]">ID</th>
            <th>Partner</th>
            <th className="w-[10%]">Service</th>
            <th>Address</th>
            <th>
              Rescue Date
              <button onClick={() => toggleSort("date")}>
                {sortField == "date" ?
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
              Total Price
              <button onClick={() => toggleSort("price")}>
                {sortField == "price" ?
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
        <tbody>
          {currentHistory.map((his, index) => (
            <tr key={his.id} className="shadow h-12 font-lexend text-sm">
              <td className="text-center">{index + 1}</td>
              <td>{his.partnerName}</td>
              <td className="text-center">{his.rescueType}</td>
              <td>{his.ulocation}</td>
              <td className="text-center">{new Date(his.startTime).toLocaleString("vi-VN")}</td>
              <td className="text-center">
                {new Intl.NumberFormat('vi-VN').format(his.total)} {his.currency}
              </td>
              <td className="px-4 text-center">
                <span className={`text-xs px-3 py-1 rounded-full ${getReqStatus(his.reqStatus)}`}>
                  {his.reqStatus}
                </span>
              </td>
              <td className="text-center">
                <button
                  className="bg-blue-200 text-blue-600 text-xs border rounded-full px-3 h-6 w-18 text-center"
                  onClick={() => handleViewDetail(his)}
                >
                  Detail
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {/* Pagination */}
      {filteredHistory.length > itemsPerPage && (
        <div className="flex justify-center mt-2 space-x-2">
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

      {/* Popup Detail */}
      {detail && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white w-[1000px] max-h-[90vh] overflow-y-auto rounded-xl p-8 relative">
            <button className="absolute top-4 right-4 text-xl font-bold" onClick={handleCloseDetail}>
              ✖
            </button>
            <h2 className="text-center text-2xl font-bold mb-4">Rescue No #{detail.rrid}</h2>
            {(recordStatus?.hasCustomerReport || recordStatus?.hasPartnerReport) && (
              <div className="float-right italic text-red-500 font-semibold text-sm text-center">
                {recordStatus.hasCustomerReport && recordStatus.hasPartnerReport ? (
                  <span>
                    *There are <strong>2 reports</strong> for this request rescue
                  </span>
                ) : recordStatus.hasCustomerReport ? (
                  <span>
                    *There is <strong>1 report from Customer</strong> for this request rescue
                  </span>
                ) : (
                  <span>
                    *There is <strong>1 report from Partner</strong> for this request rescue
                  </span>
                )}
              </div>
            )}
            <table className="w-full mt-2">
              <thead>
                <tr className="border-b border-gray-500 ">
                  <th colSpan={2} className="text-center py-2 border-r border-gray-500">Customer's Information</th>
                  <th colSpan={2} className="text-center py-2">Partner's Information</th>
                </tr>
              </thead>
              <tbody className="border-b border-gray-400">
                <tr>
                  <td className="InformationTitle">Full Name</td>
                  <td className="InformationContent">{detail.userName}</td>
                  <td className="InformationTitle">Name</td>
                  <td className="InformationContent">{detail.partnerName}</td>
                </tr>
                <tr>
                  <td className="InformationTitle">Phone No</td>
                  <td className="InformationContent">{detail.userPhone}</td>
                  <td className="InformationTitle">Phone No</td>
                  <td className="InformationContent">{detail.partnerPhone}</td>
                </tr>
              </tbody>
            </table>
            {!(extraSrv && extraSrv.price > 0) ? (
              <table className="border-b border-gray-500 w-[70vh] ml-28">
                <tbody>
                  <tr>
                    <td className="w-[35%] detailTitle pl-5">Rescue Service</td>
                    <td className="detailContent">{detail.rescueType}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Address</td>
                    <td className="detailContent">{detail.ulocation}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Booking Time</td>
                    <td className="detailContent">{new Date(detail.startTime).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Finish Time</td>
                    <td className="detailContent">{new Date(detail.endTime).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">System's Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.appFee)}{detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Paid</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.total)} {detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Method</td>
                    <td className="detailContent">{detail.paymentMethod ?? "N/A"}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Status</td>
                    <td className="detailContent"> {detail.paymentStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Status</td>
                    <td className="detailContent">{detail.reqStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Note</td>
                    <td className="detailContent">
                      <span className={detail.cancelNote == "" ? "text-gray-500 italic" : ""}>
                        {detail.cancelNote == "NULL" ? "(None)" : detail.cancelNote}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            ) : (
              <table className="border-b border-gray-500 w-[70vh] ml-28">
                <tbody>
                  <tr>
                    <td className="w-[35%] detailTitle">Rescue Service</td>
                    <td className="detailContent">{detail.rescueType}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Address</td>
                    <td className="detailContent">{detail.ulocation}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Booking Time</td>
                    <td className="detailContent">{new Date(detail.startTime).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Finish Time</td>
                    <td className="detailContent">{new Date(detail.endTime).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">System's Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.appFee)}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">
                      Extra Fee (if have)
                      <ul className="pl-10 list-disc">
                        <li>Reason</li>
                        <li>Cost</li>
                      </ul>
                    </td>
                    <td className="detailContent pt-5">
                      <p className="mt-4">{extraSrv.reason}</p>
                      <p className="mt-4">{new Intl.NumberFormat('vi-VN').format(extraSrv.price)} {detail.currency}</p>
                    </td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Before Extra Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.total - extraSrv.price)} {detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Paid</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.total)} {detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Method</td>
                    <td className="detailContent">{detail.paymentMethod ?? "N/A"}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Status</td>
                    <td className="detailContent"> {detail.paymentStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Status</td>
                    <td className="detailContent">{detail.reqStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Note</td>
                    <td className="detailContent">
                      <span className={detail.cancelNote == "" ? "text-gray-500 italic" : ""}>
                        {detail.cancelNote == "NULL" ? "(None)" : detail.cancelNote}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            )}
            <div className="flex justify-center gap-4 mt-6">
              {recordStatus && recordStatus.hasFeedbacks && (
                <button
                  className="bg-blue-400 text-white px-4 py-2 rounded-full"
                  onClick={() => handleViewFeedback(detail)}
                >
                  Feedback
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Popup Feedback */}
      {showFeedback && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white text-[#013171] w-[1000px] rounded-xl p-8 relative">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => setShowFeedback(false)}
              aria-label="Close feedback detail">
              ✖
            </button>
            <h2 className="text-center text-2xl font-bold mb-4">Feedback No #{feedback.feedbackId}</h2>
            <FeedbackDetail feedback={feedback} />
          </div>
        </div>
      )}
    </div>
  );
};

export default History;
