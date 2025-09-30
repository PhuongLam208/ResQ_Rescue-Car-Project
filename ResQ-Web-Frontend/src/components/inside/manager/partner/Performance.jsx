import React, { useState, useEffect } from "react";
import { getReqStatus } from "../../../../utils/StatusStyle";
import { reqResQsAPI, extraSrvAPI, feedbackAPI, partnerAPI } from "../../../../../manager";
import "../../../../styles/admin/partner.css";
import FeedbackDetail from "../customer/FeedbackDetail";

const Performance = ({ partner }) => {
  {/* API GET REQUEST*/ }
  const [feedbacks, setFeedbacks] = useState([]);
  const [dashboard, setDashboard] = useState([]);

  //Get Dashboard
  const fetchDashboard = async () => {
    try {
      const response = await partnerAPI.dashboard(partner.partnerId);
      setDashboard(response.data);
      
    } catch (err) {
      console.error("Cannot get dashboard data: " + err);
     
    }
  }

  //Get Performance
  const fetchPartFeed = async () => {
    try {
      const response = await feedbackAPI.findFeedbacksByPartner(partner.partnerId);
      setFeedbacks(response.data);
      
    } catch (err) {
      console.error("Cannot get feedbacks of this partner: " + err);
      
    }
  }

  {/* VIEW DETAIL*/ }
  const [feedback, setFeedback] = useState(null);
  const [requestDetail, setRequestDetail] = useState(null);
  const [extraSrv, setExtraSrv] = useState(null);
  const [recordStatus, setRecordStatus] = useState(null);

  const handleViewRRDetail = async (rrId) => {
    try {
      const response = await reqResQsAPI.findReqResQById(rrId);
      setRequestDetail(response.data);
      
    } catch (err) {
      console.error("Cannot find request rescue of this feedback: " + err);
      
    }
    //Check Report existed
    try {
      const response = await reqResQsAPI.relatedRecordCheck(rrId);
      setRecordStatus(response.data);
    } catch (err) {
      console.error("Cannot check related records status: " + err);
      
    }
    //Check extra service
    try {
      const response = await extraSrvAPI.findExtrasByResResQ(rrId);
      setExtraSrv(response.data);
      
    } catch (err) {
      console.error("Cannot find extra service: " + err);
      
    }
  }

  {/* SETUP PAGINATION*/ }
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentFeedbacks = feedbacks.slice(startIndex, endIndex);
  const totalPages = Math.ceil(feedbacks.length / itemsPerPage);
  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage === totalPages;

  useEffect(() => {
    fetchDashboard();
    fetchPartFeed();
  }, [])

  return (
    <div>
      {/* DASHBOARD*/}
      <div>
        <div className="ml-18 mt-5 text-[#013171]">
          <div className="flex flex-row gap-10">
            <div className="flex flex-row partner-dashboard pl-8">
              <div className="font-lexend mt-[1px]">
                <p className="text-2xl text-center font-semibold">
                  {dashboard.totalSuccess}
                </p>
                <p className="text-[18px] text-center font-medium">
                  Total Success
                </p>
              </div>
              <div className="ml-6">
                <img src="/images/icon-web/bill.png" width="60px" />
              </div>
            </div>
            <div className="flex flex-row partner-dashboard">
              <div className="font-lexend mt-[1px]">
                <p className="text-2xl text-center font-semibold">
                  {dashboard.totalCancel}
                </p>
                <p className="text-[18px] text-center font-medium">
                  Total Cancel
                </p>
              </div>
              <div className="ml-7">
                <img src="/images/icon-web/fail.png" width="60px" />
              </div>
            </div>
            <div className="flex flex-row partner-dashboard">
              <div className="font-lexend mt-[1px]">
                <p className="text-2xl text-center font-semibold">
                  {(dashboard.percentSuccess ?? 0).toFixed(2)}
                </p>
                <p className="text-[16px] text-center font-medium">
                  Percent Success
                </p>
              </div>
              <div className="ml-5">
                <img src="/images/icon-web/done_percentage.png" width="70px" />
              </div>
            </div>
          </div>
          <div className="flex flex-row gap-10 mt-7">
            <div className="flex flex-row px-6 partner-dashboard">
              <div className="font-lexend mt-[10px]">
                <p className="text-2xl text-center font-semibold">
                  {dashboard.totalAmount ? new Intl.NumberFormat('vi-VN').format(dashboard.totalAmount) : 0}
                </p>
                <p className="text-[18px] text-center font-medium">
                  Revenue
                </p>
              </div>
              <div className="ml-5">
                <img src="/images/icon-web/paid_history.png" width="70px" />
              </div>
            </div>
            <div className="flex flex-row partner-dashboard">
              <div className="font-lexend mt-[1px]">
                <p className="text-2xl text-center font-semibold">
                  {(partner.avgRate ?? 0).toFixed(2)}
                </p>
                <p className="text-[18px] text-center font-medium">
                  Average Rate
                </p>
              </div>
              <div className="ml-8">
                <img src="/images/icon-web/rate.png" width="65px" />
              </div>
            </div>
            <div className="flex flex-row align-item-center partner-dashboard pl-[5px]">
              <div className="font-lexend mt-[1px]">
                <p className="text-2xl text-center font-semibold">
                  {partner.avgTime} min
                </p>
                <p className="text-[18px] text-center font-medium">
                  Average Response Time
                </p>
              </div>
              <div className="mr-3">
                <img src="/images/icon-web/response_time.png" width="62px" />
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* PERFORMANCE*/}
      <table className="w-[96%] mx-8 table-auto border rounded-2xl border-r-0 border-l-0 mt-5 text-[14px]">
        {/* Head*/}
        <thead className="font-raleway border bg-[#68A2F0] text-white h-12 border-r-0 border-l-0">
          <tr>
            <th className="w-[10%]">ID</th>
            <th className="w-[18%]">Customer</th>
            <th className="w-[16%]">Request Rescue No.</th>
            <th className="w-[10%]">Service</th>
            <th className="w-[8%]">Rate</th>
            <th className="w-[10%]">Status</th>
            <th className="w-[8%]">Action</th>
          </tr>
        </thead>
        {/* Body*/}
        <tbody>
          {currentFeedbacks.map((fed, index) => (
            <tr key={index} className="shadow h-12 font-lexend" >
              <td className="pl-5">{fed.feedbackId}</td>
              <td>{fed.userName}</td>
              <td className="pl-5">{fed.rrId}</td>
              <td>{fed.rescueType}</td>
              <td className="text-center">{fed.ratePartner}</td>
              <td className="text-center">
                <p
                  className={`text-xs py-1 w-[9vh] h-6 rounded-3xl text-center ${getReqStatus(
                    fed.reqStatus
                  )}`}
                >
                  {fed.reqStatus}
                </p>
              </td>
              <td>
                <button
                  className="bg-blue-200 text-blue-600 text-xs  border rounded-full px-3 h-6 w-18 text-center align-center"
                  onClick={() => setFeedback(fed)}>
                  Detail
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {/* Show Feedback Detail */}
      {feedback && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white text-[#013171] w-[1000px] rounded-xl p-8 relative">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => setFeedback(null)}
              aria-label="Close feedback detail">
              ✖
            </button>
            <h2 className="text-center text-2xl font-bold mb-4">Feedback No #{feedback.feedbackId}</h2>
            <FeedbackDetail feedback={feedback} />
            <div className="flex justify-center mt-3">
              <button
                className="bg-green-600 text-white px-4 py-2 rounded-full"
                onClick={() => handleViewRRDetail(feedback.rrId)}
              >
                Resquest Rescue
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Show Resquest ResQ Detail */}
      {requestDetail && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white w-[1000px] max-h-[100vh] overflow-y-auto rounded-xl p-8 relative text-[#013171]">
            <button className="absolute top-4 right-4 text-xl font-bold" onClick={() => setRequestDetail(null)}>
              ✖
            </button>
            <h2 className="text-center text-2xl font-bold mb-4">Rescue No #{requestDetail.rrid}</h2>
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
                  <td className="InformationContent">{requestDetail.userName}</td>
                  <td className="InformationTitle">Name</td>
                  <td className="InformationContent">{requestDetail.partnerName}</td>
                </tr>
                <tr>
                  <td className="InformationTitle">Phone No</td>
                  <td className="InformationContent">{requestDetail.userPhone}</td>
                  <td className="InformationTitle">Phone No</td>
                  <td className="InformationContent">{requestDetail.partnerPhone}</td>
                </tr>
              </tbody>
            </table>
            {!extraSrv || (extraSrv.reason === "" && extraSrv.price === 0) ? (
              <table className="border-b border-gray-500 w-[70vh] ml-28">
                <tbody>
                  <tr>
                    <td className="w-[35%] detailTitle pl-5">Rescue Service</td>
                    <td className="detailContent">{requestDetail.rescueType}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Address</td>
                    <td className="detailContent">{requestDetail.ulocation}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Booking Time</td>
                    <td className="detailContent">{new Date(requestDetail.createdAt).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Finish Time</td>
                    <td className="detailContent">{new Date(requestDetail.updatedAt).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">App Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(requestDetail.appFee)} {requestDetail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Paid</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(requestDetail.total)} {requestDetail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Method</td>
                    <td className="detailContent">{requestDetail.paymentMethod}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Status</td>
                    <td className="detailContent"> {requestDetail.paymentStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Status</td>
                    <td className="detailContent">{requestDetail.reqStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Note</td>
                    <td className="detailContent">
                      <span className={requestDetail.cancelNote == "NULL" ? "text-gray-500 italic" : ""}>
                        {requestDetail.cancelNote == "NULL" ? "(None)" : requestDetail.cancelNote}
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
                    <td className="detailContent">{requestDetail.rescueType}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Address</td>
                    <td className="detailContent">{requestDetail.ulocation}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Booking Time</td>
                    <td className="detailContent">{new Date(requestDetail.createdAt).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Finish Time</td>
                    <td className="detailContent">{new Date(requestDetail.updatedAt).toLocaleString("vi-VN")}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">App Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(requestDetail.appFee)} {requestDetail.currency}</td>
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
                      <p className="mt-4">{new Intl.NumberFormat('vi-VN').format(extraSrv.price)} {requestDetail.currency}</p>
                    </td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Before Extra Fee</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(requestDetail.total - extraSrv.price)} {requestDetail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Total Paid</td>
                    <td className="detailContent">{new Intl.NumberFormat('vi-VN').format(requestDetail.total)} {requestDetail.currency}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Method</td>
                    <td className="detailContent">{requestDetail.paymentMethod}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Payment Status</td>
                    <td className="detailContent"> {requestDetail.paymentStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Rescue Status</td>
                    <td className="detailContent">{requestDetail.reqStatus}</td>
                  </tr>
                  <tr>
                    <td className="detailTitle">Note</td>
                    <td className="detailContent">
                      <span className={requestDetail.cancelNote == "NULL" ? "text-gray-500 italic" : ""}>
                        {requestDetail.cancelNote == "NULL" ? "(None)" : requestDetail.cancelNote}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
      {/* Pagination */}
      {feedbacks.length > itemsPerPage && (
        <div className="flex justify-center mt-1 space-x-2">
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
  );
};

export default Performance;
