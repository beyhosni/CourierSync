import React, { useEffect, useState } from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Divider,
} from '@mui/material';
import {
  LocalShipping as DeliveryIcon,
  People as DriverIcon,
  Receipt as RevenueIcon,
  TrendingUp as TrendIcon,
} from '@mui/icons-material';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { fetchDeliveries } from '../../store/slices/deliverySlice';
import { fetchDrivers } from '../../store/slices/driverSlice';
import { fetchInvoices } from '../../store/slices/billingSlice';
import { DeliveryOrder, Driver, Invoice } from '../../types';
import DeliveryStatusChart from '../../components/Dashboard/DeliveryStatusChart';
import RevenueChart from '../../components/Dashboard/RevenueChart';
import RecentDeliveries from '../../components/Dashboard/RecentDeliveries';
import ActiveDrivers from '../../components/Dashboard/ActiveDrivers';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color, subtitle }) => (
  <Card sx={{ height: '100%' }}>
    <CardContent>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography color="textSecondary" gutterBottom variant="overline">
            {title}
          </Typography>
          <Typography variant="h4" component="div">
            {value}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="textSecondary">
              {subtitle}
            </Typography>
          )}
        </Box>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            width: 60,
            height: 60,
            borderRadius: 1,
            bgcolor: color,
            color: 'white',
          }}
        >
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const {
    deliveries,
    isLoading: deliveriesLoading,
    pagination: deliveriesPagination,
  } = useAppSelector((state) => state.delivery);

  const {
    drivers,
    isLoading: driversLoading,
  } = useAppSelector((state) => state.driver);

  const {
    invoices,
    isLoading: invoicesLoading,
  } = useAppSelector((state) => state.billing);

  const [stats, setStats] = useState({
    totalDeliveries: 0,
    activeDrivers: 0,
    monthlyRevenue: 0,
    completionRate: 0,
  });

  useEffect(() => {
    // Fetch dashboard data
    dispatch(fetchDeliveries({ page: 1, limit: 10 }));
    dispatch(fetchDrivers({ page: 1, limit: 10 }));
    dispatch(fetchInvoices({ page: 1, limit: 10 }));
  }, [dispatch]);

  useEffect(() => {
    // Calculate stats when data is loaded
    if (!deliveriesLoading && !driversLoading && !invoicesLoading) {
      const totalDeliveries = deliveriesPagination.total;
      const activeDrivers = drivers.filter(
        (driver: Driver) => driver.status === 'available' || driver.status === 'busy'
      ).length;

      // Calculate monthly revenue from invoices
      const currentMonth = new Date().getMonth();
      const currentYear = new Date().getFullYear();
      const monthlyRevenue = invoices
        .filter((invoice: Invoice) => {
          const invoiceDate = new Date(invoice.issueDate);
          return (
            invoiceDate.getMonth() === currentMonth &&
            invoiceDate.getFullYear() === currentYear &&
            invoice.status === 'paid'
          );
        })
        .reduce((sum: number, invoice: Invoice) => sum + invoice.totalAmount, 0);

      // Calculate completion rate
      const completedDeliveries = deliveries.filter(
        (delivery: DeliveryOrder) => delivery.status === 'delivered'
      ).length;
      const completionRate = totalDeliveries > 0 
        ? Math.round((completedDeliveries / totalDeliveries) * 100) 
        : 0;

      setStats({
        totalDeliveries,
        activeDrivers,
        monthlyRevenue,
        completionRate,
      });
    }
  }, [deliveries, drivers, invoices, deliveriesLoading, driversLoading, invoicesLoading, deliveriesPagination]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>

      <Grid container spacing={3}>
        {/* Stat Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Deliveries"
            value={stats.totalDeliveries}
            icon={<DeliveryIcon />}
            color="#1976d2"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Drivers"
            value={stats.activeDrivers}
            icon={<DriverIcon />}
            color="#388e3c"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Monthly Revenue"
            value={`$${stats.monthlyRevenue.toFixed(2)}`}
            icon={<RevenueIcon />}
            color="#f57c00"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Completion Rate"
            value={`${stats.completionRate}%`}
            icon={<TrendIcon />}
            color="#7b1fa2"
          />
        </Grid>

        {/* Charts */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 300 }}>
            <Typography variant="h6" gutterBottom>
              Delivery Status
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {deliveriesLoading ? (
              <Box display="flex" justifyContent="center" alignItems="center" height={200}>
                <CircularProgress />
              </Box>
            ) : (
              <DeliveryStatusChart deliveries={deliveries} />
            )}
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 300 }}>
            <Typography variant="h6" gutterBottom>
              Revenue Trend
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {invoicesLoading ? (
              <Box display="flex" justifyContent="center" alignItems="center" height={200}>
                <CircularProgress />
              </Box>
            ) : (
              <RevenueChart invoices={invoices} />
            )}
          </Paper>
        </Grid>

        {/* Recent Activities */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Recent Deliveries
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {deliveriesLoading ? (
              <Box display="flex" justifyContent="center" alignItems="center" height={300}>
                <CircularProgress />
              </Box>
            ) : (
              <RecentDeliveries deliveries={deliveries.slice(0, 5)} />
            )}
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Active Drivers
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {driversLoading ? (
              <Box display="flex" justifyContent="center" alignItems="center" height={300}>
                <CircularProgress />
              </Box>
            ) : (
              <ActiveDrivers drivers={drivers.slice(0, 5)} />
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
