import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Typography, Paper, Grid, Button, Chip, Divider, CircularProgress } from '@mui/material';
import { ArrowBack, Edit, LocationOn, Person, Schedule, LocalShipping } from '@mui/icons-material';
import { fetchDeliveryById } from '../../store/slices/deliverySlice';
import { RootState, AppDispatch } from '../../store';
import Tracking3D from '../../components/Three/Tracking3D';
import DeliveryStatusTimeline from '../../components/Delivery/DeliveryStatusTimeline';

interface DeliveryDetailParams {
  id: string;
}

const DeliveryDetail: React.FC = () => {
  const { id } = useParams<DeliveryDetailParams>();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { currentDelivery, isLoading } = useSelector((state: RootState) => state.delivery);
  const [tracking3DVisible, setTracking3DVisible] = useState(false);

  useEffect(() => {
    if (id) {
      dispatch(fetchDeliveryById(id));
    }
  }, [dispatch, id]);

  const handleBack = () => {
    navigate('/deliveries');
  };

  const handleEdit = () => {
    navigate(`/deliveries/${id}/edit`);
  };

  const toggleTracking3D = () => {
    setTracking3DVisible(!tracking3DVisible);
  };

  if (isLoading || !currentDelivery) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'pending':
        return 'warning';
      case 'assigned':
        return 'info';
      case 'in_progress':
        return 'primary';
      case 'completed':
        return 'success';
      case 'cancelled':
        return 'error';
      default:
        return 'default';
    }
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Détails de la livraison
        </Typography>
        <Box>
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={handleBack}
            sx={{ mr: 2 }}
          >
            Retour
          </Button>
          <Button
            variant="contained"
            startIcon={<Edit />}
            onClick={handleEdit}
          >
            Modifier
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Informations générales
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Schedule color="primary" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Date de livraison
                    </Typography>
                    <Typography variant="body1">
                      {new Date(currentDelivery.deliveryDate).toLocaleDateString('fr-FR')}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <LocalShipping color="primary" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Statut
                    </Typography>
                    <Chip 
                      label={currentDelivery.status} 
                      color={getStatusColor(currentDelivery.status) as any}
                      size="small"
                    />
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <LocationOn color="primary" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Adresse de livraison
                    </Typography>
                    <Typography variant="body1">
                      {currentDelivery.deliveryAddress.address}, {currentDelivery.deliveryAddress.city}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Person color="primary" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Chauffeur assigné
                    </Typography>
                    <Typography variant="body1">
                      {currentDelivery.driver ? `${currentDelivery.driver.firstName} ${currentDelivery.driver.lastName}` : 'Non assigné'}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                Suivi de la livraison
              </Typography>
              <Button
                variant={tracking3DVisible ? "contained" : "outlined"}
                onClick={toggleTracking3D}
              >
                {tracking3DVisible ? 'Masquer' : 'Afficher'} la vue 3D
              </Button>
            </Box>
            <DeliveryStatusTimeline deliveryId={currentDelivery.id} />
          </Paper>

          {tracking3DVisible && (
            <Paper sx={{ p: 0, overflow: 'hidden' }}>
              <Tracking3D deliveryId={currentDelivery.id} height="500px" />
            </Paper>
          )}
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Informations sur le colis
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Poids
              </Typography>
              <Typography variant="body1">
                {currentDelivery.package.weight} kg
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Dimensions
              </Typography>
              <Typography variant="body1">
                {currentDelivery.package.dimensions.length} × {currentDelivery.package.dimensions.width} × {currentDelivery.package.dimensions.height} cm
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Type de colis
              </Typography>
              <Typography variant="body1">
                {currentDelivery.package.type}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Instructions spéciales
              </Typography>
              <Typography variant="body1">
                {currentDelivery.package.specialInstructions || 'Aucune'}
              </Typography>
            </Box>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Informations sur le client
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Nom
              </Typography>
              <Typography variant="body1">
                {currentDelivery.customer.firstName} {currentDelivery.customer.lastName}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Email
              </Typography>
              <Typography variant="body1">
                {currentDelivery.customer.email}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Téléphone
              </Typography>
              <Typography variant="body1">
                {currentDelivery.customer.phone}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Adresse de facturation
              </Typography>
              <Typography variant="body1">
                {currentDelivery.customer.billingAddress.address}, {currentDelivery.customer.billingAddress.city}
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DeliveryDetail;
