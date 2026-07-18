import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0B6E4F' },
    secondary: { main: '#1B4965' },
    background: { default: '#F4F7F5', paper: '#FFFFFF' },
  },
  typography: {
    fontFamily: '"DM Sans", "Segoe UI", sans-serif',
    h3: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
    h4: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
    h5: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
});

export default theme;