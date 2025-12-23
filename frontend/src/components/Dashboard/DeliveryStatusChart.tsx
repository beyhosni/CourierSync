import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { DeliveryOrder } from '../../types/delivery';
import { Box, Typography, useTheme } from '@mui/material';

interface DeliveryStatusChartProps {
  deliveries: DeliveryOrder[];
}

const DeliveryStatusChart: React.FC<DeliveryStatusChartProps> = ({ deliveries }) => {
  const theme = useTheme();

  // Count deliveries by status
  const statusCounts = deliveries.reduce((acc, delivery) => {
    acc[delivery.status] = (acc[delivery.status] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  // Transform data for the chart
  const data = Object.entries(statusCounts).map(([status, count]) => ({
    name: status.replace('_', ' ').replace(/\w/g, l => l.toUpperCase()),
    value: count,
    color: getStatusColor(status, theme),
  }));

  // Custom label for the pie chart
  const renderCustomizedLabel = ({
    cx, cy, midAngle, innerRadius, outerRadius, percent,
  }: any) => {
    const RADIAN = Math.PI / 180;
    const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);

    return (
      <text 
        x={x} 
        y={y} 
        fill="white" 
        textAnchor={x > cx ? 'start' : 'end'} 
        dominantBaseline="central"
      >
        {`${(percent * 100).toFixed(0)}%`}
      </text>
    );
  };

  // Custom tooltip
  const CustomTooltip = ({ active, payload }: any) => {
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
          <Typography variant="body2">{`${payload[0].name}: ${payload[0].value}`}</Typography>
        </Box>
      );
    }
    return null;
  };

  // Get color for each status
  function getStatusColor(status: string, theme: any): string {
    switch (status) {
      case 'created':
        return theme.palette.info.main;
      case 'assigned':
        return theme.palette.primary.main;
      case 'picked_up':
        return theme.palette.warning.main;
      case 'in_transit':
        return theme.palette.secondary.main;
      case 'delivered':
        return theme.palette.success.main;
      case 'cancelled':
        return theme.palette.error.main;
      default:
        return theme.palette.grey[500];
    }
  }

  if (deliveries.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="body2" color="textSecondary">
          No delivery data available
        </Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height="100%">
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          labelLine={false}
          label={renderCustomizedLabel}
          outerRadius={80}
          fill="#8884d8"
          dataKey="value"
        >
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={entry.color} />
          ))}
        </Pie>
        <Tooltip content={<CustomTooltip />} />
        <Legend 
          verticalAlign="bottom" 
          height={36} 
          formatter={(value) => <span>{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  );
};

export default DeliveryStatusChart;
