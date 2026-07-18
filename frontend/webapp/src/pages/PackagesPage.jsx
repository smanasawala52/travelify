import React from 'react';
import { Alert, CircularProgress, Grid, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PackageCard from '@shared/components/PackageCard';
import { usePackages } from '@shared/hooks/usePackages';
import { useAuth } from '@shared/context/AuthContext';

export default function PackagesPage() {
  const { packages, loading, error } = usePackages();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const onBook = (pkg) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (user.role !== 'CUSTOMER') {
      navigate(user.role === 'ADMIN' ? '/dashboard/admin' : '/dashboard/agent');
      return;
    }
    navigate(`/book/${pkg.id}`);
  };

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Travel packages
      </Typography>
      <Grid container spacing={2}>
        {packages.map((pkg) => (
          <Grid item xs={12} md={4} key={pkg.id}>
            <PackageCard pkg={pkg} onBook={onBook} />
          </Grid>
        ))}
      </Grid>
    </>
  );
}