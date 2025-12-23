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
  Avatar,
  Divider,
  IconButton,
  Menu,
  MenuList,
  MenuItem as MenuItemComponent,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
  Map as MapIcon,
  ViewList as ViewListIcon,
  MoreVert as MoreVertIcon,
  DirectionsCar as DriverIcon,
  Person as PersonIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  OnlinePrediction as OnlineIcon,
  OfflineBolt as OfflineIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../hooks/redux';
import { fetchDrivers, updateDriverStatus } from '../store/slices/driverSlice';
import DeliveryVisualization3D from '../components/Three/DeliveryVisualization3D';
import DriverDetails from '../components/Driver/DriverDetails';
import DriverForm from '../components/Driver/DriverForm';
import { Driver } from '../types/delivery';

const Drivers: React.FC = () => {
  const dispatch = useAppDispatch();
  const { drivers, isLoading, error, pagination } = useAppSelector(state => state.driver);
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedDriver, setSelectedDriver] = useState<Driver | null>(null);
  const [showDriverForm, setShowDriverForm] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuDriverId, setMenuDriverId] = useState<string | null>(null);

  useEffect(() => {
    dispatch(fetchDrivers({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  }, [dispatch, page, searchTerm, statusFilter]);

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleStatusChange = (driverId: string, status: string) => {
    dispatch(updateDriverStatus({ 
      driverId, 
      status,
      updatedBy: 'current-user' // In a real app, this would be the current user ID
    }));
    handleCloseMenu();
  };

  const handleRefresh = () => {
    dispatch(fetchDrivers({ 
      page, 
      limit: 20,
      search: searchTerm,
      status: statusFilter
    }));
  };

  const handleDriverClick = (driver: Driver) => {
    setSelectedDriver(driver);
  };

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>, driverId: string) => {
    setAnchorEl(event.currentTarget);
    setMenuDriverId(driverId);
  };

  const handleCloseMenu = () => {
    setAnchorEl(null);
    setMenuDriverId(null);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'available': return 'success';
      case 'busy': return 'warning';
      case 'offline': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'available': return <OnlineIcon />;
      case 'busy': return <DriverIcon />;
      case 'offline': return <OfflineIcon />;
      default: return <PersonIcon />;
    }
  };

  // Convert location updates for the 3D visualization
  const locationUpdates = drivers
    .filter(driver => driver.currentLocation)
    .map(driver => ({
      id: `${driver.id}-current`,
      driverId: driver.id,
      latitude: driver.currentLocation!.latitude,
      longitude: driver.currentLocation!.longitude,
      timestamp: driver.currentLocation!.timestamp,
    }));

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Chauffeurs
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
              placeholder="Rechercher un chauffeur..."
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
                <MenuItem value="available">Disponible</MenuItem>
                <MenuItem value="busy">Occupé</MenuItem>
                <MenuItem value="offline">Hors ligne</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Content */}
      {viewMode === 'map' ? (
        <Paper sx={{ p: 2, height: 600 }}>
          <Typography variant="h6" gutterBottom>
            Carte de localisation des chauffeurs
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
          {drivers.map((driver) => (
            <Grid item xs={12} md={6} lg={4} key={driver.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar sx={{ mr: 2 }}>
                      {driver.firstName.charAt(0)}{driver.lastName.charAt(0)}
                    </Avatar>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="h6" component="div">
                        {driver.firstName} {driver.lastName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {driver.email}
                      </Typography>
                    </Box>
                    <IconButton
                      aria-label="settings"
                      onClick={(e) => handleMenuClick(e, driver.id)}
                    >
                      <MoreVertIcon />
                    </IconButton>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Chip
                      icon={getStatusIcon(driver.status)}
                      label={driver.status}
                      color={getStatusColor(driver.status) as any}
                      size="small"
                    />
                    <Typography variant="body2" color="text.secondary">
                      {driver.vehicleType}
                    </Typography>
                  </Box>
                  <Divider sx={{ mb: 2 }} />
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Téléphone: {driver.phone}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    Véhicule: {driver.vehiclePlate}
                  </Typography>
                  {driver.currentLocation && (
                    <Typography variant="body2">
                      Position: {driver.currentLocation.latitude.toFixed(4)}, {driver.currentLocation.longitude.toFixed(4)}
                    </Typography>
                  )}
                </CardContent>
                <CardActions>
                  <Button size="small" onClick={() => handleDriverClick(driver)}>
                    Détails
                  </Button>
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
        onClick={() => setShowDriverForm(true)}
      >
        <AddIcon />
      </Fab>

      {/* Driver Details Dialog */}
      {selectedDriver && (
        <DriverDetails
          driver={selectedDriver}
          open={!!selectedDriver}
          onClose={() => setSelectedDriver(null)}
          onStatusChange={handleStatusChange}
        />
      )}

      {/* Driver Form Dialog */}
      <DriverForm
        open={showDriverForm}
        onClose={() => setShowDriverForm(false)}
      />

      {/* Context Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleCloseMenu}
      >
        <MenuList>
          <MenuItemComponent onClick={() => {
            const driver = drivers.find(d => d.id === menuDriverId);
            if (driver) {
              setSelectedDriver(driver);
            }
            handleCloseMenu();
          }}>
            <ListItemIcon>
              <PersonIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Voir les détails</ListItemText>
          </MenuItemComponent>
          <MenuItemComponent onClick={() => {
            const driver = drivers.find(d => d.id === menuDriverId);
            if (driver) {
              setSelectedDriver(driver);
              setShowDriverForm(true);
            }
            handleCloseMenu();
          }}>
            <ListItemIcon>
              <EditIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Modifier</ListItemText>
          </MenuItemComponent>
          <Divider />
          <MenuItemComponent onClick={() => {
            if (menuDriverId) {
              handleStatusChange(menuDriverId, 'available');
            }
          }}>
            <ListItemIcon>
              <OnlineIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Marquer comme disponible</ListItemText>
          </MenuItemComponent>
          <MenuItemComponent onClick={() => {
            if (menuDriverId) {
              handleStatusChange(menuDriverId, 'busy');
            }
          }}>
            <ListItemIcon>
              <DriverIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Marquer comme occupé</ListItemText>
          </MenuItemComponent>
          <MenuItemComponent onClick={() => {
            if (menuDriverId) {
              handleStatusChange(menuDriverId, 'offline');
            }
          }}>
            <ListItemIcon>
              <OfflineIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Marquer comme hors ligne</ListItemText>
          </MenuItemComponent>
        </MenuList>
      </Menu>
    </Box>
  );
};

export default Drivers;
