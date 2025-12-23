import React from 'react';
import { Box, List, ListItem, ListItemText, Typography, Chip, Divider, Avatar } from '@mui/material';
import { Driver } from '../../types/delivery';
import { useNavigate } from 'react-router-dom';
import { 
  LocalShipping as AvailableIcon,
  DirectionsCar as BusyIcon,
  Cancel as OfflineIcon
} from '@mui/icons-material';

interface ActiveDriversProps {
  drivers: Driver[];
}

const ActiveDrivers: React.FC<ActiveDriversProps> = ({ drivers }) => {
  const navigate = useNavigate();

  // Get status color
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'available':
        return 'success';
      case 'busy':
        return 'warning';
      case 'offline':
        return 'error';
      default:
        return 'default';
    }
  };

  // Get status icon
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'available':
        return <AvailableIcon />;
      case 'busy':
        return <BusyIcon />;
      case 'offline':
        return <OfflineIcon />;
      default:
        return <OfflineIcon />;
    }
  };

  // Format status text
  const formatStatus = (status: string) => {
    return status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase());
  };

  // Navigate to driver details
  const handleDriverClick = (id: string) => {
    navigate(`/drivers/${id}`);
  };

  if (drivers.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="body2" color="textSecondary">
          No active drivers
        </Typography>
      </Box>
    );
  }

  return (
    <List sx={{ width: '100%', overflow: 'auto', maxHeight: 300 }}>
      {drivers.map((driver, index) => (
        <React.Fragment key={driver.id}>
          <ListItem 
            alignItems="flex-start" 
            button 
            onClick={() => handleDriverClick(driver.id)}
            sx={{ py: 1.5 }}
          >
            <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
              {driver.name.substring(0, 2).toUpperCase()}
            </Avatar>
            <ListItemText
              primary={
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography variant="subtitle2" component="span">
                    {driver.name}
                  </Typography>
                  <Chip 
                    label={formatStatus(driver.status)} 
                    color={getStatusColor(driver.status) as any}
                    size="small"
                    icon={getStatusIcon(driver.status)}
                  />
                </Box>
              }
              secondary={
                <Box component="span" sx={{ mt: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Phone:</strong> {driver.phone}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Vehicle:</strong> {driver.vehicleType} ({driver.vehiclePlate})
                  </Typography>
                  {driver.currentLocation && (
                    <Typography variant="body2" color="text.secondary">
                      <strong>Last seen:</strong> {new Date(driver.currentLocation.timestamp).toLocaleString()}
                    </Typography>
                  )}
                </Box>
              }
            />
          </ListItem>
          {index < drivers.length - 1 && <Divider variant="inset" component="li" />}
        </React.Fragment>
      ))}
    </List>
  );
};

export default ActiveDrivers;
