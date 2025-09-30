import React, { useEffect, useState } from "react";
import { getReqStatus } from "../../../../utils/StatusStyle";
import { reqResQsAPI, extraSrvAPI, feedbackAPI } from "../../../../../manager";
import FeedbackDetail from "../customer/FeedbackDetail";

const RescueCalls = ({ partner }) => {
  //API Support
  const [reqResQ, setReqResQ] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  //API Get All
  const fetchRequests = async () => {
    try {
      const response = await reqResQsAPI.findReqResQsByPartner(partner.partnerId);
      setReqResQ(response.data);
      setIsLoading(false);
    } catch (err) {
      console.error("Cannot get request rescues of this partner: " + err);
      setIsLoading(false);
    }
  };


  //API Search
  const searchRescue = async (e) => {
    e.preventDefault();
    if (keyword.trim() === '') {
      fetchRequests();
    } else {
      try {
        const response = await reqResQsAPI.searchWithPartner(keyword, partner.partnerId);
        setReqResQ(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error("Cannot find any user: " + err)
        setIsLoading(false);
      }
    }
  }

  {/* SHOW - NONE Detail */ }
  const [recordStatus, setRecordStatus] = useState(null);
  const [extraSrv, setExtraSrv] = useState(null);
  const [feedback, setFeedback] = useState('');
  const [detail, selectDetail] = useState('');
  const [showFeedback, setShowFeedback] = useState(false);

  const handleViewDetail = async (req) => {
    try {
      const response = await reqResQsAPI.relatedRecordCheck(req.rrid);
      setRecordStatus(response.data);
    } catch (err) {
      console.error("Cannot check related records status: " + err);
      setIsLoading(false);
    }
    try {
      const response = await extraSrvAPI.findExtrasByResResQ(req.rrid);
      setExtraSrv(response.data);
    } catch (err) {
      console.error("Cannot find extra service: " + err);
      setIsLoading(false);
    }
    selectDetail(req);
    setIsLoading(false);
  };

  const handleViewFeedback = async (req) => {
    try {
      const response = await feedbackAPI.findFeedbacksByReqResQ(req.rrid);
      setFeedback(response.data);
      setShowFeedback(true);
      setIsLoading(false);
    } catch (err) {
      console.error("Cannot get feedbacks of this request: " + err);
      setIsLoading(false);
    }
  }

  {/* Setup Filter & Sort*/ }
  const [statusFilter, setStatusFilter] = useState('');
  const [serviceFilter, setServiceFilter] = useState('');
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState('asc');
  const filteredReq = reqResQ
    .filter((req) => {
      const statusMatch = statusFilter ? req.reqStatus === statusFilter : true;
      const serviceMatch = serviceFilter ? req.rescueType === serviceFilter : true;
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

  {/* Setup Pagination*/ }
  const itemsPerPage = 14; //Row in one page
  const [currentPage, setCurrentPage] = useState(1);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentReqResQ = filteredReq.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredReq.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  {/* use Effect*/ }

  useEffect(() => {
    fetchRequests();
    setCurrentPage(1);
  }, [statusFilter, serviceFilter]);
  return (
    <div>
      <div className="flex">
        {/* Search Form */}
        <form
          onSubmit={searchRescue}
          onChange={searchRescue}
          className="flex items-center border border-gray-300 rounded-full w-full max-w-xl px-4 py-2 my-[1vh] ml-[12vw]">
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
        {/* Filter Status*/}
        <div className="items-center border border-gray-300 rounded-full mt-[1vh] h-[4.5vh] w-36 ml-[2vw]">
          <select
            className="mx-3 mt-2 w-28"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="" className="text-center">--- Status ---</option>
            <option value="Pending">Pending</option>
            <option value="On trip">On trip</option>
            <option value="cancelled">Cancelled</option>
            <option value="Completed">Completed</option>
          </select>
        </div>
        {/* Filter Service*/}
        <div className="items-center border border-gray-300 rounded-full mt-[1vh] h-[4.5vh] w-36 ml-[2vw]">
          <select
            className="mx-3 mt-2 w-28"
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
      <table className="w-full table-auto border rounded-2xl border-r-0 border-l-0 text-[14px]">
        {/* Table Head */}
        <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0">
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Service</th>
            <th>Address</th>
            <th>
              Rescue Date
              <button onClick={() => toggleSort("date")}>
                {sortField === "date" ? (
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
            <th>
              Total Price
              <button onClick={() => toggleSort("price")}>
                {sortField === "price" ? (
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
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        {/* Table Body*/}
        <tbody>
          {currentReqResQ.map((req, index) => (
            <tr key={req.rrid} className="shadow h-12 font-lexend">
              <td className="text-center">{index + 1}</td>
              <td>{req.userName}</td>
              <td className="text-center">{req.rescueType}</td>
              <td>{req.ulocation}</td>
              <td className="text-center">
                {new Date(req.startTime).toLocaleString('vi-VN')}
              </td>
              <td className="text-center">
                {req.total ? new Intl.NumberFormat('vi-VN').format(req.total) : 0} {req.currency}
              </td>
              <td className="px-4">
                <span
                  className={`text-xs px-3 py-1 rounded-full ${getReqStatus(req.reqStatus)}`}
                >
                  {req.reqStatus}
                </span>
              </td>
              <td>
                <button
                  className="bg-blue-200 text-blue-600 text-xs border rounded-full px-3 h-6 w-18 text-center"
                  onClick={() => handleViewDetail(req)}
                >
                  Detail
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {/* Pagination */}
      {filteredReq.length > itemsPerPage && (
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

      {/* Show Resquest ResQ Detail */}
      {detail && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white w-[1000px] max-h-[90vh] overflow-y-auto rounded-xl p-8 relative text-[#013171]">
            <button className="absolute top-4 right-4 text-xl font-bold" onClick={() => selectDetail(null)}>
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
            <table className="w-full">
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
            {!extraSrv || (extraSrv.reason === "" && extraSrv.price === 0) ? (
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
                    <td className="detailTitle">App Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.appFee)} {detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Paid</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.total)} {detail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Method</td>
                    <td className="detailContent">{detail.paymentMethod}</td>
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
                      <span className={detail.cancelNote == "NULL" ? "text-gray-500 italic" : ""}>
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
                    <td className="detailTitle">App Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(detail.appFee)} {detail.currency}</td>
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
                    <td className="detailContent">{detail.paymentMethod}</td>
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
                      <span className={detail.cancelNote == "NULL" ? "text-gray-500 italic" : ""}>
                        {detail.cancelNote == "NULL" ? "(None)" : detail.cancelNote}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            )}
            <div className="flex justify-center gap-4 mt-6">
              {recordStatus.hasFeedbacks && (
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
      {/* Show Feedback Detail */}
      {showFeedback && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white text-[#013171] w-[1000px] rounded-xl p-8 relative">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => setShowFeedback(null)}
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

export default RescueCalls;
