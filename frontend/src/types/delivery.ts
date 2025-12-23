export interface DeliveryPoint {
  id: string;
  lat: number;
  lng: number;
  type: 'pickup' | 'dropoff';
  name: string;
  address: string;
  city: string;
  postalCode: string;
  contactName: string;
  contactPhone: string;
  completed?: boolean;
  timestamp?: Date;
}

export interface DeliveryRoute {
  id: string;
  points: Array<{ lat: number; lng: number }>;
  distance: number;
  estimatedTime: number;
  color?: string;
}

export interface LocationUpdate {
  id: string;
  driverId: string;
  deliveryId?: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  speed: number;
  heading: number;
  timestamp: Date;
  batteryLevel: number;
  deviceId: string;
}

export interface Driver {
  id: string;
  userId: string;
  name: string;
  phone: string;
  email: string;
  vehicleType: string;
  vehiclePlate: string;
  status: 'available' | 'busy' | 'offline';
  currentLocation?: {
    latitude: number;
    longitude: number;
    timestamp: Date;
  };
}

export interface DeliveryOrder {
  id: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  pickup: DeliveryPoint;
  dropoff: DeliveryPoint;
  priority: 'low' | 'normal' | 'high' | 'urgent';
  packageDescription: string;
  packageWeight: number;
  isMedicalSpecimen: boolean;
  temperatureControlled: boolean;
  requestedPickupTime: Date;
  assignedDriverId?: string;
  status: 'created' | 'assigned' | 'picked_up' | 'in_transit' | 'delivered' | 'cancelled';
  createdAt: Date;
  updatedAt: Date;
  completedAt?: Date;
  route?: DeliveryRoute;
  estimatedDeliveryTime?: Date;
}

export interface DeliveryEvent {
  id: string;
  deliveryId: string;
  eventType: 'created' | 'assigned' | 'picked_up' | 'in_transit' | 'delivered' | 'cancelled';
  timestamp: Date;
  userId: string;
  notes?: string;
  locationUpdate?: LocationUpdate;
}

export interface NotificationSettings {
  email: boolean;
  sms: boolean;
  push: boolean;
  events: {
    created: boolean;
    assigned: boolean;
    picked_up: boolean;
    in_transit: boolean;
    delivered: boolean;
    cancelled: boolean;
  };
}
