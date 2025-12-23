import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';

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

const trackingService = {
  getTrackingData: (deliveryId: string) => {
    return axios.get<TrackingData>(`${API_URL}/tracking/${deliveryId}`);
  },

  addTrackingPoint: (deliveryId: string, point: TrackingPoint) => {
    return axios.post<TrackingPoint>(`${API_URL}/tracking/${deliveryId}/points`, point);
  },

  startTracking: (deliveryId: string) => {
    return axios.post(`${API_URL}/tracking/${deliveryId}/start`);
  },

  stopTracking: (deliveryId: string) => {
    return axios.post(`${API_URL}/tracking/${deliveryId}/stop`);
  },

  getActiveTrackingForDriver: (driverId: string) => {
    return axios.get<TrackingData[]>(`${API_URL}/tracking/driver/${driverId}/active`);
  },
};

export default trackingService;
export { trackingService };
