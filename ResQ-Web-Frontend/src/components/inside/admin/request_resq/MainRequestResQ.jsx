import React, { useEffect, useState } from "react";
import { getReqStatus } from "../../../../utils/StatusStyle"; // Giữ nguyên hàm này
import { reqResQsAPI, feedbackAPI, extraSrvAPI } from "../../../../../admin"; // Giữ nguyên các API
import FormRequest from "./FormRequest"; // Giữ nguyên component này
import FeedbackDetail from "../feedback/FeedbackDetail"; // Giữ nguyên component này
// import "../../../../styles/admin/requestResQ.css"; // Xóa import CSS cũ

const RescueReQs = () => {
  /*API ALL*/
  const [reqResQ, setReqResQ] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [isLoading, setIsLoading] = useState(true); // Vẫn dùng isLoading cho logic, không ảnh hưởng UI trực tiếp ở đây
  const fetchRequests = async () => {
    try {
      const response = await reqResQsAPI.getAllReqResQs();
      setReqResQ(response.data);
      setIsLoading(false); // Set false sau khi fetch
    } catch (err) {
      console.error("Cannot fetch request: " + err);
      setIsLoading(false);
    }
  }

  const searchRequestResQ = async (e) => {
    e.preventDefault();
    setIsLoading(true); // Bắt đầu loading khi tìm kiếm
    if (keyword.trim() === '') {
      setStatusFilter('');
      fetchRequests(); // Gọi lại hàm fetchRequests để load tất cả
    } else {
      try {
        const response = await reqResQsAPI.searchRequestResQ(keyword);
        setReqResQ(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error("Cannot find any request: " + err);
        setIsLoading(false);
      }
    }
  }

  /* OTHER FUNC */
  const [recordStatus, setRecordStatus] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [extraSrv, setExtraSrv] = useState(null);
  const [detail, selectDetail] = useState(null);
  const [showFeedback, setShowFeedback] = useState(false);

  const handleViewDetail = async (req) => {
    setIsLoading(true); // Bắt đầu loading khi xem chi tiết
    try {
      const responseStatus = await reqResQsAPI.relatedRecordCheck(req.rrid);
      setRecordStatus(responseStatus.data);
    } catch (err) {
      console.error("Cannot check related records status: " + err);
    }
    try {
      const responseExtra = await extraSrvAPI.findExtrasByResResQ(req.rrid);
      setExtraSrv(responseExtra.data);
    } catch (err) {
      console.error("Cannot find extra service: " + err);
    }
    selectDetail(req);
    setIsLoading(false); // Kết thúc loading
  };

  const handleViewFeedback = async (req) => {
    setIsLoading(true); // Bắt đầu loading khi xem feedback
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

  const handleEdit = (req) => {
    setSelectedReq(req);
    setIsCreating(true);
    setIsEdit(true);
  };

  const handleBack = () => {
    setSelectedReq(null);
    setIsEdit(false);
    setIsCreating(false);
    fetchRequests(); // Re-fetch all requests after returning
  };

  /* SORT & FILTER */
  const [statusFilter, setStatusFilter] = useState('');
  const [serviceFilter, setServiceFilter] = useState('');
  const [sortField, setSortField] = useState('');
  const [selectedReq, setSelectedReq] = useState(null); // Đặt lại là null mặc định
  const [sortOrder, setSortOrder] = useState('asc');

  const filteredReq = reqResQ
    .filter((req) => {
      const statusMatch = statusFilter ? req.reqStatus?.toLowerCase() === statusFilter.toLowerCase() : true;
      const serviceMatch = serviceFilter ? req.rescueType?.toLowerCase() === serviceFilter.toLowerCase() : true;
      return statusMatch && serviceMatch;
    })
    .sort((a, b) => {
      if (!sortField) return 0;
      let valueA, valueB;
      if (sortField === "price") {
        valueA = a.totalPrice ?? 0;
        valueB = b.totalPrice ?? 0;
      } else {
        // Fallback for other potential sort fields
        valueA = a[sortField];
        valueB = b[sortField];
      }
      
      // Handle undefined or null values for string comparison
      if (typeof valueA === 'string' && typeof valueB === 'string') {
        return sortOrder === "asc" ? valueA.localeCompare(valueB) : valueB.localeCompare(valueA);
      }
      return sortOrder === "asc" ? valueA - valueB : valueB - valueA;
    });

  const toggleSort = (field) => {
    setSortOrder(sortField === field && sortOrder === "asc" ? "desc" : "asc");
    setSortField(field);
  };

  /* PAGINATION */
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 15;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentReqResQ = filteredReq.slice(startIndex, endIndex);
  const totalPages = Math.ceil(filteredReq.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  // Refetch data when filters change, and reset to first page
  useEffect(() => {
    fetchRequests(); // Luôn fetch lại khi component mount hoặc status/keyword thay đổi
    setCurrentPage(1); // Reset page khi filter/search thay đổi
  }, [statusFilter, serviceFilter, keyword]); // Đã sửa dependency array

  return (
    <div className="p-4 sm:p-6 bg-gray-100 min-h-screen">
      {isCreating ? (
        <FormRequest
          onBack={handleBack}
          req={selectedReq}
          isEdit={isEdit}
        />
      ) : (
        <div className="bg-white p-4 rounded-lg shadow-md">
          {/* Controls: New Request, Search, Filters */}
          <div className="flex flex-col md:flex-row items-center justify-between mb-6 gap-4">
            <button
              className="bg-[#013171] text-white px-6 py-2 rounded-full text-sm font-semibold hover:bg-blue-800 transition-colors duration-200 w-full md:w-auto"
              onClick={() => setIsCreating(true)}
            >
              + New Rescue Request
            </button>

            <form
              onSubmit={searchRequestResQ}
              className="flex items-center border border-gray-300 rounded-full px-4 py-2 flex-grow max-w-lg w-full"
            >
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                type="text"
                placeholder="Search..."
                className="flex-grow outline-none bg-transparent text-gray-700 placeholder-gray-400"
              />
              <button type="submit" className="ml-2">
                <img src="/images/icon-web/Search.png" className="h-5 w-5" alt="Search" />
              </button>
            </form>

            <div className="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
              <select
                className="border border-gray-300 rounded-full px-4 py-2 text-gray-700 outline-none focus:ring-2 focus:ring-[#013171] focus:border-transparent w-full sm:w-auto"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="">--- Status ---</option>
                <option value="Pending">Pending</option>
                <option value="On trip">On trip</option>
                <option value="Cancelled">Cancelled</option>
                <option value="Completed">Completed</option>
              </select>

              <select
                className="border border-gray-300 rounded-full px-4 py-2 text-gray-700 outline-none focus:ring-2 focus:ring-[#013171] focus:border-transparent w-full sm:w-auto"
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
          <div className="overflow-x-auto rounded-lg border border-gray-200 shadow-sm">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-[#68A2F0] text-white">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider rounded-tl-lg">ID</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Customer</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Partner</th>
                  <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider">Service</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider">Address</th>
                  <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider">Status</th>
                  <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider">
                    Total Price
                    <button onClick={() => toggleSort("price")} className="ml-2 focus:outline-none">
                      <img
                        src={`/images/icon-web/Chevron ${sortField === "price" && sortOrder === "asc" ? "Up" : "Down"}.png`}
                        className="h-3 inline-block filter brightness-0 invert" // Tailwind filter to make icon white
                        alt="sort"
                      />
                    </button>
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider rounded-tr-lg">Action</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {currentReqResQ.length > 0 ? (
                  currentReqResQ.map((req) => (
                    <tr key={req.rrid} className="hover:bg-gray-50 transition-colors duration-150">
                      <td className="px-4 py-3 text-center text-sm text-gray-800">{req.rrid}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{req.userName}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{req.partnerName}</td>
                      <td className="px-4 py-3 text-center text-sm text-gray-800">{req.rescueType}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{req.ulocation}</td>
                      <td className="px-4 py-3 text-center">
                        <span
                          className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getReqStatus(req.reqStatus)}`}
                        >
                          {req.reqStatus}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center text-sm text-gray-800">
                        {new Intl.NumberFormat('vi-VN').format(req.totalPrice)} {req.currency}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <div className="flex justify-center items-center gap-2">
                          <button
                            className="bg-blue-100 text-blue-600 text-xs border border-blue-300 rounded-full px-3 py-1 hover:bg-blue-200 transition-colors duration-150"
                            onClick={() => handleViewDetail(req)}
                          >
                            Detail
                          </button>
                          {
                            ["pending", "on trip"].includes(req.reqStatus?.toLowerCase()) && (
                              <button
                                className="bg-white border border-gray-300 rounded-full w-8 h-8 flex items-center justify-center shadow-sm hover:bg-gray-100 transition-colors duration-150"
                                onClick={() => handleEdit(req)}
                                title="Edit Request"
                              >
                                <img src="/images/icon-web/edit.png" className="w-4 h-4" alt="edit" />
                              </button>
                            )
                          }
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="px-4 py-6 text-center text-gray-500">
                      {isLoading ? "Loading requests..." : "No requests found."}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>


          {/* Pagination */}
          {filteredReq.length > itemsPerPage && (
            <div className="flex justify-center items-center mt-6 space-x-4">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={isPrevDisabled}
                className="p-2 rounded-full hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-150"
              >
                <img
                  src={isPrevDisabled ? "/images/icon-web/Back To.png" : "/images/icon-web/Back To1.png"}
                  alt="Previous"
                  className="w-8 h-8"
                />
              </button>
              <span className="text-lg font-semibold text-gray-700">
                {currentPage} / {totalPages}
              </span>
              <button
                onClick={() =>
                  setCurrentPage((prev) => Math.min(prev + 1, totalPages))
                }
                disabled={isNextDisabled}
                className="p-2 rounded-full hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-150"
              >
                <img
                  src={isNextDisabled ? "/images/icon-web/Next page.png" : "/images/icon-web/Next page1.png"}
                  alt="Next"
                  className="w-8 h-8"
                />
              </button>
            </div>
          )}

          {/* Detail Modal */}
          {detail && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-white w-full max-w-4xl max-h-[90vh] overflow-y-auto rounded-xl p-6 sm:p-8 relative text-[#013171] shadow-2xl">
                <button
                  className="absolute top-4 right-4 text-2xl font-bold text-gray-600 hover:text-gray-900 transition-colors duration-200"
                  onClick={() => selectDetail(null)}
                  aria-label="Close detail"
                >
                  ✖
                </button>
                <h2 className="text-center text-2xl sm:text-3xl font-bold mb-4 border-b pb-2 text-gray-800">
                  Rescue Request #{detail.rrid}
                </h2>

                {(recordStatus?.hasCustomerReport || recordStatus?.hasPartnerReport) && (
                  <div className="mb-4 text-sm text-red-600 italic text-right">
                    {recordStatus.hasCustomerReport && recordStatus.hasPartnerReport ? (
                      <span>
                        *There are <strong className="font-bold">2 reports</strong> for this rescue request.
                      </span>
                    ) : recordStatus.hasCustomerReport ? (
                      <span>
                        *There is <strong className="font-bold">1 report from Customer</strong> for this rescue request.
                      </span>
                    ) : (
                      <span>
                        *There is <strong className="font-bold">1 report from Partner</strong> for this rescue request.
                      </span>
                    )}
                  </div>
                )}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 mb-6">
                    {/* Customer Info */}
                    <div>
                        <h3 className="text-lg font-semibold border-b pb-2 mb-3 text-gray-700">Customer's Information</h3>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Full Name:</span>
                            <span className="text-gray-800">{detail.userName}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Phone No:</span>
                            <span className="text-gray-800">{detail.userPhone}</span>
                        </div>
                    </div>

                    {/* Partner Info */}
                    <div>
                        <h3 className="text-lg font-semibold border-b pb-2 mb-3 text-gray-700">Partner's Information</h3>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Name:</span>
                            <span className="text-gray-800">{detail.partnerName}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Phone No:</span>
                            <span className="text-gray-800">{detail.partnerPhone}</span>
                        </div>
                    </div>
                </div>

                <div className="mt-6 border-t pt-6">
                    <h3 className="text-lg font-semibold border-b pb-2 mb-3 text-gray-700">Request Details</h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-3">
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Rescue Service:</span>
                            <span className="text-gray-800">{detail.rescueType}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Rescue Address:</span>
                            <span className="text-gray-800">{detail.ulocation}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Booking Time:</span>
                            <span className="text-gray-800">{new Date(detail.createdAt).toLocaleString("vi-VN")}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">Finish Time:</span>
                            <span className="text-gray-800">{detail.endTime ? new Date(detail.endTime).toLocaleString("vi-VN") : "---"}</span>
                        </div>
                        <div className="flex justify-between py-1">
                            <span className="font-medium text-gray-600">App Fee:</span>
                            <span className="text-gray-800">{new Intl.NumberFormat('vi-VN').format(detail.appFee)} {detail.currency}</span>
                        </div>

                        {extraSrv && extraSrv.reason && extraSrv.price > 0 && (
                            <>
                                <div className="flex justify-between py-1 col-span-1">
                                    <span className="font-medium text-gray-600">Extra Fee Reason:</span>
                                    <span className="text-gray-800">{extraSrv.reason}</span>
                                </div>
                                <div className="flex justify-between py-1 col-span-1">
                                    <span className="font-medium text-gray-600">Extra Fee Cost:</span>
                                    <span className="text-gray-800">{new Intl.NumberFormat('vi-VN').format(extraSrv.price)} {detail.currency}</span>
                                </div>
                                <div className="flex justify-between py-1 col-span-full border-t pt-2 mt-2">
                                    <span className="font-medium text-gray-600">Total Before Extra Fee:</span>
                                    <span className="text-gray-800">{new Intl.NumberFormat('vi-VN').format(detail.totalPrice - extraSrv.price)} {detail.currency}</span>
                                </div>
                            </>
                        )}
                        <div className="flex justify-between py-1 col-span-full sm:col-span-1">
                            <span className="font-medium text-gray-600">Total Paid:</span>
                            <span className="text-gray-800 font-bold text-lg">{new Intl.NumberFormat('vi-VN').format(detail.totalPrice)} {detail.currency}</span>
                        </div>
                        <div className="flex justify-between py-1 col-span-full sm:col-span-1">
                            <span className="font-medium text-gray-600">Payment Method:</span>
                            <span className="text-gray-800">{detail.paymentMethod}</span>
                        </div>
                        <div className="flex justify-between py-1 col-span-full sm:col-span-1">
                            <span className="font-medium text-gray-600">Payment Status:</span>
                            <span className="text-gray-800">{detail.paymentStatus}</span>
                        </div>
                        <div className="flex justify-between py-1 col-span-full sm:col-span-1">
                            <span className="font-medium text-gray-600">Rescue Status:</span>
                            <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getReqStatus(detail.reqStatus)}`}>
                                {detail.reqStatus}
                            </span>
                        </div>
                        <div className="flex flex-col py-1 col-span-full">
                            <span className="font-medium text-gray-600 mb-1">Note:</span>
                            <span className="text-gray-800">
                                {detail.cancelNote === "NULL" || !detail.cancelNote ? (
                                    <span className="italic text-gray-500">(None)</span>
                                ) : (
                                    detail.cancelNote
                                )}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="flex justify-center gap-4 mt-6 border-t pt-4">
                  {recordStatus?.hasFeedbacks && ( // Kiểm tra recordStatus trước khi truy cập
                    <button
                      className="bg-blue-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-blue-700 transition-colors duration-200 shadow-md"
                      onClick={() => handleViewFeedback(detail)}
                    >
                      View Feedback
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Feedback Modal */}
          {showFeedback && feedback && ( // Đảm bảo feedback không null khi hiển thị
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-white text-[#013171] w-full max-w-2xl rounded-xl p-6 sm:p-8 relative shadow-2xl">
                <button
                  className="absolute top-4 right-4 text-2xl font-bold text-gray-600 hover:text-gray-900 transition-colors duration-200"
                  onClick={() => setShowFeedback(false)} // Thay null bằng false
                  aria-label="Close feedback detail"
                >
                  ✖
                </button>
                <h2 className="text-center text-2xl sm:text-3xl font-bold mb-6 border-b pb-2 text-gray-800">
                  Feedback #{feedback.feedbackId}
                </h2>
                <FeedbackDetail feedback={feedback} />
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default RescueReQs;