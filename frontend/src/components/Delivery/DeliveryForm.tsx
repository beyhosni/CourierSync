import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Divider,
  Alert,
  CircularProgress,
  Stepper,
  Step,
  StepLabel,
  Paper,
} from '@mui/material';
import {
  Close as CloseIcon,
  Save as SaveIcon,
  LocationOn as LocationIcon,
  Schedule as ScheduleIcon,
  Package as PackageIcon,
  LocalShipping as DeliveryIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { createDelivery, updateDelivery } from '../../store/slices/deliverySlice';
import { fetchDrivers } from '../../store/slices/driverSlice';
import { DeliveryOrder } from '../../types/delivery';
import { format } from 'date-fns';

interface DeliveryFormProps {
  open: boolean;
  onClose: () => void;
  delivery?: DeliveryOrder | null;
}

interface FormData {
  customerId: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  pickupName: string;
  pickupAddress: string;
  pickupCity: string;
  pickupPostalCode: string;
  pickupCountry: string;
  pickupLat: number;
  pickupLng: number;
  pickupContactName: string;
  pickupContactPhone: string;
  dropoffName: string;
  dropoffAddress: string;
  dropoffCity: string;
  dropoffPostalCode: string;
  dropoffCountry: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffContactName: string;
  dropoffContactPhone: string;
  priority: 'low' | 'normal' | 'high' | 'urgent';
  packageDescription: string;
  packageWeight: number;
  isMedicalSpecimen: boolean;
  temperatureControlled: boolean;
  requestedPickupTime: string;
  assignedDriverId?: string;
  notes?: string;
}

const steps = ['Informations client', 'Points de livraison', 'Détails colis', 'Confirmation'];

const DeliveryForm: React.FC<DeliveryFormProps> = ({
  open,
  onClose,
  delivery,
}) => {
  const dispatch = useAppDispatch();
  const { drivers } = useAppSelector(state => state.driver);
  const { isLoading, error } = useAppSelector(state => state.delivery);
  const [activeStep, setActiveStep] = useState(0);
  const [isEditing, setIsEditing] = useState(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch,
  } = useForm<FormData>({
    defaultValues: {
      customerId: '',
      customerName: '',
      customerEmail: '',
      customerPhone: '',
      pickupName: '',
      pickupAddress: '',
      pickupCity: '',
      pickupPostalCode: '',
      pickupCountry: 'France',
      pickupLat: 0,
      pickupLng: 0,
      pickupContactName: '',
      pickupContactPhone: '',
      dropoffName: '',
      dropoffAddress: '',
      dropoffCity: '',
      dropoffPostalCode: '',
      dropoffCountry: 'France',
      dropoffLat: 0,
      dropoffLng: 0,
      dropoffContactName: '',
      dropoffContactPhone: '',
      priority: 'normal',
      packageDescription: '',
      packageWeight: 1,
      isMedicalSpecimen: false,
      temperatureControlled: false,
      requestedPickupTime: format(new Date(), "yyyy-MM-dd'T'HH:mm"),
      notes: '',
    },
  });

  useEffect(() => {
    if (open && delivery) {
      setIsEditing(true);
      // Populate form with delivery data
      setValue('customerId', delivery.customerId);
      setValue('customerName', delivery.customerName);
      setValue('customerEmail', delivery.customerEmail || '');
      setValue('customerPhone', delivery.customerPhone || '');
      setValue('pickupName', delivery.pickup.name);
      setValue('pickupAddress', delivery.pickup.address);
      setValue('pickupCity', delivery.pickup.city);
      setValue('pickupPostalCode', delivery.pickup.postalCode);
      setValue('pickupCountry', delivery.pickup.country);
      setValue('pickupLat', delivery.pickup.latitude);
      setValue('pickupLng', delivery.pickup.longitude);
      setValue('pickupContactName', delivery.pickup.contactName || '');
      setValue('pickupContactPhone', delivery.pickup.contactPhone || '');
      setValue('dropoffName', delivery.dropoff.name);
      setValue('dropoffAddress', delivery.dropoff.address);
      setValue('dropoffCity', delivery.dropoff.city);
      setValue('dropoffPostalCode', delivery.dropoff.postalCode);
      setValue('dropoffCountry', delivery.dropoff.country);
      setValue('dropoffLat', delivery.dropoff.latitude);
      setValue('dropoffLng', delivery.dropoff.longitude);
      setValue('dropoffContactName', delivery.dropoff.contactName || '');
      setValue('dropoffContactPhone', delivery.dropoff.contactPhone || '');
      setValue('priority', delivery.priority);
      setValue('packageDescription', delivery.packageDescription);
      setValue('packageWeight', delivery.packageWeight);
      setValue('isMedicalSpecimen', delivery.isMedicalSpecimen);
      setValue('temperatureControlled', delivery.temperatureControlled);
      setValue('requestedPickupTime', delivery.requestedPickupTime);
      setValue('assignedDriverId', delivery.assignedDriverId || '');
      setValue('notes', delivery.notes || '');
    } else if (open) {
      setIsEditing(false);
      reset();
    }

    // Fetch available drivers
    dispatch(fetchDrivers({ page: 1, limit: 100, status: 'available' }));
  }, [open, delivery, dispatch, setValue, reset]);

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const onSubmit = (data: FormData) => {
    const deliveryData = {
      customerId: data.customerId,
      customerName: data.customerName,
      pickup: {
        name: data.pickupName,
        address: data.pickupAddress,
        city: data.pickupCity,
        postalCode: data.pickupPostalCode,
        country: data.pickupCountry,
        latitude: data.pickupLat,
        longitude: data.pickupLng,
        contactName: data.pickupContactName,
        contactPhone: data.pickupContactPhone,
      },
      dropoff: {
        name: data.dropoffName,
        address: data.dropoffAddress,
        city: data.dropoffCity,
        postalCode: data.dropoffPostalCode,
        country: data.dropoffCountry,
        latitude: data.dropoffLat,
        longitude: data.dropoffLng,
        contactName: data.dropoffContactName,
        contactPhone: data.dropoffContactPhone,
      },
      priority: data.priority,
      packageDescription: data.packageDescription,
      packageWeight: data.packageWeight,
      isMedicalSpecimen: data.isMedicalSpecimen,
      temperatureControlled: data.temperatureControlled,
      requestedPickupTime: data.requestedPickupTime,
      notes: data.notes,
    };

    if (isEditing && delivery) {
      dispatch(updateDelivery({ id: delivery.id, delivery: deliveryData }))
        .unwrap()
        .then(() => {
          onClose();
        });
    } else {
      dispatch(createDelivery(deliveryData))
        .unwrap()
        .then(() => {
          onClose();
        });
    }
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Informations client
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="customerName"
                control={control}
                rules={{ required: 'Le nom du client est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Nom du client"
                    fullWidth
                    error={!!errors.customerName}
                    helperText={errors.customerName?.message}
                    InputProps={{
                      startAdornment: (
                        <PersonIcon color="action" sx={{ mr: 1 }} />
                      ),
                    }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="customerPhone"
                control={control}
                rules={{ required: 'Le téléphone du client est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Téléphone"
                    fullWidth
                    error={!!errors.customerPhone}
                    helperText={errors.customerPhone?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="customerEmail"
                control={control}
                rules={{
                  required: 'L'email du client est requis',
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
                    error={!!errors.customerEmail}
                    helperText={errors.customerEmail?.message}
                  />
                )}
              />
            </Grid>
          </Grid>
        );
      case 1:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Points de livraison
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Point de ramassage
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="pickupName"
                control={control}
                rules={{ required: 'Le nom du point de ramassage est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Nom du lieu"
                    fullWidth
                    error={!!errors.pickupName}
                    helperText={errors.pickupName?.message}
                    InputProps={{
                      startAdornment: (
                        <LocationIcon color="action" sx={{ mr: 1 }} />
                      ),
                    }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="pickupContactName"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Nom du contact"
                    fullWidth
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="pickupAddress"
                control={control}
                rules={{ required: 'L'adresse est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Adresse"
                    fullWidth
                    error={!!errors.pickupAddress}
                    helperText={errors.pickupAddress?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="pickupPhone"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Téléphone du contact"
                    fullWidth
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="pickupCity"
                control={control}
                rules={{ required: 'La ville est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Ville"
                    fullWidth
                    error={!!errors.pickupCity}
                    helperText={errors.pickupCity?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="pickupPostalCode"
                control={control}
                rules={{ required: 'Le code postal est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Code postal"
                    fullWidth
                    error={!!errors.pickupPostalCode}
                    helperText={errors.pickupPostalCode?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="pickupCountry"
                control={control}
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
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Point de livraison
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="dropoffName"
                control={control}
                rules={{ required: 'Le nom du point de livraison est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Nom du lieu"
                    fullWidth
                    error={!!errors.dropoffName}
                    helperText={errors.dropoffName?.message}
                    InputProps={{
                      startAdornment: (
                        <LocationIcon color="action" sx={{ mr: 1 }} />
                      ),
                    }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="dropoffContactName"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Nom du contact"
                    fullWidth
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="dropoffAddress"
                control={control}
                rules={{ required: 'L'adresse est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Adresse"
                    fullWidth
                    error={!!errors.dropoffAddress}
                    helperText={errors.dropoffAddress?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="dropoffPhone"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Téléphone du contact"
                    fullWidth
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="dropoffCity"
                control={control}
                rules={{ required: 'La ville est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Ville"
                    fullWidth
                    error={!!errors.dropoffCity}
                    helperText={errors.dropoffCity?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="dropoffPostalCode"
                control={control}
                rules={{ required: 'Le code postal est requis' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Code postal"
                    fullWidth
                    error={!!errors.dropoffPostalCode}
                    helperText={errors.dropoffPostalCode?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="dropoffCountry"
                control={control}
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
          </Grid>
        );
      case 2:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Détails du colis
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="packageDescription"
                control={control}
                rules={{ required: 'La description du colis est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Description du colis"
                    fullWidth
                    multiline
                    rows={3}
                    error={!!errors.packageDescription}
                    helperText={errors.packageDescription?.message}
                    InputProps={{
                      startAdornment: (
                        <PackageIcon color="action" sx={{ mr: 1, alignSelf: 'flex-start', mt: 2 }} />
                      ),
                    }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="packageWeight"
                control={control}
                rules={{ 
                  required: 'Le poids est requis',
                  min: { value: 0.1, message: 'Le poids doit être supérieur à 0' },
                  max: { value: 100, message: 'Le poids ne peut dépasser 100 kg' }
                }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Poids (kg)"
                    type="number"
                    fullWidth
                    error={!!errors.packageWeight}
                    helperText={errors.packageWeight?.message}
                    inputProps={{ min: 0.1, max: 100, step: 0.1 }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="priority"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Priorité</InputLabel>
                    <Select {...field} label="Priorité">
                      <MenuItem value="low">Faible</MenuItem>
                      <MenuItem value="normal">Normale</MenuItem>
                      <MenuItem value="high">Élevée</MenuItem>
                      <MenuItem value="urgent">Urgente</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Controller
                name="requestedPickupTime"
                control={control}
                rules={{ required: 'La date de ramassage est requise' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Date de ramassage souhaitée"
                    type="datetime-local"
                    fullWidth
                    error={!!errors.requestedPickupTime}
                    helperText={errors.requestedPickupTime?.message}
                    InputProps={{
                      startAdornment: (
                        <ScheduleIcon color="action" sx={{ mr: 1 }} />
                      ),
                    }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="isMedicalSpecimen"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} onChange={field.onChange} />}
                    label="Spécimen médical"
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="temperatureControlled"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} onChange={field.onChange} />}
                    label="Contrôle de température"
                  />
                )}
              />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="notes"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Notes supplémentaires"
                    fullWidth
                    multiline
                    rows={3}
                  />
                )}
              />
            </Grid>
          </Grid>
        );
      case 3:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Confirmation
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            <Grid item xs={12}>
              <Paper variant="outlined" sx={{ p: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Résumé de la livraison
                </Typography>
                <Divider sx={{ mb: 2 }} />
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2" color="text.secondary">
                      Client:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('customerName')}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2" color="text.secondary">
                      Téléphone:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('customerPhone')}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Point de ramassage:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('pickupName')}, {watch('pickupAddress')}, {watch('pickupCity')}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Point de livraison:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('dropoffName')}, {watch('dropoffAddress')}, {watch('dropoffCity')}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Typography variant="body2" color="text.secondary">
                      Colis:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('packageDescription')} ({watch('packageWeight')} kg)
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Typography variant="body2" color="text.secondary">
                      Priorité:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {watch('priority') === 'low' ? 'Faible' : 
                       watch('priority') === 'normal' ? 'Normale' :
                       watch('priority') === 'high' ? 'Élevée' : 'Urgente'}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Typography variant="body2" color="text.secondary">
                      Date de ramassage:
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      {format(new Date(watch('requestedPickupTime')), 'dd MMM yyyy à HH:mm')}
                    </Typography>
                  </Grid>
                  {watch('isMedicalSpecimen') && (
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Spécimen médical: Oui
                      </Typography>
                    </Grid>
                  )}
                  {watch('temperatureControlled') && (
                    <Grid item xs={12} md={6}>
                      <Typography variant="body2" color="text.secondary">
                        Contrôle de température: Oui
                      </Typography>
                    </Grid>
                  )}
                  {watch('notes') && (
                    <Grid item xs={12}>
                      <Typography variant="body2" color="text.secondary">
                        Notes:
                      </Typography>
                      <Typography variant="body1" sx={{ mb: 1 }}>
                        {watch('notes')}
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              </Paper>
            </Grid>
          </Grid>
        );
      default:
        return null;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">
          {isEditing ? 'Modifier la livraison' : 'Nouvelle livraison'}
        </Typography>
        <IconButton edge="end" onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent dividers>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {renderStepContent(activeStep)}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isLoading}>
          Annuler
        </Button>
        <Button
          color="inherit"
          disabled={activeStep === 0}
          onClick={handleBack}
          sx={{ mr: 1 }}
        >
          Précédent
        </Button>
        <Button
          onClick={activeStep === steps.length - 1 ? handleSubmit(onSubmit) : handleNext}
          variant="contained"
          disabled={isLoading}
          startIcon={activeStep === steps.length - 1 ? <SaveIcon /> : null}
        >
          {activeStep === steps.length - 1 ? (isLoading ? <CircularProgress size={20} /> : 'Enregistrer') : 'Suivant'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeliveryForm;
