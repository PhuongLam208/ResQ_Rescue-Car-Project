import axios from 'axios';
import { Navigate } from 'react-router-dom';

export const Url = "http://localhost:9090";

const adminApi = axios.create({
  baseURL: 'http://localhost:9090/api/resq/admin',
  headers: {
    'Accept': 'application/json',
  },
});

adminApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const fetchNavigate = (navigate) => {
  adminApi.interceptors.response.use(
    (response) => {
      return response;
    },
    (error) => {
      if (error.response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        navigate("/login");
      }
      return Promise.reject(error);
    }
  );
};
/// W ///
export const getAllStaff = () => adminApi.get('/staffs');

export const getAllManagers = () => adminApi.get('/managers');

export const getAllRefunds = () => adminApi.get('/refunds');

export const getRefundById = (id) => adminApi.get(`/refunds/${id}`);

export const searchRefundsByName = (name) => adminApi.get(`/refunds/search/${name}`);

export const ReceivedRefund = (id, message) => adminApi.put(`/refunds/received/${id}`, message);

export const RejectRefund = (id, message) => adminApi.put(`/refunds/reject/${id}`, message);

export const getAllSchedule = () => adminApi.get('/schedule');

export const createSchedule = (scheduleData) => adminApi.post('/schedule/save', scheduleData);

export const updateSchedule = (id, scheduleData) => adminApi.put(`/schedule/update/${id}`, scheduleData);

export const deleteSchedule = (id) => adminApi.delete(`/schedule/delete/${id}`);

export const deleteScheduleByDate = (id, date) => adminApi.delete(`/schedule/delete/${id}/${date}`);

export const markRefundAsReceived = (id) => adminApi.put(`/refunds/received/${id}`);

export const getAllUserCustomer = () => adminApi.get('/allcustomers');

/// H ///
export const rescueAPI = {
  getAllRescueInfo: () => adminApi.get("/rescue-info"),

  getRescueDetail: (rrid) =>
    adminApi.get(`/rescue-info/detail/${rrid}`),

  updateStatus: (billId, status) =>
    adminApi.put("/rescue-info/update-status", { billId, status }),
};

/// P ///
/*PARTNER*/
export const partnerAPI = {
  getAllPartners: () => adminApi.get('/partners'),
  search: (keyword) => adminApi.get(`/partners/searchPartners/${keyword}`),
  findById: (partnerId) => adminApi.get(`/partners/findPartnerById/${partnerId}`),
  dashboard: (partnerId) => adminApi.get(`/partners/partnerDashboard/${partnerId}`),
  approveParnter: (partnerId) => adminApi.get(`/partners/approvePartner/${partnerId}`)
};

/*USER*/
export const userAPI = {
  findById: (id) => adminApi.get(`/users/${id}`),
}


