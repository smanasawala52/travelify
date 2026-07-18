import React from 'react';
import { Card, CardContent, CardActions, Typography, Button, Stack, Chip } from '@mui/material';

export default function PackageCard({ pkg, onBook, actionLabel = 'Book now' }) {
  return (
    <Card variant="outlined" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Stack spacing={1}>
          <Typography variant="h6">{pkg.title}</Typography>
          <Chip label={pkg.destination} size="small" color="primary" variant="outlined" />
          <Typography variant="body2" color="text.secondary">
            {pkg.description}
          </Typography>
          <Typography variant="subtitle1">${Number(pkg.price).toFixed(2)} Â· {pkg.durationDays} days</Typography>
        </Stack>
      </CardContent>
      {onBook && (
        <CardActions>
          <Button size="small" variant="contained" onClick={() => onBook(pkg)}>
            {actionLabel}
          </Button>
        </CardActions>
      )}
    </Card>
  );
}