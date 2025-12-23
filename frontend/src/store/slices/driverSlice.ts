import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { driverAPI } from '../../services/api';
import { Driver, LocationUpdate } from '../../types/delivery';

interface DriverState {
  drivers: Driver[];
  selectedDriver: Driver | null;
  driverLocationHistory: LocationUpdate[];
  driverDeliveries: any[];
  isLoading: boolean;
  error: string | null;
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
  filters: {
    status?: string;
  };
}

const initialState: DriverState = {
  drivers: [],
  selectedDriver: null,
  driverLocationHistory: [],
  driverDeliveries: [],
  isLoading: false,
  error: null,
  pagination: {
    page: 1,
    limit: 10,
    total: 0,
    totalPages: 0,
  },
  filters: {},
};

// Async thunks
export const fetchDrivers = createAsyncThunk(
  'driver/fetchDrivers',
  async (params?: {
    page?: number;
    limit?: number;
    status?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await driverAPI.getDrivers(params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch drivers');
    }
  }
);

export const fetchDriverById = createAsyncThunk(
  'driver/fetchDriverById',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await driverAPI.getDriver(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch driver');
    }
  }
);

export const updateDriver = createAsyncThunk(
  'driver/updateDriver',
  async ({ id, driver }: {
    id: string;
    driver: Partial<Driver>;
  }, { rejectWithValue }) => {
    try {
      const response = await driverAPI.updateDriver(id, driver);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update driver');
    }
  }
);

export const updateDriverStatus = createAsyncThunk(
  'driver/updateDriverStatus',
  async ({
    id,
    status,
    updatedBy
  }: {
    id: string;
    status: string;
    updatedBy: string;
  }, { rejectWithValue }) => {
    try {
      const response = await driverAPI.updateDriverStatus(id, status, updatedBy);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update driver status');
    }
  }
);

export const fetchDriverDeliveries = createAsyncThunk(
  'driver/fetchDriverDeliveries',
  async ({
    id,
    params
  }: {
    id: string;
    params?: {
      page?: number;
      limit?: number;
      status?: string;
    };
  }, { rejectWithValue }) => {
    try {
      const response = await driverAPI.getDriverDeliveries(id, params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch driver deliveries');
    }
  }
);

export const fetchDriverLocationHistory = createAsyncThunk(
  'driver/fetchDriverLocationHistory',
  async ({
    id,
    params
  }: {
    id: string;
    params?: {
      page?: number;
      limit?: number;
      dateFrom?: string;
      dateTo?: string;
    };
  }, { rejectWithValue }) => {
    try {
      const response = await driverAPI.getDriverLocationHistory(id, params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch driver location history');
    }
  }
);

// Driver slice
const driverSlice = createSlice({
  name: 'driver',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedDriver: (state, action: PayloadAction<Driver | null>) => {
      state.selectedDriver = action.payload;
    },
    setFilters: (state, action: PayloadAction<any>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    updateDriverInList: (state, action: PayloadAction<Driver>) => {
      const index = state.drivers.findIndex(d => d.id === action.payload.id);
      if (index !== -1) {
        state.drivers[index] = action.payload;
      }
    },
    updateDriverLocation: (state, action: PayloadAction<{ driverId: string; location: LocationUpdate }>) => {
      const { driverId, location } = action.payload;

      // Update in drivers list
      const driverIndex = state.drivers.findIndex(d => d.id === driverId);
      if (driverIndex !== -1) {
        state.drivers[driverIndex].currentLocation = {
          latitude: location.latitude,
          longitude: location.longitude,
          timestamp: location.timestamp,
        };
      }

      // Update selected driver if it matches
      if (state.selectedDriver && state.selectedDriver.id === driverId) {
        state.selectedDriver.currentLocation = {
          latitude: location.latitude,
          longitude: location.longitude,
          timestamp: location.timestamp,
        };
      }

      // Add to location history
      state.driverLocationHistory.unshift(location);
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch drivers
      .addCase(fetchDrivers.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDrivers.fulfilled, (state, action) => {
        state.isLoading = false;
        state.drivers = action.payload.drivers;
        state.pagination = action.payload.pagination;
        state.error = null;
      })
      .addCase(fetchDrivers.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch driver by ID
      .addCase(fetchDriverById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDriverById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedDriver = action.payload;
        state.error = null;
      })
      .addCase(fetchDriverById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update driver
      .addCase(updateDriver.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDriver.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.drivers.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.drivers[index] = action.payload;
        }
        if (state.selectedDriver && state.selectedDriver.id === action.payload.id) {
          state.selectedDriver = action.payload;
        }
        state.error = null;
      })
      .addCase(updateDriver.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update driver status
      .addCase(updateDriverStatus.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDriverStatus.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.drivers.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.drivers[index] = action.payload;
        }
        if (state.selectedDriver && state.selectedDriver.id === action.payload.id) {
          state.selectedDriver = action.payload;
        }
        state.error = null;
      })
      .addCase(updateDriverStatus.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch driver deliveries
      .addCase(fetchDriverDeliveries.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDriverDeliveries.fulfilled, (state, action) => {
        state.isLoading = false;
        state.driverDeliveries = action.payload.deliveries;
        state.error = null;
      })
      .addCase(fetchDriverDeliveries.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch driver location history
      .addCase(fetchDriverLocationHistory.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDriverLocationHistory.fulfilled, (state, action) => {
        state.isLoading = false;
        state.driverLocationHistory = action.payload.locationUpdates;
        state.error = null;
      })
      .addCase(fetchDriverLocationHistory.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const {
  clearError,
  setSelectedDriver,
  setFilters,
  clearFilters,
  updateDriverInList,
  updateDriverLocation,
} = driverSlice.actions;

export default driverSlice.reducer;
