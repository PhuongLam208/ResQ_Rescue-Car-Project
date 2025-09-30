export const getUserStatus = (status) => {
    switch (status) {
      case "Waiting":
        return "bg-yellow-200 text-yellow-800";
      case "Blocked":
        return "bg-red-200 text-red-800";
      case "Active":
        return "bg-green-200 text-green-800";
      case "Deactive":
        return "bg-gray-200 text-gray-800";
      default:
        return "bg-orange-200 text-orange-800";
    }
  };