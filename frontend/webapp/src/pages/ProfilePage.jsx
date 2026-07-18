import React, { useState } from 'react';
import {
  Alert,
  Avatar,
  Box,
  Button,
  Divider,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useForm, useWatch } from 'react-hook-form';
import { useAuth } from '@shared/context/AuthContext';
import { FormTextField } from '../components/AuthForm';
import PasswordStrengthIndicator from '../components/PasswordStrengthIndicator';

export default function ProfilePage() {
  const { user, updateProfile, changePassword } = useAuth();
  const [profileMsg, setProfileMsg] = useState('');
  const [profileErr, setProfileErr] = useState('');
  const [pwdMsg, setPwdMsg] = useState('');
  const [pwdErr, setPwdErr] = useState('');
  const [busyProfile, setBusyProfile] = useState(false);
  const [busyPwd, setBusyPwd] = useState(false);

  const profileForm = useForm({
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phone: user?.phone || '',
      avatarUrl: user?.avatarUrl || '',
    },
  });

  const pwdForm = useForm({
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = useWatch({ control: pwdForm.control, name: 'newPassword' });

  const onSaveProfile = profileForm.handleSubmit(async (values) => {
    setBusyProfile(true);
    setProfileMsg('');
    setProfileErr('');
    try {
      await updateProfile(values);
      setProfileMsg('Profile updated');
    } catch (err) {
      setProfileErr(err.response?.data?.error || 'Update failed');
    } finally {
      setBusyProfile(false);
    }
  });

  const onChangePassword = pwdForm.handleSubmit(async (values) => {
    setBusyPwd(true);
    setPwdMsg('');
    setPwdErr('');
    try {
      const res = await changePassword({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      setPwdMsg(res.message || 'Password updated');
      pwdForm.reset({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPwdErr(err.response?.data?.error || 'Password change failed');
    } finally {
      setBusyPwd(false);
    }
  });

  const initials = `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || '?';

  return (
    <Stack spacing={3} sx={{ maxWidth: 640 }}>
      <Typography variant="h4">My account</Typography>
      <Typography variant="body2" color="text.secondary">
        Manage your Travelify profile — similar to the WP Travel customer dashboard account area.
      </Typography>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
          <Avatar src={user?.avatarUrl || undefined} sx={{ width: 64, height: 64, bgcolor: 'primary.main' }}>
            {initials}
          </Avatar>
          <Box>
            <Typography variant="h6">{user?.fullName}</Typography>
            <Typography variant="body2" color="text.secondary">
              {user?.email} · {user?.role}
            </Typography>
          </Box>
        </Stack>

        <Stack component="form" spacing={2} onSubmit={onSaveProfile} noValidate>
          {profileMsg && <Alert severity="success">{profileMsg}</Alert>}
          {profileErr && <Alert severity="error">{profileErr}</Alert>}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              name="firstName"
              control={profileForm.control}
              label="First name"
              rules={{ required: 'Required' }}
            />
            <FormTextField
              name="lastName"
              control={profileForm.control}
              label="Last name"
              rules={{ required: 'Required' }}
            />
          </Stack>
          <FormTextField name="phone" control={profileForm.control} label="Phone" />
          <FormTextField name="avatarUrl" control={profileForm.control} label="Avatar URL" />
          <Button type="submit" variant="contained" disabled={busyProfile}>
            {busyProfile ? 'Saving…' : 'Save profile'}
          </Button>
        </Stack>
      </Paper>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Change password
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Stack component="form" spacing={2} onSubmit={onChangePassword} noValidate>
          {pwdMsg && <Alert severity="success">{pwdMsg}</Alert>}
          {pwdErr && <Alert severity="error">{pwdErr}</Alert>}
          <FormTextField
            name="oldPassword"
            control={pwdForm.control}
            label="Current password"
            type="password"
            rules={{ required: 'Required' }}
          />
          <FormTextField
            name="newPassword"
            control={pwdForm.control}
            label="New password"
            type="password"
            rules={{
              required: 'Required',
              minLength: { value: 8, message: 'At least 8 characters' },
              validate: (v) =>
                (/[A-Za-z]/.test(v) && /\d/.test(v)) || 'Include a letter and a digit',
            }}
          />
          <PasswordStrengthIndicator password={newPassword || ''} />
          <FormTextField
            name="confirmPassword"
            control={pwdForm.control}
            label="Confirm new password"
            type="password"
            rules={{
              required: 'Confirm password',
              validate: (v) => v === newPassword || 'Passwords do not match',
            }}
          />
          <Button type="submit" variant="outlined" disabled={busyPwd}>
            {busyPwd ? 'Updating…' : 'Update password'}
          </Button>
        </Stack>
      </Paper>
    </Stack>
  );
}
