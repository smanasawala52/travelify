import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Step,
  StepButton,
  Stepper,
  TextField,
  Typography,
  Switch,
  RadioGroup,
  Radio,
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  ArrowBack as ArrowBackIcon,
  ArrowForward as ArrowForwardIcon,
} from '@mui/icons-material';
import { useForm, useFieldArray, Controller } from 'react-hook-form';
import { useTrips, useTemplates, useCategories, useCreateTrip, useUpdateTrip, useTrip } from '../../hooks/useTrips';
import { PublishStatus, PricingType } from '../../constants';

const STEPS = [
  'Choose Base',
  'Basic Info',
  'Pricing',
  'Itinerary',
  'Departures',
  'Add-ons',
  'Images',
];

const defaultValues = {
  templateId: null,
  title: '',
  slug: '',
  shortDescription: '',
  fullDescription: '',
  featuredImage: '',
  categoryId: null,
  difficulty: '',
  durationDays: 7,
  minAge: 0,
  maxGroupSize: 20,
  isFeatured: false,
  status: PublishStatus.DRAFT,
  pricing: [],
  itinerary: [],
  departures: [],
  images: [],
  serviceIds: [],
};

const difficultyOptions = ['Easy', 'Moderate', 'Challenging', 'Difficult', 'Expert'];

