import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '@shared/context/AuthContext';
import AppLayout from './layouts/AppLayout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProfilePage from './pages/ProfilePage';
import AdminUsersPage from './pages/AdminUsersPage';
import PackagesPage from './pages/PackagesPage';
import BookingPage from './pages/BookingPage';
import CustomerDashboard from './pages/CustomerDashboard';
import AgentDashboard from './pages/AgentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import ProtectedRoute from './components/ProtectedRoute';
import RoleBasedRoute from './components/RoleBasedRoute';

function dashboardFor(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

function DashboardRedirect() {
  const { user } = useAuth();
  return <Navigate to={dashboardFor(user?.role)} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/packages" element={<PackagesPage />} />

        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleBasedRoute roles={['ADMIN']}>
              <AdminUsersPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/book/:packageId"
          element={
            <RoleBasedRoute roles={['CUSTOMER']}>
              <BookingPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardRedirect />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/customer"
          element={
            <RoleBasedRoute roles={['CUSTOMER']}>
              <CustomerDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/dashboard/agent"
          element={
            <RoleBasedRoute roles={['AGENT', 'ADMIN']}>
              <AgentDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/dashboard/admin"
          element={
            <RoleBasedRoute roles={['ADMIN']}>
              <AdminDashboard />
            </RoleBasedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
