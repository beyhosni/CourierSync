import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Divider,
  Card,
  CardContent,
  CardActions,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Alert,
  CircularProgress,
  Avatar,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
  Chip,
} from '@mui/material';
import {
  Save as SaveIcon,
  Person as PersonIcon,
  Business as BusinessIcon,
  Notifications as NotificationsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  LocalShipping as DeliveryIcon,
  Map as MapIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Add as AddIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { updateUserProfile } from '../../store/slices/authSlice';
import { fetchSettings, updateSettings } from '../../store/slices/settingsSlice';
import { User } from '../../types/auth';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = (props) => {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`settings-tabpanel-${index}`}
      aria-labelledby={`settings-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const Settings: React.FC = () => {
  const dispatch = useAppDispatch();
  const { user } = useAppSelector(state => state.auth);
  const { settings, isLoading, error } = useAppSelector(state => state.settings);
  const [tabValue, setTabValue] = useState(0);
  const [successMessage, setSuccessMessage] = useState('');
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [profileImagePreview, setProfileImagePreview] = useState<string | null>(null);

  const {
    control: profileControl,
    handleSubmit: handleProfileSubmit,
    formState: { errors: profileErrors },
    reset: resetProfile,
    setValue: setProfileValue,
  } = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
    },
  });

  const {
    control: companyControl,
    handleSubmit: handleCompanySubmit,
    formState: { errors: companyErrors },
    reset: resetCompany,
    setValue: setCompanyValue,
  } = useForm({
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      postalCode: '',
      country: 'France',
      taxId: '',
    },
  });

  const {
    control: notificationControl,
    handleSubmit: handleNotificationSubmit,
    formState: { errors: notificationErrors },
    reset: resetNotification,
    setValue: setNotificationValue,
  } = useForm({
    defaultValues: {
      emailNotifications: true,
      smsNotifications: false,
      pushNotifications: true,
      deliveryUpdates: true,
      newOrders: true,
      driverAlerts: false,
      systemAlerts: true,
    },
  });

  const {
    control: paymentControl,
    handleSubmit: handlePaymentSubmit,
    formState: { errors: paymentErrors },
    reset: resetPayment,
    setValue: setPaymentValue,
  } = useForm({
    defaultValues: {
      paymentMethod: 'stripe',
      stripeApiKey: '',
      paypalClientId: '',
      currency: 'EUR',
      taxRate: 20,
      invoicePrefix: 'INV-',
      paymentTerms: 30,
      autoGenerateInvoices: true,
    },
  });

  const {
    control: deliveryControl,
    handleSubmit: handleDeliverySubmit,
    formState: { errors: deliveryErrors },
    reset: resetDelivery,
    setValue: setDeliveryValue,
  } = useForm({
    defaultValues: {
      defaultRadius: 10,
      maxDeliveryDistance: 50,
      deliveryFeeCalculation: 'distance',
      baseDeliveryFee: 5,
      feePerKm: 1.5,
      urgentDeliveryFee: 10,
      autoAssignDrivers: false,
      requireProofOfDelivery: true,
    },
  });

  useEffect(() => {
    dispatch(fetchSettings());
  }, [dispatch]);

  useEffect(() => {
    if (user) {
      setProfileValue('firstName', user.firstName);
      setProfileValue('lastName', user.lastName);
      setProfileValue('email', user.email);
      setProfileValue('phone', user.phone || '');
      if (user.profileImage) {
        setProfileImagePreview(user.profileImage);
      }
    }
  }, [user, setProfileValue]);

  useEffect(() => {
    if (settings) {
      // Company settings
      setCompanyValue('name', settings.company?.name || '');
      setCompanyValue('email', settings.company?.email || '');
      setCompanyValue('phone', settings.company?.phone || '');
      setCompanyValue('address', settings.company?.address || '');
      setCompanyValue('city', settings.company?.city || '');
      setCompanyValue('postalCode', settings.company?.postalCode || '');
      setCompanyValue('country', settings.company?.country || 'France');
      setCompanyValue('taxId', settings.company?.taxId || '');

      // Notification settings
      setNotificationValue('emailNotifications', settings.notifications?.emailNotifications !== false);
      setNotificationValue('smsNotifications', settings.notifications?.smsNotifications || false);
      setNotificationValue('pushNotifications', settings.notifications?.pushNotifications !== false);
      setNotificationValue('deliveryUpdates', settings.notifications?.deliveryUpdates !== false);
      setNotificationValue('newOrders', settings.notifications?.newOrders !== false);
      setNotificationValue('driverAlerts', settings.notifications?.driverAlerts || false);
      setNotificationValue('systemAlerts', settings.notifications?.systemAlerts !== false);

      // Payment settings
      setPaymentValue('paymentMethod', settings.payment?.paymentMethod || 'stripe');
      setPaymentValue('stripeApiKey', settings.payment?.stripeApiKey || '');
      setPaymentValue('paypalClientId', settings.payment?.paypalClientId || '');
      setPaymentValue('currency', settings.payment?.currency || 'EUR');
      setPaymentValue('taxRate', settings.payment?.taxRate || 20);
      setPaymentValue('invoicePrefix', settings.payment?.invoicePrefix || 'INV-');
      setPaymentValue('paymentTerms', settings.payment?.paymentTerms || 30);
      setPaymentValue('autoGenerateInvoices', settings.payment?.autoGenerateInvoices !== false);

      // Delivery settings
      setDeliveryValue('defaultRadius', settings.delivery?.defaultRadius || 10);
      setDeliveryValue('maxDeliveryDistance', settings.delivery?.maxDeliveryDistance || 50);
      setDeliveryValue('deliveryFeeCalculation', settings.delivery?.deliveryFeeCalculation || 'distance');
      setDeliveryValue('baseDeliveryFee', settings.delivery?.baseDeliveryFee || 5);
      setDeliveryValue('feePerKm', settings.delivery?.feePerKm || 1.5);
      setDeliveryValue('urgentDeliveryFee', settings.delivery?.urgentDeliveryFee || 10);
      setDeliveryValue('autoAssignDrivers', settings.delivery?.autoAssignDrivers || false);
      setDeliveryValue('requireProofOfDelivery', settings.delivery?.requireProofOfDelivery !== false);
    }
  }, [settings, setCompanyValue, setNotificationValue, setPaymentValue, setDeliveryValue]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleProfileImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      setProfileImage(file);

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setProfileImagePreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const onProfileSubmit = (data: any) => {
    const formData = new FormData();

    // Append all form fields
    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, value.toString());
      }
    });

    // Append profile image if selected
    if (profileImage) {
      formData.append('profileImage', profileImage);
    }

    dispatch(updateUserProfile(formData))
      .unwrap()
      .then(() => {
        setSuccessMessage('Profil mis à jour avec succès');
        setTimeout(() => setSuccessMessage(''), 3000);
      });
  };

  const onCompanySubmit = (data: any) => {
    dispatch(updateSettings({ 
      ...settings,
      company: data
    }))
      .unwrap()
      .then(() => {
        setSuccessMessage('Informations de l'entreprise mises à jour avec succès');
        setTimeout(() => setSuccessMessage(''), 3000);
      });
  };

  const onNotificationSubmit = (data: any) => {
    dispatch(updateSettings({ 
      ...settings,
      notifications: data
    }))
      .unwrap()
      .then(() => {
        setSuccessMessage('Paramètres de notification mis à jour avec succès');
        setTimeout(() => setSuccessMessage(''), 3000);
      });
  };

  const onPaymentSubmit = (data: any) => {
    dispatch(updateSettings({ 
      ...settings,
      payment: data
    }))
      .unwrap()
      .then(() => {
        setSuccessMessage('Paramètres de paiement mis à jour avec succès');
        setTimeout(() => setSuccessMessage(''), 3000);
      });
  };

  const onDeliverySubmit = (data: any) => {
    dispatch(updateSettings({ 
      ...settings,
      delivery: data
    }))
      .unwrap()
      .then(() => {
        setSuccessMessage('Paramètres de livraison mis à jour avec succès');
        setTimeout(() => setSuccessMessage(''), 3000);
      });
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Typography variant="h4" gutterBottom>
        Paramètres
      </Typography>

      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {successMessage}
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ width: '100%' }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
        >
          <Tab icon={<PersonIcon />} label="Profil" />
          <Tab icon={<BusinessIcon />} label="Entreprise" />
          <Tab icon={<NotificationsIcon />} label="Notifications" />
          <Tab icon={<PaymentIcon />} label="Paiement" />
          <Tab icon={<DeliveryIcon />} label="Livraison" />
        </Tabs>

        {/* Profile Settings */}
        <TabPanel value={tabValue} index={0}>
          <Box component="form" onSubmit={handleProfileSubmit(onProfileSubmit)}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                  <Avatar
                    src={profileImagePreview}
                    sx={{ width: 120, height: 120, mb: 2 }}
                  >
                    {!profileImagePreview && (
                      <PersonIcon sx={{ fontSize: 60 }} />
                    )}
                  </Avatar>
                  <Button
                    variant="outlined"
                    component="label"
                    startIcon={<EditIcon />}
                  >
                    Changer la photo
                    <input
                      type="file"
                      accept="image/*"
                      hidden
                      onChange={handleProfileImageChange}
                    />
                  </Button>
                </Box>
              </Grid>
              <Grid item xs={12} md={8}>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="firstName"
                      control={profileControl}
                      rules={{ required: 'Le prénom est requis' }}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Prénom"
                          fullWidth
                          error={!!profileErrors.firstName}
                          helperText={profileErrors.firstName?.message}
                        />
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="lastName"
                      control={profileControl}
                      rules={{ required: 'Le nom est requis' }}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Nom"
                          fullWidth
                          error={!!profileErrors.lastName}
                          helperText={profileErrors.lastName?.message}
                        />
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="email"
                      control={profileControl}
                      rules={{
                        required: 'L'email est requis',
                        pattern: {
                          value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                          message: 'Adresse email invalide',
                        },
                      }}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Email"
                          fullWidth
                          error={!!profileErrors.email}
                          helperText={profileErrors.email?.message}
                          InputProps={{
                            startAdornment: (
                              <EmailIcon color="action" sx={{ mr: 1 }} />
                            ),
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="phone"
                      control={profileControl}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Téléphone"
                          fullWidth
                          InputProps={{
                            startAdornment: (
                              <PhoneIcon color="action" sx={{ mr: 1 }} />
                            ),
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <Button
                      type="submit"
                      variant="contained"
                      color="primary"
                      startIcon={<SaveIcon />}
                      disabled={isLoading}
                    >
                      {isLoading ? <CircularProgress size={24} /> : 'Enregistrer'}
                    </Button>
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Company Settings */}
        <TabPanel value={tabValue} index={1}>
          <Box component="form" onSubmit={handleCompanySubmit(onCompanySubmit)}>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Controller
                  name="name"
                  control={companyControl}
                  rules={{ required: 'Le nom de l'entreprise est requis' }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Nom de l'entreprise"
                      fullWidth
                      error={!!companyErrors.name}
                      helperText={companyErrors.name?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="taxId"
                  control={companyControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Numéro d'identification fiscale"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="email"
                  control={companyControl}
                  rules={{
                    required: 'L'email de l'entreprise est requis',
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: 'Adresse email invalide',
                    },
                  }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Email de l'entreprise"
                      fullWidth
                      error={!!companyErrors.email}
                      helperText={companyErrors.email?.message}
                      InputProps={{
                        startAdornment: (
                          <EmailIcon color="action" sx={{ mr: 1 }} />
                        ),
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="phone"
                  control={companyControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Téléphone de l'entreprise"
                      fullWidth
                      InputProps={{
                        startAdornment: (
                          <PhoneIcon color="action" sx={{ mr: 1 }} />
                        ),
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="address"
                  control={companyControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Adresse"
                      fullWidth
                      InputProps={{
                        startAdornment: (
                          <LocationIcon color="action" sx={{ mr: 1 }} />
                        ),
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <Controller
                  name="city"
                  control={companyControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Ville"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <Controller
                  name="postalCode"
                  control={companyControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Code postal"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <Controller
                  name="country"
                  control={companyControl}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Pays</InputLabel>
                      <Select {...field} label="Pays">
                        <MenuItem value="France">France</MenuItem>
                        <MenuItem value="Belgique">Belgique</MenuItem>
                        <MenuItem value="Suisse">Suisse</MenuItem>
                        <MenuItem value="Luxembourg">Luxembourg</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={isLoading}
                >
                  {isLoading ? <CircularProgress size={24} /> : 'Enregistrer'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Notification Settings */}
        <TabPanel value={tabValue} index={2}>
          <Box component="form" onSubmit={handleNotificationSubmit(onNotificationSubmit)}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Préférences de notification
                </Typography>
                <Divider sx={{ mb: 2 }} />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="emailNotifications"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notifications par email"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="smsNotifications"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notifications par SMS"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="pushNotifications"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Notifications push"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Types de notifications
                </Typography>
                <Divider sx={{ mb: 2 }} />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="deliveryUpdates"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Mises à jour des livraisons"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="newOrders"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Nouvelles commandes"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="driverAlerts"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Alertes chauffeur"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="systemAlerts"
                  control={notificationControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Alertes système"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={isLoading}
                >
                  {isLoading ? <CircularProgress size={24} /> : 'Enregistrer'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Payment Settings */}
        <TabPanel value={tabValue} index={3}>
          <Box component="form" onSubmit={handlePaymentSubmit(onPaymentSubmit)}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Méthode de paiement
                </Typography>
                <Divider sx={{ mb: 2 }} />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="paymentMethod"
                  control={paymentControl}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Méthode de paiement</InputLabel>
                      <Select {...field} label="Méthode de paiement">
                        <MenuItem value="stripe">Stripe</MenuItem>
                        <MenuItem value="paypal">PayPal</MenuItem>
                        <MenuItem value="cash">Espèces</MenuItem>
                        <MenuItem value="bank_transfer">Virement bancaire</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="currency"
                  control={paymentControl}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Devise</InputLabel>
                      <Select {...field} label="Devise">
                        <MenuItem value="EUR">EUR (Euro)</MenuItem>
                        <MenuItem value="USD">USD (Dollar américain)</MenuItem>
                        <MenuItem value="GBP">GBP (Livre sterling)</MenuItem>
                        <MenuItem value="CHF">CHF (Franc suisse)</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="stripeApiKey"
                  control={paymentControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Clé API Stripe"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="paypalClientId"
                  control={paymentControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="ID client PayPal"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Facturation
                </Typography>
                <Divider sx={{ mb: 2 }} />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="taxRate"
                  control={paymentControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Taux de TVA (%)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 0, max: 100 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="invoicePrefix"
                  control={paymentControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Préfixe de facture"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="paymentTerms"
                  control={paymentControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Conditions de paiement (jours)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 0 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="autoGenerateInvoices"
                  control={paymentControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Générer automatiquement les factures"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={isLoading}
                >
                  {isLoading ? <CircularProgress size={24} /> : 'Enregistrer'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Delivery Settings */}
        <TabPanel value={tabValue} index={4}>
          <Box component="form" onSubmit={handleDeliverySubmit(onDeliverySubmit)}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Configuration de livraison
                </Typography>
                <Divider sx={{ mb: 2 }} />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="defaultRadius"
                  control={deliveryControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Rayon de recherche par défaut (km)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 1 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="maxDeliveryDistance"
                  control={deliveryControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Distance maximale de livraison (km)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 1 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="deliveryFeeCalculation"
                  control={deliveryControl}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Calcul des frais de livraison</InputLabel>
                      <Select {...field} label="Calcul des frais de livraison">
                        <MenuItem value="distance">Basé sur la distance</MenuItem>
                        <MenuItem value="flat">Tarif fixe</MenuItem>
                        <MenuItem value="weight">Basé sur le poids</MenuItem>
                        <MenuItem value="time">Basé sur le temps</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="baseDeliveryFee"
                  control={deliveryControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Frais de livraison de base (€)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 0, step: 0.1 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="feePerKm"
                  control={deliveryControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Frais par kilomètre (€)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 0, step: 0.1 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="urgentDeliveryFee"
                  control={deliveryControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Frais de livraison urgente (€)"
                      type="number"
                      fullWidth
                      InputProps={{
                        inputProps: { min: 0, step: 0.1 }
                      }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="autoAssignDrivers"
                  control={deliveryControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Assigner automatiquement les chauffeurs"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="requireProofOfDelivery"
                  control={deliveryControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Exiger une preuve de livraison"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={isLoading}
                >
                  {isLoading ? <CircularProgress size={24} /> : 'Enregistrer'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>
      </Paper>
    </Box>
  );
};

export default Settings;
