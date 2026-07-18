import React, { useState } from 'react';
import {
  Box,
  Button,
  Chip,
  Checkbox,
  CircularProgress,
  FormControl,
  FormLabel,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Radio,
  RadioGroup,
  Select,
  Stack,
  Step,
  StepButton,
  Stepper,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import { Add as AddIcon, Remove as RemoveIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useCreateService, useUpdateService } from '../../hooks/useTrips';
import { ServiceType, PublishStatus } from '../../constants';

const SERVICE_STEPS = [
  'Basic Info',
  'Details & Metadata',
];

const defaultServiceValues = {
  serviceType: ServiceType.HOTEL_ROOM,
  name: '',
  description: '',
  price: 0,
  currency: 'USD',
  meta: {},
  status: PublishStatus.DRAFT,
};

// Meta fields configuration for each service type
const SERVICE_TYPE_META = {
  [ServiceType.HOTEL_ROOM]: [
    { name: 'bedType', label: 'Bed Type', type: 'select', options: ['Single', 'Double', 'Queen', 'King', 'Twin'] },
    { name: 'maxOccupancy', label: 'Max Occupancy', type: 'number' },
    { name: 'roomSize', label: 'Room Size (sqm)', type: 'number' },
    { name: 'amenities', label: 'Amenities', type: 'chips', options: ['WiFi', 'Breakfast', 'Air Conditioning', 'TV', 'Mini-bar', 'Balcony'] },
  ],
  [ServiceType.INSURANCE_PLAN]: [
    { name: 'coverageAmount', label: 'Coverage Amount ($)', type: 'number' },
    { name: 'durationDays', label: 'Duration (days)', type: 'number' },
    { name: 'ageLimit', label: 'Age Limit', type: 'number' },
    { name: 'coverageType', label: 'Coverage Type', type: 'multiselect', options: ['Medical', 'Trip Cancellation', 'Baggage', 'Emergency Evacuation', 'Flight Delay'] },
  ],
  [ServiceType.VISA_SERVICE]: [
    { name: 'processingTime', label: 'Processing Time (days)', type: 'number' },
    { name: 'validity', label: 'Validity (days)', type: 'number' },
    { name: 'entryType', label: 'Entry Type', type: 'select', options: ['Single', 'Multiple'] },
    { name: 'requiredDocuments', label: 'Required Documents', type: 'chips', options: ['Passport', 'Photo', 'Application Form', 'Invitation Letter', 'Travel Itinerary'] },
    { name: 'visaCountry', label: 'Visa Country', type: 'text' },
  ],
  [ServiceType.CUSTOM]: [
    { name: 'customField1', label: 'Custom Field 1', type: 'text' },
    { name: 'customField2', label: 'Custom Field 2', type: 'text' },
  ],
};

// Step 1: Basic Info
const BasicInfoStep = ({ control }) => {
  return (
    <Stack spacing={3}>
      <Typography variant="h6">Service Basic Information</Typography>

      <FormControl fullWidth>
        <InputLabel>Service Type</InputLabel>
        <Controller
          name="serviceType"
          control={control}
          render={({ field }) => (
            <Select {...field} label="Service Type">
              <MenuItem value={ServiceType.HOTEL_ROOM}>Hotel Room</MenuItem>
              <MenuItem value={ServiceType.INSURANCE_PLAN}>Insurance Plan</MenuItem>
              <MenuItem value={ServiceType.VISA_SERVICE}>Visa Service</MenuItem>
              <MenuItem value={ServiceType.CUSTOM}>Custom</MenuItem>
            </Select>
          )}
        />
      </FormControl>

      <Controller
        name="name"
        control={control}
        rules={{ required: 'Name is required' }}
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label="Name"
            error={!!fieldState.error}
            helperText={fieldState.error?.message}
            fullWidth
          />
        )}
      />

      <Controller
        name="description"
        control={control}
        render={({ field }) => (
          <TextField
            {...field}
            label="Description"
            multiline
            rows={3}
            fullWidth
          />
        )}
      />

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Controller
            name="price"
            control={control}
            rules={{ 
              required: 'Price is required',
              min: { value: 0, message: 'Price cannot be negative' },
            }}
            render={({ field, fieldState }) => (
              <TextField
                {...field}
                label="Price"
                type="number"
                error={!!fieldState.error}
                helperText={fieldState.error?.message}
                fullWidth
                InputProps={{ startAdornment: '$' }}
                onChange={(e) => field.onChange(Number(e.target.value))}
              />
            )}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <Controller
            name="currency"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                label="Currency"
                fullWidth
              />
            )}
          />
        </Grid>
      </Grid>

      <FormControl fullWidth>
        <InputLabel>Status</InputLabel>
        <Controller
          name="status"
          control={control}
          render={({ field }) => (
            <Select {...field} label="Status">
              <MenuItem value={PublishStatus.DRAFT}>Draft</MenuItem>
              <MenuItem value={PublishStatus.PUBLISHED}>Published</MenuItem>
            </Select>
          )}
        />
      </FormControl>
    </Stack>
  );
};

