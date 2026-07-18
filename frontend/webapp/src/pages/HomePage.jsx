import React from 'react';
import { Box, Button, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export default function HomePage() {
  return (
    <Box
      sx={{
        minHeight: '70vh',
        display: 'grid',
        alignContent: 'center',
        gap: 2,
        backgroundImage:
          'linear-gradient(120deg, rgba(11,110,79,0.85), rgba(27,73,101,0.75)), url(https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1600&q=80)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        color: '#fff',
        borderRadius: 3,
        px: { xs: 3, md: 6 },
        py: { xs: 8, md: 10 },
      }}
    >
      <Typography variant="h3">Travelify</Typography>
      <Typography variant="h5" sx={{ maxWidth: 560, opacity: 0.95 }}>
        Plan, book, and manage trips for customers, agents, and admins in one place.
      </Typography>
      <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
        <Button component={RouterLink} to="/packages" variant="contained" color="secondary" size="large">
          Browse packages
        </Button>
        <Button component={RouterLink} to="/register" variant="outlined" size="large" sx={{ color: '#fff', borderColor: '#fff' }}>
          Create account
        </Button>
      </Stack>
    </Box>
  );
}