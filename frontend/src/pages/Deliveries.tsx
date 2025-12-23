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
  Chip,
  Card,
  CardContent,
  CardActions,
  CircularProgress,
  Alert,
  Pagination,
  Fab,
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
  Map as MapIcon,
  ViewList as ViewListIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../hooks/redux';
import { fetchDeliveries, updateDeliveryStatus } from '../store/slices/deliverySlice';
import DeliveryVisualization3D from '../components/Three/DeliveryVisualization3D';
import DeliveryDetails from '../components/Delivery/DeliveryDetails';
import DeliveryForm from '../components/Delivery/DeliveryForm';
import { DeliveryOrder } from '../types/delivery';

const Deliveries: React.FC = () => {
  const dispatch = useAppDispatch();
  const { deliveries, isLoading, error, pagination } = useAppSelector(state => state.delivery);
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedDelivery, setSelectedDelivery] = useState<DeliveryOrder | null>(null);
  const [showDeliveryForm, setShowDeliveryForm] = useState(false);

  useEffect(() => {
    dispatch(fetchDeliveries({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  }, [dispatch, page, searchTerm, statusFilter]);

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleStatusChange = (deliveryId: string, status: string) => {
    dispatch(updateDeliveryStatus({ 
      deliveryId, 
      status,
      updatedBy: 'current-user' // In a real app, this would be the current user ID
    }));
  };

  const handleRefresh = () => {
    dispatch(fetchDeliveries({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  };

  const handleDeliveryClick = (delivery: DeliveryOrder) => {
    setSelectedDelivery(delivery);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'created': return 'default';
      case 'assigned': return 'info';
      case 'picked_up': return 'warning';
      case 'in_transit': return 'warning';
      case 'delivered': return 'success';
      case 'cancelled': return 'error';
      default: return 'default';
    }
  };

  // Convert location updates for the 3D visualization
  const locationUpdates = deliveries.flatMap(delivery => 
    delivery.locationUpdates ? delivery.locationUpdates.map(update => ({
      id: update.id,
      driverId: delivery.assignedDriverId || '',
      deliveryId: delivery.id,
      latitude: update.latitude,
      longitude: update.longitude,
      timestamp: update.timestamp,
    })) : []
  );

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Livraisons
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant={viewMode === 'list' ? 'contained' : 'outlined'}
            startIcon={<ViewListIcon />}
            onClick={() => setViewMode('list')}
          >
            Liste
          </Button>
          <Button
            variant={viewMode === 'map' ? 'contained' : 'outlined'}
            startIcon={<MapIcon />}
            onClick={() => setViewMode('map')}
          >
            Carte 3D
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
            disabled={isLoading}
          >
            Actualiser
          </Button>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              placeholder="Rechercher une livraison..."
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
                <MenuItem value="created">Créée</MenuItem>
                <MenuItem value="assigned">Assignée</MenuItem>
                <MenuItem value="picked_up">Enlevée</MenuItem>
                <MenuItem value="in_transit">En transit</MenuItem>
                <MenuItem value="delivered">Livrée</MenuItem>
                <MenuItem value="cancelled">Annulée</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Content */}
      {viewMode === 'map' ? (
        <Paper sx={{ p: 2, height: 600 }}>
          <Typography variant="h6" gutterBottom>
            Carte de suivi en temps réel
          </Typography>
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 500 }}>
              <CircularProgress />
            </Box>
          ) : (
            <DeliveryVisualization3D
              locationUpdates={locationUpdates}
            />
          )}
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {deliveries.map((delivery) => (
            <Grid item xs={12} md={6} lg={4} key={delivery.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" component="div">
                    {delivery.orderNumber}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    Client: {delivery.customerName}
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Chip
                      label={delivery.status}
                      color={getStatusColor(delivery.status) as any}
                      size="small"
                    />
                    <Typography variant="body2" color="text.secondary">
                      {delivery.priority}
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Ramassage: {delivery.pickup.name}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Livraison: {delivery.dropoff.name}
                  </Typography>
                  {delivery.assignedDriverId && (
                    <Typography variant="body2">
                      Chauffeur: {delivery.assignedDriverId}
                    </Typography>
                  )}
                </CardContent>
                <CardActions>
                  <Button size="small" onClick={() => handleDeliveryClick(delivery)}>
                    Détails
                  </Button>
                  {delivery.status === 'assigned' && (
                    <Button 
                      size="small" 
                      onClick={() => handleStatusChange(delivery.id, 'picked_up')}
                    >
                      Marquer comme enlevée
                    </Button>
                  )}
                  {delivery.status === 'picked_up' && (
                    <Button 
                      size="small" 
                      onClick={() => handleStatusChange(delivery.id, 'in_transit')}
                    >
                      Marquer en transit
                    </Button>
                  )}
                  {delivery.status === 'in_transit' && (
                    <Button 
                      size="small" 
                      onClick={() => handleStatusChange(delivery.id, 'delivered')}
                    >
                      Marquer comme livrée
                    </Button>
                  )}
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Pagination */}
      {viewMode === 'list' && pagination.totalPages > 1 && (
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
        onClick={() => setShowDeliveryForm(true)}
      >
        <AddIcon />
      </Fab>

      {/* Delivery Details Dialog */}
      {selectedDelivery && (
        <DeliveryDetails
          delivery={selectedDelivery}
          open={!!selectedDelivery}
          onClose={() => setSelectedDelivery(null)}
          onStatusChange={handleStatusChange}
        />
      )}

      {/* Delivery Form Dialog */}
      <DeliveryForm
        open={showDeliveryForm}
        onClose={() => setShowDeliveryForm(false)}
      />
    </Box>
  );
};

export default Deliveries;
