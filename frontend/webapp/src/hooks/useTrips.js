import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { agentApi } from '@shared/api/client';

// ==================== TRIP HOOKS ====================

export function useTrips(params) {
  return useQuery({
    queryKey: ['agent-trips', params],
    queryFn: () => agentApi.listTrips(params).then((res) => res.data),
    placeholderData: { content: [], totalElements: 0 },
  });
}

export function useTrip(id) {
  return useQuery({
    queryKey: ['agent-trip', id],
    queryFn: () => agentApi.getTrip(id).then((res) => res.data),
    enabled: !!id,
  });
}

export function useCreateTrip() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (payload) => agentApi.createTrip(payload).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agent-trips'] });
      queryClient.invalidateQueries({ queryKey: ['agent-templates'] });
    },
  });
}

export function useUpdateTrip() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, payload }) => agentApi.updateTrip(id, payload).then((res) => res.data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['agent-trip', id] });
      queryClient.invalidateQueries({ queryKey: ['agent-trips'] });
    },
  });
}

export function useDeleteTrip() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id) => agentApi.deleteTrip(id).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agent-trips'] });
    },
  });
}

export function useCopyTrip() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id) => agentApi.copyTrip(id).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agent-trips'] });
    },
  });
}

// ==================== TEMPLATE HOOKS ====================

export function useTemplates(params) {
  return useQuery({
    queryKey: ['agent-templates', params],
    queryFn: () => agentApi.listTemplates(params).then((res) => res.data),
    placeholderData: { content: [], totalElements: 0 },
  });
}

export function useTemplate(id) {
  return useQuery({
    queryKey: ['agent-template', id],
    queryFn: () => agentApi.getTemplate(id).then((res) => res.data),
    enabled: !!id,
  });
}

// ==================== SERVICE HOOKS ====================

export function useServices(params) {
  return useQuery({
    queryKey: ['provider-services', params],
    queryFn: () => agentApi.listServices(params).then((res) => res.data),
    placeholderData: { content: [], totalElements: 0 },
  });
}

export function useService(id) {
  return useQuery({
    queryKey: ['provider-service', id],
    queryFn: () => agentApi.getService(id).then((res) => res.data),
    enabled: !!id,
  });
}

export function useCreateService() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (payload) => agentApi.createService(payload).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['provider-services'] });
    },
  });
}

export function useUpdateService() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, payload }) => agentApi.updateService(id, payload).then((res) => res.data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['provider-service', id] });
      queryClient.invalidateQueries({ queryKey: ['provider-services'] });
    },
  });
}

export function useDeleteService() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id) => agentApi.deleteService(id).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['provider-services'] });
    },
  });
}

// ==================== CATEGORY HOOKS ====================

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => agentApi.listCategories().then((res) => res.data),
    placeholderData: [],
  });
}

// ==================== PREFETCH HOOKS ====================

export function usePrefetchTrips(queryClient) {
  return (params) => {
    queryClient.prefetchQuery({
      queryKey: ['agent-trips', params],
      queryFn: () => agentApi.listTrips(params).then((res) => res.data),
    });
  };
}

export function usePrefetchTrip(queryClient) {
  return (id) => {
    if (id) {
      queryClient.prefetchQuery({
        queryKey: ['agent-trip', id],
        queryFn: () => agentApi.getTrip(id).then((res) => res.data),
      });
    }
  };
}
