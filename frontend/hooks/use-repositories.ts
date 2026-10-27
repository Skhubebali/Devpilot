import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { BACKEND_URL } from "@/lib/api";

export interface Repo {
  id: string;
  githubRepoId: number;
  owner: string;
  name: string;
  fullname: string;
  private: boolean;
  defaultBranch: string;
  language: string | null;
  htmlUrl: string;
  description: string | null;
  indexStatus: "PENDING" | "INDEXING" | "READY" | "FAILED";
  chunkCount: number;
  fileProcessed: number;
  totalFiles: number;
  errorMsg: string | null;
  createdAt: string;
  updatedAt: string;
}

const getRepositories = async (): Promise<Repo[]> => {
  const response = await fetch(
    `${BACKEND_URL}/api/repositories`,
    {
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to fetch repositories");
  }

  return response.json();
};

export const useRepositories = (options?: { refetchInterval?: number | false }) => {
  return useQuery({
    queryKey: ["repositories"],
    queryFn: getRepositories,
    ...options,
  });
};

const getRepository = async (id: string): Promise<Repo> => {
  const response = await fetch(`${BACKEND_URL}/api/repositories/${id}`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Failed to fetch repository");
  }

  return response.json();
};

export const useRepository = (id: string) => {
  return useQuery({
    queryKey: ["repository", id],
    queryFn: () => getRepository(id),
    enabled: !!id,
  });
};


const indexRepository = async (repositoryId: string): Promise<string> => {
  const response = await fetch(
    `${BACKEND_URL}/api/repositories/${repositoryId}/index`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to start indexing");
  }

  return response.text();
};

export const useIndexRepository = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: indexRepository,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["repositories"] });
    },
  });
};
