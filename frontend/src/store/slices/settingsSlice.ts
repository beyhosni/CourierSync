import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { settingsService } from '../../services/settingsService';

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

interface SettingsState {
  settings: Settings | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: SettingsState = {
  settings: null,
  isLoading: false,
  error: null,
};

export const fetchSettings = createAsyncThunk(
  'settings/fetchSettings',
  async (_, { rejectWithValue }) => {
    try {
      const response = await settingsService.getSettings();
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch settings');
    }
  }
);

export const updateSettings = createAsyncThunk(
  'settings/updateSettings',
  async (settings: Settings, { rejectWithValue }) => {
    try {
      const response = await settingsService.updateSettings(settings);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update settings');
    }
  }
);

const settingsSlice = createSlice({
  name: 'settings',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch settings
      .addCase(fetchSettings.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchSettings.fulfilled, (state, action: PayloadAction<Settings>) => {
        state.isLoading = false;
        state.settings = action.payload;
      })
      .addCase(fetchSettings.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update settings
      .addCase(updateSettings.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateSettings.fulfilled, (state, action: PayloadAction<Settings>) => {
        state.isLoading = false;
        state.settings = action.payload;
      })
      .addCase(updateSettings.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError } = settingsSlice.actions;
export default settingsSlice.reducer;
