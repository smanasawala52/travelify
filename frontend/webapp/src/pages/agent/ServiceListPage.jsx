import React, { useState } from 'react';
import {
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Tooltip,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  FilterList as FilterListIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useServices, useDeleteService } from '../../hooks/useTrips';
import { ServiceType, PublishStatus } from '../../constants';

const SERVICE_TYPE_OPTIONS = [
  { value: '', label: 'All Types' },
  { value: 'HOTEL_ROOM', label: 'Hotel Room' },
  { value: 'INSURANCE_PLAN', label: 'Insurance Plan' },
  { value: 'VISA_SERVICE', label: 'Visa Service' },
  { value: 'CUSTOM', label: 'Custom' },
];

const STATUS_OPTIONS = [
  { value: '', label: 'All Status' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PUBLISHED', label: 'Published' },
  { value: 'ARCHIVED', label: 'Archived' },
];

const ServiceStatusChip = ({ status }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'PUBLISHED':
        return 'success';
      case 'DRAFT':
        return 'default';
      case 'ARCHIVED':
        return 'error';
      default:
        return 'default';
    }
  };

  return <Chip label={status} color={getStatusColor(status)} size="small" />;
};

const ServiceTypeChip = ({ type }) => {
  const getTypeLabel = (type) => {
    switch (type) {
      case 'HOTEL_ROOM':
        return 'Hotel';
      case 'INSURANCE_PLAN':
        return 'Insurance';
      case 'VISA_SERVICE':
        return 'Visa';
      case 'CUSTOM':
        return 'Custom';
      default:
        return type;
    }
  };

  return <Chip label={getTypeLabel(type)} size="small" color="primary" />;
};

const ServiceActionMenu = ({ service, onEdit, onDelete }) => (
  <Stack direction="row" spacing={0.5}>
    <Tooltip title="Edit">
      <IconButton size="small" onClick={() => onEdit(service)} color="primary">
        <EditIcon fontSize="small" />
      </IconButton>
    </Tooltip>
    <Tooltip title="Delete">
      <IconButton size="small" onClick={() => onDelete(service)} color="error">
        <DeleteIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  </Stack>
);

export default function ServiceListPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({
    serviceType: '',
    status: '',
    search: '',
    page: 0,
    size: 10,
    sort: 'createdAt,desc',
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [serviceToDelete, setServiceToDelete] = useState(null);

  const { data: servicesData, isLoading, isError, error } = useServices(filters);
  const deleteServiceMutation = useDeleteService();

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value, page: 0 }));
  };

  const handleSearchChange = (e) => {
    setFilters((prev) => ({ ...prev, search: e.target.value, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
  };

  const handleEdit = (service) => {
    navigate(`/provider/services/${service.id}/edit`);
  };

  const handleDeleteClick = (service) => {
    setServiceToDelete(service);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (serviceToDelete) {
      await deleteServiceMutation.mutateAsync(serviceToDelete.id);
      setDeleteDialogOpen(false);
      setServiceToDelete(null);
    }
  };

  const handleCreateNew = () => {
    navigate('/provider/services/create');
  };

  if (isLoading) {
    return (
      <Stack alignItems="center" justifyContent="center" sx={{ py: 8 }}>
        <CircularProgress />
      </Stack>
    );
  }

  if (isError) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography color="error">Error loading services: {error.message}</Typography>
      </Paper>
    );
  }

  const services = servicesData?.content || [];
  const totalElements = servicesData?.totalElements || 0;
  const totalPages = servicesData?.totalPages || 0;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">My Services</Typography>

      {/* Actions Bar */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between">
        <Stack direction="row" spacing={2}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateNew}
          >
            Create New Service
          </Button>
        </Stack>
      </Stack>

      {/* Filters */}
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              size="small"
              label="Search"
              name="search"
              value={filters.search}
              onChange={handleSearchChange}
              InputProps={{
                startAdornment: <SearchIcon color="action" sx={{ mr: 1 }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel>Type</InputLabel>
              <Select
                name="serviceType"
                value={filters.serviceType}
                onChange={handleFilterChange}
                label="Type"
              >
                {SERVICE_TYPE_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel>Status</InputLabel>
              <Select
                name="status"
                value={filters.status}
                onChange={handleFilterChange}
                label="Status"
              >
                {STATUS_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <Button
              fullWidth
              variant="outlined"
              startIcon={<FilterListIcon />}
              onClick={() => setFilters({
                serviceType: '',
                status: '',
                search: '',
                page: 0,
                size: 10,
                sort: 'createdAt,desc',
              })}
            >
              Clear
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Services Table */}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Price</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Currency</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {services.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No services found. Create your first service!
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              services.map((service) => (
                <TableRow key={service.id} hover>
                  <TableCell>
                    <Stack spacing={0.5}>
                      <Typography variant="body2" fontWeight={500}>
                        {service.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {service.description}
                      </Typography>
                    </Stack>
                  </TableCell>
                  <TableCell>
                    <ServiceTypeChip type={service.serviceType} />
                  </TableCell>
                  <TableCell>${service.price}</TableCell>
                  <TableCell>
                    <ServiceStatusChip status={service.status} />
                  </TableCell>
                  <TableCell>{service.currency || 'USD'}</TableCell>
                  <TableCell>
                    {new Date(service.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
                    <ServiceActionMenu
                      service={service}
                      onEdit={handleEdit}
                      onDelete={handleDeleteClick}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      {totalPages > 1 && (
        <Stack direction="row" justifyContent="center" spacing={2}>
          <Button
            variant="outlined"
            disabled={filters.page === 0}
            onClick={() => handlePageChange(filters.page - 1)}
          >
            Previous
          </Button>
          <Typography alignSelf="center">
            Page {filters.page + 1} of {totalPages}
          </Typography>
          <Button
            variant="outlined"
            disabled={filters.page >= totalPages - 1}
            onClick={() => handlePageChange(filters.page + 1)}
          >
            Next
          </Button>
        </Stack>
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          Are you sure you want to delete "{serviceToDelete?.name}"? This action cannot be undone.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} color="secondary">
            Cancel
          </Button>
          <Button
            onClick={handleDeleteConfirm}
            color="error"
            variant="contained"
            disabled={deleteServiceMutation.isPending}
          >
            {deleteServiceMutation.isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
