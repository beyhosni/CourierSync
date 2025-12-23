import React, { useState, useEffect } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, CircularProgress, Button, Pagination } from '@mui/material';
import { useSelector, useDispatch } from 'react-redux';
import { fetchDriverDeliveries } from '../../store/slices/driverSlice';
import { RootState, AppDispatch } from '../../store';
import { useNavigate } from 'react-router-dom';

interface DriverDeliveriesProps {
  driverId: string;
}

interface Delivery {
  id: string;
  trackingNumber: string;
  status: string;
  deliveryDate: string;
  customerName: string;
  deliveryAddress: {
    address: string;
    city: string;
    postalCode: string;
  };
}

const DriverDeliveries: React.FC<DriverDeliveriesProps> = ({ driverId }) => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { deliveries, isLoading } = useSelector((state: RootState) => state.driver);
  const [localDeliveries, setLocalDeliveries] = useState<Delivery[]>([]);
  const [page, setPage] = useState(1);
  const rowsPerPage = 10;

  useEffect(() => {
    if (driverId) {
      dispatch(fetchDriverDeliveries(driverId));
    }
  }, [dispatch, driverId]);

  useEffect(() => {
    if (deliveries[driverId]) {
      setLocalDeliveries(deliveries[driverId]);
    } else {
      // Si nous n'avons pas de données, créons des données factices pour la démonstration
      const mockDeliveries: Delivery[] = Array.from({ length: 25 }, (_, i) => ({
        id: `delivery-${i + 1}`,
        trackingNumber: `TRK-${1000 + i}`,
        status: ['pending', 'assigned', 'in_progress', 'completed', 'cancelled'][Math.floor(Math.random() * 5)],
        deliveryDate: new Date(Date.now() - Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000).toISOString(),
        customerName: `Client ${i + 1}`,
        deliveryAddress: {
          address: `${100 + i} Rue de la Livraison`,
          city: 'Ville',
          postalCode: `${1000 + i}`
        }
      }));
      setLocalDeliveries(mockDeliveries);
    }
  }, [deliveries, driverId]);

  const handleChangePage = (event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleViewDelivery = (deliveryId: string) => {
    navigate(`/deliveries/${deliveryId}`);
  };

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

  const getStatusText = (status: string) => {
    switch (status) {
      case 'pending':
        return 'En attente';
      case 'assigned':
        return 'Assigné';
      case 'in_progress':
        return 'En cours';
      case 'completed':
        return 'Terminé';
      case 'cancelled':
        return 'Annulé';
      default:
        return status;
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Livraisons du chauffeur
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Numéro de suivi</TableCell>
              <TableCell>Client</TableCell>
              <TableCell>Adresse</TableCell>
              <TableCell>Date de livraison</TableCell>
              <TableCell>Statut</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {localDeliveries
              .slice((page - 1) * rowsPerPage, page * rowsPerPage)
              .map((delivery) => (
                <TableRow key={delivery.id}>
                  <TableCell>{delivery.trackingNumber}</TableCell>
                  <TableCell>{delivery.customerName}</TableCell>
                  <TableCell>
                    {delivery.deliveryAddress.address}, {delivery.deliveryAddress.city}
                  </TableCell>
                  <TableCell>
                    {new Date(delivery.deliveryDate).toLocaleDateString('fr-FR')}
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={getStatusText(delivery.status)} 
                      color={getStatusColor(delivery.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Button 
                      size="small" 
                      onClick={() => handleViewDelivery(delivery.id)}
                    >
                      Voir
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
        <Pagination
          count={Math.ceil(localDeliveries.length / rowsPerPage)}
          page={page}
          onChange={handleChangePage}
          color="primary"
        />
      </Box>
    </Box>
  );
};

export default DriverDeliveries;
