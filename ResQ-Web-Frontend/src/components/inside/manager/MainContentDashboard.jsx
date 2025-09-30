import React from "react";
import "../../../styles/main-content-admin.css";
import Schedule from "./Calendar/ManagerSchedule";
import Report from "./report/MainReport";
import Staff from "./staff/MainStaff";
import Chat from "./chatbox/MainChatbox";
import Customer from "./customer/MainCustomer";
import Partner from "./partner/MainPartner";
import { AuthContext } from "../../../context/AuthContext";
import { useContext } from "react";

const MainContentDashboard = ({activeComponent}) => {
  
  const { user} = useContext(AuthContext);
  console.log("User in MainContentDashboard:", user);

  if (!user) {
    return <div className="text-center text-red-500">Please log in to access this page.</div>;
  }
  
  const componentsMap = {
    schedule: <Schedule />,
    report: <Report />,
    staff: <Staff />,
    chat: <Chat />,
    customer: <Customer />,
    partner: <Partner />
  };

  const isAllowed = user.onShift || activeComponent === "schedule";

  const CurrentComponent = isAllowed
    ? componentsMap[activeComponent]
    : <div className="text-red-500 text-lg text-center mt-10">Access Denied</div>;

  return (
    <div className="w-full flex justify-center items-center">
      <div className="main-content-admin">
        {CurrentComponent ? CurrentComponent : <div className="text-center text-lg">You are only allowed to access this feature when your shift has started.</div>}
      </div>
    </div>
  );
};

export default MainContentDashboard;
