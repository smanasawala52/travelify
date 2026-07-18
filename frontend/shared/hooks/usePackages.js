import { useEffect, useState } from 'react';
import { packageApi } from '../api/api';

export function usePackages() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    packageApi
      .list()
      .then((res) => {
        if (mounted) setPackages(res.data);
      })
      .catch((err) => {
        if (mounted) setError(err.message || 'Failed to load packages');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  return { packages, loading, error };
}