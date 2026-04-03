import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authAPI, userAPI } from '../services/api';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Initialize auth state on app load (checks if valid JWT cookie exists)
      initializeAuth: async () => {
        // If we already have user data, skip re-fetch
        if (get().user && get().isAuthenticated) {
          return true;
        }
        try {
          const response = await userAPI.getProfile();
          set({
            user: response.data.data,
            isAuthenticated: true,
          });
          return true;
        } catch (error) {
          // If no valid session, reset auth state
          set({ user: null, isAuthenticated: false });
          return false;
        }
      },

      login: async (credentials) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authAPI.login(credentials);
          const { user } = response.data.data;
          set({
            user,
            isAuthenticated: true,
            isLoading: false,
          });
          return { success: true };
        } catch (error) {
          set({
            error: error.response?.data?.message || 'Login failed',
            isLoading: false,
          });
          return { success: false, error: error.response?.data?.message };
        }
      },

      register: async (userData) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authAPI.register(userData);
          const { user } = response.data.data;
          set({
            user,
            isAuthenticated: true,
            isLoading: false,
          });
          return { success: true };
        } catch (error) {
          set({
            error: error.response?.data?.message || 'Registration failed',
            isLoading: false,
          });
          return { success: false, error: error.response?.data?.message };
        }
      },

      logout: async () => {
        try {
          await authAPI.logout();
        } catch (error) {
          console.error('Logout error:', error);
        }
        set({
          user: null,
          isAuthenticated: false,
        });
      },

      updateProfile: async (data) => {
        set({ isLoading: true, error: null });
        try {
          const response = await userAPI.updateProfile(data);
          set({
            user: response.data.data,
            isLoading: false,
          });
          return { success: true };
        } catch (error) {
          set({
            error: error.response?.data?.message || 'Update failed',
            isLoading: false,
          });
          return { success: false, error: error.response?.data?.message };
        }
      },

      fetchProfile: async () => {
        try {
          const response = await userAPI.getProfile();
          set({ 
            user: response.data.data,
            isAuthenticated: true,
          });
          return true;
        } catch (error) {
          console.error('Failed to fetch profile:', error);
          // If 401, user is not authenticated
          if (error.response?.status === 401) {
            set({ user: null, isAuthenticated: false });
          }
          return false;
        }
      },

      isAdmin: () => {
        const user = get().user;
        return user?.roles?.includes('ADMIN') || false;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        // token is stored in httpOnly cookie — do NOT persist to localStorage
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
