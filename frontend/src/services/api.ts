import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { store } from '../store';
import { logoutUser } from '../store/slices/authSlice';

// Create an Axios instance with default configuration
const api: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add the auth token to requests
api.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = store.getState().auth.token;
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle common errors
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    // Handle 401 Unauthorized errors by logging out the user
    if (error.response && error.response.status === 401) {
      store.dispatch(logoutUser());
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Define API endpoints
export const authAPI = {
  login: (credentials: { email: string; password: string }) =>
    api.post('/auth/login', credentials),
  register: (userData: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone: string;
    role: string;
  }) => api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout'),
  refreshToken: () => api.post('/auth/refresh'),
  loadUser: () => api.get('/auth/me'),
  requestPasswordReset: (email: string) =>
    api.post('/auth/password/reset-request', { email }),
  resetPassword: (token: string, password: string) =>
    api.post('/auth/password/reset', { token, password }),
  changePassword: (data: {
    currentPassword: string;
    newPassword: string;
  }) => api.post('/auth/password/change', data),
  updateProfile: (formData: FormData) =>
    api.put('/auth/profile', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
};

export const deliveryAPI = {
  getDeliveries: (params?: {
    page?: number;
    limit?: number;
    status?: string;
    customerId?: string;
    driverId?: string;
    dateFrom?: string;
    dateTo?: string;
    priority?: string;
  }) => api.get('/deliveries', { params }),
  getDelivery: (id: string) => api.get(`/deliveries/${id}`),
  createDelivery: (delivery: any) => api.post('/deliveries', delivery),
  updateDelivery: (id: string, delivery: any) => api.put(`/deliveries/${id}`, delivery),
  deleteDelivery: (id: string) => api.delete(`/deliveries/${id}`),
  assignDriver: (id: string, driverId: string, assignedBy: string) =>
    api.put(`/deliveries/${id}/assign`, { driverId, assignedBy }),
  updateStatus: (id: string, status: string, reason?: string, updatedBy?: string) =>
    api.put(`/deliveries/${id}/status`, { status, reason, updatedBy }),
  getDeliveryEvents: (id: string) => api.get(`/deliveries/${id}/events`),
  getDeliveryRoute: (id: string) => api.get(`/deliveries/${id}/route`),
  createDeliveryEvent: (id: string, event: any) =>
    api.post(`/deliveries/${id}/events`, event),
};

export const driverAPI = {
  getDrivers: (params?: {
    page?: number;
    limit?: number;
    status?: string;
  }) => api.get('/drivers', { params }),
  getDriver: (id: string) => api.get(`/drivers/${id}`),
  updateDriver: (id: string, driver: any) => api.put(`/drivers/${id}`, driver),
  updateDriverStatus: (id: string, status: string, updatedBy: string) =>
    api.put(`/drivers/${id}/status`, { status, updatedBy }),
  getDriverDeliveries: (id: string, params?: {
    page?: number;
    limit?: number;
    status?: string;
  }) => api.get(`/drivers/${id}/deliveries`, { params }),
  getDriverLocationHistory: (id: string, params?: {
    page?: number;
    limit?: number;
    dateFrom?: string;
    dateTo?: string;
  }) => api.get(`/drivers/${id}/location-history`, { params }),
};

export const trackingAPI = {
  getLocationUpdates: (params?: {
    page?: number;
    limit?: number;
    driverId?: string;
    deliveryId?: string;
    dateFrom?: string;
    dateTo?: string;
  }) => api.get('/tracking/locations', { params }),
  createLocationUpdate: (locationUpdate: any) =>
    api.post('/tracking/locations', locationUpdate),
  getLatestDriverLocation: (driverId: string) =>
    api.get(`/tracking/locations/driver/${driverId}/latest`),
  getLatestDeliveryLocation: (deliveryId: string) =>
    api.get(`/tracking/locations/delivery/${deliveryId}/latest`),
  getDriverLocationHistory: (driverId: string, params?: {
    page?: number;
    limit?: number;
    dateFrom?: string;
    dateTo?: string;
  }) => api.get(`/tracking/locations/driver/${driverId}/history`, { params }),
  getDeliveryLocationHistory: (deliveryId: string, params?: {
    page?: number;
    limit?: number;
    dateFrom?: string;
    dateTo?: string;
  }) => api.get(`/tracking/locations/delivery/${deliveryId}/history`, { params }),
};

export const billingAPI = {
  getInvoices: (params?: {
    page?: number;
    limit?: number;
    status?: string;
    customerId?: string;
    dateFrom?: string;
    dateTo?: string;
  }) => api.get('/billing/invoices', { params }),
  getInvoice: (id: string) => api.get(`/billing/invoices/${id}`),
  createInvoice: (invoice: any) => api.post('/billing/invoices', invoice),
  updateInvoice: (id: string, invoice: any) =>
    api.put(`/billing/invoices/${id}`, invoice),
  deleteInvoice: (id: string) => api.delete(`/billing/invoices/${id}`),
  markInvoiceAsPaid: (id: string, paymentMethod: string, paymentReference?: string) =>
    api.post(`/billing/invoices/${id}/mark-paid`, { paymentMethod, paymentReference }),
  generateInvoicePDF: (id: string) => api.get(`/billing/invoices/${id}/pdf`, {
    responseType: 'blob',
  }),
  getPricingRules: (params?: {
    page?: number;
    limit?: number;
    customerId?: string;
  }) => api.get('/billing/pricing-rules', { params }),
  getPricingRule: (id: string) => api.get(`/billing/pricing-rules/${id}`),
  createPricingRule: (rule: any) => api.post('/billing/pricing-rules', rule),
  updatePricingRule: (id: string, rule: any) =>
    api.put(`/billing/pricing-rules/${id}`, rule),
  deletePricingRule: (id: string) => api.delete(`/billing/pricing-rules/${id}`),
  calculatePrice: (params: {
    pickupLat: number;
    pickupLng: number;
    dropoffLat: number;
    dropoffLng: number;
    packageWeight: number;
    isMedicalSpecimen: boolean;
    temperatureControlled: boolean;
    priority: string;
    pricingRuleId?: string;
  }) => api.post('/billing/calculate-price', params),
};

export const notificationAPI = {
  getNotifications: (params?: {
    page?: number;
    limit?: number;
    unreadOnly?: boolean;
  }) => api.get('/notifications', { params }),
  markAsRead: (id: string) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
  getNotificationSettings: () => api.get('/notifications/settings'),
  updateNotificationSettings: (settings: any) =>
    api.put('/notifications/settings', settings),
};

export default api;
