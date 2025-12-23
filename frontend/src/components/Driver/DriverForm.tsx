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
  Avatar,
} from '@mui/material';
import {
  Close as CloseIcon,
  Save as SaveIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  DirectionsCar as CarIcon,
  CameraAlt as CameraIcon,
  UploadFile as UploadIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../hooks/redux';
import { createDriver, updateDriver } from '../../store/slices/driverSlice';
import { Driver } from '../../types/delivery';

interface DriverFormProps {
  open: boolean;
  onClose: () => void;
  driver?: Driver | null;
}

interface FormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
  vehicleType: string;
  vehicleMake: string;
  vehicleModel: string;
  vehicleYear: string;
  vehiclePlate: string;
  vehicleColor: string;
  licenseNumber: string;
  licenseExpiryDate: string;
  profileImage?: File;
  licenseImage?: File;
  vehicleRegistrationImage?: File;
  insuranceImage?: File;
  notes?: string;
}

const DriverForm: React.FC<DriverFormProps> = ({
  open,
  onClose,
  driver,
}) => {
  const dispatch = useAppDispatch();
  const { isLoading, error } = useAppSelector(state => state.driver);
  const [isEditing, setIsEditing] = useState(false);
  const [profileImagePreview, setProfileImagePreview] = useState<string | null>(null);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch,
  } = useForm<FormData>({
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      postalCode: '',
      country: 'France',
      vehicleType: 'van',
      vehicleMake: '',
      vehicleModel: '',
      vehicleYear: new Date().getFullYear().toString(),
      vehiclePlate: '',
      vehicleColor: '',
      licenseNumber: '',
      licenseExpiryDate: '',
      notes: '',
    },
  });

  useEffect(() => {
    if (open && driver) {
      setIsEditing(true);
      // Populate form with driver data
      setValue('firstName', driver.firstName);
      setValue('lastName', driver.lastName);
      setValue('email', driver.email);
      setValue('phone', driver.phone);
      setValue('address', driver.address || '');
      setValue('city', driver.city || '');
      setValue('postalCode', driver.postalCode || '');
      setValue('country', driver.country || 'France');
      setValue('vehicleType', driver.vehicleType);
      setValue('vehicleMake', driver.vehicleMake || '');
      setValue('vehicleModel', driver.vehicleModel || '');
      setValue('vehicleYear', driver.vehicleYear || '');
      setValue('vehiclePlate', driver.vehiclePlate);
      setValue('vehicleColor', driver.vehicleColor || '');
      setValue('licenseNumber', driver.licenseNumber || '');
      setValue('licenseExpiryDate', driver.licenseExpiryDate || '');
      setValue('notes', driver.notes || '');

      if (driver.profileImage) {
        setProfileImagePreview(driver.profileImage);
      }
    } else if (open) {
      setIsEditing(false);
      reset();
      setProfileImagePreview(null);
    }
  }, [open, driver, setValue, reset]);

  const handleProfileImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      setValue('profileImage', file);

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setProfileImagePreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const onSubmit = (data: FormData) => {
    const formData = new FormData();

    // Append all form fields
    Object.entries(data).forEach(([key, value]) => {
      if (value instanceof File) {
        formData.append(key, value);
      } else if (value !== undefined && value !== null) {
        formData.append(key, value.toString());
      }
    });

    if (isEditing && driver) {
      dispatch(updateDriver({ id: driver.id, driver: formData }))
        .unwrap()
        .then(() => {
          onClose();
        });
    } else {
      dispatch(createDriver(formData))
        .unwrap()
        .then(() => {
          onClose();
        });
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">
          {isEditing ? 'Modifier le chauffeur' : 'Ajouter un chauffeur'}
        </Typography>
        <Button onClick={onClose} color="inherit">
          <CloseIcon />
        </Button>
      </DialogTitle>
      <DialogContent dividers>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
          <Avatar
            src={profileImagePreview}
            sx={{ width: 120, height: 120, bgcolor: 'primary.main' }}
          >
            {!profileImagePreview && (
              <PersonIcon sx={{ fontSize: 60 }} />
            )}
          </Avatar>
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
          <Button
            variant="outlined"
            component="label"
            startIcon={<CameraIcon />}
          >
            Télécharger une photo
            <input
              type="file"
              accept="image/*"
              hidden
              onChange={handleProfileImageChange}
            />
          </Button>
        </Box>

        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Informations personnelles
            </Typography>
            <Divider sx={{ mb: 2 }} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="firstName"
              control={control}
              rules={{ required: 'Le prénom est requis' }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Prénom"
                  fullWidth
                  error={!!errors.firstName}
                  helperText={errors.firstName?.message}
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
              name="lastName"
              control={control}
              rules={{ required: 'Le nom est requis' }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Nom"
                  fullWidth
                  error={!!errors.lastName}
                  helperText={errors.lastName?.message}
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="email"
              control={control}
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
                  error={!!errors.email}
                  helperText={errors.email?.message}
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
              control={control}
              rules={{ required: 'Le téléphone est requis' }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Téléphone"
                  fullWidth
                  error={!!errors.phone}
                  helperText={errors.phone?.message}
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
              control={control}
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
              control={control}
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
              control={control}
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
            <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
              Informations véhicule
            </Typography>
            <Divider sx={{ mb: 2 }} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehicleType"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth>
                  <InputLabel>Type de véhicule</InputLabel>
                  <Select {...field} label="Type de véhicule">
                    <MenuItem value="motorcycle">Moto</MenuItem>
                    <MenuItem value="car">Voiture</MenuItem>
                    <MenuItem value="van">Fourgon</MenuItem>
                    <MenuItem value="truck">Camion</MenuItem>
                  </Select>
                </FormControl>
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehiclePlate"
              control={control}
              rules={{ required: 'La plaque d'immatriculation est requise' }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Plaque d'immatriculation"
                  fullWidth
                  error={!!errors.vehiclePlate}
                  helperText={errors.vehiclePlate?.message}
                  InputProps={{
                    startAdornment: (
                      <CarIcon color="action" sx={{ mr: 1 }} />
                    ),
                  }}
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehicleMake"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Marque"
                  fullWidth
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehicleModel"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Modèle"
                  fullWidth
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehicleYear"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Année"
                  type="number"
                  fullWidth
                  inputProps={{ min: 1990, max: new Date().getFullYear() + 1 }}
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="vehicleColor"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Couleur"
                  fullWidth
                />
              )}
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
              Documents
            </Typography>
            <Divider sx={{ mb: 2 }} />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="licenseNumber"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Numéro de permis"
                  fullWidth
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="licenseExpiryDate"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Date d'expiration du permis"
                  type="date"
                  fullWidth
                  InputLabelProps={{ shrink: true }}
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
                  label="Notes"
                  multiline
                  rows={3}
                  fullWidth
                />
              )}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="inherit">
          Annuler
        </Button>
        <Button
          onClick={handleSubmit(onSubmit)}
          variant="contained"
          disabled={isLoading}
          startIcon={isLoading ? <CircularProgress size={20} /> : <SaveIcon />}
        >
          {isEditing ? 'Mettre à jour' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DriverForm;
