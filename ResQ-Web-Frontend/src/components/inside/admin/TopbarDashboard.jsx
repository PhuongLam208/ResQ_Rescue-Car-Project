import React, {useState} from "react";
import "../../../styles/topbar.css";
import { AuthContext } from "../../../context/AuthContext";
import { useContext } from "react";
import { UseRefreshContext } from "../../../context/RefreshContext";


const TopbarDashboard = () => {
  const { logout } = useContext(AuthContext);

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const toggleDropdown = () => setIsDropdownOpen(prev => !prev);

  return (
    <div className="w-topbar flex justify-end items-center">
      
     
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
                <a href="/my-profile" className="text-drop-topbar font-lexend px-4 py-2 text-gray-700 hover:bg-gray-100">
                  My Profile
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
      
      </div>

  );
};

export default TopbarDashboard;
