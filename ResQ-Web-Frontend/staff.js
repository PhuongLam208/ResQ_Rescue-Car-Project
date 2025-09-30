import axios from 'axios';
import { Navigate } from 'react-router-dom';

const staffApi = axios.create({
    baseURL: 'http://localhost:9090/api/resq/staff',
    headers: {
        'Accept': 'application/json',
    },
});

staffApi.interceptors.request.use(
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
    staffApi.interceptors.response.use(
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

export const createRefund = (refundData) => staffApi.post('/refunds/save', refundData);

export const getAllSchedule = () => staffApi.get('/schedule');

export const createReport = (reportData) => staffApi.post('/report/create', reportData);

export const getStaffById = (id) => staffApi.get(`/users/${id}`);

export const getRequestRescue4Report = () => staffApi.get('/for-report');

export const getAllCustomers = () => staffApi.get('/customers');

export const getAllPartners = () => staffApi.get('/partners');

export const searchRRByUser = (userId) => staffApi.get(`/reqResQs/searchByUser/${userId}`);

export const searchRRByPartner = (partnerId) => staffApi.get(`/reqResQs/searchByPartner/${partnerId}`);

export const createRequest = (data) => staffApi.post('/reqResQs/createRequest', data);

export const createCustomer = (data) => staffApi.post('/customers/createCustomer', data, {
    headers: {
        'Content-Type': 'multipart/form-data',
    },
});

export const getCustomerPayments = (customerId) => staffApi.get(`/payments/getCustomerPayments/${customerId}`);

export const findServicesByType = (srvType) => staffApi.get(`/services/searchBySrvType/${srvType}`);

export const getCusRequestForCancel = (customerId) => staffApi.get(`/reqResQs/getCusRequestForCancel/${customerId}`);

export const cancelRequest = (requestId, reason) => staffApi.post(`reqResQs/cancelRequest/${requestId}`, { reason });
