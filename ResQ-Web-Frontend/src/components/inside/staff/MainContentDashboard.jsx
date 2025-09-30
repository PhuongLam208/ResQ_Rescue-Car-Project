import React from "react";
import "../../../styles/main-content-admin.css";
import Schedule from "./Calendar/StaffSchedule";
import StaffRefund from "./refund/StaffRefund";
import Report from "./report/ReportSection";
import User from "./user/UserSection";
// import MainChat from "./MainChatbox";
import RequestResQ from "./request_resq/FormRequest"
import { AuthContext } from "../../../context/AuthContext";
import { useContext } from "react";

const MainContentDashboard = ({activeComponent}) => {

  const { user } = useContext(AuthContext);

  if (!user) {
    return <div className="text-center text-red-500">Please log in to access this page.</div>;
  }
  
  const componentsMap = {
    schedule: <Schedule />,
    refund: <StaffRefund />,
    report: <Report />,
    resq: <RequestResQ />,
    // chat: <MainChat />
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
