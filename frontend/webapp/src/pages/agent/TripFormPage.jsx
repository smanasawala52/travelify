import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Paper, Stack } from '@mui/material';
import TripForm from '../../components/trips/TripForm';
import { useTrip } from '../../hooks/useTrips';

export default function TripFormPage() {
  const navigate = useNavigate();
  const { tripId } = useParams();
  const { data: trip } = useTrip(tripId);
  const isEditMode = !!tripId;

  const handleSuccess = () => {
    navigate('/agent/trips');
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Paper elevation={0} sx={{ p: 3 }}>
        <Stack spacing={3}>
          <TripForm
            tripId={tripId}
            onSuccess={handleSuccess}
          />
        </Stack>
      </Paper>
    </Container>
  );
}
