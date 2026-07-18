import React, { useState } from 'react';
import { Alert, Button } from '@mui/material';
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm, useWatch } from 'react-hook-form';
import { authApi } from '../api/api';
import AuthForm, { FormTextField } from '../components/AuthForm';

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const tokenFromUrl = params.get('token') || '';
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const { control, handleSubmit } = useForm({
    defaultValues: {
      token: tokenFromUrl,
      newPassword: '',
      confirmPassword: '',
    },
  });

  const passwordValue = useWatch({ control, name: 'newPassword' });

  const onSubmit = handleSubmit(async (values) => {
    setBusy(true);
    setError('');
    try {
      await authApi.resetPassword(values.token, values.newPassword);
      navigate('/login', { replace: true, state: { reset: true } });
    } catch (err) {
      setError(err.response?.data?.error || 'Reset failed');
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthForm
      title="Reset password"
      subtitle="Choose a new password for your Travelify account."
      onSubmit={onSubmit}
      control={control}
      busy={busy}
      error={error}
      submitLabel="Update password"
      showPasswordStrength
      passwordValue={passwordValue}
      footer={
        <Button component={RouterLink} to="/login">
          Back to login
        </Button>
      }
    >
      {!tokenFromUrl && (
        <Alert severity="warning">Paste the reset token from your email (or server log).</Alert>
      )}
      <FormTextField
        name="token"
        control={control}
        label="Reset token"
        rules={{ required: 'Reset token is required' }}
      />
      <FormTextField
        name="newPassword"
        control={control}
        label="New password"
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
    </AuthForm>
  );
}
