import React, { useState, useEffect } from "react";
import "../../../../styles/admin/feedback.css";
import { feedbackAPI } from "../../../../../admin";

const StarRating = ({ count }) => {
  return (
    <div className="flex items-center justify-center">
      {[...Array(count)].map((_, i) => (
        <svg
          key={i}
          className="w-5 h-5 text-yellow-500"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.97a1 1 0 00.95.69h4.18c.969 0 1.371 1.24.588 1.81l-3.39 2.46a1 1 0 00-.364 1.118l1.287 3.97c.3.921-.755 1.688-1.54 1.118l-3.39-2.46a1 1 0 00-1.175 0l-3.39 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.97a1 1 0 00-.364-1.118l-3.39-2.46c-.783-.57-.38-1.81.588-1.81h4.18a1 1 0 00.95-.69l1.286-3.97z" />
        </svg>
      ))}
    </div>
  );
};

const MainFeedback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [feedbacks, setFeedbacks] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  useEffect(() => {
    const fetchFeedbacks = async () => {
      try {
        const response = await feedbackAPI.getAllFeedbacks();
        setFeedbacks(response.data || []);
      } catch (error) {
        console.error("Cannot get feedbacks: ", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeedbacks();
  }, []);

  const totalPages = Math.ceil(feedbacks.length / itemsPerPage);
  const currentFeedbacks = feedbacks.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const renderRow = (label, renderFn) => (
    <tr>
      <td className="font-raleway tableHead">{label}</td>
      {currentFeedbacks.map((item, index) => (
        <td key={index} className="font-lexend tableContent text-center">
          {renderFn(item, index)}
        </td>
      ))}
    </tr>
  );

  return (
    <div className="overflow-x-auto">
      <table className="mx-14 mt-10 w-[130vh] border border-separate border-spacing-0 border-[#D8D8D8] rounded-2xl">
        <tbody>
          {isLoading ? (
            <></>
          ) : (
            <>
              {renderRow("No.", (_, index) => (currentPage - 1) * itemsPerPage + index + 1)}
              {renderRow("Request Rescue No.", item => item.rrId ?? "---")}
              {renderRow("Rescue Rate", item => <StarRating count={item.rateRequest ?? 0} />)}
              {renderRow("Feedback For Request", item => item.feedbackRequest || "---")}
              {renderRow("Customer", item => item.userName || "---")}
              {renderRow("Customer's Rate", item => <StarRating count={item.rateCustomer ?? 0} />)}
              {renderRow("Feedback For Customer", item => item.feedbackCustomer || "---")}
              {renderRow("Partner", item => item.partnerName || "---")}
              {renderRow("Partner's Rate", item =>
                item.ratePartner > 0 ? <StarRating count={item.ratePartner} /> : "---"
              )}
              {renderRow("Feedback For Partner", item => item.feedbackPartner || "---")}
            </>
          )}
        </tbody>
      </table>

      {!isLoading && totalPages > 1 && (
        <div className="flex justify-center mt-4 items-center space-x-4">
          <button
            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
          >
            <img
              src={`/images/icon-web/${currentPage === 1 ? "Back To" : "Back To1"}.png`}
              alt="Back"
              className="w-9"
            />
          </button>
          <span className="px-3 py-2 font-semibold text-lg">
            {currentPage} / {totalPages}
          </span>
          <button
            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
          >
            <img
              src={`/images/icon-web/${currentPage === totalPages ? "Next page" : "Next page1"}.png`}
              alt="Next"
              className="w-9"
            />
          </button>
        </div>
      )}
    </div>
  );
};

export default MainFeedback;
