import React, { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  CircularProgress,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { agentApi } from '@shared/api/client';
import PackageCard from '@shared/components/PackageCard';

const emptyForm = {
  title: '',
  description: '',
  destination: '',
  price: '',
  durationDays: 5,
  active: true,
};

export default function AgentDashboard() {
  const [packages, setPackages] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const refresh = async () => {
    const [pkgRes, bookRes] = await Promise.all([agentApi.packages(), agentApi.bookings()]);
    setPackages(pkgRes.data);
    setBookings(bookRes.data);
  };

  useEffect(() => {
    refresh()
      .catch((err) => setError(err.response?.data?.error || 'Failed to load agent data'))
      .finally(() => setLoading(false));
  }, []);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await agentApi.createPackage({
        ...form,
        price: Number(form.price),
        durationDays: Number(form.durationDays),
        active: true,
      });
      setForm(emptyForm);
      await refresh();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create package');
    }
  };

  const onStatus = async (id, status) => {
    await agentApi.updateBookingStatus(id, status);
    await refresh();
  };

  if (loading) return <CircularProgress />;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Travel agent dashboard</Typography>
      {error && <Alert severity="error">{error}</Alert>}

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Create package
        </Typography>
        <Stack component="form" spacing={2} onSubmit={onCreate}>
          <TextField name="title" label="Title" value={form.title} onChange={onChange} required />
          <TextField name="destination" label="Destination" value={form.destination} onChange={onChange} required />
          <TextField name="description" label="Description" value={form.description} onChange={onChange} multiline minRows={2} />
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField name="price" label="Price" type="number" value={form.price} onChange={onChange} required fullWidth />
            <TextField name="durationDays" label="Days" type="number" value={form.durationDays} onChange={onChange} required fullWidth />
          </Stack>
          <Button type="submit" variant="contained">
            Add package
          </Button>
        </Stack>
      </Paper>

      <Typography variant="h6">Packages</Typography>
      <Grid container spacing={2}>
        {packages.map((pkg) => (
          <Grid item xs={12} md={4} key={pkg.id}>
            <PackageCard pkg={pkg} />
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6">Bookings</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Customer</TableCell>
              <TableCell>Package</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.customerEmail}</TableCell>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>{b.status}</TableCell>
                <TableCell>
                  <TextField
                    select
                    size="small"
                    value={b.status}
                    onChange={(e) => onStatus(b.id, e.target.value)}
                    sx={{ minWidth: 140 }}
                  >
                    {['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'].map((s) => (
                      <MenuItem key={s} value={s}>
                        {s}
                      </MenuItem>
                    ))}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  );
}