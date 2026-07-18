import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Button,
  CircularProgress,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { customerApi, packageApi } from '@shared/api/client';

export default function BookingPage() {
  const { packageId } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [travelDate, setTravelDate] = useState('');
  const [travelers, setTravelers] = useState(1);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    packageApi
      .get(packageId)
      .then((res) => setPkg(res.data))
      .catch(() => setError('Package not found'));
  }, [packageId]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await customerApi.createBooking({
        packageId: Number(packageId),
        travelDate,
        travelers: Number(travelers),
      });
      navigate('/dashboard/customer');
    } catch (err) {
      setError(err.response?.data?.error || 'Booking failed');
    } finally {
      setBusy(false);
    }
  };

  if (!pkg && !error) return <CircularProgress />;

  return (
    <Paper sx={{ p: 3, maxWidth: 520 }} variant="outlined">
      <Typography variant="h5" gutterBottom>
        Book: {pkg?.title}
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        {pkg?.destination} Â· ${Number(pkg?.price || 0).toFixed(2)} / person
      </Typography>
      <Stack component="form" spacing={2} onSubmit={onSubmit} sx={{ mt: 2 }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField
          label="Travel date"
          type="date"
          InputLabelProps={{ shrink: true }}
          value={travelDate}
          onChange={(e) => setTravelDate(e.target.value)}
          required
          fullWidth
        />
        <TextField
          label="Travelers"
          type="number"
          inputProps={{ min: 1 }}
          value={travelers}
          onChange={(e) => setTravelers(e.target.value)}
          required
          fullWidth
        />
        <Button type="submit" variant="contained" disabled={busy}>
          {busy ? 'Bookingâ€¦' : 'Confirm booking'}
        </Button>
      </Stack>
    </Paper>
  );
}