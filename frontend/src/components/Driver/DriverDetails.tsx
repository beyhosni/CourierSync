import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Avatar,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  LinearProgress,
} from '@mui/material';
import {
  Close as CloseIcon,
  Person as PersonIcon,
  LocalShipping as DeliveryIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  LocationOn as LocationIcon,
  AccessTime as TimeIcon,
  Schedule as ScheduleIcon,
  Map as MapIcon,
  Timeline as TimelineIcon,
  DirectionsCar as CarIcon,
  Star as StarIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { Driver } from '../../types/delivery';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

interface DriverDetailsProps {
  driver: Driver;
  open: boolean;
  onClose: () => void;
  onStatusChange?: (driverId: string, status: string) => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = (props) => {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`driver-tabpanel-${index}`}
      aria-labelledby={`driver-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const DriverDetails: React.FC<DriverDetailsProps> = ({
  driver,
  open,
  onClose,
  onStatusChange,
}) => {
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'available': return 'success';
      case 'busy': return 'warning';
      case 'offline': return 'error';
      default: return 'default';
    }
  };

  const formatDateTime = (dateString: string) => {
    try {
      return format(new Date(dateString), 'dd MMM yyyy à HH:mm', { locale: fr });
    } catch (error) {
      return dateString;
    }
  };

  const renderStatusActions = () => {
    if (!onStatusChange) return null;

    switch (driver.status) {
      case 'available':
        return (
          <Button
            variant="contained"
            color="warning"
            onClick={() => onStatusChange(driver.id, 'busy')}
            startIcon={<DeliveryIcon />}
          >
            Marquer comme occupé
          </Button>
        );
      case 'busy':
        return (
          <Button
            variant="contained"
            color="success"
            onClick={() => onStatusChange(driver.id, 'available')}
            startIcon={<PersonIcon />}
          >
            Marquer comme disponible
          </Button>
        );
      case 'offline':
        return (
          <Button
            variant="contained"
            color="success"
            onClick={() => onStatusChange(driver.id, 'available')}
            startIcon={<PersonIcon />}
          >
            Marquer comme disponible
          </Button>
        );
      default:
        return null;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">Détails du chauffeur</Typography>
        <IconButton edge="end" onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent dividers>
        <Box sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
          <Avatar sx={{ mr: 2, width: 64, height: 64, bgcolor: 'primary.main' }}>
            {driver.firstName.charAt(0)}{driver.lastName.charAt(0)}
          </Avatar>
          <Box>
            <Typography variant="h5">{driver.firstName} {driver.lastName}</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
              <Chip
                label={driver.status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase())}
                color={getStatusColor(driver.status) as any}
                size="small"
              />
              <Box sx={{ ml: 2, display: 'flex', alignItems: 'center' }}>
                {[...Array(5)].map((_, i) => (
                  <StarIcon
                    key={i}
                    sx={{
                      color: i < Math.floor(driver.rating || 0) ? 'warning.main' : 'action.disabled',
                      fontSize: 20,
                    }}
                  />
                ))}
                <Typography variant="body2" sx={{ ml: 1 }}>
                  ({driver.rating?.toFixed(1) || '0.0'})
                </Typography>
              </Box>
            </Box>
          </Box>
        </Box>

        <Tabs value={tabValue} onChange={handleTabChange} aria-label="driver details tabs">
          <Tab label="Informations" icon={<PersonIcon />} />
          <Tab label="Véhicule" icon={<CarIcon />} />
          <Tab label="Livraisons" icon={<DeliveryIcon />} />
          <Tab label="Localisation" icon={<MapIcon />} />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Informations personnelles
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <EmailIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">{driver.email}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <PhoneIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">{driver.phone}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <ScheduleIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body2">
                      Inscrit le {formatDateTime(driver.createdAt)}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Statistiques
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Livraisons complétées: {driver.completedDeliveries || 0}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Distance totale: {driver.totalDistance || 0} km
                  </Typography>
                  <Typography variant="body2">
                    Temps de conduite total: {driver.totalDrivingTime || 0} heures
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Notes
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Typography variant="body2">
                    {driver.notes || 'Aucune note disponible'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Informations véhicule
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        Type: {driver.vehicleType}
                      </Typography>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        Marque: {driver.vehicleMake || 'Non spécifié'}
                      </Typography>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        Modèle: {driver.vehicleModel || 'Non spécifié'}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        Plaque d'immatriculation: {driver.vehiclePlate}
                      </Typography>
                      <Typography variant="body2" sx={{ mb: 1 }}>
                        Année: {driver.vehicleYear || 'Non spécifié'}
                      </Typography>
                      <Typography variant="body2">
                        Couleur: {driver.vehicleColor || 'Non spécifié'}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Documents véhicule
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Typography variant="body2" color="text.secondary">
                    Les documents du véhicule seront bientôt disponibles.
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Livraisons récentes
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Numéro</TableCell>
                      <TableCell>Client</TableCell>
                      <TableCell>Date</TableCell>
                      <TableCell>Statut</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {driver.recentDeliveries && driver.recentDeliveries.length > 0 ? (
                      driver.recentDeliveries.map((delivery) => (
                        <TableRow key={delivery.id}>
                          <TableCell>{delivery.orderNumber}</TableCell>
                          <TableCell>{delivery.customerName}</TableCell>
                          <TableCell>{formatDateTime(delivery.createdAt)}</TableCell>
                          <TableCell>
                            <Chip
                              label={delivery.status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase())}
                              size="small"
                              color={
                                delivery.status === 'delivered' ? 'success' :
                                delivery.status === 'cancelled' ? 'error' : 'default'
                              }
                            />
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={4} align="center">
                          Aucune livraison récente
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </TabPanel>

        <TabPanel value={tabValue} index={3}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Localisation actuelle
              </Typography>
              <Divider sx={{ mb: 2 }} />
              {driver.currentLocation ? (
                <Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <LocationIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">
                      {driver.currentLocation.latitude.toFixed(6)}, {driver.currentLocation.longitude.toFixed(6)}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <TimeIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body2">
                      Dernière mise à jour: {formatDateTime(driver.currentLocation.timestamp)}
                    </Typography>
                  </Box>
                  <Box sx={{ mt: 2, height: 300, bgcolor: 'grey.200', borderRadius: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Typography variant="body2" color="text.secondary">
                      La carte sera bientôt disponible
                    </Typography>
                  </Box>
                </Box>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  Aucune localisation disponible
                </Typography>
              )}
            </CardContent>
          </Card>
        </TabPanel>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Fermer</Button>
        {renderStatusActions()}
      </DialogActions>
    </Dialog>
  );
};

export default DriverDetails;