// Step 1: Choose Base (Template or Scratch)
const ChooseBaseStep = ({ control, setValue, watch, errors }) => {
  const { data: templates = [], isLoading } = useTemplates({ size: 50 });
  const fromTemplate = new URLSearchParams(window.location.search).get('from') === 'template';

  const selectedTemplateId = watch('templateId');

  const handleSelectTemplate = (template) => {
    setValue('templateId', template.id);
    setValue('title', template.title + ' (Copy)');
    setValue('slug', template.slug + '-copy');
    setValue('shortDescription', template.shortDescription || '');
    setValue('fullDescription', template.fullDescription || '');
    setValue('categoryId', template.category?.id || null);
    setValue('difficulty', template.difficulty || '');
    setValue('durationDays', template.durationDays || 7);
    setValue('minAge', template.minAge || 0);
    setValue('maxGroupSize', template.maxGroupSize || 20);
    setValue('featuredImage', template.featuredImage || '');
  };

  const handleStartFromScratch = () => {
    setValue('templateId', null);
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Select how you want to create your trip</Typography>
      
      <FormControl component="fieldset">
        <RadioGroup row name="create-method">
          <FormControlLabel
            value="scratch"
            control={<Radio checked={!selectedTemplateId} onChange={handleStartFromScratch} />}
            label="Start from Scratch"
          />
          <FormControlLabel
            value="template"
            control={<Radio checked={!!selectedTemplateId} onChange={() => {}} />}
            label="Use a Template"
          />
        </RadioGroup>
      </FormControl>

      {selectedTemplateId === null && (
        <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.paper' }}>
          <Typography variant="body2" color="text.secondary">
            Create a completely new trip with custom details.
          </Typography>
        </Paper>
      )}

      {selectedTemplateId === null && fromTemplate && (
        <Button
          variant="outlined"
          onClick={() => setValue('templateId', '')}
          startIcon={<ArrowBackIcon />}
        >
          Back to Templates
        </Button>
      )}

      {isLoading && <CircularProgress size={24} />}

      {!isLoading && templates.content && templates.content.length > 0 && selectedTemplateId === null && (
        <Stack spacing={2}>
          <Typography variant="subtitle2">Available Templates:</Typography>
          <Grid container spacing={2}>
            {templates.content.map((template) => (
              <Grid item xs={12} md={6} key={template.id}>
                <Paper
                  variant="outlined"
                  sx={{
                    p: 2,
                    cursor: 'pointer',
                    '&:hover': { boxShadow: 2 },
                    borderColor: selectedTemplateId === template.id ? 'primary.main' : 'divider',
                    bgcolor: selectedTemplateId === template.id ? 'action.selected' : 'background.paper',
                  }}
                  onClick={() => handleSelectTemplate(template)}
                >
                  <Stack spacing={1}>
                    <Typography variant="subtitle1" fontWeight={500}>{template.title}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {template.category?.name} | {template.durationDays} days
                    </Typography>
                    <Chip
                      label={template.status}
                      size="small"
                      color={template.status === PublishStatus.PUBLISHED ? 'success' : 'default'}
                    />
                  </Stack>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </Stack>
      )}

      {selectedTemplateId && (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Stack spacing={1} direction="row" alignItems="center">
            <Typography variant="body2">
              Selected: {templates.content?.find(t => t.id === selectedTemplateId)?.title}
            </Typography>
            <Button size="small" onClick={handleStartFromScratch}>Change</Button>
          </Stack>
        </Paper>
      )}
    </Stack>
  );
};

// Step 2: Basic Info
const BasicInfoStep = ({ control, register, errors, watch }) => {
  const { data: categories = [] } = useCategories();

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Trip Basic Information</Typography>

      <TextField
        label="Title"
        {...register('title', { required: 'Title is required' })}
        error={!!errors.title}
        helperText={errors.title?.message}
        fullWidth
      />

      <TextField
        label="Slug (URL-friendly name)"
        {...register('slug', {
          required: 'Slug is required',
          pattern: {
            value: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
            message: 'Slug must be lowercase alphanumeric with hyphens only',
          },
        })}
        error={!!errors.slug}
        helperText={errors.slug?.message}
        fullWidth
      />

      <TextField
        label="Short Description"
        {...register('shortDescription')}
        multiline
        rows={2}
        fullWidth
      />

      <TextField
        label="Full Description"
        {...register('fullDescription')}
        multiline
        rows={4}
        fullWidth
      />

      <TextField
        label="Featured Image URL"
        {...register('featuredImage')}
        fullWidth
        placeholder="https://example.com/image.jpg"
      />

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Category</InputLabel>
            <Controller
              name="categoryId"
              control={control}
              render={({ field }) => (
                <Select
                  {...field}
                  label="Category"
                  error={!!errors.categoryId}
                >
                  <MenuItem value="">None</MenuItem>
                  {categories.map((category) => (
                    <MenuItem key={category.id} value={category.id}>
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
              )}
            />
          </FormControl>
        </Grid>
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Difficulty</InputLabel>
            <Controller
              name="difficulty"
              control={control}
              render={({ field }) => (
                <Select {...field} label="Difficulty">
                  <MenuItem value="">None</MenuItem>
                  {difficultyOptions.map((option) => (
                    <MenuItem key={option} value={option}>
                      {option}
                    </MenuItem>
                  ))}
                </Select>
              )}
            />
          </FormControl>
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={6} md={4}>
          <TextField
            label="Duration (days)"
            type="number"
            {...register('durationDays', { 
              required: 'Duration is required',
              min: { value: 1, message: 'Minimum 1 day' },
            })}
            error={!!errors.durationDays}
            helperText={errors.durationDays?.message}
            fullWidth
          />
        </Grid>
        <Grid item xs={6} md={4}>
          <TextField
            label="Min Age"
            type="number"
            {...register('minAge', { 
              min: { value: 0, message: 'Cannot be negative' },
            })}
            error={!!errors.minAge}
            helperText={errors.minAge?.message}
            fullWidth
          />
        </Grid>
        <Grid item xs={6} md={4}>
          <TextField
            label="Max Group Size"
            type="number"
            {...register('maxGroupSize', { 
              min: { value: 1, message: 'Minimum 1 person' },
            })}
            error={!!errors.maxGroupSize}
            helperText={errors.maxGroupSize?.message}
            fullWidth
          />
        </Grid>
      </Grid>

      <FormControlLabel
        control={
          <Controller
            name="isFeatured"
            control={control}
            render={({ field }) => (
              <Switch {...field} checked={field.value} />
            )}
          />
        }
        label="Featured Trip"
      />

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

// Step 3: Pricing
const PricingStep = ({ control, register, errors, useFieldArray }) => {
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'pricing',
  });

  const addPricing = () => {
    append({
      pricingType: PricingType.PER_PERSON,
      price: 0,
      currency: 'USD',
      adultPrice: null,
      childPrice: null,
      infantPrice: null,
      minParticipants: null,
      maxParticipants: null,
    });
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Pricing Options</Typography>

      <Typography variant="body2" color="text.secondary">
        Add one or more pricing options for this trip.
      </Typography>

      {fields.length === 0 && (
        <Typography variant="body2" color="warning.main">
          At least one pricing option is required.
        </Typography>
      )}

      <Stack spacing={2}>
        {fields.map((field, index) => (
          <Paper key={field.id} variant="outlined" sx={{ p: 2 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
              <Typography variant="subtitle2">Pricing Option {index + 1}</Typography>
              <IconButton size="small" color="error" onClick={() => remove(index)}>
                <RemoveIcon />
              </IconButton>
            </Stack>

            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <InputLabel>Pricing Type</InputLabel>
                  <Controller
                    name={`pricing.${index}.pricingType`}
                    control={control}
                    render={({ field }) => (
                      <Select {...field} label="Pricing Type" defaultValue={PricingType.PER_PERSON}>
                        <MenuItem value={PricingType.PER_PERSON}>Per Person</MenuItem>
                        <MenuItem value={PricingType.PER_GROUP}>Per Group</MenuItem>
                        <MenuItem value={PricingType.FIXED}>Fixed Price</MenuItem>
                      </Select>
                    )}
                  />
                </FormControl>
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Price"
                  type="number"
                  {...register(`pricing.${index}.price`, { 
                    required: 'Price is required',
                    min: { value: 0, message: 'Price cannot be negative' },
                  })}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  InputProps={{ startAdornment: '$' }}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  label="Currency"
                  {...register(`pricing.${index}.currency`)}
                  fullWidth
                  defaultValue="USD"
                />
              </Grid>

              {watch(`pricing.${index}.pricingType`) === PricingType.PER_PERSON && (
                <>
                  <Grid item xs={12} md={4}>
                    <TextField
                      label="Adult Price"
                      type="number"
                      {...register(`pricing.${index}.adultPrice`)}
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                      InputProps={{ startAdornment: '$' }}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      label="Child Price"
                      type="number"
                      {...register(`pricing.${index}.childPrice`)}
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                      InputProps={{ startAdornment: '$' }}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      label="Infant Price"
                      type="number"
                      {...register(`pricing.${index}.infantPrice`)}
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                      InputProps={{ startAdornment: '$' }}
                    />
                  </Grid>
                </>
              )}

              {watch(`pricing.${index}.pricingType`) === PricingType.PER_GROUP && (
                <>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Min Participants"
                      type="number"
                      {...register(`pricing.${index}.minParticipants`)}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Max Participants"
                      type="number"
                      {...register(`pricing.${index}.maxParticipants`)}
                      fullWidth
                    />
                  </Grid>
                </>
              )}
            </Grid>
          </Paper>
        ))}
      </Stack>

      <Button
        variant="outlined"
        startIcon={<AddIcon />}
        onClick={addPricing}
        fullWidth
      >
        Add Pricing Option
      </Button>
    </Stack>
  );
};

// Step 4: Itinerary
const ItineraryStep = ({ control }) => {
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'itinerary',
  });

  const durationDays = control._defaultValues.durationDays || 7;

  const addDay = () => {
    append({
      dayNumber: fields.length + 1,
      title: '',
      description: '',
      activities: '',
      accommodation: '',
      meals: '',
    });
  };

  // Auto-fill day numbers when adding
  useEffect(() => {
    if (fields.length === 0 && durationDays > 0) {
      for (let i = 1; i <= durationDays; i++) {
        append({
          dayNumber: i,
          title: `Day ${i}`,
          description: '',
          activities: '',
          accommodation: '',
          meals: '',
        });
      }
    }
  }, [fields.length, durationDays, append]);

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Trip Itinerary</Typography>

      <Typography variant="body2" color="text.secondary">
        Add a detailed day-by-day breakdown of the trip.
      </Typography>

      {fields.length === 0 && (
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={addDay}
          fullWidth
        >
          Add Day
        </Button>
      )}

      <Stack spacing={2}>
        {fields.map((field, index) => (
          <Paper key={field.id} variant="outlined" sx={{ p: 2 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
              <Typography variant="subtitle2">
                Day {field.dayNumber || index + 1}
              </Typography>
              <IconButton size="small" color="error" onClick={() => remove(index)}>
                <RemoveIcon />
              </IconButton>
            </Stack>

            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Controller
                  name={`itinerary.${index}.title`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Day Title"
                      fullWidth
                      placeholder={`Day ${index + 1} Title`}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name={`itinerary.${index}.description`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Description"
                      multiline
                      rows={2}
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name={`itinerary.${index}.activities`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Activities"
                      multiline
                      rows={2}
                      fullWidth
                      placeholder="List of activities for this day"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`itinerary.${index}.accommodation`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Accommodation"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`itinerary.${index}.meals`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Meals"
                      fullWidth
                      placeholder="Breakfast, Lunch, Dinner"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </Paper>
        ))}
      </Stack>

      {fields.length > 0 && (
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={addDay}
          fullWidth
        >
          Add Another Day
        </Button>
      )}
    </Stack>
  );
};

// Step 5: Departures
const DeparturesStep = ({ control }) => {
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'departures',
  });

  const durationDays = control._defaultValues.durationDays || 7;

  const addDeparture = () => {
    append({
      departureDate: '',
      endDate: '',
      availableSeats: 20,
      priceOverride: null,
      isCancelled: false,
    });
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Departure Dates</Typography>

      <Typography variant="body2" color="text.secondary">
        Add one or more departure dates for this trip. Each departure can have different pricing and availability.
      </Typography>

      {fields.length === 0 && (
        <Typography variant="body2" color="warning.main">
          At least one departure date is recommended.
        </Typography>
      )}

      <Stack spacing={2}>
        {fields.map((field, index) => (
          <Paper key={field.id} variant="outlined" sx={{ p: 2 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
              <Typography variant="subtitle2">Departure {index + 1}</Typography>
              <IconButton size="small" color="error" onClick={() => remove(index)}>
                <RemoveIcon />
              </IconButton>
            </Stack>

            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`departures.${index}.departureDate`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Departure Date"
                      type="date"
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`departures.${index}.endDate`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="End Date"
                      type="date"
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                      helperText={`Automatically ${durationDays} days after departure`}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`departures.${index}.availableSeats`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Available Seats"
                      type="number"
                      fullWidth
                      defaultValue={20}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name={`departures.${index}.priceOverride`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Price Override (optional)"
                      type="number"
                      fullWidth
                      InputLabelProps={{ shrink: true }}
                      InputProps={{ startAdornment: '$' }}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Controller
                      name={`departures.${index}.isCancelled`}
                      control={control}
                      render={({ field }) => (
                        <Switch {...field} checked={field.value} />
                      )}
                    />
                  }
                  label="Cancelled"
                />
              </Grid>
            </Grid>
          </Paper>
        ))}
      </Stack>

      <Button
        variant="outlined"
        startIcon={<AddIcon />}
        onClick={addDeparture}
        fullWidth
      >
        Add Departure Date
      </Button>
    </Stack>
  );
};

// Step 6: Add-ons (Services)
const AddonsStep = ({ control }) => {
  const { data: services = [], isLoading } = useServices({ size: 100 });
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'serviceIds',
  });

  const selectedServiceIds = watch('serviceIds') || [];

  const addService = (serviceId) => {
    if (!selectedServiceIds.includes(serviceId)) {
      append(serviceId);
    }
  };

  const removeService = (serviceId) => {
    const index = selectedServiceIds.indexOf(serviceId);
    if (index > -1) {
      remove(index);
    }
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Add-on Services</Typography>

      <Typography variant="body2" color="text.secondary">
        Select optional services that customers can add to their booking (hotel rooms, insurance, visas, etc.).
      </Typography>

      {isLoading && <CircularProgress size={24} />}

      {!isLoading && (
        <>
          {selectedServiceIds.length > 0 && (
            <Stack spacing={1}>
              <Typography variant="subtitle2">Selected Services:</Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap">
                {selectedServiceIds.map((serviceId) => {
                  const service = services.content?.find(s => s.id === serviceId);
                  return service ? (
                    <Chip
                      key={serviceId}
                      label={`${service.name} ($${service.price})`}
                      onDelete={() => removeService(serviceId)}
                      color="primary"
                    />
                  ) : null;
                })}
              </Stack>
            </Stack>
          )}

          <Typography variant="subtitle2">Available Services:</Typography>
          <Grid container spacing={2}>
            {services.content?.map((service) => (
              <Grid item xs={12} md={6} key={service.id}>
                <Paper
                  variant="outlined"
                  sx={{
                    p: 2,
                    cursor: 'pointer',
                    '&:hover': { boxShadow: 2 },
                    borderColor: selectedServiceIds.includes(service.id) ? 'primary.main' : 'divider',
                    bgcolor: selectedServiceIds.includes(service.id) ? 'action.selected' : 'background.paper',
                  }}
                  onClick={() => addService(service.id)}
                >
                  <Stack spacing={1}>
                    <Typography variant="subtitle1" fontWeight={500}>{service.name}</Typography>
                    <Typography variant="body2">
                      {service.serviceType.replace('_', ' ')} | ${service.price}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {service.provider?.businessName || service.provider?.firstName}
                    </Typography>
                  </Stack>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </>
      )}
    </Stack>
  );
};

// Step 7: Images
const ImagesStep = ({ control }) => {
  const { fields, append, remove } = useFieldArray({
    control,
    name: 'images',
  });

  const addImage = () => {
    append({
      imageUrl: '',
      isFeatured: false,
      sortOrder: fields.length,
    });
  };

  const handleSetFeatured = (index) => {
    // Set this image as featured, unset others
    fields.forEach((_, i) => {
      control._updateFieldValue(`images.${i}.isFeatured`, i === index);
    });
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h6">Gallery Images</Typography>

      <Typography variant="body2" color="text.secondary">
        Upload images for your trip. The first image will be used as the featured image by default.
      </Typography>

      {fields.length === 0 && (
        <Typography variant="body2" color="warning.main">
          At least one image is recommended.
        </Typography>
      )}

      <Stack spacing={2}>
        {fields.map((field, index) => (
          <Paper key={field.id} variant="outlined" sx={{ p: 2 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
              <Typography variant="subtitle2">Image {index + 1}</Typography>
              <IconButton size="small" color="error" onClick={() => remove(index)}>
                <RemoveIcon />
              </IconButton>
            </Stack>

            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Controller
                  name={`images.${index}.imageUrl`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Image URL"
                      fullWidth
                      placeholder="https://example.com/image.jpg"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                {field.imageUrl && (
                  <Box
                    component="img"
                    src={field.imageUrl}
                    alt="Preview"
                    sx={{
                      width: '100%',
                      maxHeight: 200,
                      objectFit: 'cover',
                      borderRadius: 1,
                      mt: 1,
                    }}
                    onError={(e) => {
                      e.target.style.display = 'none';
                    }}
                  />
                )}
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Controller
                      name={`images.${index}.isFeatured`}
                      control={control}
                      render={({ field }) => (
                        <Switch
                          {...field}
                          checked={field.value}
                          onChange={(e) => {
                            field.onChange(e.target.checked);
                            if (e.target.checked) {
                              handleSetFeatured(index);
                            }
                          }}
                        />
                      )}
                    />
                  }
                  label="Featured Image"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Sort Order"
                  type="number"
                  {...register(`images.${index}.sortOrder`)}
                  fullWidth
                  defaultValue={index}
                />
              </Grid>
            </Grid>
          </Paper>
        ))}
      </Stack>

      <Button
        variant="outlined"
        startIcon={<AddIcon />}
        onClick={addImage}
        fullWidth
      >
        Add Image
      </Button>
    </Stack>
  );
};

// Main Form Component
export default function TripForm({ tripId, onSuccess }) {
  const navigate = useNavigate();
  const { data: existingTrip } = useTrip(tripId);
  const isEditMode = !!tripId;

  const createTripMutation = useCreateTrip();
  const updateTripMutation = useUpdateTrip();

  const { control, handleSubmit, watch, setValue, register } = useForm({
    defaultValues: isEditMode && existingTrip ? mapTripToForm(existingTrip) : defaultValues,
  });

  const [activeStep, setActiveStep] = useState(0);

  // Map existing trip to form values
  function mapTripToForm(trip) {
    return {
      templateId: trip.template?.id || null,
      title: trip.title || '',
      slug: trip.slug || '',
      shortDescription: trip.shortDescription || '',
      fullDescription: trip.fullDescription || '',
      featuredImage: trip.featuredImage || '',
      categoryId: trip.category?.id || null,
      difficulty: trip.difficulty || '',
      durationDays: trip.durationDays || 7,
      minAge: trip.minAge || 0,
      maxGroupSize: trip.maxGroupSize || 20,
      isFeatured: trip.isFeatured || false,
      status: trip.status || PublishStatus.DRAFT,
      pricing: trip.pricing || [],
      itinerary: trip.itinerary || [],
      departures: trip.departures || [],
      images: trip.images || [],
      serviceIds: trip.tripServices?.map(ts => ts.service?.id) || [],
    };
  }

  // Map form to API payload
  function mapFormToPayload(data) {
    return {
      templateId: data.templateId || null,
      title: data.title,
      slug: data.slug,
      shortDescription: data.shortDescription,
      fullDescription: data.fullDescription,
      featuredImage: data.featuredImage,
      categoryId: data.categoryId || null,
      difficulty: data.difficulty,
      durationDays: Number(data.durationDays),
      minAge: Number(data.minAge),
      maxGroupSize: Number(data.maxGroupSize),
      isFeatured: data.isFeatured,
      status: data.status,
      pricing: data.pricing || [],
      itinerary: data.itinerary || [],
      departures: data.departures || [],
      images: data.images || [],
      serviceIds: data.serviceIds || [],
    };
  }

  const onSubmit = async (data) => {
    try {
      const payload = mapFormToPayload(data);
      
      if (isEditMode) {
        await updateTripMutation.mutateAsync({ id: tripId, payload });
      } else {
        await createTripMutation.mutateAsync(payload);
      }
      
      onSuccess?.();
      navigate(isEditMode ? `/agent/trips/${tripId}` : '/agent/trips');
    } catch (error) {
      console.error('Error saving trip:', error);
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
        return <ChooseBaseStep control={control} setValue={setValue} watch={watch} />;
      case 1:
        return <BasicInfoStep control={control} register={register} watch={watch} />;
      case 2:
        return <PricingStep control={control} register={register} watch={watch} />;
      case 3:
        return <ItineraryStep control={control} />;
      case 4:
        return <DeparturesStep control={control} />;
      case 5:
        return <AddonsStep control={control} watch={watch} />;
      case 6:
        return <ImagesStep control={control} register={register} />;
      default:
        return null;
    }
  };

  return (
    <Stack spacing={3}>
      <Typography variant="h4">{isEditMode ? 'Edit Trip' : 'Create New Trip'}</Typography>

      {/* Progress Stepper */}
      <Stepper activeStep={activeStep} alternativeLabel nonLinear>
        {STEPS.map((label, index) => (
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
                startIcon={<ArrowBackIcon />}
                disabled={activeStep === 0}
              >
                Back
              </Button>
            )}

            {activeStep < STEPS.length - 1 ? (
              <Button
                variant="contained"
                onClick={handleNext}
                endIcon={<ArrowForwardIcon />}
              >
                Next
              </Button>
            ) : (
              <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={createTripMutation.isPending || updateTripMutation.isPending}
              >
                {createTripMutation.isPending || updateTripMutation.isPending ? (
                  <CircularProgress size={20} color="inherit" />
                ) : (
                  isEditMode ? 'Update Trip' : 'Create Trip'
                )}
              </Button>
            )}
          </Stack>
        </Stack>
      </form>
    </Stack>
  );
}
