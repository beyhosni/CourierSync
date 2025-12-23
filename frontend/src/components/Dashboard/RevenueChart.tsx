import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Invoice } from '../../types/billing';
import { Box, Typography, useTheme } from '@mui/material';
import dayjs from 'dayjs';

interface RevenueChartProps {
  invoices: Invoice[];
}

const RevenueChart: React.FC<RevenueChartProps> = ({ invoices }) => {
  const theme = useTheme();

  // Process invoice data to get monthly revenue
  const processRevenueData = () => {
    const revenueByMonth: Record<string, number> = {};

    // Initialize with the last 6 months
    for (let i = 5; i >= 0; i--) {
      const month = dayjs().subtract(i, 'month').format('MMM YYYY');
      revenueByMonth[month] = 0;
    }

    // Sum up revenue from paid invoices
    invoices
      .filter(invoice => invoice.status === 'paid')
      .forEach(invoice => {
        const month = dayjs(invoice.issueDate).format('MMM YYYY');
        if (revenueByMonth.hasOwnProperty(month)) {
          revenueByMonth[month] += invoice.totalAmount;
        }
      });

    // Transform to chart data format
    return Object.entries(revenueByMonth).map(([month, revenue]) => ({
      month,
      revenue: parseFloat(revenue.toFixed(2)),
    }));
  };

  const data = processRevenueData();

  // Custom tooltip
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <Box 
          sx={{ 
            p: 1, 
            bgcolor: 'background.paper', 
            boxShadow: theme.shadows[2],
            border: `1px solid ${theme.palette.divider}`
          }}
        >
          <Typography variant="body2">{`Month: ${label}`}</Typography>
          <Typography variant="body2">{`Revenue: $${payload[0].value}`}</Typography>
        </Box>
      );
    }
    return null;
  };

  // Format Y-axis tick to show currency
  const formatYAxisTick = (value: any) => {
    return `$${value}`;
  };

  if (invoices.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="body2" color="textSecondary">
          No revenue data available
        </Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart
        data={data}
        margin={{
          top: 5,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
        <XAxis 
          dataKey="month" 
          tick={{ fontSize: 12 }}
          stroke={theme.palette.text.secondary}
        />
        <YAxis 
          tick={{ fontSize: 12 }}
          stroke={theme.palette.text.secondary}
          tickFormatter={formatYAxisTick}
        />
        <Tooltip content={<CustomTooltip />} />
        <Line 
          type="monotone" 
          dataKey="revenue" 
          stroke={theme.palette.primary.main}
          strokeWidth={2}
          dot={{ fill: theme.palette.primary.main, r: 4 }}
          activeDot={{ r: 6 }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
};

export default RevenueChart;
