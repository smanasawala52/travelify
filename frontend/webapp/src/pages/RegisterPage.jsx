import React, { useState } from 'react';
import { Alert, Button, Stack } from '@mui/material';
import { Link as RouterLink, Navigate, useNavigate } from 'react-router-dom';
import { useForm, useWatch } from 'react-hook-form';
import { useAuth } from '@shared/context/AuthContext';
import AuthForm, { FormTextField, RoleSelect } from '../components/AuthForm';

export default function RegisterPage() {
  const { register: registerUser, isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const { control, handleSubmit } = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'CUSTOMER',
    },
  });

  const passwordValue = useWatch({ control, name: 'password' });

  if (!loading && isAuthenticated) {
    return <Navigate to="/profile" replace />;
  }

  const onSubmit = handleSubmit(async (values) => {
    setBusy(true);
    setError('');
    try {
      await registerUser({
        firstName: values.firstName,
        lastName: values.lastName,
        phone: values.phone || undefined,
        email: values.email,
        password: values.password,
        role: values.role,
      });
      navigate('/login', {
        replace: true,
        state: { registered: true, email: values.email },
      });
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed');
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthForm
      title="Create an account"
      subtitle="Join Travelify as a customer or travel agent. Admin accounts are created by administrators."
      onSubmit={onSubmit}
      control={control}
      busy={busy}
      error={error}
      submitLabel="Register"
      showPasswordStrength
      passwordValue={passwordValue}
      footer={
        <Button component={RouterLink} to="/login">
          Already have an account? Sign in
        </Button>
      }
    >
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <FormTextField
          name="firstName"
          control={control}
          label="First name"
          rules={{ required: 'First name is required' }}
        />
        <FormTextField
          name="lastName"
          control={control}
          label="Last name"
          rules={{ required: 'Last name is required' }}
        />
      </Stack>
      <FormTextField name="phone" control={control} label="Phone (optional)" />
      <FormTextField
        name="email"
        control={control}
        label="Email"
        type="email"
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
        rules={{
          required: 'Password is required',
          minLength: { value: 8, message: 'At least 8 characters' },
          validate: (v) =>
            (/[A-Za-z]/.test(v) && /\d/.test(v)) || 'Include a letter and a digit',
        }}
      />
      <FormTextField
        name="confirmPassword"
        control={control}
        label="Confirm password"
        type="password"
        rules={{
          required: 'Confirm your password',
          validate: (v) => v === passwordValue || 'Passwords do not match',
        }}
      />
      <RoleSelect control={control} />
      <Alert severity="info">
        Admin registration is only available from the admin panel.
      </Alert>
    </AuthForm>
  );
}
