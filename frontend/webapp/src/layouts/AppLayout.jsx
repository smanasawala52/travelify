import React from 'react';
import { Link as RouterLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  Alert,
  AppBar,
  Box,
  Button,
  Container,
  Link,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material';
import { useAuth } from '@shared/context/AuthContext';

function dashboardPath(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

export default function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const flash =
    location.state?.registered
      ? 'Account created. Please sign in.'
      : location.state?.reset
        ? 'Password reset. Please sign in.'
        : null;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background:
          'radial-gradient(circle at top left, rgba(11,110,79,0.12), transparent 40%), linear-gradient(180deg, #F4F7F5 0%, #E8F0EC 100%)',
      }}
    >
      <AppBar position="sticky" color="transparent" elevation={0} sx={{ borderBottom: '1px solid rgba(0,0,0,0.08)' }}>
        <Toolbar sx={{ gap: 1, flexWrap: 'wrap' }}>
          <Typography
            component={RouterLink}
            to="/"
            variant="h5"
            sx={{ flexGrow: 1, textDecoration: 'none', color: 'primary.main', fontFamily: 'Fraunces, serif' }}
          >
            Travelify
          </Typography>
          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
            <Button component={RouterLink} to="/packages" color="inherit">
              Packages
            </Button>
            {isAuthenticated ? (
              <>
                <Button component={RouterLink} to={dashboardPath(user.role)} color="inherit">
                  Dashboard
                </Button>
                <Button component={RouterLink} to="/profile" color="inherit">
                  Profile
                </Button>
                {user.role === 'ADMIN' && (
                  <Button component={RouterLink} to="/admin/users" color="inherit">
                    Users
                  </Button>
                )}
                <Typography variant="body2" sx={{ px: 1 }}>
                  {user.fullName}
                </Typography>
                <Button
                  variant="outlined"
                  onClick={async () => {
                    await logout();
                    navigate('/');
                  }}
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Button component={RouterLink} to="/login" color="inherit">
                  Login
                </Button>
                <Button component={RouterLink} to="/register" variant="contained">
                  Register
                </Button>
              </>
            )}
          </Stack>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {flash && location.pathname === '/login' && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {flash}
          </Alert>
        )}
        <Outlet />
      </Container>
      <Box component="footer" sx={{ py: 3, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          © {new Date().getFullYear()} Travelify · Book journeys with confidence
        </Typography>
        <Link href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer" underline="hover">
          API docs
        </Link>
      </Box>
    </Box>
  );
}
