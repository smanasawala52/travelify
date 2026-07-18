import React from 'react';
import { Box, LinearProgress, Stack, Typography } from '@mui/material';

function scorePassword(password = '') {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 1;
  else if (/[a-zA-Z]/.test(password)) score += 0.5;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  return Math.min(4, Math.floor(score));
}

const LABELS = ['Too weak', 'Weak', 'Fair', 'Good', 'Strong'];
const COLORS = ['error', 'error', 'warning', 'info', 'success'];

export default function PasswordStrengthIndicator({ password }) {
  if (!password) return null;
  const score = scorePassword(password);
  const pct = ((score + 1) / 5) * 100;

  return (
    <Stack spacing={0.5} sx={{ mt: 0.5 }}>
      <LinearProgress
        variant="determinate"
        value={pct}
        color={COLORS[score]}
        sx={{ height: 6, borderRadius: 1 }}
      />
      <Typography variant="caption" color="text.secondary">
        Password strength: {LABELS[score]}
      </Typography>
      <Box component="ul" sx={{ m: 0, pl: 2, color: 'text.secondary', typography: 'caption' }}>
        <li>At least 8 characters</li>
        <li>Include a letter and a number</li>
      </Box>
    </Stack>
  );
}
