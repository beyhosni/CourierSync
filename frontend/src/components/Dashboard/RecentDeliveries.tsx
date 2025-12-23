import React from 'react';
import { Box, List, ListItem, ListItemText, Typography, Chip, Divider, Avatar } from '@mui/material';
import { DeliveryOrder } from '../../types/delivery';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';

interface RecentDeliveriesProps {
  deliveries: DeliveryOrder[];
}

const RecentDeliveries: React.FC<RecentDeliveriesProps> = ({ deliveries }) => {
  const navigate = useNavigate();

  // Get status color
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'created':
        return 'default';
      case 'assigned':
        return 'primary';
      case 'picked_up':
        return 'warning';
      case 'in_transit':
        return 'secondary';
      case 'delivered':
        return 'success';
      case 'cancelled':
        return 'error';
      default:
        return 'default';
    }
  };

  // Format status text
  const formatStatus = (status: string) => {
    return status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase());
  };

  // Navigate to delivery details
  const handleDeliveryClick = (id: string) => {
    navigate(`/deliveries/${id}`);
  };

  if (deliveries.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="body2" color="textSecondary">
          No recent deliveries
        </Typography>
      </Box>
    );
  }

  return (
    <List sx={{ width: '100%', overflow: 'auto', maxHeight: 300 }}>
      {deliveries.map((delivery, index) => (
        <React.Fragment key={delivery.id}>
          <ListItem 
            alignItems="flex-start" 
            button 
            onClick={() => handleDeliveryClick(delivery.id)}
            sx={{ py: 1.5 }}
          >
            <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
              {delivery.orderNumber.substring(0, 2).toUpperCase()}
            </Avatar>
            <ListItemText
              primary={
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography variant="subtitle2" component="span">
                    {delivery.orderNumber}
                  </Typography>
                  <Chip 
                    label={formatStatus(delivery.status)} 
                    color={getStatusColor(delivery.status) as any}
                    size="small"
                  />
                </Box>
              }
              secondary={
                <Box component="span" sx={{ mt: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>From:</strong> {delivery.pickup.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>To:</strong> {delivery.dropoff.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Customer:</strong> {delivery.customerName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {dayjs(delivery.createdAt).format('MMM DD, YYYY h:mm A')}
                  </Typography>
                </Box>
              }
            />
          </ListItem>
          {index < deliveries.length - 1 && <Divider variant="inset" component="li" />}
        </React.Fragment>
      ))}
    </List>
  );
};

export default RecentDeliveries;
