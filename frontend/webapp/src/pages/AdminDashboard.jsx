import React, { useEffect, useState } from 'react';
import {
  Alert,
  CircularProgress,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { adminApi } from '@shared/api/client';

export default function AdminDashboard() {
  const [overview, setOverview] = useState(null);
  const [users, setUsers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([adminApi.overview(), adminApi.users({ size: 50 }), adminApi.bookings()])
      .then(([o, u, b]) => {
        setOverview(o.data);
        setUsers(Array.isArray(u.data) ? u.data : u.data?.content || []);
        setBookings(b.data);
      })
      .catch((err) => setError(err.response?.data?.error || 'Failed to load admin data'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Admin dashboard</Typography>
      <Grid container spacing={2}>
        {['users', 'packages', 'bookings'].map((key) => (
          <Grid item xs={12} md={4} key={key}>
            <Paper variant="outlined" sx={{ p: 2 }}>
              <Typography variant="overline">{key}</Typography>
              <Typography variant="h4">{overview?.[key] ?? 0}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6">Users</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.id}>
                <TableCell>{u.fullName || `${u.firstName || ''} ${u.lastName || ''}`.trim()}</TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>{u.role}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <Typography variant="h6">All bookings</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Customer</TableCell>
              <TableCell>Package</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.customerEmail}</TableCell>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>${Number(b.totalPrice).toFixed(2)}</TableCell>
                <TableCell>{b.status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  );
}