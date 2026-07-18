import React from 'react';
import { Navigate } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from '@shared/context/AuthContext';
import ProtectedRoute from './ProtectedRoute';

function dashboardFor(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

/** Requires authentication and one of the allowed roles. */
export default function RoleBasedRoute({ children, roles = [] }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', minHeight: 240 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <ProtectedRoute>
      {roles.length > 0 && !roles.includes(user?.role) ? (
        <Navigate to={dashboardFor(user?.role)} replace />
      ) : (
        children
      )}
    </ProtectedRoute>
  );
}