/*STAFF*/
export const staffAPI = {
  getStaffs: () => adminApi.get('/staffs-dto'),
  searchStaff: (keyword) => adminApi.get(`/staffs/searchStaffs/${keyword}`),
  createNew: (data) => adminApi.post('/staffs/createStaff', data),
  updateStaff: (staffId, data) => adminApi.put(`/staffs/${staffId}`, data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
};


/*MANAGER*/
export const managerAPI = {
  getManagers: () => adminApi.get('/managers-dto'),
  searchManager: (keyword) => adminApi.get(`/managers/searchManagers/${keyword}`),
  createNew: (data) => adminApi.post('/managers/createManager', data),
  updateManager: (managerId, data) => adminApi.put(`/managers/${managerId}`, data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/*CUSTOMER*/
export const customerAPI = {
  getAllCustomers: () => adminApi.get('/customers/dto'),
  findCustomerById: (customerId) => adminApi.get(`/customer/searchCustomerById/${customerId}`),
  search: (keyword) => adminApi.get(`/customers/searchCustomers/${keyword}`),
  dashboard: (userId) => adminApi.get(`/customers/customerDashboard/${userId}`),
  createNew: (data) => adminApi.post('/customers/createCustomer', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
}

/*FEEDBACK*/
export const feedbackAPI = {
  getAllFeedbacks: () => adminApi.get('/feedbacks'),
  findFeedbacksByPartner: (partId) => adminApi.get(`/feedbacks/searchFeedbackByPartner/${partId}`),
  findFeedbacksByReqResQ: (rrId) => adminApi.get(`/feedbacks/searchFeedbackByRR/${rrId}`),
  averageRate: (partnerId) => adminApi.get(`/feedbacks/averageRate/${partnerId}`)
}

/*REQUEST RESQ*/
export const UserPaymentOfRefund = (refundId) => adminApi.get(`/payment/refundPayment/${refundId}`)
export const reqResQsAPI = {
  
  getAllReqResQs: () => adminApi.get('/reqResQs'),
  findReqResQsByPartner: (partId) => adminApi.get(`/reqResQs/searchByPartner/${partId}`),
  findReqResQsByUser: (userId) => adminApi.get(`/reqResQs/searchByUser/${userId}`), //Rescue Calls - Partner
  searchWithPartner: (keyword, partId) => adminApi.get(`/reqResQs/searchWithPartner/${partId}/${keyword}`), //History - User
  findReqResQById: (rrId) => adminApi.get(`/reqResQs/${rrId}`), //Detail Request Rescue
  searchCustomer: (userId, keyword) => adminApi.get(`/reqResQs/searchWithUser/${userId}/${keyword}`),
  searchRequestResQ: (keyword) => adminApi.get(`/reqResQs/searchRequestResQ/${keyword}`),
  relatedRecordCheck: (rrId) => adminApi.get(`/reqResQs/existedRecords/${rrId}`),
  createNew: (data) => adminApi.post('/reqResQs/createRequest', data),
  updateRequest: (requestId, data) => adminApi.put(`/reqResQs/${requestId}`, data)
}

/*SERVICE*/
export const serviceAPI = {
  findBySrvType: (srvType) => adminApi.get(`/services/searchBySrvType/${srvType}`),
}

/*EXTRA SERVICE*/
export const extraSrvAPI = {
  findExtrasByResResQ: (rrId) => adminApi.get(`/extraSrv/searchByReqResQ/${rrId}`),
}

/*PAYMENT*/
export const paymentAPI = {
  customerPayments: (customerId) => adminApi.get(`/payments/getCustomerPayments/${customerId}`),
}

/*REQUEST SERVICE*/
export const requestSrvAPI = {
  getRequestServices: (rrId) => adminApi.get(`/requestServices/getByResquest/${rrId}`),
}

/*PERSONAL DATA*/
export const personalDataAPI = {
  getUnverifiedUserData: (customerId) => adminApi.get(`/personalDatas/getUnverifiedUserData/${customerId}`),
  approveCustomer: (customerId) => adminApi.put(`/personalDatas/approvedCustomer/${customerId}`),
  rejectCustomer: (customerId, rejectData) => adminApi.put(`/personalDatas/rejectedCustomer/${customerId}`, rejectData),
}

/*DOCUMENT*/
export const documentAPI = {
  getUnverifiedPartnerDoc: (partnerId) => adminApi.get(`/documents/getUnverifiedPartnerDoc/${partnerId}`),
  updatePartnerDoc: (partnerId, rejectData) => adminApi.put(`/documents/updatePartnerDoc/${partnerId}`, rejectData)
}



/// T ///
/*DISCOUNT*/
//Get Discount List
const BASE_URL1 = "http://localhost:9090/api/resq/admin/discount";
const getAuthHeaders = () => {
  const token = localStorage.getItem("token");
  if (!token) {
    console.error("Token không tồn tại.");
    return null;
  }
  return { Authorization: `Bearer ${token}` };
};
export const getAllDiscount = async () => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return [];

    const res = await axios.get(BASE_URL1, { headers });
    return res.data.data;
  } catch (error) {
    console.error("Lỗi khi lấy danh sách discounts:", error);
    return [];
  }
};

// Thêm discount
export const addDiscount = async (newDiscountData) => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return { success: false, message: "Token không tồn tại." };

    const res = await axios.post(BASE_URL1, newDiscountData, { headers });
    return { success: true, data: res.data.data };
  } catch (error) {
    const response = error.response?.data;
    return {
      success: false,
      message: response?.message || "Có lỗi xảy ra!",
      errors: response?.errors || [],
    };
  }
};


// Cập nhật discount
export const updateDiscount = async (id, updatedData) => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return { success: false, message: "Token không tồn tại." };

    const res = await axios.put(`${BASE_URL1}/${id}`, updatedData, { headers });
    return { success: true, data: res.data.data };
  } catch (error) {
    const message = error.response?.data?.message || "Có lỗi xảy ra!";
    const errors = error.response?.data?.errors || null;
    return { success: false, message, errors };
  }
};

// Lấy discount theo ID
export const getDiscountById = async (id) => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return null;

    const res = await axios.get(`${BASE_URL1}/${id}`, { headers });
    return res.data.data;
  } catch (error) {
    console.error("Lỗi khi lấy discount theo ID:", error);
    return null;
  }
};
// Deactivate Discount
export const deactivateDiscount = async (id) => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return null;

    const res = await axios.patch(`${BASE_URL1}/deactivate/${id}`, null, { headers });
    return res.data; // hoặc res.data.data tuỳ theo API trả về
  } catch (error) {
    console.error("Lỗi khi deactivate discount:", error);
    return null;
  }
};
// Search Discount
export const searchDiscounts = async (name) => {
  try {
    const headers = getAuthHeaders();
    if (!headers) return null;

    const res = await axios.get(`${BASE_URL1}/search`, {
      params: { name },
      headers
    });
    return res.data; // hoặc res.data.data nếu muốn lấy luôn data
  } catch (error) {
    console.error("Lỗi khi search discounts:", error);
    return null;
  }
};

