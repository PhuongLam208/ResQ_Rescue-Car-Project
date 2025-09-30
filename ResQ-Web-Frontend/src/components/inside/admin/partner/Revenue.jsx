import React, { useEffect, useState } from "react";
import { getPartnerRevenue } from "../../../../../admin";

const formatVND = (number) =>
  number?.toLocaleString("vi-VN", { style: "currency", currency: "VND" }) || "0 VND";

const Revenue = ({ partnerId }) => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sortBy, setSortBy] = useState("newest");

  useEffect(() => {
    const fetchRevenue = async () => {
      try {
        setLoading(true);
        const result = await getPartnerRevenue(partnerId);
        setData(result || []);
      } catch (error) {
        console.error("Failed to fetch revenue", error);
      } finally {
        setLoading(false);
      }
    };

    if (partnerId) fetchRevenue();
  }, [partnerId]);

  const sortedData = [...data].sort((a, b) => {
    const dateA = new Date(a.year, a.month - 1);
    const dateB = new Date(b.year, b.month - 1);
    return sortBy === "newest" ? dateB - dateA : dateA - dateB;
  });

  const totalRevenue = data.reduce((sum, item) => sum + (item.totalRevenue || 0), 0);
  const totalCommission = data.reduce((sum, item) => sum + (item.totalAppFee || 0), 0);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">Tổng quan doanh thu theo tháng</h2>
        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="px-4 py-2 rounded-full border bg-white hover:bg-gray-100"
        >
          <option value="newest">Gần nhất</option>
          <option value="oldest">Cũ nhất</option>
        </select>
      </div>

      {loading ? (
        <div className="text-gray-600">Đang tải dữ liệu...</div>
      ) : (
        <div className="overflow-x-auto bg-white rounded-lg shadow-md">
          <table className="w-full table-auto">
            <thead className="bg-blue-400 text-white">
              <tr>
                <th className="px-4 py-3 text-left">Tháng</th>
                <th className="px-4 py-3 text-left">Doanh thu</th>
                <th className="px-4 py-3 text-left">Hoa hồng ResQ</th>
                <th className="px-4 py-3 text-left">Trạng thái</th>
                <th className="px-4 py-3 text-left">Ngày thanh toán</th>
              </tr>
            </thead>
            <tbody>
              {/* Tổng cộng */}
              <tr className="bg-gray-100 font-semibold border-b">
                <td className="px-4 py-3">Tổng</td>
                <td className="px-4 py-3">{formatVND(totalRevenue)}</td>
                <td className="px-4 py-3">{formatVND(totalCommission)}</td>
                <td colSpan={2}></td>
              </tr>

              {/* Dữ liệu từng tháng */}
              {sortedData.length > 0 ? (
                sortedData.map((item, idx) => (
                  <tr
                    key={idx}
                    className="border-t hover:bg-gray-50 transition-colors"
                  >
                    <td className="px-4 py-3">{`${item.month}/${item.year}`}</td>
                    <td className="px-4 py-3">{formatVND(item.totalRevenue)}</td>
                    <td className="px-4 py-3">{formatVND(item.totalAppFee)}</td>
                    <td className="px-4 py-3 text-center">
                      <img
                        src={
                          item.status === "COMPLETED"
                            ? "/images/icon-web/Checked Checkbox.png1.png"
                            : "/images/icon-web/Close Window.png"
                        }
                        alt={item.status === "COMPLETED" ? "Đã thanh toán" : "Chưa thanh toán"}
                        className="w-5 h-5 mx-auto"
                      />
                    </td>
                    <td className="px-4 py-3">
                      {item.paymentDate
                        ? new Date(item.paymentDate).toLocaleDateString("vi-VN")
                        : "N/A"}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="text-center py-4 text-gray-500">
                    Không có dữ liệu doanh thu.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Revenue;
