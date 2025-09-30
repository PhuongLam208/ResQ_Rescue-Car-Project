import React from "react";
import "../../../styles/topbar.css";
import { AuthContext } from "../../../context/AuthContext";
import { useContext, useState } from "react";
import { UseRefreshContext } from "../../../context/RefreshContext";

const TopbarDashboard = () => {
  const { logout } = useContext(AuthContext);
  const { triggerRefresh } = UseRefreshContext();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const toggleDropdown = () => setIsDropdownOpen(prev => !prev);

  return (
    <div className="w-topbar flex justify-between items-center">
      <button className="btn-back flex justify-center items-center ms-4"
        onClick={triggerRefresh}
      >
        🔄 Refresh
        {/* <img
          src="images/icon-web/Reply Arrow1.png"
          alt="back-icon"
          className="back-icon-admin"
        /> */}
      </button>
      <div className="flex justify-center items-center me-8 w-right-topbar">
        <div className="flex justify-between items-center">
          <button >
            <img
              src="images/icon-web/Alarm.png"
              alt="back-icon"
              className="noti-icon me-5"
            />
          </button>
          {/* <img
            src="images/icon-web/Person Calendar.png"
            alt="back-icon"
            className="noti-icon me-5"
          /> */}
          <div className="relative">
            <button onClick={toggleDropdown} className="flex items-center space-x-3 px-4 py-2">
              <img src="images/icon-web/avatar.jpg" alt="avatar" className="avatar-icon me-7" />
            </button>

            {isDropdownOpen && (
              <div className="absolute right-0 z-20 mt-2 w-48 flex flex-col bg-white rounded-lg py-2 shadow-lg">
                <a href="#" className="text-drop-topbar font-lexend px-4 py-2 text-gray-700 hover:bg-gray-100">
                  My Profile
                </a>
                <a href="#" className="text-drop-topbar font-lexend px-4 py-2 text-gray-700 hover:bg-gray-100">
                  Lịch trình cá nhân
                </a>
                <a
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    logout();
                  }}
                  className="text-drop-topbar font-lexend px-4 py-2 text-red-500 hover:bg-gray-100"
                >
                  Log out
                </a>
              </div>
            )}
          </div>

          <img
            src="images/icon-web/Settings.png"
            alt="back-icon"
            className="noti-icon"
          />
          <img />
        </div>
      </div>
    </div>
  );
};

export default TopbarDashboard;
