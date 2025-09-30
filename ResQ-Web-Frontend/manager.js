import axios from 'axios';
import { Navigate } from 'react-router-dom';

const managerApi = axios.create({
    baseURL: 'http://localhost:9090/api/resq/manager',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
});

managerApi.interceptors.request.use(
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
    managerApi.interceptors.response.use(
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

export const getAllStaff = () => managerApi.get('/staffs');

export const getAllSchedule = () => managerApi.get('/schedule');

export const updateSchedule = (id, scheduleData) => managerApi.put(`/schedule/update/${id}`, scheduleData);

/*REPORT*/
export const getAllReport = () => managerApi.get('/report');
export const getReportById = (id) => managerApi.get(`/report/${id}`);
export const resolveReport = (id, resolveData) => managerApi.put(`/report/${id}/resolve`, resolveData);
export const searchUsersByKeyword = (keyword) =>
  managerApi.get('/report/users/search', { params: { keyword } });
export const searchPartnersByKeyword = (keyword) =>
  managerApi.get('/report/partners/search', { params: { keyword } });
export const filterReportsByStatus = (status) =>
  managerApi.get('/report/filter/status', { params: { status } });
export const getReportsByStaff = (staffId) =>
  managerApi.get(`/report/staff/${staffId}`);
export const getReportedPartner = (partnerId) =>
  managerApi.get('/report/by-partner', { params: { partnerId } });

/*STAFF*/
export const staffAPI = {
  getStaffs: () => managerApi.get('/staffs-dto'),
  searchStaff: (keyword) => managerApi.get(`/staffs/searchStaffs/${keyword}`),
  createNew: (data) => managerApi.post('/staffs/createStaff', data),
  updateStaff: (staffId, data) => managerApi.put(`/staffs/${staffId}`, data)
};

export const getAllCustomers = () => managerApi.get('/customers');

export const getAllPartners = () => managerApi.get('/partners');

export const blockUser24h = (userId) =>
  managerApi.put(`/users/${userId}/block-24h`);

/*CUSTOMER*/
export const customerAPI = {
  getAllCustomers: () => managerApi.get('/customers/dto'),
  findCustomerById: (customerId) => managerApi.get(`/customer/searchCustomerById/${customerId}`),
  search: (keyword) => managerApi.get(`/customers/searchCustomers/${keyword}`),
  dashboard: (userId) => managerApi.get(`/customers/customerDashboard/${userId}`),
  createNew: (data) => managerApi.post('/customers/createCustomer', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
}

/*PD*/
export const getAllPersonalData = () => managerApi.get('/personaldoc');
export const getPersonalDataById = (id) => managerApi.get(`/personaldoc/${id}`);
export const getUserPersonalDocuments = (userId) => managerApi.get(`/personaldoc/by-user/${userId}`);
export const addPersonalData = (formData) =>
  managerApi.post('/personaldoc/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
export const getImgDocuments = (path) =>
  managerApi.get('/personaldoc/image', {
    params: { path },
    responseType: 'blob',
  });

/*REQUEST RESQ*/
export const reqResQsAPI = {
  getAllReqResQs: () => managerApi.get('/reqResQs'),
  findReqResQsByPartner: (partId) => managerApi.get(`/reqResQs/searchByPartner/${partId}`),
  findReqResQsByUser: (userId) => managerApi.get(`/reqResQs/searchByUser/${userId}`), //Rescue Calls - Partner
  searchWithPartner: (keyword, partId) => managerApi.get(`/reqResQs/searchWithPartner/${partId}/${keyword}`), //History - User
  findReqResQById: (rrId) => managerApi.get(`/reqResQs/${rrId}`), //Detail Request Rescue
  searchCustomer: (userId, keyword) => managerApi.get(`/reqResQs/searchWithUser/${userId}/${keyword}`),
  searchRequestResQ: (keyword) => managerApi.get(`/reqResQs/searchRequestResQ/${keyword}`),
  relatedRecordCheck: (rrId) => managerApi.get(`/reqResQs/existedRecords/${rrId}`),
  createNew: (data) => managerApi.post('/reqResQs/createRequest', data),
  updateRequest: (requestId, data) => managerApi.put(`/reqResQs/${requestId}`, data)
}

/*PERSONAL DATA*/
export const personalDataAPI = {
  getUnverifiedUserData: (customerId) => managerApi.get(`/personalDatas/getUnverifiedUserData/${customerId}`),
  approveCustomer: (customerId) => managerApi.put(`/personalDatas/approvedCustomer/${customerId}`),
  rejectCustomer: (customerId, rejectData) => managerApi.put(`/personalDatas/rejectedCustomer/${customerId}`, rejectData),
}

/*CUSTOMER*/
// export const getCustomerTransactions = (customerId) =>
//   managerApi.get(`/users/${customerId}/transactions`);
export const getCustomerReports = (customerId) =>
  managerApi.get(`/users/${customerId}/reports`);

/*DOCUMENTARY*/
export const getPartnerDocuments = (partnerId) => managerApi.get(`/documentary/by-partner/${partnerId}`);
export const getPartnerImgDocuments = (path) =>
  managerApi.get('/documentary/image', {
    params: { path },
    responseType: 'blob',
  });

/*PARTNER*/
export const partnerAPI = {
  getAllPartners: () => managerApi.get('/partners'),
  search: (keyword) => managerApi.get(`/partners/searchPartners/${keyword}`),
  findById: (partnerId) => managerApi.get(`/partners/findPartnerById/${partnerId}`),
  dashboard: (partnerId) => managerApi.get(`/partners/partnerDashboard/${partnerId}`),
  approveParnter: (partnerId) => managerApi.get(`/partners/approvePartner/${partnerId}`)
};

/*FEEDBACK*/
export const feedbackAPI = {
  getAllFeedbacks: () => managerApi.get('/feedbacks'),
  findFeedbacksByPartner: (partId) => managerApi.get(`/feedbacks/searchFeedbackByPartner/${partId}`),
  findFeedbacksByReqResQ: (rrId) => managerApi.get(`/feedbacks/searchFeedbackByRR/${rrId}`),
  averageRate: (partnerId) => managerApi.get(`/feedbacks/averageRate/${partnerId}`)
}

/*EXTRA SERVICE*/
export const extraSrvAPI = {
  findExtrasByResResQ: (rrId) => managerApi.get(`/extraSrv/searchByReqResQ/${rrId}`),
}

/*DOCUMENT*/
export const documentAPI = {
  getUnverifiedPartnerDoc: (partnerId) => managerApi.get(`/documents/getUnverifiedPartnerDoc/${partnerId}`),
  updatePartnerDoc: (partnerId, rejectData) => managerApi.put(`/documents/updatePartnerDoc/${partnerId}`, rejectData)
}

/*VEHICLE*/
export const getPartnerVehicle = (partnerId) => managerApi.get(`/vehicle/partnerVehicle/${partnerId}`)
export const getUserVehicle = (userId) => managerApi.get(`/vehicle/by-user/${userId}`);
export const getImgVehicle = (path) =>
  managerApi.get('/vehicle/image', {
    params: { path },
    responseType: 'blob',
  });
