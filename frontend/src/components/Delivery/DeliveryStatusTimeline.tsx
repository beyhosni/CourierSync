import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Timeline, TimelineItem, TimelineSeparator, TimelineConnector, TimelineContent, TimelineDot, TimelineOppositeContent } from '@mui/lab';
import { Typography, Box, CircularProgress, Paper } from '@mui/material';
import { LocalShipping, CheckCircle, Assignment, Schedule, DoneAll } from '@mui/icons-material';
import { fetchDeliveryStatusHistory } from '../../store/slices/deliverySlice';
import { RootState, AppDispatch } from '../../store';

interface DeliveryStatusTimelineProps {
  deliveryId: string;
}

interface StatusUpdate {
  id: string;
  status: string;
  timestamp: string;
  location?: string;
  notes?: string;
  updatedBy: string;
}

const DeliveryStatusTimeline: React.FC<DeliveryStatusTimelineProps> = ({ deliveryId }) => {
  const dispatch = useDispatch<AppDispatch>();
  const { statusHistory, isLoading } = useSelector((state: RootState) => state.delivery);
  const [localStatusHistory, setLocalStatusHistory] = useState<StatusUpdate[]>([]);

  useEffect(() => {
    if (deliveryId) {
      dispatch(fetchDeliveryStatusHistory(deliveryId));
    }
  }, [dispatch, deliveryId]);

  useEffect(() => {
    if (statusHistory[deliveryId]) {
      setLocalStatusHistory(statusHistory[deliveryId]);
    } else {
      // Si nous n'avons pas d'historique, créons des données factices pour la démonstration
      const mockHistory: StatusUpdate[] = [
        {
          id: '1',
          status: 'pending',
          timestamp: new Date(Date.now() - 86400000 * 2).toISOString(),
          notes: 'Commande créée',
          updatedBy: 'Système'
        },
        {
          id: '2',
          status: 'assigned',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          location: 'Entrepôt',
          notes: 'Chauffeur assigné à la livraison',
          updatedBy: 'Opérateur'
        },
        {
          id: '3',
          status: 'in_progress',
          timestamp: new Date(Date.now() - 3600000 * 4).toISOString(),
          location: 'En route vers le client',
          notes: 'Livraison en cours',
          updatedBy: 'Chauffeur'
        }
      ];

      setLocalStatusHistory(mockHistory);
    }
  }, [statusHistory, deliveryId]);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'pending':
        return <Assignment />;
      case 'assigned':
        return <Schedule />;
      case 'in_progress':
        return <LocalShipping />;
      case 'completed':
        return <DoneAll />;
      case 'cancelled':
        return <CheckCircle color="error" />;
      default:
        return <Schedule />;
    }
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
        return 'grey';
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
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Paper sx={{ p: 2 }}>
      {localStatusHistory.length === 0 ? (
        <Box sx={{ textAlign: 'center', p: 3 }}>
          <Typography variant="body2" color="text.secondary">
            Aucune mise à jour de statut disponible
          </Typography>
        </Box>
      ) : (
        <Timeline>
          {localStatusHistory.map((update, index) => (
            <TimelineItem key={update.id}>
              <TimelineOppositeContent
                sx={{ m: 'auto 0' }}
                align="right"
                variant="body2"
                color="text.secondary"
              >
                {new Date(update.timestamp).toLocaleString('fr-FR')}
              </TimelineOppositeContent>
              <TimelineSeparator>
                <TimelineDot color={getStatusColor(update.status) as any}>
                  {getStatusIcon(update.status)}
                </TimelineDot>
                {index < localStatusHistory.length - 1 && <TimelineConnector />}
              </TimelineSeparator>
              <TimelineContent sx={{ py: '12px', px: 2 }}>
                <Typography variant="h6" component="span">
                  {getStatusText(update.status)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Par {update.updatedBy}
                  {update.location && ` - ${update.location}`}
                </Typography>
                {update.notes && (
                  <Typography variant="body2">
                    {update.notes}
                  </Typography>
                )}
              </TimelineContent>
            </TimelineItem>
          ))}
        </Timeline>
      )}
    </Paper>
  );
};

export default DeliveryStatusTimeline;
