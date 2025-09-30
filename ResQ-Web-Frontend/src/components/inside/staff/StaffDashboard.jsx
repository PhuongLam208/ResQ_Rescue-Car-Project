import React from "react";
import { useState } from "react";
import SidebarDashboard from "./SidebarDashboard";
import TopbarDashboard from "./TopbarDashboard";
import MainContentDashboard from "./MainContentDashboard";
import "../../../styles/main-content-admin.css";

const StaffDashboard = () => {
  const [activeComponent, setActiveComponent] = useState("schedule");

  return (
    <div style={{ display: "flex" }} className="w-full all-content-admin">
      {/* Sidebar */}
      <div className="w-admin-sidebar">
        {" "}
        <SidebarDashboard setActiveComponent={setActiveComponent}/>
      </div>

      {/* Main Content Area */}
      <div style={{ flex: 1 }}>
        {/* Topbar */}
        <TopbarDashboard />

        {/* Main Content */}
        <MainContentDashboard activeComponent={activeComponent}/>
      </div>
    </div>
  );
};

export default StaffDashboard;