/*REPORT*/
export const getAllReport = () => adminApi.get('/report');
export const getReportById = (id) => adminApi.get(`/report/${id}`);
export const resolveReport = (id, resolveData) => adminApi.put(`/report/${id}/resolve`, resolveData);
export const searchUsersByKeyword = (keyword) =>
  adminApi.get('/report/users/search', { params: { keyword } });
export const searchPartnersByKeyword = (keyword) =>
  adminApi.get('/report/partners/search', { params: { keyword } });
export const filterReportsByStatus = (status) =>
  adminApi.get('/report/filter/status', { params: { status } });
export const getReportsByStaff = (staffId) =>
  adminApi.get(`/report/staff/${staffId}`);
export const getReportedPartner = (partnerId) =>
  adminApi.get('/report/by-partner', { params: { partnerId } });

/*SERVICES*/
export const getAllService = () => adminApi.get('/service');
export const getServiceById = (id) => adminApi.get(`/service/${id}`);
export const updateServicePrice = (id, updatedPrices) =>
  adminApi.put(`/service/${id}/update-price`, updatedPrices);
export const searchService = (name) =>
  adminApi.get(`/service/search/${name}`);
export const filterServiceByType = (type) =>
  adminApi.get(`/service/filter/${type}`);

/*DASHBOARD*/
export const getRevenueBChart = (range) =>
  adminApi.post('/dashboard/revenue', range);
export const fetchDailyRescueData = (date) =>
  adminApi.get('/dashboard/rescue/daily', { params: { date } });
export const fetchRangeRescueData = (start, end) =>
  adminApi.get('/dashboard/rescue/range', { params: { start, end } });
export const getTotal = () =>
  adminApi.get('/dashboard/revenue/total');
export const getTotalLastMonth = () =>
  adminApi.get('/dashboard/revenue/last-month');
export const getRescue = () =>
  adminApi.get('/dashboard/rescue/this-month');
export const getRescueLastMonth = () =>
  adminApi.get('/dashboard/rescue/last-month');
export const getCustomer = () =>
  adminApi.get('/dashboard/customer/this-month');
export const getCustomerLastMonth = () =>
  adminApi.get('/dashboard/customer/last-month');
export const getReturnCustomer = () =>
  adminApi.get('/dashboard/customer/returning-this-month');
export const getReturnCustomerLastMonth = () =>
  adminApi.get('/dashboard/customer/returning-last-month');

export const getPartnerRevenue = (partnerId, year) =>
  adminApi.get('/bill/monthly-revenue', {
    params: { partnerId, ...(year && { year }) },
  });

export const getRequestRescue4Report = () =>
  adminApi.get('/request-rescue/for-report');

export const getRequestRescue4ReportById = (rrid) =>
  adminApi.get('/request-rescue/for-report', {
    params: { rrid },
  });

/*DOCUMENTARY*/
export const getAllDocumentaries = () => adminApi.get('/documentary');
export const getDocumentaryById = (id) => adminApi.get(`/documentary/${id}`);
export const getPartnerDocuments = (partnerId) => adminApi.get(`/documentary/by-partner/${partnerId}`);
export const getPartnerImgDocuments = (path) =>
  adminApi.get('/documentary/image', {
    params: { path },
    responseType: 'blob',
  });
export const addDocumentary = (formData) =>
  adminApi.post('/documentary/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

/*PD*/
export const getAllPersonalData = () => adminApi.get('/personaldoc');
export const getPersonalDataById = (id) => adminApi.get(`/personaldoc/${id}`);
export const getUserPersonalDocuments = (userId) => adminApi.get(`/personaldoc/by-user/${userId}`);
export const addPersonalData = (formData) =>
  adminApi.post('/personaldoc/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
export const getImgDocuments = (path) =>
  adminApi.get('/personaldoc/image', {
    params: { path },
    responseType: 'blob',
  });

/*VEHICLE*/
export const getPartnerVehicle = (partnerId) => adminApi.get(`/vehicle/partnerVehicle/${partnerId}`)
export const getAllVehicles = () => adminApi.get('/vehicle');
export const getVehicleById = (id) => adminApi.get(`/vehicle/${id}`);
export const getUserVehicle = (userId) => adminApi.get(`/vehicle/by-user/${userId}`);
export const getVehiclesByPartnerId = (partnerId) => adminApi.get(`/vehicle/by-partner/${partnerId}`);
export const getImgVehicle = (path) =>
  adminApi.get('/vehicle/image', {
    params: { path },
    responseType: 'blob',
  });
export const addVehicle = (formData) =>
  adminApi.post('/vehicle/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });


export const getCustomerPayment = (customerId) => adminApi.get(`/payment/customers/${customerId}`);

/*CUSTOMER*/
export const getCustomerTransactions = (customerId) =>
  adminApi.get(`/users/${customerId}/transactions`);
export const getCustomerReports = (customerId) =>
  adminApi.get(`/users/${customerId}/reports`);

/*BLOCK*/
export const blockUser24h = (userId) =>
  adminApi.put(`/users/${userId}/block-24h`);
export const blockUser = (userId) =>
  adminApi.put(`/users/${userId}/block`);