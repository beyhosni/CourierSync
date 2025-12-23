import React, { useState, useEffect } from 'react';
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
  Paper,
  LinearProgress,
  Avatar,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Close as CloseIcon,
  Person as PersonIcon,
  LocalShipping as DeliveryIcon,
  Schedule as ScheduleIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  AccessTime as TimeIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Map as MapIcon,
  Timeline as TimelineIcon,
  Description as DescriptionIcon,
} from '@mui/icons-material';
import { DeliveryOrder, DeliveryEvent } from '../../types/delivery';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

interface DeliveryDetailsProps {
  delivery: DeliveryOrder;
  open: boolean;
  onClose: () => void;
  onStatusChange?: (deliveryId: string, status: string) => void;
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
      id={`delivery-tabpanel-${index}`}
      aria-labelledby={`delivery-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const DeliveryDetails: React.FC<DeliveryDetailsProps> = ({
  delivery,
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
      case 'created': return 'default';
      case 'assigned': return 'info';
      case 'picked_up': return 'warning';
      case 'in_transit': return 'warning';
      case 'delivered': return 'success';
      case 'cancelled': return 'error';
      default: return 'default';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'low': return 'success';
      case 'normal': return 'info';
      case 'high': return 'warning';
      case 'urgent': return 'error';
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

  const getEventIcon = (eventType: string) => {
    switch (eventType) {
      case 'created': return <DescriptionIcon color="primary" />;
      case 'assigned': return <PersonIcon color="info" />;
      case 'picked_up': return <DeliveryIcon color="warning" />;
      case 'in_transit': return <DeliveryIcon color="warning" />;
      case 'delivered': return <CheckCircleIcon color="success" />;
      case 'cancelled': return <CancelIcon color="error" />;
      default: return <TimelineIcon color="action" />;
    }
  };

  const getStatusProgress = (status: string) => {
    switch (status) {
      case 'created': return 20;
      case 'assigned': return 40;
      case 'picked_up': return 60;
      case 'in_transit': return 80;
      case 'delivered': return 100;
      case 'cancelled': return 0;
      default: return 0;
    }
  };

  const renderStatusActions = () => {
    if (!onStatusChange) return null;

    switch (delivery.status) {
      case 'assigned':
        return (
          <Button
            variant="contained"
            color="primary"
            onClick={() => onStatusChange(delivery.id, 'picked_up')}
            startIcon={<DeliveryIcon />}
          >
            Marquer comme enlevée
          </Button>
        );
      case 'picked_up':
        return (
          <Button
            variant="contained"
            color="primary"
            onClick={() => onStatusChange(delivery.id, 'in_transit')}
            startIcon={<DeliveryIcon />}
          >
            Marquer en transit
          </Button>
        );
      case 'in_transit':
        return (
          <Button
            variant="contained"
            color="success"
            onClick={() => onStatusChange(delivery.id, 'delivered')}
            startIcon={<CheckCircleIcon />}
          >
            Marquer comme livrée
          </Button>
        );
      default:
        return null;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">Détails de la livraison {delivery.orderNumber}</Typography>
        <IconButton edge="end" onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent dividers>
        <Box sx={{ mb: 2 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <Chip
                label={delivery.status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase())}
                color={getStatusColor(delivery.status) as any}
              />
            </Grid>
            <Grid item>
              <Chip
                label={`Priorité: ${delivery.priority}`}
                color={getPriorityColor(delivery.priority) as any}
                size="small"
              />
            </Grid>
          </Grid>
          <Box sx={{ mt: 2 }}>
            <LinearProgress
              variant="determinate"
              value={getStatusProgress(delivery.status)}
              sx={{ height: 8, borderRadius: 4 }}
            />
          </Box>
        </Box>

        <Tabs value={tabValue} onChange={handleTabChange} aria-label="delivery details tabs">
          <Tab label="Informations" icon={<DescriptionIcon />} />
          <Tab label="Itinéraire" icon={<MapIcon />} />
          <Tab label="Événements" icon={<TimelineIcon />} />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Informations client
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <PersonIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">{delivery.customerName}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <EmailIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body2">{delivery.customerEmail}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <PhoneIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body2">{delivery.customerPhone}</Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Informations chauffeur
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  {delivery.assignedDriverId ? (
                    <Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <Avatar sx={{ mr: 1, bgcolor: 'primary.main' }}>
                          {delivery.assignedDriverName?.charAt(0)}
                        </Avatar>
                        <Typography variant="body1">{delivery.assignedDriverName}</Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <PhoneIcon sx={{ mr: 1, color: 'text.secondary' }} />
                        <Typography variant="body2">{delivery.assignedDriverPhone}</Typography>
                      </Box>
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Aucun chauffeur assigné
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Point de ramassage
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <LocationIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">{delivery.pickup.name}</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {delivery.pickup.address}, {delivery.pickup.city}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                    <ScheduleIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body2">
                      {formatDateTime(delivery.requestedPickupTime)}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Point de livraison
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <LocationIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    <Typography variant="body1">{delivery.dropoff.name}</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {delivery.dropoff.address}, {delivery.dropoff.city}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Informations colis
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Description: {delivery.packageDescription}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Poids: {delivery.packageWeight} kg
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Spécimen médical: {delivery.isMedicalSpecimen ? 'Oui' : 'Non'}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Température contrôlée: {delivery.temperatureControlled ? 'Oui' : 'Non'}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Paper variant="outlined" sx={{ p: 2, height: 400, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              La carte d'itinéraire sera bientôt disponible.
            </Typography>
          </Paper>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <List>
            {delivery.events && delivery.events.length > 0 ? (
              delivery.events.map((event: DeliveryEvent, index: number) => (
                <React.Fragment key={event.id}>
                  <ListItem alignItems="flex-start">
                    <ListItemIcon>
                      {getEventIcon(event.eventType)}
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body1">
                            {event.eventType.replace('_', ' ').replace(/\w/g, l => l.toUpperCase())}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {formatDateTime(event.timestamp)}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Box sx={{ mt: 1 }}>
                          {event.notes && (
                            <Typography variant="body2" color="text.secondary">
                              {event.notes}
                            </Typography>
                          )}
                          {event.createdBy && (
                            <Typography variant="body2" color="text.secondary">
                              Par: {event.createdBy}
                            </Typography>
                          )}
                        </Box>
                      }
                    />
                  </ListItem>
                  {index < delivery.events.length - 1 && <Divider variant="inset" component="li" />}
                </React.Fragment>
              ))
            ) : (
              <Box sx={{ textAlign: 'center', py: 3 }}>
                <Typography variant="body2" color="text.secondary">
                  Aucun événement enregistré
                </Typography>
              </Box>
            )}
          </List>
        </TabPanel>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        {renderStatusActions()}
        <Button onClick={onClose} variant="outlined">
          Fermer
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeliveryDetails;
