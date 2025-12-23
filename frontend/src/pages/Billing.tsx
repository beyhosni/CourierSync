import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Button,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Card,
  CardContent,
  CardActions,
  CircularProgress,
  Alert,
  Pagination,
  Fab,
  Chip,
  Divider,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
  PictureAsPdf as PdfIcon,
  Download as DownloadIcon,
  Send as SendIcon,
  Assessment as ChartIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../hooks/redux';
import { fetchInvoices } from '../store/slices/billingSlice';
import InvoiceDetails from '../components/Billing/InvoiceDetails';
import InvoiceForm from '../components/Billing/InvoiceForm';
import PricingRules from '../components/Billing/PricingRules';
import { Invoice } from '../types/billing';
import TabPanel from '../components/Common/TabPanel';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const Billing: React.FC = () => {
  const dispatch = useAppDispatch();
  const { invoices, isLoading, error, pagination } = useAppSelector(state => state.billing);
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [showInvoiceForm, setShowInvoiceForm] = useState(false);
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    dispatch(fetchInvoices({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  }, [dispatch, page, searchTerm, statusFilter]);

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleRefresh = () => {
    dispatch(fetchInvoices({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  };

  const handleInvoiceClick = (invoice: Invoice) => {
    setSelectedInvoice(invoice);
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'draft': return 'default';
      case 'sent': return 'info';
      case 'paid': return 'success';
      case 'overdue': return 'warning';
      case 'cancelled': return 'error';
      default: return 'default';
    }
  };

  const formatCurrency = (amount: number, currency: string = 'EUR') => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Facturation
        </Typography>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={handleRefresh}
          disabled={isLoading}
        >
          Actualiser
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Factures" />
          <Tab label="Règles de tarification" />
          <Tab label="Rapports" />
        </Tabs>
      </Box>

      {/* Tab Panels */}
      <TabPanel value={tabValue} index={0}>
        {/* Filters */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                placeholder="Rechercher une facture..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel id="status-filter-label">Statut</InputLabel>
                <Select
                  labelId="status-filter-label"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  label="Statut"
                  startAdornment={
                    <InputAdornment position="start">
                      <FilterIcon />
                    </InputAdornment>
                  }
                >
                  <MenuItem value="">Tous</MenuItem>
                  <MenuItem value="draft">Brouillon</MenuItem>
                  <MenuItem value="sent">Envoyée</MenuItem>
                  <MenuItem value="paid">Payée</MenuItem>
                  <MenuItem value="overdue">En retard</MenuItem>
                  <MenuItem value="cancelled">Annulée</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </Paper>

        {/* Invoices Table */}
        <Paper sx={{ p: 2 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Numéro</TableCell>
                  <TableCell>Client</TableCell>
                  <TableCell>Date d'émission</TableCell>
                  <TableCell>Date d'échéance</TableCell>
                  <TableCell>Montant total</TableCell>
                  <TableCell>Statut</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      <CircularProgress />
                    </TableCell>
                  </TableRow>
                ) : invoices.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      Aucune facture trouvée
                    </TableCell>
                  </TableRow>
                ) : (
                  invoices.map((invoice) => (
                    <TableRow key={invoice.id}>
                      <TableCell>{invoice.invoiceNumber}</TableCell>
                      <TableCell>{invoice.customerName}</TableCell>
                      <TableCell>{formatDate(invoice.issueDate.toString())}</TableCell>
                      <TableCell>{formatDate(invoice.dueDate.toString())}</TableCell>
                      <TableCell>{formatCurrency(invoice.totalAmount, invoice.currency)}</TableCell>
                      <TableCell>
                        <Chip
                          label={invoice.status}
                          color={getStatusColor(invoice.status) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          onClick={() => handleInvoiceClick(invoice)}
                        >
                          Détails
                        </Button>
                        <Button
                          size="small"
                          startIcon={<PdfIcon />}
                        >
                          PDF
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
            <Pagination
              count={pagination.totalPages}
              page={page}
              onChange={handlePageChange}
              color="primary"
            />
          </Box>
        )}

        {/* Floating Action Button */}
        <Fab
          color="primary"
          aria-label="add"
          sx={{
            position: 'fixed',
            bottom: 16,
            right: 16,
          }}
          onClick={() => setShowInvoiceForm(true)}
        >
          <AddIcon />
        </Fab>

        {/* Invoice Details Dialog */}
        {selectedInvoice && (
          <InvoiceDetails
            invoice={selectedInvoice}
            open={!!selectedInvoice}
            onClose={() => setSelectedInvoice(null)}
          />
        )}

        {/* Invoice Form Dialog */}
        <InvoiceForm
          open={showInvoiceForm}
          onClose={() => setShowInvoiceForm(false)}
        />
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <PricingRules />
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Paper sx={{ p: 2, textAlign: 'center' }}>
          <ChartIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            Rapports de facturation
          </Typography>
          <Typography variant="body1" paragraph>
            Les rapports de facturation seront bientôt disponibles.
          </Typography>
          <Button variant="contained" startIcon={<DownloadIcon />}>
            Exporter les données
          </Button>
        </Paper>
      </TabPanel>
    </Box>
  );
};

export default Billing;
