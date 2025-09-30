import React, { useState } from "react";
import MainReport from "./MainReport";
import ReportDetails from "./ReportDetails";

const ReportPart = () => {
  const [selectedReport, setSelectedReport] = useState(null);

  return (
    <div className="p-8">
      {selectedReport ? (
        <ReportDetails
          onBack={() => setSelectedReport(null)}
          resolved={selectedReport.resolved}
          data={selectedReport}
        />
      ) : (
        <MainReport onShowDetails={(item) => setSelectedReport(item)} />
      )}
    </div>
  );
};

export default ReportPart;
