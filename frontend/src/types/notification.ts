export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  isRead: boolean;
  createdAt: Date;
  readAt?: Date;
  data?: any;
  actionUrl?: string;
}

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
}

export interface NotificationSettings {
  id: string;
  userId: string;
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
  deliveryUpdates: boolean;
  newOrders: boolean;
  driverAlerts: boolean;
  systemAlerts: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateNotificationData {
  userId: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  data?: any;
  actionUrl?: string;
}

export interface UpdateNotificationSettingsData {
  emailNotifications?: boolean;
  smsNotifications?: boolean;
  pushNotifications?: boolean;
  deliveryUpdates?: boolean;
  newOrders?: boolean;
  driverAlerts?: boolean;
  systemAlerts?: boolean;
}
