import React from "react";
import DashboardContent from "./DashboardContent"; // ← file mới tạo
import MainDiscount from "./discount/MainDiscount";
import "../../../styles/main-content-admin.css";
import MainReport from "./report/MainReport";
import MainFeedback from "./feedback/MainFeedback";
import MainStaff from "./staff/MainStaff";
import MainManager from "./manager/MainManager";
import MainCustomer from "./customer/MainCustomer";
import MainPartner from "./partner/MainPartner";
import MainServices from "./rescue_service/MainServices";
import MainRequestResQ from "./request_resq/MainRequestResQ";
import MainCalender from "./calender/AdminSchedule";
import MainChatbox from "./chatbox/MainChatbox";
import MainPayment from "./payment/MainPayment";
import MainRefund from "./refund/AdminRefund";
import DiscountPart from "./discount/MainDiscount";
import ReportPart from "./report/ReportPart";
import NotificationTable from "./notification/NotificationTable";
// Có thể không cần gọi ở đây

const MainContentDashboard = ({ activeComponent }) => {
  const renderContent = () => {
    switch (activeComponent) {
      case "dashboard":
        return <DashboardContent />;
      case "feedback":
        return <MainFeedback />;
      case "discount":
        return <MainDiscount />;
      case "report":
        return <ReportPart />;
      case "staff":
        return <MainStaff />;
      case "manager":
        return <MainManager />;
      case "customer":
        return <MainCustomer />;
      case "partner":
        return <MainPartner />;
      case "services":
        return <MainServices />;
      case "resq":
        return <MainRequestResQ />;
      case "schedule":
        return <MainCalender />;
      case "chat":
        return <MainChatbox />;
      case "payment":
        return <MainPayment />;
      case "refund":
        return <MainRefund />;
      case "notification":
        return <NotificationTable />;
      default:
        return <DashboardContent />;
    }
  };

  return (
    <div className="flex justify-center items-center">
      <div className="main-content-admin shadow-sm h-full">
        {renderContent()}
      </div>
    </div>
  );
};

export default MainContentDashboard;
