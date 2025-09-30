import React, { useState } from "react";
import "../../../styles/sidebar_dashboard.css";

const SidebarDashboard = ({ setActiveComponent }) => {
  // State to track the active button
  const [activeButton, setActiveButton] = useState("dashboard");

  // Function to handle button click and change active button
  const handleButtonClick = (buttonId) => {
    setActiveButton(buttonId); // Set the active button
    setActiveComponent(buttonId); // Set the active component in the parent
  };

  return (
    <div className="w-full border-sidebar">
      <div className="flex flex-col">
        <div className="w-full flex justify-center items-center">
          <img
            src="../../../../public/images/6.6.png"
            alt="logo_resq"
            className="w-logo"
          />
        </div>

        {/* Sidebar Content */}
        {/* Content1 */}
        <div className="w-full flex flex-col justify-start items-center">
          {/* Title */}
          <div className="title-sidebar my-2">Staff's Panel</div>


          {/* Schedule */}
          <button
            className={`btn-sidebar mb-3 ${
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
              Schedule
            </p>
          </button>


          {/* Report */}
          <button
            className={`btn-sidebar mb-3 ${
              activeButton === "report" ? "focus-btn" : ""
            }`}
            onClick={() => handleButtonClick("report")}
          >
            <img
              src={
                activeButton === "report"
                  ? "/images/icon-web/High Importance1.png"
                  : "/images/icon-web/High Importance.png"
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


          {/* Refund */}
          <button
            className={`btn-sidebar mb-3 ${
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


          {/* Rescue Request */}
          <button
            className={`btn-sidebar mb-3 ${
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
      </div>
    </div>
  );
};

export default SidebarDashboard;
