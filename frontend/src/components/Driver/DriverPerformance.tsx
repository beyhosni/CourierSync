import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, CircularProgress, LinearProgress } from '@mui/material';
import { useSelector, useDispatch } from 'react-redux';
import { fetchDriverPerformance } from '../../store/slices/driverSlice';
import { RootState, AppDispatch } from '../../store';

interface DriverPerformanceProps {
  driverId: string;
}

interface PerformanceData {
  totalDeliveries: number;
  successfulDeliveries: number;
  averageDeliveryTime: number;
  rating: number;
  onTimeDeliveryRate: number;
  totalDistance: number;
  weeklyStats: {
    week: string;
    deliveries: number;
    successful: number;
    onTime: number;
  }[];
}

const DriverPerformance: React.FC<DriverPerformanceProps> = ({ driverId }) => {
  const dispatch = useDispatch<AppDispatch>();
  const { performanceData, isLoading } = useSelector((state: RootState) => state.driver);
  const [localPerformanceData, setLocalPerformanceData] = useState<PerformanceData | null>(null);

  useEffect(() => {
    if (driverId) {
      dispatch(fetchDriverPerformance(driverId));
    }
  }, [dispatch, driverId]);

  useEffect(() => {
    if (performanceData[driverId]) {
      setLocalPerformanceData(performanceData[driverId]);
    } else {
      // Si nous n'avons pas de données, créons des données factices pour la démonstration
      const mockData: PerformanceData = {
        totalDeliveries: 142,
        successfulDeliveries: 135,
        averageDeliveryTime: 28, // en minutes
        rating: 4.7,
        onTimeDeliveryRate: 92, // en pourcentage
        totalDistance: 2845, // en km
        weeklyStats: [
          { week: 'Semaine 1', deliveries: 28, successful: 27, onTime: 25 },
          { week: 'Semaine 2', deliveries: 31, successful: 30, onTime: 28 },
          { week: 'Semaine 3', deliveries: 35, successful: 33, onTime: 32 },
          { week: 'Semaine 4', deliveries: 48, successful: 45, onTime: 45 },
        ]
      };
      setLocalPerformanceData(mockData);
    }
  }, [performanceData, driverId]);

  if (isLoading || !localPerformanceData) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  const successRate = Math.round((localPerformanceData.successfulDeliveries / localPerformanceData.totalDeliveries) * 100);

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Performance du chauffeur
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Livraisons totales
              </Typography>
              <Typography variant="h5" component="div">
                {localPerformanceData.totalDeliveries}
              </Typography>
              <Typography color="textSecondary">
                Ce mois-ci
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Taux de réussite
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ width: '100%', mr: 1 }}>
                  <LinearProgress variant="determinate" value={successRate} />
                </Box>
                <Box sx={{ minWidth: 35 }}>
                  <Typography variant="body2" color="text.secondary">{`${successRate}%`}</Typography>
                </Box>
              </Box>
              <Typography color="textSecondary">
                {localPerformanceData.successfulDeliveries} réussies sur {localPerformanceData.totalDeliveries}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Temps moyen de livraison
              </Typography>
              <Typography variant="h5" component="div">
                {localPerformanceData.averageDeliveryTime} min
              </Typography>
              <Typography color="textSecondary">
                Par livraison
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Note moyenne
              </Typography>
              <Typography variant="h5" component="div">
                {localPerformanceData.rating}/5
              </Typography>
              <Typography color="textSecondary">
                Basée sur les retours clients
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Livraisons à l'heure
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ width: '100%', mr: 1 }}>
                  <LinearProgress variant="determinate" value={localPerformanceData.onTimeDeliveryRate} />
                </Box>
                <Box sx={{ minWidth: 35 }}>
                  <Typography variant="body2" color="text.secondary">{`${localPerformanceData.onTimeDeliveryRate}%`}</Typography>
                </Box>
              </Box>
              <Typography color="textSecondary">
                Ce mois-ci
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Distance totale
              </Typography>
              <Typography variant="h5" component="div">
                {localPerformanceData.totalDistance} km
              </Typography>
              <Typography color="textSecondary">
                Ce mois-ci
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>
          Statistiques hebdomadaires
        </Typography>
        <Card>
          <CardContent>
            <Grid container spacing={2}>
              {localPerformanceData.weeklyStats.map((week, index) => (
                <Grid item xs={12} sm={6} md={3} key={index}>
                  <Box sx={{ p: 2, border: '1px solid #eee', borderRadius: 1 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      {week.week}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Livraisons: {week.deliveries}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Réussies: {week.successful}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      À l'heure: {week.onTime}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};

export default DriverPerformance;
