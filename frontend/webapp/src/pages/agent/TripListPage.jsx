import React, { useState } from 'react';
import {
  Button,
  Chip,
  CircularProgress,
  Grid,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Box,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ContentCopy as CopyIcon,
  FilterList as FilterListIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useTrips, useDeleteTrip, useCopyTrip } from '../../hooks/useTrips';
import { useCategories } from '../../hooks/useTrips';
import { PublishStatus } from '../../constants';

const STATUS_OPTIONS = [
  { value: '', label: 'All Status' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PUBLISHED', label: 'Published' },
  { value: 'ARCHIVED', label: 'Archived' },
];

const TripStatusChip = ({ status }) => {
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

const TripActionMenu = ({ trip, onEdit, onDelete, onCopy }) => (
  <Stack direction="row" spacing={0.5}>
    <Tooltip title="Edit">
      <IconButton size="small" onClick={() => onEdit(trip)} color="primary">
        <EditIcon fontSize="small" />
      </IconButton>
    </Tooltip>
    <Tooltip title="Copy">
      <IconButton size="small" onClick={() => onCopy(trip)} color="secondary">
        <CopyIcon fontSize="small" />
      </IconButton>
    </Tooltip>
    <Tooltip title="Delete">
      <IconButton size="small" onClick={() => onDelete(trip)} color="error">
        <DeleteIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  </Stack>
);

export default function TripListPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({
    status: '',
    categoryId: '',
    search: '',
    featured: '',
    page: 0,
    size: 10,
    sort: 'createdAt,desc',
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [tripToDelete, setTripToDelete] = useState(null);

  const { data: tripsData, isLoading, isError, error } = useTrips(filters);
  const { data: categories = [] } = useCategories();
  const deleteTripMutation = useDeleteTrip();
  const copyTripMutation = useCopyTrip();

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

  const handleSortChange = (sortField) => {
    const newSort = filters.sort?.startsWith(sortField) 
      ? (filters.sort?.startsWith('-') ? sortField : `-${sortField}`)
      : `${sortField},desc`;
    setFilters((prev) => ({ ...prev, sort: newSort }));
  };

  const handleEdit = (trip) => {
    navigate(`/agent/trips/${trip.id}/edit`);
  };

  const handleDeleteClick = (trip) => {
    setTripToDelete(trip);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (tripToDelete) {
      await deleteTripMutation.mutateAsync(tripToDelete.id);
      setDeleteDialogOpen(false);
      setTripToDelete(null);
    }
  };

  const handleCopy = async (trip) => {
    await copyTripMutation.mutateAsync(trip.id);
  };

  const handleCreateNew = () => {
    navigate('/agent/trips/create');
  };

  const handleCreateFromTemplate = () => {
    navigate('/agent/trips/create?from=template');
  };

  const getPriceRange = (trip) => {
    if (!trip.pricing || trip.pricing.length === 0) {
      return 'N/A';
    }
    const prices = trip.pricing.map((p) => p.price || 0);
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    return minPrice === maxPrice 
      ? `$${minPrice}`
      : `$${minPrice} - $${maxPrice}`;
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
        <Typography color="error">Error loading trips: {error.message}</Typography>
      </Paper>
    );
  }

  const trips = tripsData?.content || [];
  const totalElements = tripsData?.totalElements || 0;
  const totalPages = tripsData?.totalPages || 0;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">My Trips</Typography>

      {/* Actions Bar */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between">
        <Stack direction="row" spacing={2}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateNew}
          >
            Create New Trip
          </Button>
          <Button
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={handleCreateFromTemplate}
          >
            Create from Template
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
          <Grid item xs={12} md={2}>
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
            <FormControl fullWidth size="small">
              <InputLabel>Category</InputLabel>
              <Select
                name="categoryId"
                value={filters.categoryId}
                onChange={handleFilterChange}
                label="Category"
              >
                <MenuItem value="">All Categories</MenuItem>
                {categories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Featured</InputLabel>
              <Select
                name="featured"
                value={filters.featured}
                onChange={handleFilterChange}
                label="Featured"
              >
                <MenuItem value="">All</MenuItem>
                <MenuItem value="true">Featured Only</MenuItem>
                <MenuItem value="false">Non-Featured</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <Button
              fullWidth
              variant="outlined"
              startIcon={<FilterListIcon />}
              onClick={() => setFilters({
                status: '',
                categoryId: '',
                search: '',
                featured: '',
                page: 0,
                size: 10,
                sort: 'createdAt,desc',
              })}
            >
              Clear Filters
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Trips Table */}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>
                <Stack direction="row" alignItems="center" spacing={1}>
                  Title
                </Stack>
              </TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Duration</TableCell>
              <TableCell>Price</TableCell>
              <TableCell>Featured</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {trips.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No trips found. Create your first trip!
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              trips.map((trip) => (
                <TableRow key={trip.id} hover>
                  <TableCell>
                    <Stack spacing={0.5}>
                      <Typography variant="body2" fontWeight={500}>
                        {trip.title}
                      </Typography>
                      {trip.template && (
                        <Typography variant="caption" color="text.secondary">
                          From: {trip.template.title}
                        </Typography>
                      )}
                    </Stack>
                  </TableCell>
                  <TableCell>{trip.category?.name || 'N/A'}</TableCell>
                  <TableCell>
                    <TripStatusChip status={trip.status} />
                  </TableCell>
                  <TableCell>{trip.durationDays || 'N/A'} days</TableCell>
                  <TableCell>{getPriceRange(trip)}</TableCell>
                  <TableCell>
                    {trip.isFeatured ? (
                      <Chip label="Yes" color="primary" size="small" />
                    ) : (
                      <Chip label="No" size="small" />
                    )}
                  </TableCell>
                  <TableCell>
                    {new Date(trip.createdAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
                    <TripActionMenu
                      trip={trip}
                      onEdit={handleEdit}
                      onDelete={handleDeleteClick}
                      onCopy={handleCopy}
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
          Are you sure you want to delete "{tripToDelete?.title}"? This action cannot be undone.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} color="secondary">
            Cancel
          </Button>
          <Button
            onClick={handleDeleteConfirm}
            color="error"
            variant="contained"
            disabled={deleteTripMutation.isPending}
          >
            {deleteTripMutation.isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
