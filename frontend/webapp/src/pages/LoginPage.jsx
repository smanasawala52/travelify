import React, { useState } from 'react';
import { Button, Stack } from '@mui/material';
import { Link as RouterLink, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '@shared/context/AuthContext';
import AuthForm, { FormTextField } from '../components/AuthForm';

function dashboardFor(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

export default function LoginPage() {
  const { login, isAuthenticated, user, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const { control, handleSubmit } = useForm({
    defaultValues: {
      email: 'customer@travelify.com',
      password: 'password123',
    },
  });

  if (!loading && isAuthenticated) {
    const dest = location.state?.from?.pathname || dashboardFor(user?.role);
    return <Navigate to={dest} replace />;
  }

  const onSubmit = handleSubmit(async (values) => {
    setBusy(true);
    setError('');
    try {
      const auth = await login(values.email, values.password);
      const role = auth.user?.role || auth.role;
      navigate(location.state?.from?.pathname || dashboardFor(role), { replace: true });
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed');
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthForm
      title="Sign in"
      subtitle="Welcome back to Travelify — manage trips, bookings, and your account."
      onSubmit={onSubmit}
      control={control}
      busy={busy}
      error={error}
      submitLabel="Login"
      footer={
        <Stack spacing={1}>
          <Button component={RouterLink} to="/forgot-password">
            Forgot password?
          </Button>
          <Button component={RouterLink} to="/register">
            Create an account
          </Button>
        </Stack>
      }
    >
      <FormTextField
        name="email"
        control={control}
        label="Email"
        type="email"
        autoComplete="email"
        rules={{
          required: 'Email is required',
          pattern: {
            value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            message: 'Enter a valid email',
          },
        }}
      />
      <FormTextField
        name="password"
        control={control}
        label="Password"
        type="password"
        autoComplete="current-password"
        rules={{ required: 'Password is required' }}
      />
    </AuthForm>
  );
}
