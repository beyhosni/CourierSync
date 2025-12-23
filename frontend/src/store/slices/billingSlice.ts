import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { billingAPI } from '../../services/api';
import { Invoice, PricingRule, PriceEstimate } from '../../types/billing';

interface BillingState {
  invoices: Invoice[];
  selectedInvoice: Invoice | null;
  pricingRules: PricingRule[];
  selectedPricingRule: PricingRule | null;
  priceEstimate: PriceEstimate | null;
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
    dateFrom?: string;
    dateTo?: string;
  };
}

const initialState: BillingState = {
  invoices: [],
  selectedInvoice: null,
  pricingRules: [],
  selectedPricingRule: null,
  priceEstimate: null,
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
export const fetchInvoices = createAsyncThunk(
  'billing/fetchInvoices',
  async (params?: {
    page?: number;
    limit?: number;
    status?: string;
    customerId?: string;
    dateFrom?: string;
    dateTo?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.getInvoices(params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch invoices');
    }
  }
);

export const fetchInvoiceById = createAsyncThunk(
  'billing/fetchInvoiceById',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await billingAPI.getInvoice(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch invoice');
    }
  }
);

export const createInvoice = createAsyncThunk(
  'billing/createInvoice',
  async (invoice: {
    customerId: string;
    customerName: string;
    customerAddress: string;
    customerCity: string;
    customerPostalCode: string;
    customerCountry: string;
    issueDate: string;
    dueDate: string;
    items: Array<{
      itemType: string;
      description: string;
      quantity: number;
      unitPrice: number;
      discountPercent: number;
      deliveryId?: string;
    }>;
    notes?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.createInvoice(invoice);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create invoice');
    }
  }
);

export const updateInvoice = createAsyncThunk(
  'billing/updateInvoice',
  async ({ id, invoice }: {
    id: string;
    invoice: Partial<Invoice>;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.updateInvoice(id, invoice);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update invoice');
    }
  }
);

export const deleteInvoice = createAsyncThunk(
  'billing/deleteInvoice',
  async (id: string, { rejectWithValue }) => {
    try {
      await billingAPI.deleteInvoice(id);
      return id;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete invoice');
    }
  }
);

export const markInvoiceAsPaid = createAsyncThunk(
  'billing/markInvoiceAsPaid',
  async ({
    id,
    paymentMethod,
    paymentReference
  }: {
    id: string;
    paymentMethod: string;
    paymentReference?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.markInvoiceAsPaid(id, paymentMethod, paymentReference);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to mark invoice as paid');
    }
  }
);

export const generateInvoicePDF = createAsyncThunk(
  'billing/generateInvoicePDF',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await billingAPI.generateInvoicePDF(id);
      // Create a blob URL for the PDF
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);

      // Create a temporary link to download the PDF
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice-${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // Clean up the blob URL
      window.URL.revokeObjectURL(url);

      return url;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to generate invoice PDF');
    }
  }
);

