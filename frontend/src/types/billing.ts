export interface Invoice {
  id: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  customerAddress: string;
  customerCity: string;
  customerPostalCode: string;
  customerCountry: string;
  issueDate: Date;
  dueDate: Date;
  status: 'draft' | 'sent' | 'paid' | 'overdue' | 'cancelled';
  subtotal: number;
  taxRate: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
  paymentMethod?: string;
  paymentDate?: Date;
  paymentReference?: string;
  notes?: string;
  createdAt: Date;
  updatedAt: Date;
  items: InvoiceItem[];
}

export interface InvoiceItem {
  id: string;
  itemType: 'delivery' | 'surcharge' | 'discount' | 'other';
  description: string;
  quantity: number;
  unitPrice: number;
  discountPercent: number;
  lineTotal: number;
  deliveryId?: string;
  createdAt: Date;
}

export interface PricingRule {
  id: string;
  name: string;
  description: string;
  basePrice: number;
  pricePerKm: number;
  pricePerMinute: number;
  weightFee: number;
  medicalSpecimenFee: number;
  temperatureControlledFee: number;
  priorityFee: {
    low: number;
    normal: number;
    high: number;
    urgent: number;
  };
  timeOfDayMultiplier: {
    peak: number;
    offPeak: number;
    night: number;
  };
  customerId?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface PriceEstimate {
  deliveryId?: string;
  pickup: {
    lat: number;
    lng: number;
  };
  dropoff: {
    lat: number;
    lng: number;
  };
  distance: number;
  estimatedTime: number;
  packageWeight: number;
  isMedicalSpecimen: boolean;
  temperatureControlled: boolean;
  priority: 'low' | 'normal' | 'high' | 'urgent';
  pricingRuleId: string;
  basePrice: number;
  distanceFee: number;
  timeFee: number;
  weightFee: number;
  medicalSpecimenFee: number;
  temperatureControlledFee: number;
  priorityFee: number;
  timeOfDayMultiplier: number;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
}

export interface PaymentMethod {
  id: string;
  customerId: string;
  type: 'credit_card' | 'bank_account' | 'paypal';
  lastFour?: string;
  expiryDate?: string;
  brand?: string;
  isDefault: boolean;
  createdAt: Date;
}
