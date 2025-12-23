import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { trackingService } from '../../services/trackingService';

interface TrackingPoint {
  latitude: number;
  longitude: number;
  timestamp: string;
  speed?: number;
  heading?: number;
}

interface TrackingData {
  deliveryId: string;
  driverId: string;
  points: TrackingPoint[];
  isActive: boolean;
}

interface TrackingState {
  activeTrackings: Record<string, TrackingData>;
  isLoading: boolean;
  error: string | null;
}

const initialState: TrackingState = {
  activeTrackings: {},
  isLoading: false,
  error: null,
};

export const fetchTrackingData = createAsyncThunk(
  'tracking/fetchTrackingData',
  async (deliveryId: string, { rejectWithValue }) => {
    try {
      const response = await trackingService.getTrackingData(deliveryId);
      return { deliveryId, data: response.data };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch tracking data');
    }
  }
);

export const addTrackingPoint = createAsyncThunk(
  'tracking/addTrackingPoint',
  async ({ deliveryId, point }: { deliveryId: string; point: TrackingPoint }, { rejectWithValue }) => {
    try {
      const response = await trackingService.addTrackingPoint(deliveryId, point);
      return { deliveryId, point: response.data };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to add tracking point');
    }
  }
);

export const startTracking = createAsyncThunk(
  'tracking/startTracking',
  async (deliveryId: string, { rejectWithValue }) => {
    try {
      await trackingService.startTracking(deliveryId);
      return deliveryId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to start tracking');
    }
  }
);

export const stopTracking = createAsyncThunk(
  'tracking/stopTracking',
  async (deliveryId: string, { rejectWithValue }) => {
    try {
      await trackingService.stopTracking(deliveryId);
      return deliveryId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to stop tracking');
    }
  }
);

const trackingSlice = createSlice({
  name: 'tracking',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    updateTrackingPoint: (state, action: PayloadAction<{ deliveryId: string; point: TrackingPoint }>) => {
      const { deliveryId, point } = action.payload;
      if (state.activeTrackings[deliveryId]) {
        state.activeTrackings[deliveryId].points.push(point);
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch tracking data
      .addCase(fetchTrackingData.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchTrackingData.fulfilled, (state, action) => {
        state.isLoading = false;
        const { deliveryId, data } = action.payload;
        state.activeTrackings[deliveryId] = data;
      })
      .addCase(fetchTrackingData.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Add tracking point
      .addCase(addTrackingPoint.fulfilled, (state, action) => {
        const { deliveryId, point } = action.payload;
        if (state.activeTrackings[deliveryId]) {
          state.activeTrackings[deliveryId].points.push(point);
        }
      })
      .addCase(addTrackingPoint.rejected, (state, action) => {
        state.error = action.payload as string;
      })
      // Start tracking
      .addCase(startTracking.fulfilled, (state, action) => {
        const deliveryId = action.payload;
        if (state.activeTrackings[deliveryId]) {
          state.activeTrackings[deliveryId].isActive = true;
        }
      })
      .addCase(startTracking.rejected, (state, action) => {
        state.error = action.payload as string;
      })
      // Stop tracking
      .addCase(stopTracking.fulfilled, (state, action) => {
        const deliveryId = action.payload;
        if (state.activeTrackings[deliveryId]) {
          state.activeTrackings[deliveryId].isActive = false;
        }
      })
      .addCase(stopTracking.rejected, (state, action) => {
        state.error = action.payload as string;
      });
  },
});

export const { clearError, updateTrackingPoint } = trackingSlice.actions;
export default trackingSlice.reducer;
