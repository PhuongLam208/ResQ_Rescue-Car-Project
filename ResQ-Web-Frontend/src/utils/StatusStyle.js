export const getUserStatus = (status) => {
  switch (status.toLowerCase()) {
    case "waiting":
      return "bg-blue-200 text-blue-800";
    case "active":
      return "bg-green-200 text-green-800";
    case "deactive":
      return "bg-yellow-200 text-yellow-800";
    case "24h":
      return "bg-orange-200 text-orange-800";
    case "blocked":
      return "bg-red-200 text-red-800";
    default:
      return "bg-gray-200 text-gray-800";
  }
};

export const getReqStatus = (status) => {
  if (!status || typeof status !== "string") return "bg-gray-300 text-gray-600";
  switch (status.toLowerCase()) {
    case "pending":
      return "bg-sky-800 text-white px-3";
    case "accepted":
      return "bg-blue-200 text-blue-800 px-3";
    case "on trip":
      return "bg-yellow-200 text-yellow-800 px-3";
    case "failed":
      return "bg-red-200 text-red-800 px-1.5";
    case "cancelled":
      return "bg-red-500 text-white px-3";
    case "completed":
      return "bg-green-200 text-green-800 px-2";
    default:
      return "bg-gray-100 text-gray-800";
  }
};