import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import dayjs from 'dayjs';
import {fetchRangeRescueData, fetchDailyRescueData} from "../../../../admin.js";

const COLORS = ['#abc6fc', '#1744a5', '#6f99ed'];

const RescuePieChart = () => {
const [selectedDate, setSelectedDate] = useState(dayjs().format('YYYY-MM-DD'));

  const [dailyData, setDailyData] = useState({ towing: 0, repairOnSite: 0, driverReplacement: 0 });
  const [weeklyData, setWeeklyData] = useState({ towing: 0, repairOnSite: 0, driverReplacement: 0 });
  const [monthlyData, setMonthlyData] = useState({ towing: 0, repairOnSite: 0, driverReplacement: 0 });
  const [prevMonthlyData, setPrevMonthlyData] = useState({ towing: 0, repairOnSite: 0, driverReplacement: 0 });

  const selected = dayjs(selectedDate);
  const prevWeekStart = selected.subtract(7, 'day').format('YYYY-MM-DD');
  const prevWeekEnd = selected.subtract(1, 'day').format('YYYY-MM-DD');

  const currentMonthStart = selected.startOf('month').format('YYYY-MM-DD');
  const currentMonthEnd = selected.endOf('month').format('YYYY-MM-DD');

  const prevMonthStart = selected.subtract(1, 'month').startOf('month').format('YYYY-MM-DD');
  const prevMonthEnd = selected.subtract(1, 'month').endOf('month').format('YYYY-MM-DD');




useEffect(() => {
  const fetchData = async () => {
    const daily = await fetchDailyRescueData(selectedDate);
    const weekly = await fetchRangeRescueData(prevWeekStart, prevWeekEnd);
    const monthly = await fetchRangeRescueData(currentMonthStart, currentMonthEnd);
    const prevMonthly = await fetchRangeRescueData(prevMonthStart, prevMonthEnd);

    setDailyData(daily);
    setWeeklyData(weekly);
    setMonthlyData(monthly);
    setPrevMonthlyData(prevMonthly);
  };

  fetchData();
}, [selectedDate]);

  const pieData = [
    { name: 'Towing', value: dailyData.towing || 0 },
    { name: 'Repair on Site', value: dailyData.repairOnSite || 0 },
    { name: 'Driver Replacement', value: dailyData.driverReplacement || 0 },
  ];

  return (
       <div className="border-spacing-1 rounded-2xl p-6 mt-6 shadow-md bg-white">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Rescue Trips by Type</h2>

      {/* Date Picker */}
      <div className="mb-6">
        <label className="mr-2 font-medium text-gray-700">Select Date:</label>
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="border border-gray-300 px-3 py-2 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Pie Chart */}
      <div className="w-full h-[300px]">
        <ResponsiveContainer>
          <PieChart>
            <Pie
              data={pieData}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              outerRadius={100}
              label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
            >
              {pieData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* Rescue Table */}
      <div className="mt-8">
        <h3 className="text-lg font-semibold text-gray-700 mb-3">Rescue Trip Analysis</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full border text-sm text-left border-gray-200 rounded-md overflow-hidden">
            <thead className="bg-gray-100 text-gray-700">
              <tr>
                <th className="border px-4 py-2">Type</th>
                {/* <th className="border px-4 py-2">{dayjs(selectedDate).format('DD/MM')}</th> */}
                  <th className="border px-4 py-2">This Week</th>
                <th className="border px-4 py-2">Last Week</th>
                <th className="border px-4 py-2">This Month</th>
                <th className="border px-4 py-2">Previous Month</th>
              </tr>
            </thead>
            <tbody className="text-gray-800">
              {['towing', 'repairOnSite', 'driverReplacement'].map((key, idx) => (
                <tr key={key} className="hover:bg-gray-50 transition">
                  <td className="border px-4 py-2 font-medium">
                    {['Towing', 'Repair on Site', 'Driver Replacement'][idx]}
                  </td>
                  <td className="border px-4 py-2">{dailyData[key] || 0}</td>
                  <td className="border px-4 py-2">{weeklyData[key] || 0}</td>
                  <td className="border px-4 py-2">{monthlyData[key] || 0}</td>
                  <td className="border px-4 py-2">{prevMonthlyData[key] || 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default RescuePieChart;
