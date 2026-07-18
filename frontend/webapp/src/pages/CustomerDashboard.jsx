import React, { useEffect, useState } from 'react';
import {
  Alert,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { customerApi } from '@shared/api/client';

export default function CustomerDashboard() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    customerApi
      .bookings()
      .then((res) => setBookings(res.data))
      .catch((err) => setError(err.response?.data?.error || 'Failed to load bookings'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Customer dashboard
      </Typography>
      <Paper variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Package</TableCell>
              <TableCell>Destination</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Travelers</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>{b.destination}</TableCell>
                <TableCell>{b.travelDate}</TableCell>
                <TableCell>{b.travelers}</TableCell>
                <TableCell>${Number(b.totalPrice).toFixed(2)}</TableCell>
                <TableCell>{b.status}</TableCell>
              </TableRow>
            ))}
            {bookings.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}>No bookings yet. Browse packages to get started.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </>
  );
}