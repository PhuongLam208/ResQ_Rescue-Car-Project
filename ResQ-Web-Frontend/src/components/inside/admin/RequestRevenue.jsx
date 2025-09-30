import React, { useState, useEffect } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import dayjs from "dayjs";
import isoWeek from "dayjs/plugin/isoWeek";
import { getRevenueBChart } from "../../../../admin.js"; // Đường dẫn đúng

dayjs.extend(isoWeek);

// Custom shape cho bar
const CustomBar = ({ x, y, width, height, fill }) => {
  const newWidth = width * 0.3;
  return (
    <rect
      x={x + (width - newWidth) / 2}
      y={y}
      width={newWidth}
      height={height}
      rx={1}
      ry={1}
      fill={fill}
    />
  );
};

// Custom legend
const CustomLegend = ({ payload }) => (
  <div
    style={{
      display: "flex",
      gap: "20px",
      fontWeight: "500",
      fontFamily: "Roboto",
      justifyContent: "center",
      marginBottom: "2rem",
      fontSize: "0.9rem",
    }}
  >
    {payload.map((entry, index) => (
      <div key={`item-${index}`} style={{ color: entry.color }}>
        <span
          style={{
            display: "inline-block",
            width: 10,
            height: 10,
            backgroundColor: entry.color,
            marginRight: 10,
            borderRadius: 6,
          }}
        ></span>
        {entry.value}
      </div>
    ))}
  </div>
);

// Custom tooltip
const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div
        style={{
          backgroundColor: "#fff",
          border: "1px solid #ccc",
          padding: 10,
          borderRadius: 6,
          boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
          fontSize: 14,
          color: "#333",
        }}
      >
        <p><strong>{label}</strong></p>
        {payload.map((item) => (
          <p key={item.name} style={{ color: item.fill, margin: 0 }}>
            {item.name}: {item.value}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

const RequestRevenue = () => {
  const [filterType, setFilterType] = useState("month");
  const [filterValue, setFilterValue] = useState(dayjs().format("YYYY-MM"));
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    const fetchChartData = async () => {
      try {
        let start = null;
        let end = null;

        if (filterType === "month") {
          const [year, month] = filterValue.split("-");
          start = dayjs(`${year}-${month}-01`).startOf("month").format("YYYY-MM-DD");
          end = dayjs(`${year}-${month}-01`).endOf("month").format("YYYY-MM-DD");
        } else if (filterType === "week") {
          const [year, week] = filterValue.split("-W");
          const startDate = dayjs().year(year).isoWeek(parseInt(week)).startOf("week");
          const endDate = startDate.endOf("week");
          start = startDate.format("YYYY-MM-DD");
          end = endDate.format("YYYY-MM-DD");
        } else if (filterType === "year") {
          start = `${filterValue}-01-01`;
          end = `${filterValue}-12-31`;
        }

        const raw = await getRevenueBChart({ start, end });
        const rawData = raw?.data?.length ? raw.data : [
          {
            date: "2025-07-01",
            resTow: 1200000,
            resFix: 800000,
            resDrive: 600000,
          },
          {
            date: "2025-07-02",
            resTow: 1000000,
            resFix: 900000,
            resDrive: 750000,
          },
          {
            date: "2025-07-03",
            resTow: 950000,
            resFix: 850000,
            resDrive: 700000,
          },
        ];
        
        const mapped = rawData.map(item => ({
          date: item.date,
          tow: item.resTow,
          fix: item.resFix,
          drive: item.resDrive,
        }));

        setChartData(mapped);
      } catch (error) {
        console.error("Lỗi khi lấy dữ liệu biểu đồ:", error);
      }
    };

    fetchChartData();
  }, [filterType, filterValue]);

  const handleFilterTypeChange = (e) => {
    const type = e.target.value;
    setFilterType(type);

    if (type === "week") {
      setFilterValue(dayjs().format("YYYY-[W]WW"));
    } else if (type === "month") {
      setFilterValue(dayjs().format("YYYY-MM"));
    } else {
      setFilterValue(dayjs().format("YYYY"));
    }
  };

  return (
  <div className="bg-white border-spacing-1 rounded-2xl p-6 mt-6 shadow-md my-6">
  <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4 mb-6">
    <h2 className="text-xl font-semibold text-gray-800">Rescue Request Statistics</h2>

    <div className="flex flex-wrap items-center gap-3">
      <select
        className="px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
        value={filterType}
        onChange={handleFilterTypeChange}
      >
        <option value="week">Weekly</option>
        <option value="month">Monthly</option>
        <option value="year">Yearly</option>
      </select>

      {filterType === "month" && (
        <input
          type="month"
          value={filterValue}
          onChange={(e) => setFilterValue(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      )}
      {filterType === "week" && (
        <input
          type="week"
          value={filterValue}
          onChange={(e) => setFilterValue(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      )}
      {filterType === "year" && (
        <input
          type="number"
          min="2000"
          max="2100"
          value={filterValue}
          onChange={(e) => setFilterValue(e.target.value)}
          className="w-24 px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      )}
    </div>
  </div>

  <div className="w-full h-[400px]">
    <ResponsiveContainer>
      <BarChart
        data={chartData}
        barCategoryGap="10%"
        barGap={-30}
        onMouseLeave={() => {}}
      >
        <CartesianGrid stroke="#e5e7eb" strokeDasharray="0" vertical={false} strokeWidth={0.5} />
        <XAxis
          dataKey="date"
          tickFormatter={(dateStr) => dayjs(dateStr).format("D MMM")}
          tickLine={false}
          axisLine={false}
          tick={{ fill: '#6b7280', fontSize: 12 }}
        />
        <YAxis
          tickLine={false}
          axisLine={false}
          tick={{ fill: '#6b7280', fontSize: 12 }}
        />
        <Tooltip content={<CustomTooltip />} />
        <Legend content={<CustomLegend />} verticalAlign="top" layout="horizontal" />
        <Bar dataKey="tow" name="Towing" fill="#abc6fc" shape={<CustomBar />} />
        <Bar dataKey="fix" name="On-site Repair" fill="#1744a5" shape={<CustomBar />} />
        <Bar dataKey="drive" name="Driver Replacement" fill="#6f99ed" shape={<CustomBar />} />
      </BarChart>
    </ResponsiveContainer>
  </div>
</div>

  );
};

export default RequestRevenue;
