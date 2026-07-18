import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { adminApi } from '../api/api';

const ROLES = ['CUSTOMER', 'AGENT', 'ADMIN'];

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [role, setRole] = useState('');
  const [isActive, setIsActive] = useState('');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = { page, size, sort: 'id,asc' };
      if (role) params.role = role;
      if (isActive !== '') params.isActive = isActive === 'true';
      if (search.trim()) params.search = search.trim();
      const { data } = await adminApi.users(params);
      setUsers(data.content || []);
      setTotal(data.totalElements ?? 0);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [page, size, role, isActive, search]);

  useEffect(() => {
    load();
  }, [load]);

  const onRoleChange = async (id, nextRole) => {
    setNotice('');
    try {
      await adminApi.updateUserRole(id, nextRole);
      setNotice('Role updated');
      await load();
    } catch (err) {
      setError(err.response?.data?.error || 'Role update failed');
    }
  };

  const onStatusToggle = async (id, nextActive) => {
    setNotice('');
    try {
      await adminApi.updateUserStatus(id, nextActive);
      setNotice(nextActive ? 'User enabled' : 'User disabled');
      await load();
    } catch (err) {
      setError(err.response?.data?.error || 'Status update failed');
    }
  };

  return (
    <Stack spacing={2}>
      <Typography variant="h4">User management</Typography>
      <Typography variant="body2" color="text.secondary">
        WordPress-style role and status administration for Travelify accounts.
      </Typography>

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          <TextField
            label="Search"
            size="small"
            value={search}
            onChange={(e) => {
              setPage(0);
              setSearch(e.target.value);
            }}
            fullWidth
          />
          <FormControl size="small" sx={{ minWidth: 140 }}>
            <InputLabel>Role</InputLabel>
            <Select
              label="Role"
              value={role}
              onChange={(e) => {
                setPage(0);
                setRole(e.target.value);
              }}
            >
              <MenuItem value="">All</MenuItem>
              {ROLES.map((r) => (
                <MenuItem key={r} value={r}>
                  {r}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 140 }}>
            <InputLabel>Status</InputLabel>
            <Select
              label="Status"
              value={isActive}
              onChange={(e) => {
                setPage(0);
                setIsActive(e.target.value);
              }}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="true">Active</MenuItem>
              <MenuItem value="false">Disabled</MenuItem>
            </Select>
          </FormControl>
        </Stack>
      </Paper>

      {error && <Alert severity="error">{error}</Alert>}
      {notice && <Alert severity="success">{notice}</Alert>}

      <Paper variant="outlined">
        {loading ? (
          <Box sx={{ p: 4, display: 'grid', placeItems: 'center' }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Active</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell>{u.fullName}</TableCell>
                    <TableCell>{u.email}</TableCell>
                    <TableCell>
                      <FormControl size="small" sx={{ minWidth: 130 }}>
                        <Select
                          value={u.role}
                          onChange={(e) => onRoleChange(u.id, e.target.value)}
                        >
                          {ROLES.map((r) => (
                            <MenuItem key={r} value={r}>
                              {r}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </TableCell>
                    <TableCell>
                      <Switch
                        checked={!!u.isActive}
                        onChange={(e) => onStatusToggle(u.id, e.target.checked)}
                        color="primary"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={u.isActive ? 'Active' : 'Disabled'}
                        color={u.isActive ? 'success' : 'default'}
                      />
                    </TableCell>
                  </TableRow>
                ))}
                {users.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5}>No users found</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={total}
              page={page}
              onPageChange={(_, p) => setPage(p)}
              rowsPerPage={size}
              onRowsPerPageChange={(e) => {
                setSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
              rowsPerPageOptions={[5, 10, 20, 50]}
            />
          </>
        )}
      </Paper>
    </Stack>
  );
}
