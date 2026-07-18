import React, { useState } from 'react';
import { Alert, Button } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { authApi } from '../api/api';
import AuthForm, { FormTextField } from '../components/AuthForm';

export default function ForgotPasswordPage() {
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const { control, handleSubmit } = useForm({ defaultValues: { email: '' } });

  const onSubmit = handleSubmit(async ({ email }) => {
    setBusy(true);
    setError('');
    setMessage('');
    try {
      const { data } = await authApi.forgotPassword(email);
      setMessage(data.message || 'If an account exists, a reset link has been sent.');
    } catch (err) {
      setError(err.response?.data?.error || 'Request failed');
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthForm
      title="Forgot password"
      subtitle="Enter your email and we will send a reset link (logged in the backend console in development)."
      onSubmit={onSubmit}
      control={control}
      busy={busy}
      error={error}
      submitLabel="Send reset link"
      footer={
        <Button component={RouterLink} to="/login">
          Back to login
        </Button>
      }
    >
      {message && <Alert severity="success">{message}</Alert>}
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
    </AuthForm>
  );
}
