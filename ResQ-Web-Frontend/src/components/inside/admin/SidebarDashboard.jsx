import React, { useState } from "react";
import "../../../styles/sidebar_dashboard.css";

const SidebarDashboard = ({ setActiveComponent }) => {
  // State to track the active button
  const [activeButton, setActiveButton] = useState("dashboard");

  // Function to handle button click and change active button
  const handleButtonClick = (buttonId) => {
    setActiveButton(buttonId); // Set the active button
    setActiveComponent(buttonId);
  };
  return (
    <div className="w-full border-sidebar">
      <div className="flex flex-col">
        <div className="w-full flex justify-center items-center">
          <img
            src="/images/logo.png"
            alt="logo_resq"
            className="w-logo"
          />
        </div>

        {/* Sidebar Content */}
        {/* Content1 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">Overview & General</div>

          {/* Dashboard */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "dashboard" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("dashboard")}
          >
            <img
              src={
                activeButton === "dashboard"
                  ? "/images/icon-web/LaptopMetrics_w.png"
                  : "/images/icon-web/Laptop_Metrics.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "dashboard" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Dashboard
            </p>
          </button>
          {/* Feedback */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "feedback" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("feedback")}
          >
            <img
              src={
                activeButton === "feedback"
                  ? "/images/icon-web/Popular1.png"
                  : "/images/icon-web/Popular.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "feedback" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Feedback
            </p>
          </button>
          {/* Report */}
          <button
            className={`btn-sidebar ${
              activeButton === "report" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("report")}
          >
            <img
              src={
                activeButton === "report"
                  ? "/images/icon-web/icons8-high-importance-50-818181.png"
                  : "/images/icon-web/icons8-high-importance-50.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "report" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Report
            </p>
          </button>
        </div>

        {/* Content2 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">Human resources & partners</div>

          {/* Staff */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "staff" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("staff")}
          >
            <img
              src={
                activeButton === "staff"
                  ? "/images/icon-web/Staff1.png"
                  : "/images/icon-web/Staff.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "staff" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Staff
            </p>
          </button>

          {/* Manager */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "manager" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("manager")}
          >
            <img
              src={
                activeButton === "manager"
                  ? "/images/icon-web/Manager1.png"
                  : "/images/icon-web/Manager.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "manager" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Manager
            </p>
          </button>

          {/* Partner */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "partner" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("partner")}
          >
            <img
              src={
                activeButton === "partner"
                  ? "/images/icon-web/Handshake1.png"
                  : "/images/icon-web/Handshake.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "partner" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Partner
            </p>
          </button>
        </div>

        {/* Content3 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">Customer & Service</div>

          {/* Customer */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "customer" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("customer")}
          >
            <img
              src={
                activeButton === "customer"
                  ? "/images/icon-web/Customer Insight1.png"
                  : "/images/icon-web/Customer Insight.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "customer" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Customer
            </p>
          </button>

          {/* Rescue Servies */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "services" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("services")}
          >
            <img
              src={
                activeButton === "services"
                  ? "/images/icon-web/Google Home1.png"
                  : "/images/icon-web/Google Home.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "services" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Rescue Services
            </p>
          </button>

          {/* Rescue Request */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "resq" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("resq")}
          >
            <img
              src={
                activeButton === "resq"
                  ? "/images/icon-web/Request Service1.png"
                  : "/images/icon-web/Request Service.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "resq" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Request Rescue
            </p>
          </button>
          <button
            className={`btn-sidebar ${
              activeButton === "notification" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("notification")}
          >
            <img
              src={
                activeButton === "notification"
                  ? "/images/icon-web/Push Notifications1.png"
                  : "/images/icon-web/Push Notifications.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "notification" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Noti Templates
            </p>
          </button>
        </div>

        {/* Content4 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">Preferential and events</div>

          {/* Discount */}
          <button
            className={`btn-sidebar mb-2 ${
              activeButton === "discount" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("discount")}
          >
            <img
              src={
                activeButton === "discount"
                  ? "/images/icon-web/Discount1.png"
                  : "/images/icon-web/Discount.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "discount" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Discounts
            </p>
          </button>

          {/* Schedule */}
          <button
            className={`btn-sidebar ${
              activeButton === "schedule" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("schedule")}
          >
            <img
              src={
                activeButton === "schedule"
                  ? "/images/icon-web/Schedule1.png"
                  : "/images/icon-web/Schedule.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "schedule" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Calender
            </p>
          </button>
        </div>

        {/* Content5 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">
Communication & Support</div>

          {/* Chat */}
          <button
            className={`btn-sidebar mb-3 ${
              activeButton === "chat" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("chat")}
          >
            <img
              src={
                activeButton === "chat"
                  ? "/images/icon-web/Computer Chat1.png"
                  : "/images/icon-web/Computer Chat.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "chat" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Chatbox
            </p>
          </button>

          
        </div>

        {/* Content6 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">
Payment transaction</div>

          {/* Payment */}
          <button
            className={`btn-sidebar mb-3 ${
              activeButton === "payment" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("payment")}
          >
            <img
              src={
                activeButton === "payment"
                  ? "/images/icon-web/Taxi Mobile Payment Euro_hover.png"
                  : "/images/icon-web/Taxi Mobile Payment Euro.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "payment" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Payment
            </p>
          </button>

          {/* Refund */}
          <button
            className={`btn-sidebar mb-5 ${
              activeButton === "refund" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("refund")}
          >
            <img
              src={
                activeButton === "refund"
                  ? "/images/icon-web/Exchange Dollar.png"
                  : "/images/icon-web/Exchange Dollar1.png"
              }
              alt="item-logo-sidebar"
              className="img-icon-small"
            />
            <p
              className={`font-lexend text-btn-sidebar ${
                activeButton === "refund" ? "text-white" : "text-btn-sidebar"
              }`}
            >
              Refund
            </p>
          </button>
        </div>
      </div>
    </div>
  );
};

export default SidebarDashboard;
