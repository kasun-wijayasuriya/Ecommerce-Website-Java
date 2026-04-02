import { create } from 'zustand';
import { authAPI, userAPI } from '../services/api';

export const useAuthStore = create((set, get) => ({
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,

    setToken: (token) => {
        set({
            token,
            isAuthenticated: !!token,
        });
    },

    setUser: (user) => {
        set({ user });
    },

    login: async (credentials) => {
        set({ isLoading: true, error: null });

        try {
            const response = await authAPI.login(credentials);
            const { token, user } = response.data.data;

            set({
                user,
                token,
                isAuthenticated: true,
                isLoading: false,
                error: null,
            });

            return true;
        } catch (error) {
            set({
                error: error.response?.data?.message || 'Login failed',
                isLoading: false,
            });

            return false;
        }
    },

    register: async (userData) => {
        set({ isLoading: true, error: null });

        try {
            const response = await authAPI.register(userData);
            const { token, user } = response.data.data;

            set({
                user,
                token,
                isAuthenticated: true,
                isLoading: false,
                error: null,
            });

            return true;
        } catch (error) {
            set({
                error: error.response?.data?.message || 'Registration failed',
                isLoading: false,
            });

            return false;
        }
    },

    logout: () => {
        set({
            user: null,
            token: null,
            isAuthenticated: false,
            error: null,
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
        if (!get().token) return;

        try {
            const response = await userAPI.getProfile();
            set({ user: response.data.data });
        } catch (error) {
            console.error('Failed to fetch profile:', error);
        }
    },

    isAdmin: () => {
        const user = get().user;
        return user?.roles?.includes('ADMIN') || false;
    },
}));