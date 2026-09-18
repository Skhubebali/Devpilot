import { useQuery } from '@tanstack/react-query';

export interface User {
  id: string;
  githubId: number;
  uname: string;
  displayName: string;
  avatarUrl: string;
}

export const useCurrentUser = () => {
  return useQuery<User>({
    queryKey: ['currentUser'],
    queryFn: async () => {
      const response = await fetch('/api/auth/me', {
        credentials: 'include',
      });
      if (!response.ok) {
        throw new Error(`Not authenticated (Status: ${response.status})`);
      }
      return response.json();
    },
    retry: false,
  });
};
