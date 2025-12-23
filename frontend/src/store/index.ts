import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import deliveryReducer from './slices/deliverySlice';
import trackingReducer from './slices/trackingSlice';
import billingReducer from './slices/billingSlice';
import notificationReducer from './slices/notificationSlice';
import settingsReducer from './slices/settingsSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    delivery: deliveryReducer,
    tracking: trackingReducer,
    billing: billingReducer,
    notification: notificationReducer,
    settings: settingsReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
