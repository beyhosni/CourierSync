import React, { useState } from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Divider,
  Box,
  Collapse,
  ListSubheader,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  LocalShipping,
  People,
  Receipt,
  Map,
  ExpandLess,
  ExpandMore,
  ViewInAr,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { styled } from '@mui/material/styles';

const drawerWidth = 240;

const StyledDrawer = styled(Drawer)(({ theme }) => ({
  width: drawerWidth,
  flexShrink: 0,
  '& .MuiDrawer-paper': {
    width: drawerWidth,
    boxSizing: 'border-box',
  },
}));

interface SidebarProps {
  userRole: string;
}

const Sidebar: React.FC<SidebarProps> = ({ userRole }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [trackingMenuOpen, setTrackingMenuOpen] = useState(true);

  const handleNavigation = (path: string) => {
    navigate(path);
  };

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  const menuItems = [
    {
      text: 'Dashboard',
      icon: <DashboardIcon />,
      path: '/dashboard',
    },
    {
      text: 'Deliveries',
      icon: <LocalShipping />,
      path: '/deliveries',
    },
    {
      text: 'Drivers',
      icon: <People />,
      path: '/drivers',
      roles: ['admin', 'dispatcher'],
    },
    {
      text: 'Billing',
      icon: <Receipt />,
      path: '/billing',
      roles: ['admin', 'finance'],
    },
  ];

  const trackingItems = [
    {
      text: 'Map View',
      icon: <Map />,
      path: '/tracking',
    },
    {
      text: '3D View',
      icon: <ViewInAr />,
      path: '/tracking-3d',
    },
  ];

  const filteredMenuItems = menuItems.filter(item => 
    !item.roles || item.roles.includes(userRole)
  );

  return (
    <StyledDrawer variant="permanent">
      <Toolbar />
      <Box sx={{ overflow: 'auto' }}>
        <List>
          {filteredMenuItems.map((item) => (
            <ListItem key={item.text} disablePadding>
              <ListItemButton
                selected={isActive(item.path)}
                onClick={() => handleNavigation(item.path)}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>

        <Divider />

        <List
          subheader={
            <ListSubheader component="div" sx={{ bgcolor: 'background.paper' }}>
              Tracking
            </ListSubheader>
          }
        >
          <ListItem disablePadding>
            <ListItemButton onClick={() => setTrackingMenuOpen(!trackingMenuOpen)}>
              <ListItemIcon>
                <Map />
              </ListItemIcon>
              <ListItemText primary="Tracking" />
              {trackingMenuOpen ? <ExpandLess /> : <ExpandMore />}
            </ListItemButton>
          </ListItem>
          <Collapse in={trackingMenuOpen} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {trackingItems.map((item) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    selected={isActive(item.path)}
                    onClick={() => handleNavigation(item.path)}
                  >
                    <ListItemIcon>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </Collapse>
        </List>

        <Box sx={{ mt: 2, p: 2 }}>
          <Typography variant="caption" color="text.secondary">
            CourierSync v1.0.0
          </Typography>
        </Box>
      </Box>
    </StyledDrawer>
  );
};

export default Sidebar;
