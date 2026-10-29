import { useMutation } from "@tanstack/react-query";
import { BACKEND_URL, clearAuthToken, getAuthHeaders } from "@/lib/api";

const logout = async (): Promise<void> => {
  const response = await fetch(`${BACKEND_URL}/api/auth/logout`, {
    method: "POST",
    headers: {
      ...getAuthHeaders(),
    },
    credentials: "include",
  });

  clearAuthToken();

  if (!response.ok) {
    throw new Error("Logout failed");
  }
};

export const useLogout = () => {
  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      clearAuthToken();
      window.location.href = "/login";
    },
    onError: () => {
      clearAuthToken();
      window.location.href = "/login";
    },
  });
};
