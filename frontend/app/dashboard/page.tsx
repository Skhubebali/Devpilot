"use client";
import React, { useEffect } from "react";

import { useCurrentUser } from "@/hooks/use-current-user";
import {
  useIndexRepository,
  useRepositories,
  useSyncRepositories,
  Repo,
} from "@/hooks/use-repositories";

function RepoCard({ repo }: { repo: Repo }) {
  const indexMutation = useIndexRepository();

  const handleIndexClick = () => {
    indexMutation.mutate(repo.id);
  };

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all flex flex-col">
      <div className="flex justify-between items-start mb-4">
        <div className="pr-4">
          <h2 className="text-lg font-bold text-slate-900 truncate">
            {repo.name}
          </h2>
          <p className="text-sm text-slate-500 truncate">{repo.fullname}</p>
        </div>
        <span className="text-xs px-3 py-1 rounded-full bg-blue-50 text-blue-700 font-semibold shrink-0">
          {repo.private ? "Private" : "Public"}
        </span>
      </div>

      <p className="text-sm text-slate-600 mb-6 flex-1 line-clamp-2">
        {repo.description || "No description provided."}
      </p>

      <div className="grid grid-cols-2 gap-y-2 text-sm mb-6">
        <p>
          <span className="text-slate-500">Language:</span>{" "}
          <span className="font-medium text-slate-700">
            {repo.language || "Unknown"}
          </span>
        </p>
        <p>
          <span className="text-slate-500">Branch:</span>{" "}
          <span className="font-medium text-slate-700">
            {repo.defaultBranch}
          </span>
        </p>
        <p className="col-span-2">
          <span className="text-slate-500">Status:</span>{" "}
          <span className="font-medium text-slate-700">{repo.indexStatus}</span>
        </p>
        {repo.indexStatus === "INDEXING" && (
          <p className="col-span-2">
            <span className="text-slate-500">Progress:</span>{" "}
            <span className="font-medium text-blue-600">
              {repo.fileProcessed} / {repo.totalFiles} files (Chunks:{" "}
              {repo.chunkCount})
            </span>
          </p>
        )}
        {repo.indexStatus === "FAILED" && repo.errorMsg && (
          <p className="col-span-2 text-red-500 text-xs">{repo.errorMsg}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-3 mt-auto">
        <a
          href={`/chat/${repo.id}`}
          className={`block text-center font-medium py-2.5 rounded-xl transition-colors shadow-sm ${
            repo.indexStatus === "READY"
              ? "bg-blue-600 hover:bg-blue-700 text-white"
              : "bg-slate-200 text-slate-400 cursor-not-allowed pointer-events-none"
          }`}
        >
          Start Chat
        </a>

        {repo.indexStatus === "PENDING" && (
          <button
            onClick={handleIndexClick}
            disabled={indexMutation.isPending}
            className="block text-center bg-white border-2 border-blue-600 text-blue-600 hover:bg-blue-50 font-medium py-2.5 rounded-xl transition-colors disabled:opacity-50"
          >
            {indexMutation.isPending ? "Starting..." : "Index Now"}
          </button>
        )}

        {repo.indexStatus === "INDEXING" && (
          <button
            disabled
            className="block text-center bg-slate-50 border-2 border-slate-200 text-slate-500 font-medium py-2.5 rounded-xl transition-colors"
          >
            Indexing...
          </button>
        )}

        {repo.indexStatus === "READY" && (
          <button
            disabled
            className="block text-center bg-green-50 border-2 border-green-500 text-green-600 font-medium py-2.5 rounded-xl transition-colors"
          >
            Ready
          </button>
        )}

        {repo.indexStatus === "FAILED" && (
          <button
            onClick={handleIndexClick}
            disabled={indexMutation.isPending}
            className="block text-center bg-red-50 border-2 border-red-500 text-red-600 hover:bg-red-100 font-medium py-2.5 rounded-xl transition-colors disabled:opacity-50"
          >
            {indexMutation.isPending ? "Retrying..." : "Retry"}
          </button>
        )}
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { data: user, isLoading, error } = useCurrentUser();

  const {
    data: repositories,
    isLoading: repositoriesLoading,
    error: repositoriesError,
  } = useRepositories({
    refetchInterval: 3000,
  });

  const syncMutation = useSyncRepositories();

  // Auto-sync GitHub repos when user is loaded
  useEffect(() => {
    if (user?.id && !syncMutation.isPending && !syncMutation.isSuccess) {
      syncMutation.mutate(user.id);
    }
  }, [user?.id]);

  const isSyncing = syncMutation.isPending;
  const showEmpty =
    !isSyncing &&
    !repositoriesLoading &&
    !syncMutation.isPending &&
    repositories?.length === 0;

  return (
    <div className="flex h-screen bg-slate-50 text-slate-900 font-sans">
      {/* Sidebar */}
      <aside className="w-72 bg-white text-slate-900 flex-col hidden md:flex border-r border-slate-200 shadow-sm">
        <div className="p-6 text-2xl font-extrabold border-b border-slate-100 text-blue-600">
          DevPilot
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <a
            href="#"
            className="block p-3 rounded-xl bg-blue-50 text-blue-700 font-semibold transition-colors"
          >
            Dashboard
          </a>
          <a
            href="#"
            className="block p-3 rounded-xl hover:bg-slate-50 text-slate-600 hover:text-slate-900 font-medium transition-colors"
          >
            Repositories
          </a>
        </nav>
        <div className="p-6 border-t border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center overflow-hidden border border-blue-200 shrink-0">
              {user?.avatarUrl ? (
                <img
                  src={user.avatarUrl}
                  alt={user.displayName || "Avatar"}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-sm font-semibold text-blue-700">GH</span>
              )}
            </div>
            <div className="text-sm overflow-hidden">
              {error ? (
                <p className="font-medium text-red-500 text-xs truncate">
                  Error: {error.message}
                </p>
              ) : (
                <>
                  <p className="font-semibold text-slate-900 truncate">
                    {isLoading
                      ? "Loading..."
                      : user?.displayName || "GitHub User"}
                  </p>
                  <p className="text-slate-500 text-xs truncate">
                    @{user?.uname || "username"}
                  </p>
                </>
              )}
            </div>
          </div>
        </div>
      </aside>

      <div className="flex-1 p-6 md:p-10 overflow-y-auto">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-4xl font-extrabold text-slate-900">Dashboard</h1>
          <button
            onClick={() => user?.id && syncMutation.mutate(user.id)}
            disabled={isSyncing || !user?.id}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSyncing ? (
              <>
                <svg
                  className="animate-spin h-4 w-4"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                  />
                </svg>
                Syncing...
              </>
            ) : (
              <>
                <svg
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
                Sync from GitHub
              </>
            )}
          </button>
        </div>
        <p className="text-slate-500 mb-8 text-lg">
          Your GitHub repositories
        </p>

        {(repositoriesLoading || isSyncing) && (
          <div className="flex items-center gap-3 text-slate-500">
            <svg
              className="animate-spin h-5 w-5 text-blue-600"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
              />
            </svg>
            {isSyncing ? "Syncing repositories from GitHub..." : "Loading repositories..."}
          </div>
        )}

        {repositoriesError && (
          <p className="text-red-500 bg-red-50 p-4 rounded-xl border border-red-100">
            {repositoriesError.message}
          </p>
        )}

        {syncMutation.isError && (
          <p className="text-red-500 bg-red-50 p-4 rounded-xl border border-red-100 mb-4">
            Failed to sync from GitHub: {syncMutation.error?.message}
          </p>
        )}

        {showEmpty && (
          <div className="text-center py-16">
            <p className="text-slate-500 text-lg mb-4">No repositories found.</p>
            <p className="text-slate-400 text-sm">
              Make sure your GitHub OAuth app has the <code className="bg-slate-100 px-1 rounded">repo</code> scope enabled.
            </p>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {repositories?.map((repo: Repo) => (
            <RepoCard key={repo.id} repo={repo} />
          ))}
        </div>
      </div>
    </div>
  );
}
