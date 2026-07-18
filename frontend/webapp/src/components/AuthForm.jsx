import React from 'react';
import {
  Alert,
  Box,
  Button,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Controller } from 'react-hook-form';
import PasswordStrengthIndicator from './PasswordStrengthIndicator';

/**
 * Reusable auth form shell used by Login / Register flows.
 * Parent owns react-hook-form (`control`, `errors`, `handleSubmit`).
 */
export default function AuthForm({
  title,
  subtitle,
  onSubmit,
  control,
  errors,
  busy,
  submitLabel,
  error,
  children,
  footer,
  showPasswordStrength,
  passwordValue,
}) {
  return (
    <Box sx={{ maxWidth: 480, mx: 'auto' }}>
      <Paper sx={{ p: 3 }} elevation={0} variant="outlined">
        <Typography variant="h5" gutterBottom>
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {subtitle}
          </Typography>
        )}
        <Stack component="form" spacing={2} onSubmit={onSubmit} noValidate>
          {error && <Alert severity="error">{error}</Alert>}
          {children}
          {showPasswordStrength && <PasswordStrengthIndicator password={passwordValue || ''} />}
          <Button type="submit" variant="contained" disabled={busy} size="large">
            {busy ? 'Please wait…' : submitLabel}
          </Button>
          {footer}
        </Stack>
      </Paper>
    </Box>
  );
}

export function FormTextField({
  name,
  control,
  label,
  type = 'text',
  rules,
  select = false,
  children,
  ...rest
}) {
  return (
    <Controller
      name={name}
      control={control}
      rules={rules}
      render={({ field, fieldState }) => (
        <TextField
          {...field}
          {...rest}
          select={select}
          type={type}
          label={label}
          fullWidth
          error={!!fieldState.error}
          helperText={fieldState.error?.message}
        >
          {children}
        </TextField>
      )}
    />
  );
}

export function RoleSelect({ control }) {
  return (
    <FormTextField
      name="role"
      control={control}
      label="Account type"
      select
      rules={{ required: 'Select an account type' }}
    >
      <MenuItem value="CUSTOMER">Customer</MenuItem>
      <MenuItem value="AGENT">Travel Agent</MenuItem>
    </FormTextField>
  );
}
