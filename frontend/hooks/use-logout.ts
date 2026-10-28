import { useMutation } from "@tanstack/react-query";
import { BACKEND_URL } from "@/lib/api";

const logout = async (): Promise<void> => {
  const response = await fetch(`${BACKEND_URL}/api/auth/logout`, {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Logout failed");
  }
};

export const useLogout = () => {
  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      // Redirect to login page after successful logout
      window.location.href = "/login";
    },
    onError: () => {
      // Even on error, redirect to login
      window.location.href = "/login";
    },
  });
};