// Step 2: Details & Metadata
const DetailsStep = ({ control, watch }) => {
  const serviceType = watch('serviceType');
  const metaFields = SERVICE_TYPE_META[serviceType] || [];

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Service Metadata</Typography>
      <Typography variant="body2" color="text.secondary">
        Add type-specific metadata for your service.
      </Typography>

      {metaFields.length === 0 && (
        <Typography variant="body2" color="text.secondary">
          No metadata fields defined for this service type.
        </Typography>
      )}

      {metaFields.map((fieldConfig) => {
        switch (fieldConfig.type) {
          case 'text':
            return (
              <TextField
                key={fieldConfig.name}
                label={fieldConfig.label}
                fullWidth
                Controller
                name={`meta.${fieldConfig.name}`}
                control={control}
                render={({ field }) => (
                  <TextField {...field} label={fieldConfig.label} fullWidth />
                )}
              />
            );

          case 'number':
            return (
              <Controller
                key={fieldConfig.name}
                name={`meta.${fieldConfig.name}`}
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label={fieldConfig.label}
                    type="number"
                    fullWidth
                    onChange={(e) => field.onChange(Number(e.target.value))}
                  />
                )}
              />
            );

          case 'select':
            return (
              <FormControl key={fieldConfig.name} fullWidth>
                <InputLabel>{fieldConfig.label}</InputLabel>
                <Controller
                  name={`meta.${fieldConfig.name}`}
                  control={control}
                  render={({ field }) => (
                    <Select {...field} label={fieldConfig.label}>
                      {fieldConfig.options.map((option) => (
                        <MenuItem key={option} value={option}>
                          {option}
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
              </FormControl>
            );

          case 'multiselect':
            return (
              <FormControl key={fieldConfig.name} fullWidth>
                <FormLabel>{fieldConfig.label}</FormLabel>
                <Controller
                  name={`meta.${fieldConfig.name}`}
                  control={control}
                  defaultValue={[]}
                  render={({ field }) => (
                    <Select
                      {...field}
                      multiple
                      label={fieldConfig.label}
                      renderValue={(selected) => (
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                          {(selected || []).map((value) => (
                            <Chip key={value} label={value} size="small" />
                          ))}
                        </Box>
                      )}
                      onChange={(e) => field.onChange(e.target.value)}
                    >
                      {fieldConfig.options.map((option) => (
                        <MenuItem key={option} value={option}>
                          <Checkbox checked={(field.value || []).includes(option)} />
                          <Typography>{option}</Typography>
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
              </FormControl>
            );

          case 'chips':
            return (
              <Controller
                key={fieldConfig.name}
                name={`meta.${fieldConfig.name}`}
                control={control}
                defaultValue={[]}
                render={({ field }) => (
                  <Box>
                    <Typography variant="subtitle2">{fieldConfig.label}</Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mt: 1 }}>
                      {fieldConfig.options.map((option) => (
                        <Chip
                          key={option}
                          label={option}
                          clickable
                          color={(field.value || []).includes(option) ? 'primary' : 'default'}
                          onClick={() => {
                            const current = field.value || [];
                            const newValue = current.includes(option)
                              ? current.filter((v) => v !== option)
                              : [...current, option];
                            field.onChange(newValue);
                          }}
                        />
                      ))}
                    </Stack>
                  </Box>
                )}
              />
            );

          default:
            return null;
        }
      })}
    </Stack>
  );
};

// Main Service Form Component
export default function ServiceForm({ serviceId, service: existingService, onSuccess }) {
  const navigate = useNavigate();
  const isEditMode = !!serviceId;

  const createServiceMutation = useCreateService();
  const updateServiceMutation = useUpdateService();

  // Map existing service to form values
  const mapServiceToForm = (service) => {
    if (!service) return defaultServiceValues;
    
    return {
      serviceType: service.serviceType || ServiceType.HOTEL_ROOM,
      name: service.name || '',
      description: service.description || '',
      price: service.price || 0,
      currency: service.currency || 'USD',
      meta: service.meta || {},
      status: service.status || PublishStatus.DRAFT,
    };
  };

  // Map form to API payload
  const mapFormToPayload = (data) => {
    return {
      serviceType: data.serviceType,
      name: data.name,
      description: data.description,
      price: Number(data.price),
      currency: data.currency,
      meta: data.meta || {},
      status: data.status,
    };
  };

  const { control, handleSubmit, watch } = useForm({
    defaultValues: isEditMode ? mapServiceToForm(existingService) : defaultServiceValues,
  });

  const [activeStep, setActiveStep] = useState(0);

  const onSubmit = async (data) => {
    try {
      const payload = mapFormToPayload(data);
      
      if (isEditMode) {
        await updateServiceMutation.mutateAsync({ id: serviceId, payload });
      } else {
        await createServiceMutation.mutateAsync(payload);
      }
      
      onSuccess?.();
      navigate('/provider/services');
    } catch (error) {
      console.error('Error saving service:', error);
    }
  };

  const handleNext = () => {
    setActiveStep((prev) => prev + 1);
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const handleStepChange = (step) => {
    setActiveStep(step);
  };

  // Render appropriate step
  const renderStep = () => {
    switch (activeStep) {
      case 0:
        return <BasicInfoStep control={control} />;
      case 1:
        return <DetailsStep control={control} watch={watch} />;
      default:
        return null;
    }
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h4">{isEditMode ? 'Edit Service' : 'Create New Service'}</Typography>

      {/* Progress Stepper */}
      <Stepper activeStep={activeStep} alternativeLabel nonLinear>
        {SERVICE_STEPS.map((label, index) => (
          <Step key={label} completed={activeStep > index}>
            <StepButton onClick={() => handleStepChange(index)} color="inherit">
              {label}
            </StepButton>
          </Step>
        ))}
      </Stepper>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Stack spacing={3}>
          {renderStep()}

          {/* Navigation Buttons */}
          <Stack direction="row" spacing={2} justifyContent="flex-end">
            {activeStep > 0 && (
              <Button
                variant="outlined"
                onClick={handleBack}
                disabled={activeStep === 0}
              >
                Back
              </Button>
            )}

            {activeStep < SERVICE_STEPS.length - 1 ? (
              <Button
                variant="contained"
                onClick={handleNext}
              >
                Next
              </Button>
            ) : (
              <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={createServiceMutation.isPending || updateServiceMutation.isPending}
              >
                {createServiceMutation.isPending || updateServiceMutation.isPending ? (
                  <CircularProgress size={20} color="inherit" />
                ) : (
                  isEditMode ? 'Update Service' : 'Create Service'
                )}
              </Button>
            )}
          </Stack>
        </Stack>
      </form>
    </Stack>
  );
}
