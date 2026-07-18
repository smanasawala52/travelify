import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Paper, Stack, Typography } from '@mui/material';
import ServiceForm from '../../components/trips/ServiceForm';
import { useService } from '../../hooks/useTrips';

export default function ServiceFormPage() {
  const navigate = useNavigate();
  const { serviceId } = useParams();
  const { data: service } = useService(serviceId);
  const isEditMode = !!serviceId;

  const handleSuccess = () => {
    navigate('/provider/services');
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper elevation={0} sx={{ p: 3 }}>
        <Stack spacing={3}>
          <ServiceForm
            serviceId={serviceId}
            service={service}
            onSuccess={handleSuccess}
          />
        </Stack>
      </Paper>
    </Container>
  );
}
