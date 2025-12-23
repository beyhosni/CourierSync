import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';

interface CompanySettings {
  name: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
  taxId: string;
}

interface NotificationSettings {
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
  deliveryUpdates: boolean;
  newOrders: boolean;
  driverAlerts: boolean;
  systemAlerts: boolean;
}

interface PaymentSettings {
  paymentMethod: 'stripe' | 'paypal';
  stripeApiKey?: string;
  paypalClientId?: string;
  currency: string;
  taxRate: number;
  invoicePrefix: string;
  paymentTerms: number;
  autoGenerateInvoices: boolean;
}

interface DeliverySettings {
  defaultRadius: number;
  maxDeliveryDistance: number;
  deliveryFeeCalculation: 'distance' | 'flat' | 'weight';
  baseDeliveryFee: number;
  feePerKm: number;
  urgentDeliveryFee: number;
  autoAssignDrivers: boolean;
  requireProofOfDelivery: boolean;
}

interface Settings {
  company?: CompanySettings;
  notifications?: NotificationSettings;
  payment?: PaymentSettings;
  delivery?: DeliverySettings;
}

const settingsService = {
  getSettings: () => {
    return axios.get<Settings>(`${API_URL}/settings`);
  },

  updateSettings: (settings: Settings) => {
    return axios.put<Settings>(`${API_URL}/settings`, settings);
  },

  updateCompanySettings: (settings: CompanySettings) => {
    return axios.put<CompanySettings>(`${API_URL}/settings/company`, settings);
  },

  updateNotificationSettings: (settings: NotificationSettings) => {
    return axios.put<NotificationSettings>(`${API_URL}/settings/notifications`, settings);
  },

  updatePaymentSettings: (settings: PaymentSettings) => {
    return axios.put<PaymentSettings>(`${API_URL}/settings/payment`, settings);
  },

  updateDeliverySettings: (settings: DeliverySettings) => {
    return axios.put<DeliverySettings>(`${API_URL}/settings/delivery`, settings);
  },
};

export default settingsService;
export { settingsService };
