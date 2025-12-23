import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Paper,
  Divider,
  Fab,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  DirectionsCar as DriverIcon,
  LocalShipping as DeliveryIcon,
  Assessment as StatsIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../hooks/redux';
import { fetchDeliveries } from '../store/slices/deliverySlice';
import { fetchDrivers } from '../store/slices/driverSlice';
import DeliveryVisualization3D from '../components/Three/DeliveryVisualization3D';
import DeliveryList from '../components/Delivery/DeliveryList';
import DriverStatusList from '../components/Driver/DriverStatusList';
import StatsCards from '../components/Dashboard/StatsCards';
import DeliveryForm from '../components/Delivery/DeliveryForm';

const Dashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const { deliveries, isLoading: deliveriesLoading, error: deliveriesError } = useAppSelector(state => state.delivery);
  const { drivers, isLoading: driversLoading, error: driversError } = useAppSelector(state => state.driver);
  const [selectedDriverId, setSelectedDriverId] = useState<string | undefined>();
  const [showDeliveryForm, setShowDeliveryForm] = useState(false);

  useEffect(() => {
    dispatch(fetchDeliveries({ page: 1, limit: 100 }));
    dispatch(fetchDrivers({ page: 1, limit: 100 }));
  }, [dispatch]);

  const handleDriverSelect = (driverId: string) => {
    setSelectedDriverId(driverId === selectedDriverId ? undefined : driverId);
  };

  const isLoading = deliveriesLoading || driversLoading;
  const error = deliveriesError || driversError;

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
      <Typography variant="h4" gutterBottom>
        Tableau de bord
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={3}>
        {/* Stats Cards */}
        <Grid item xs={12}>
          <StatsCards />
        </Grid>

        {/* 3D Visualization */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, height: 500, position: 'relative' }}>
            <Typography variant="h6" gutterBottom>
              Suivi en temps réel
            </Typography>
            {isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
                <CircularProgress />
              </Box>
            ) : (
              <DeliveryVisualization3D
                locationUpdates={locationUpdates}
                selectedDriverId={selectedDriverId}
              />
            )}
          </Paper>
        </Grid>

        {/* Driver Status */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2, height: 500, overflow: 'auto' }}>
            <Typography variant="h6" gutterBottom>
              Statut des chauffeurs
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <DriverStatusList 
                drivers={drivers} 
                onDriverSelect={handleDriverSelect}
                selectedDriverId={selectedDriverId}
              />
            )}
          </Paper>
        </Grid>

        {/* Recent Deliveries */}
        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Livraisons récentes
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <DeliveryList deliveries={deliveries.slice(0, 5)} />
            )}
          </Paper>
        </Grid>
      </Grid>

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

      {/* Delivery Form Dialog */}
      <DeliveryForm
        open={showDeliveryForm}
        onClose={() => setShowDeliveryForm(false)}
      />
    </Box>
  );
};

export default Dashboard;
