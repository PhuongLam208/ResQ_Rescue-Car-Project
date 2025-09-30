import React, { useEffect, useState } from "react";
import { rescueAPI } from "../../../../../admin";

const getStatusColor = (status) => {
  switch (status) {
    case 'PAID':
      return 'bg-green-200 text-green-800';
    case 'UNPAID':
      return 'bg-yellow-200 text-yellow-800';
    case 'FAILED':
    case 'Error':
      return 'bg-red-200 text-red-800';
    default:
      return 'bg-gray-200 text-gray-800';
  }
};

const formatCurrency = (amount) => {
  return amount.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
};

const formatDate = (isoDate) => {
  const date = new Date(isoDate);
  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  }) + ' ' + date.toLocaleDateString('vi-VN');
};

const MainPayment = () => {
  const [transactions, setTransactions] = useState([]);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState("All");
  const [sortField, setSortField] = useState(null); // 'createdAt' | 'total'
  const [sortOrder, setSortOrder] = useState("asc"); // 'asc' | 'desc'
  //const [selectedStatus, setSelectedStatus] = useState('');
  const [rescueDetail, setRescueDetail] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12; // Số item mỗi trang


  const openModal = (transaction) => {
    setSelectedTransaction(transaction);
    setIsModalOpen(true);
    //setSelectedStatus(transaction.status);

  };

  const closeModal = () => {
    setSelectedTransaction(null);
    setIsModalOpen(false);
  };


  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await rescueAPI.getAllRescueInfo();
        setTransactions(res.data);
        setSortField("createdAt"); // Mặc định sort theo thời gian
        setSortOrder("desc");      // Mới nhất lên đầu
      } catch (error) {
        console.error("Lỗi khi gọi API:", error);
      }
    };
    fetchData();
  }, []);


  let filteredTransactions = transactions.filter((item) => {
    const values = [
      item.billId,
      item.rrid,
      item.customerName,
      item.partnerName,
      item.paymentMethod,
      item.method,
      item.createdAt,
      item.total,
      item.status,
    ];
    const matchesSearch = values.some((val) =>
      val?.toString().toLowerCase().includes(searchTerm)
    );

    const matchesStatus =
      statusFilter === "All" || item.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  if (sortField) {
    filteredTransactions.sort((a, b) => {
      const aVal = a[sortField];
      const bVal = b[sortField];

      if (sortField === "createdAt") {
        return sortOrder === "asc"
          ? new Date(aVal) - new Date(bVal)
          : new Date(bVal) - new Date(aVal);
      }

      if (sortField === "total") {
        return sortOrder === "asc" ? aVal - bVal : bVal - aVal;
      }

      return 0;
    });
  }

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredTransactions.slice(indexOfFirstItem, indexOfLastItem);

  const totalPages = Math.ceil(filteredTransactions.length / itemsPerPage);


  // const handleUpdateStatus = async () => {
  //   try {
  //     await axios.put(`http://localhost:9090/api/rescue-info/update-status`, {
  //       billId: selectedTransaction.billId,
  //       status: selectedStatus,
  //     });
  //     // Sau khi cập nhật thành công:
  //     setTransactions((prev) =>
  //       prev.map((t) =>
  //         t.billId === selectedTransaction.billId
  //           ? { ...t, status: selectedStatus }
  //           : t
  //       )
  //     );
  //     closeModal();
  //   } catch (error) {
  //     console.error("❌ Lỗi khi cập nhật trạng thái:", error);
  //   }
  // };

  const handleRescueDetail = async (rrid) => {
    try {
      const res = await rescueAPI.getRescueDetail(rrid);
      setRescueDetail(res.data);
      setIsDetailModalOpen(true);
    } catch (err) {
      console.error("❌ Lỗi khi lấy rescue detail:", err);
    }
  };

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, statusFilter, sortField, sortOrder]);

  return (
    <div className="p-6 bg-white rounded-xl shadow-md max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <input
          type="text"
          placeholder="Search..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value.toLowerCase())}
          className="px-4 py-2 border rounded-full w-1/3 focus:outline-none"
        />

        <select
          className="px-4 py-2 border rounded-full"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="All">All</option>
          <option value="Success">Success</option>
          <option value="Pending">Pending</option>
          <option value="Failed">Failed</option>
        </select>

      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full rounded-xl overflow-hidden border border-gray-200">
          <thead>
            <tr className="bg-[#68A2F0] text-white text-sm font-semibold">
              <th className="py-2 px-3 text-left">ID</th>
              <th className="py-2 px-3 text-left">Rescue Code</th>
              <th className="py-2 px-3 text-left">Customer</th>
              <th className="py-2 px-3 text-left">Partner</th>
              <th
                className="py-2 px-3 text-left cursor-pointer"
                onClick={() => {
                  setSortField("total");
                  setSortOrder(sortOrder === "asc" ? "desc" : "asc");
                }}
              >
                Amount {sortField === "total" && (sortOrder === "asc" ? "↑" : "↓")}
              </th>
              <th className="py-2 px-3 text-left">Method</th>
              <th
                className="py-2 px-3 text-left cursor-pointer"
                onClick={() => {
                  setSortField("createdAt");
                  setSortOrder(sortOrder === "asc" ? "desc" : "asc");
                }}
              >
                Date {sortField === "createdAt" && (sortOrder === "asc" ? "↑" : "↓")}
              </th>
              <th className="py-2 px-3 text-left">Status</th>
              <th className="py-2 px-3 text-left">Action</th>
            </tr>
          </thead>

          <tbody className="text-sm bg-white">
            {currentItems.map((item, index) => (
              <tr key={index} className="border-b">
                <td className="py-2 px-3">{item.billId}</td>
                <td className="py-2 px-3">{item.rrid}</td>
                <td className="py-2 px-3">{item.customerName}</td>
                <td className="py-2 px-3">{item.partnerName}</td>
                <td className="py-2 px-3">{formatCurrency(item.total)}</td>
                <td className="py-2 px-3">{item.paymentMethod || item.method || 'Không rõ'}</td>
                <td className="py-2 px-3">{formatDate(item.createdAt)}</td>
                <td className="py-2 px-3">
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                      item.status
                    )}`}
                  >
                    {item.status}
                  </span>
                </td>
                <td className="py-2 px-3">
                  <button
                    onClick={() => openModal(item)}
                    className="bg-blue-200 hover:bg-blue-300 text-blue-900 px-3 py-1 rounded-full text-sm"
                  >
                    Details
                  </button>


                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="flex justify-center mt-4 gap-2">
  <button
    onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
    disabled={currentPage === 1}
    className="px-3 py-1 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
  >
    Prev
  </button>

  {[...Array(totalPages)].map((_, i) => (
    <button
      key={i}
      onClick={() => setCurrentPage(i + 1)}
      className={`px-3 py-1 rounded ${
        currentPage === i + 1 ? "bg-blue-500 text-white" : "bg-gray-100 hover:bg-gray-300"
      }`}
    >
      {i + 1}
    </button>
  ))}

  <button
    onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
    disabled={currentPage === totalPages}
    className="px-3 py-1 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
  >
    Next
  </button>
</div>

      </div>
      {isModalOpen && selectedTransaction && (
  <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
    <div className="bg-white rounded-xl p-8 w-full max-w-3xl relative shadow-lg">
      <button
        onClick={closeModal}
        className="absolute top-4 right-4 text-2xl font-bold text-gray-500 hover:text-red-500"
      >
        &times;
      </button>

      <h2 className="text-2xl font-bold text-center text-[#1A3E6F] mb-8">
        Payment Receipt #{selectedTransaction.billId || 'N/A'}
      </h2>

      <div className="border rounded-lg p-6 grid grid-cols-2 gap-y-4 text-[#1A3E6F] font-medium text-sm">
        {/* Left column with labels */}
        <div className="flex flex-col gap-4 border-r pr-6">
          <div>Transaction ID</div>
          <div>Customer</div>
          <div>Service Used</div>
          <div>Payment Time</div>
          <div>Payment Method</div>
          <div>Amount</div>
          <div>Rescue Code</div>
          <div>Discount</div>
          <div>VAT</div>
          <div>Payment Status</div>
        </div>

        {/* Right column with values */}
        <div className="flex flex-col gap-4 pl-6 text-right font-normal">
          <div>{selectedTransaction.billId || 'N/A'}</div>
          <div>{selectedTransaction.customerName}</div>
          <div>{selectedTransaction.serviceType || 'Towing'}</div>
          <div>{formatDate(selectedTransaction.createdAt)}</div>
          <div>{selectedTransaction.paymentMethod || 'Unknown'}</div>
          <div>{formatCurrency(selectedTransaction.total)}</div>
          <div>{selectedTransaction.rrid}</div>
          <div>10%</div>
          <div>5%</div>
          <div>{selectedTransaction.status}</div>
        </div>
      </div>

      <div className="flex justify-center gap-4 mt-8 items-center">
      <button
  onClick={() => handleRescueDetail(selectedTransaction.rrid)}
  className="bg-[#85B8FF] text-white px-6 py-2 rounded-full hover:bg-[#6da6fa] transition"
>
  Rescue Details
</button>


        {/* <select
          className="bg-[#A7E4B5] text-white px-6 py-2 rounded-full hover:bg-[#89d79f] transition"
          value={selectedStatus}
          onChange={(e) => setSelectedStatus(e.target.value)}
        >
          <option value="Success">Success</option>
          <option value="Failed">Failed</option>
          <option value="Pending">Pending</option>
        </select>

        <button
          onClick={handleUpdateStatus}
          className="bg-[#A7E4B5] text-white px-6 py-2 rounded-full hover:bg-[#89d79f] transition"
        >
          Save
        </button> */}
      </div>
    </div>
  </div>
)}

{isDetailModalOpen && rescueDetail && (
  <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
    <div className="bg-white rounded-xl p-8 w-full max-w-4xl relative shadow-lg">
      <button
        onClick={() => setIsDetailModalOpen(false)}
        className="absolute top-4 right-4 text-2xl font-bold text-gray-500 hover:text-red-500"
      >
        &times;
      </button>

      <h2 className="text-2xl font-bold text-center text-[#1A3E6F] mb-8">
        Rescue Code #{selectedTransaction?.rrid || 'N/A'}
      </h2>

      {/* Thông tin khách hàng & đối tác */}
      <div className="grid grid-cols-2 border mb-6">
        <div className="p-4 border-r">
          <p className="font-semibold mb-2">Customer Info</p>
          <p>Name: {rescueDetail.customerName}</p>
          <p>Phone: {rescueDetail.customerPhone}</p>
        </div>
        <div className="p-4">
          <p className="font-semibold mb-2">Partner Info</p>
          <p>Name: {rescueDetail.partnerName}</p>
          <p>Phone: {rescueDetail.partnerPhone}</p>
        </div>
      </div>

      {/* Chi tiết cứu hộ */}
      <div className="grid grid-cols-2 gap-y-4 gap-x-6 text-sm border p-6 rounded border-blue-400">
        <div>Rescue Type</div>
        <div className="text-right">{rescueDetail.rescueType}</div>

        <div>Location</div>
        <div className="text-right">{rescueDetail.location}</div>

        <div>Requested Time</div>
        <div className="text-right">{formatDate(rescueDetail.startTime)}</div>

        <div>Executed Time</div>
        <div className="text-right">{formatDate(rescueDetail.endTime)}</div>

        <div>App Fee</div>
        <div className="text-right">{formatCurrency(rescueDetail.appFee)}</div>

        <div>Total</div>
        <div className="text-right">{formatCurrency(rescueDetail.total)}</div>

        <div>Payment Method</div>
        <div className="text-right">{rescueDetail.method}</div>

        <div>Payment Status</div>
        <div className="text-right">{rescueDetail.status}</div>

        <div>Request Status</div>
        <div className="text-right">{rescueDetail.requestStatus}</div>

        <div>Cancel Note</div>
        <div className="text-right">{rescueDetail.cancelNote || "(None)"}</div>
      </div>

      {/* Buttons */}
      {/* <div className="flex justify-center mt-8 gap-4">
        <button className="bg-blue-400 px-6 py-2 rounded-full text-white hover:bg-blue-500">Pay</button>
        <button className="bg-green-400 px-6 py-2 rounded-full text-white hover:bg-green-500">Rate</button>
        <button className="bg-red-400 px-6 py-2 rounded-full text-white hover:bg-red-500">Report</button>
      </div> */}
    </div>
  </div>
)}


    </div>
  );
};

export default MainPayment;


