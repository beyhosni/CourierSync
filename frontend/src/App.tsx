import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import { useAppSelector } from './hooks/redux';
import Header from './components/Layout/Header';
import Sidebar from './components/Layout/Sidebar';
import Login from './pages/Auth/Login';
import Dashboard from './pages/Dashboard/Dashboard';
import Deliveries from './pages/Deliveries/Deliveries';
import DeliveryDetail from './pages/Deliveries/DeliveryDetail';
import Drivers from './pages/Drivers/Drivers';
import DriverDetail from './pages/Drivers/DriverDetail';
import Billing from './pages/Billing/Billing';
import InvoiceDetail from './pages/Billing/InvoiceDetail';
import Profile from './pages/Profile/Profile';
import Tracking3D from './components/Three/Tracking3D';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  const { isAuthenticated, user } = useAppSelector(state => state.auth);

  if (!isAuthenticated) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Login />
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Box sx={{ display: 'flex' }}>
          <Header user={user} />
          <Sidebar userRole={user?.role || ''} />
          <Box
            component="main"
            sx={{
              flexGrow: 1,
              p: 3,
              width: { sm: `calc(100% - 240px)` },
              mt: 8,
            }}
          >
            <Routes>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/deliveries" element={<Deliveries />} />
              <Route path="/deliveries/:id" element={<DeliveryDetail />} />
              <Route path="/drivers" element={<Drivers />} />
              <Route path="/drivers/:id" element={<DriverDetail />} />
              <Route path="/billing" element={<Billing />} />
              <Route path="/billing/invoices/:id" element={<InvoiceDetail />} />
              <Route path="/profile" element={<Profile />} />
              <Route path="/tracking-3d" element={<Tracking3D />} />
            </Routes>
          </Box>
        </Box>
      </Router>
    </ThemeProvider>
  );
}

export default App;
