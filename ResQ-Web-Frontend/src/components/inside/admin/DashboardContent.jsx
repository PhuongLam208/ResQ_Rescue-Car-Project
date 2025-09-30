import React, { useState, useEffect } from "react";
import RequestRevenue from "./RequestRevenue";
import RescuePieChart from "./RescuePieChart";
import {
  getTotal,
  getRescue,
  getCustomer,
  getReturnCustomer,
  getTotalLastMonth,
  getRescueLastMonth,
  getCustomerLastMonth,
  getReturnCustomerLastMonth,
} from "../../../../admin";

const DashboardContent = () => {
  const [activeTab, setActiveTab] = useState("overview");

  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalRescue, setTotalRescue] = useState(0);
  const [totalCustomer, setTotalCustomer] = useState(0);
  const [totalReturnsCustomer, setTotalReturnsCustomer] = useState(0);

  const [revenueChange, setRevenueChange] = useState(0);
  const [rescueChange, setRescueChange] = useState(0);
  const [customerChange, setCustomerChange] = useState(0);
  const [returnChange, setReturnChange] = useState(0);

  const toNumber = (value) => Number(value) || 0;

  const calculateChangePercent = (previous, current) => {
    const prev = toNumber(previous);
    const curr = toNumber(current);
    if (prev === 0) return curr > 0 ? 100 : 0;
    return ((curr - prev) / prev) * 100;
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [
          revenueRes,
          rescueRes,
          customerRes,
          returnCustomerRes,
          lastRevenueRes,
          lastRescueRes,
          lastCustomerRes,
          lastReturnCustomerRes,
        ] = await Promise.all([
          getTotal(),
          getRescue(),
          getCustomer(),
          getReturnCustomer(),
          getTotalLastMonth(),
          getRescueLastMonth(),
          getCustomerLastMonth(),
          getReturnCustomerLastMonth(),
        ]);

        const revenue = revenueRes.data?.data || 0;
        const rescue = rescueRes.data?.data || 0;
        const customer = customerRes.data?.data || 0;
        const returnCustomer = returnCustomerRes.data?.data || 0;

        const lastRevenue = lastRevenueRes.data?.data || 0;
        const lastRescue = lastRescueRes.data?.data || 0;
        const lastCustomer = lastCustomerRes.data?.data || 0;
        const lastReturnCustomer = lastReturnCustomerRes.data?.data || 0;

        setTotalRevenue(revenue);
        setTotalRescue(rescue);
        setTotalCustomer(customer);
        setTotalReturnsCustomer(returnCustomer);

        setRevenueChange(calculateChangePercent(lastRevenue, revenue));
        setRescueChange(calculateChangePercent(lastRescue, rescue));
        setCustomerChange(calculateChangePercent(lastCustomer, customer));
        setReturnChange(
          calculateChangePercent(lastReturnCustomer, returnCustomer)
        );
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
      }
    };
    fetchData();
  }, []);

  const formattedRevenue = new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(totalRevenue);

  const renderChange = (change) => {
    const isPositive = change >= 0;
    const percentText = `${Math.abs(change).toFixed(1)}%`;
    const iconSrc = isPositive
      ? "/images/icon-web/Chart Arrow Rise.png"
      : "/images/icon-web/Chart Arrow Descent.png";

    return (
      <div
        className={`flex items-center gap-1 ${
          isPositive ? "text-green-500" : "text-red-500"
        }`}
      >
        <img src={iconSrc} alt="change-icon" className="w-4 h-4" />
        <span className="font-semibold text-sm">{percentText}</span>
      </div>
    );
  };
  const Card = ({ title, value, change, iconSrc, highlight = false }) => (
    <div
      className={`relative bg-white p-6 rounded-2xl shadow-md hover:shadow-lg transition-all duration-300 flex flex-col h-40 ${
        highlight ? "bg-blue-50 border border-blue-200" : ""
      }`}
    >
      {/* Title and icon */}
      <div className="flex items-center gap-3 mb-3">
        <div className="bg-blue-100 rounded-full p-2">
          <img src={iconSrc} alt={`${title} icon`} className="w-6 h-6" />
        </div>
        <h2 className="text-gray-700 text-base font-semibold">{title}</h2>
      </div>

      {/* Value - center if highlight */}
      <div
        className={`text-3xl font-bold text-blue-900 
          flex-1 flex items-center justify-center
        `}
      >
        {value}
      </div>

      {/* Change at bottom right */}
      <div className="absolute top-8 right-4 text-sm text-gray-500">
        {renderChange(change)}
      </div>
    </div>
  );

  return (
    <div className="p-4 sm:p-6  min-h-screen">
      {/* Tabs */}
      <div className="mb-6 flex gap-4 border-b border-gray-300">
        {["overview", "rescue"].map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 font-medium transition ${
              activeTab === tab
                ? "border-b-2 border-blue-600 text-blue-600"
                : "text-gray-500 hover:text-blue-500"
            }`}
          >
            {tab === "overview" && "Overview"}
            {tab === "rescue" && "Rescue Analysis"}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === "overview" && (
        <>
          {/* Cards */}
          <div className="grid grid-cols-3 gap-4 mb-8">
            {/* Card đầu tiên chiếm full 3 cột */}
            <div className="col-span-3">
              <Card
                title="Total Revenue"
                value={formattedRevenue}
                change={revenueChange}
                iconSrc="/images/icon-web/Us Dollar Circled.png"
                highlight
              />
            </div>

            {/* 3 card còn lại tự động chiếm 1 cột mỗi cái */}
            <div>
              <Card
                title="Total Rescue"
                value={totalRescue}
                change={rescueChange}
                iconSrc="/images/icon-web/Request Service.png"
              />
            </div>
            <div>
              <Card
                title="Total Customer"
                value={totalCustomer}
                change={customerChange}
                iconSrc="/images/icon-web/People.png"
              />
            </div>
            <div>
              <Card
                title="Total Returns"
                value={totalReturnsCustomer}
                change={returnChange}
                iconSrc="/images/icon-web/Grinning Squinting Face.png"
              />
            </div>
          </div>

          {/* Request Revenue Chart */}
          <div>
            <RequestRevenue />
          </div>
        </>
      )}

      {activeTab === "rescue" && <RescuePieChart />}
    </div>
  );
};

export default DashboardContent;