export const fetchPricingRules = createAsyncThunk(
  'billing/fetchPricingRules',
  async (params?: {
    page?: number;
    limit?: number;
    customerId?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.getPricingRules(params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch pricing rules');
    }
  }
);

export const fetchPricingRuleById = createAsyncThunk(
  'billing/fetchPricingRuleById',
  async (id: string, { rejectWithValue }) => {
    try {
      const response = await billingAPI.getPricingRule(id);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch pricing rule');
    }
  }
);

export const createPricingRule = createAsyncThunk(
  'billing/createPricingRule',
  async (rule: {
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
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.createPricingRule(rule);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create pricing rule');
    }
  }
);

export const updatePricingRule = createAsyncThunk(
  'billing/updatePricingRule',
  async ({ id, rule }: {
    id: string;
    rule: Partial<PricingRule>;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.updatePricingRule(id, rule);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update pricing rule');
    }
  }
);

export const deletePricingRule = createAsyncThunk(
  'billing/deletePricingRule',
  async (id: string, { rejectWithValue }) => {
    try {
      await billingAPI.deletePricingRule(id);
      return id;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete pricing rule');
    }
  }
);

export const calculatePrice = createAsyncThunk(
  'billing/calculatePrice',
  async (params: {
    pickupLat: number;
    pickupLng: number;
    dropoffLat: number;
    dropoffLng: number;
    packageWeight: number;
    isMedicalSpecimen: boolean;
    temperatureControlled: boolean;
    priority: string;
    pricingRuleId?: string;
  }, { rejectWithValue }) => {
    try {
      const response = await billingAPI.calculatePrice(params);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to calculate price');
    }
  }
);

// Billing slice
const billingSlice = createSlice({
  name: 'billing',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedInvoice: (state, action: PayloadAction<Invoice | null>) => {
      state.selectedInvoice = action.payload;
    },
    setSelectedPricingRule: (state, action: PayloadAction<PricingRule | null>) => {
      state.selectedPricingRule = action.payload;
    },
    setFilters: (state, action: PayloadAction<any>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    updateInvoiceInList: (state, action: PayloadAction<Invoice>) => {
      const index = state.invoices.findIndex(i => i.id === action.payload.id);
      if (index !== -1) {
        state.invoices[index] = action.payload;
      }
    },
    updatePricingRuleInList: (state, action: PayloadAction<PricingRule>) => {
      const index = state.pricingRules.findIndex(r => r.id === action.payload.id);
      if (index !== -1) {
        state.pricingRules[index] = action.payload;
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch invoices
      .addCase(fetchInvoices.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchInvoices.fulfilled, (state, action) => {
        state.isLoading = false;
        state.invoices = action.payload.invoices;
        state.pagination = action.payload.pagination;
        state.error = null;
      })
      .addCase(fetchInvoices.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch invoice by ID
      .addCase(fetchInvoiceById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchInvoiceById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedInvoice = action.payload;
        state.error = null;
      })
      .addCase(fetchInvoiceById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Create invoice
      .addCase(createInvoice.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createInvoice.fulfilled, (state, action) => {
        state.isLoading = false;
        state.invoices.unshift(action.payload);
        state.selectedInvoice = action.payload;
        state.error = null;
      })
      .addCase(createInvoice.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update invoice
      .addCase(updateInvoice.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateInvoice.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.invoices.findIndex(i => i.id === action.payload.id);
        if (index !== -1) {
          state.invoices[index] = action.payload;
        }
        if (state.selectedInvoice && state.selectedInvoice.id === action.payload.id) {
          state.selectedInvoice = action.payload;
        }
        state.error = null;
      })
      .addCase(updateInvoice.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Delete invoice
      .addCase(deleteInvoice.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteInvoice.fulfilled, (state, action) => {
        state.isLoading = false;
        state.invoices = state.invoices.filter(i => i.id !== action.payload);
        if (state.selectedInvoice && state.selectedInvoice.id === action.payload) {
          state.selectedInvoice = null;
        }
        state.error = null;
      })
      .addCase(deleteInvoice.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Mark invoice as paid
      .addCase(markInvoiceAsPaid.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(markInvoiceAsPaid.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.invoices.findIndex(i => i.id === action.payload.id);
        if (index !== -1) {
          state.invoices[index] = action.payload;
        }
        if (state.selectedInvoice && state.selectedInvoice.id === action.payload.id) {
          state.selectedInvoice = action.payload;
        }
        state.error = null;
      })
      .addCase(markInvoiceAsPaid.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Generate invoice PDF
      .addCase(generateInvoicePDF.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(generateInvoicePDF.fulfilled, (state) => {
        state.isLoading = false;
        state.error = null;
      })
      .addCase(generateInvoicePDF.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch pricing rules
      .addCase(fetchPricingRules.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchPricingRules.fulfilled, (state, action) => {
        state.isLoading = false;
        state.pricingRules = action.payload.pricingRules;
        state.pagination = action.payload.pagination;
        state.error = null;
      })
      .addCase(fetchPricingRules.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch pricing rule by ID
      .addCase(fetchPricingRuleById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchPricingRuleById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedPricingRule = action.payload;
        state.error = null;
      })
      .addCase(fetchPricingRuleById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Create pricing rule
      .addCase(createPricingRule.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createPricingRule.fulfilled, (state, action) => {
        state.isLoading = false;
        state.pricingRules.unshift(action.payload);
        state.selectedPricingRule = action.payload;
        state.error = null;
      })
      .addCase(createPricingRule.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update pricing rule
      .addCase(updatePricingRule.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updatePricingRule.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.pricingRules.findIndex(r => r.id === action.payload.id);
        if (index !== -1) {
          state.pricingRules[index] = action.payload;
        }
        if (state.selectedPricingRule && state.selectedPricingRule.id === action.payload.id) {
          state.selectedPricingRule = action.payload;
        }
        state.error = null;
      })
      .addCase(updatePricingRule.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Delete pricing rule
      .addCase(deletePricingRule.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deletePricingRule.fulfilled, (state, action) => {
        state.isLoading = false;
        state.pricingRules = state.pricingRules.filter(r => r.id !== action.payload);
        if (state.selectedPricingRule && state.selectedPricingRule.id === action.payload) {
          state.selectedPricingRule = null;
        }
        state.error = null;
      })
      .addCase(deletePricingRule.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Calculate price
      .addCase(calculatePrice.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(calculatePrice.fulfilled, (state, action) => {
        state.isLoading = false;
        state.priceEstimate = action.payload;
        state.error = null;
      })
      .addCase(calculatePrice.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const {
  clearError,
  setSelectedInvoice,
  setSelectedPricingRule,
  setFilters,
  clearFilters,
  updateInvoiceInList,
  updatePricingRuleInList,
} = billingSlice.actions;

export default billingSlice.reducer;
