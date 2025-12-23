import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { deliveryAPI } from '../../services/api';
import { DeliveryOrder, DeliveryEvent, DeliveryPoint, DeliveryRoute } from '../../types/delivery';

interface DeliveryState {
  deliveries: DeliveryOrder[];
  selectedDelivery: DeliveryOrder | null;
  deliveryEvents: DeliveryEvent[];
  deliveryRoute: DeliveryRoute | null;
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
    customerId?: string;
    driverId?: string;
    dateFrom?: string;
    dateTo?: string;
    priority?: string;
  };
}

const initialState: DeliveryState = {
  deliveries: [],
  selectedDelivery: null,
  deliveryEvents: [],
  deliveryRoute: null,
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
export const fetchDeliveries = createAsyncThunk(
  'delivery/fetchDeliveries',
  async (params?: {
    page?: number;
    limit?: number;
    status?: string;
    customerId?: string;
    driverId?: string;
    dateFrom?: string;
    dateTo?: string;
    priority?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.getDeliveries(params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch deliveries');
    }
  }
);

export const fetchDeliveryById = createAsyncThunk(
  'delivery/fetchDeliveryById',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.getDelivery(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch delivery');
    }
  }
);

export const createDelivery = createAsyncThunk(
  'delivery/createDelivery',
  async (delivery: {
    customerId: string;
    customerName: string;
    pickup: DeliveryPoint;
    dropoff: DeliveryPoint;
    priority: 'low' | 'normal' | 'high' | 'urgent';
    packageDescription: string;
    packageWeight: number;
    isMedicalSpecimen: boolean;
    temperatureControlled: boolean;
    requestedPickupTime: string;
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.createDelivery(delivery);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create delivery');
    }
  }
);

export const updateDelivery = createAsyncThunk(
  'delivery/updateDelivery',
  async ({ id, delivery }: {
    id: string;
    delivery: Partial<DeliveryOrder>;
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.updateDelivery(id, delivery);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update delivery');
    }
  }
);

export const deleteDelivery = createAsyncThunk(
  'delivery/deleteDelivery',
  async (id: string, { rejectWithValue }) => {
    try {
      await deliveryAPI.deleteDelivery(id);
      return id;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete delivery');
    }
  }
);

export const assignDriver = createAsyncThunk(
  'delivery/assignDriver',
  async ({
    id,
    driverId,
    assignedBy
  }: {
    id: string;
    driverId: string;
    assignedBy: string;
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.assignDriver(id, driverId, assignedBy);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to assign driver');
    }
  }
);

export const updateDeliveryStatus = createAsyncThunk(
  'delivery/updateDeliveryStatus',
  async ({
    id,
    status,
    reason,
    updatedBy
  }: {
    id: string;
    status: string;
    reason?: string;
    updatedBy?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.updateStatus(id, status, reason, updatedBy);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update delivery status');
    }
  }
);

export const fetchDeliveryEvents = createAsyncThunk(
  'delivery/fetchDeliveryEvents',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.getDeliveryEvents(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch delivery events');
    }
  }
);

export const fetchDeliveryRoute = createAsyncThunk(
  'delivery/fetchDeliveryRoute',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.getDeliveryRoute(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch delivery route');
    }
  }
);

export const createDeliveryEvent = createAsyncThunk(
  'delivery/createDeliveryEvent',
  async ({
    id,
    event
  }: {
    id: string;
    event: {
      eventType: string;
      notes?: string;
    };
  }, { rejectWithValue }) => {
    try {
      const response = await deliveryAPI.createDeliveryEvent(id, event);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create delivery event');
    }
  }
);

// Delivery slice
const deliverySlice = createSlice({
  name: 'delivery',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedDelivery: (state, action: PayloadAction<DeliveryOrder | null>) => {
      state.selectedDelivery = action.payload;
    },
    setFilters: (state, action: PayloadAction<any>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    updateDeliveryInList: (state, action: PayloadAction<DeliveryOrder>) => {
      const index = state.deliveries.findIndex(d => d.id === action.payload.id);
      if (index !== -1) {
        state.deliveries[index] = action.payload;
      }
    },
    addDeliveryEvent: (state, action: PayloadAction<DeliveryEvent>) => {
      state.deliveryEvents.unshift(action.payload);
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch deliveries
      .addCase(fetchDeliveries.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveries.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveries = action.payload.deliveries;
        state.pagination = action.payload.pagination;
        state.error = null;
      })
      .addCase(fetchDeliveries.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch delivery by ID
      .addCase(fetchDeliveryById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveryById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedDelivery = action.payload;
        state.error = null;
      })
      .addCase(fetchDeliveryById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Create delivery
      .addCase(createDelivery.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createDelivery.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveries.unshift(action.payload);
        state.selectedDelivery = action.payload;
        state.error = null;
      })
      .addCase(createDelivery.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update delivery
      .addCase(updateDelivery.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDelivery.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.deliveries.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.deliveries[index] = action.payload;
        }
        if (state.selectedDelivery && state.selectedDelivery.id === action.payload.id) {
          state.selectedDelivery = action.payload;
        }
        state.error = null;
      })
      .addCase(updateDelivery.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Delete delivery
      .addCase(deleteDelivery.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteDelivery.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveries = state.deliveries.filter(d => d.id !== action.payload);
        if (state.selectedDelivery && state.selectedDelivery.id === action.payload) {
          state.selectedDelivery = null;
        }
        state.error = null;
      })
      .addCase(deleteDelivery.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Assign driver
      .addCase(assignDriver.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(assignDriver.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.deliveries.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.deliveries[index] = action.payload;
        }
        if (state.selectedDelivery && state.selectedDelivery.id === action.payload.id) {
          state.selectedDelivery = action.payload;
        }
        state.error = null;
      })
      .addCase(assignDriver.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update delivery status
      .addCase(updateDeliveryStatus.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDeliveryStatus.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.deliveries.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.deliveries[index] = action.payload;
        }
        if (state.selectedDelivery && state.selectedDelivery.id === action.payload.id) {
          state.selectedDelivery = action.payload;
        }
        state.error = null;
      })
      .addCase(updateDeliveryStatus.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch delivery events
      .addCase(fetchDeliveryEvents.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveryEvents.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveryEvents = action.payload.events;
        state.error = null;
      })
      .addCase(fetchDeliveryEvents.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch delivery route
      .addCase(fetchDeliveryRoute.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveryRoute.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveryRoute = action.payload;
        state.error = null;
      })
      .addCase(fetchDeliveryRoute.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Create delivery event
      .addCase(createDeliveryEvent.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createDeliveryEvent.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveryEvents.unshift(action.payload);
        state.error = null;
      })
      .addCase(createDeliveryEvent.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const {
  clearError,
  setSelectedDelivery,
  setFilters,
  clearFilters,
  updateDeliveryInList,
  addDeliveryEvent,
} = deliverySlice.actions;

export default deliverySlice.reducer;
