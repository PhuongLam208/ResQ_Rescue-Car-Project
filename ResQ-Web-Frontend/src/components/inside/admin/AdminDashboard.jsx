import React, { useState } from "react";
import SidebarDashboard from "./SidebarDashboard";
import TopbarDashboard from "./TopbarDashboard";
import MainContentDashboard from "./MainContentDashboard";
import "../../../styles/admin.css";

const AdminDashboard = () => {
  const [activeComponent, setActiveComponent] = useState("dashboard");

  return (
    <div style={{ display: "flex" }} className="w-full">
      {/* Sidebar */}
      <div className="w-admin-sidebar">
        {" "}
        <SidebarDashboard setActiveComponent={setActiveComponent} />
      </div>

      {/* Main Content Area */}
      <div style={{ flex: 1 }} className="all-content-admin">
        {/* Topbar */}
        <TopbarDashboard />

        {/* Main Content */}
        <MainContentDashboard activeComponent={activeComponent} />
        
      </div>
    </div>
  );
};

export default AdminDashboard;
