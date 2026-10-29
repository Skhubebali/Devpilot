"use client";
import React, { useEffect } from "react";

import { useCurrentUser } from "@/hooks/use-current-user";
import {
  useIndexRepository,
  useRepositories,
  useSyncRepositories,
  Repo,
} from "@/hooks/use-repositories";
import { useLogout } from "@/hooks/use-logout";

// ─────────────────────────────────────────────────────────
// Indexing Progress Bar
// ─────────────────────────────────────────────────────────
function IndexingProgressBar({ repo }: { repo: Repo }) {
  const pct =
    repo.totalFiles > 0
      ? Math.round((repo.fileProcessed / repo.totalFiles) * 100)
      : 0;

  return (
    <div className="col-span-2 mt-1">
      <div className="flex justify-between text-xs text-muted-foreground mb-1">
        <span>
          {repo.fileProcessed} / {repo.totalFiles} files &nbsp;·&nbsp; {repo.chunkCount} chunks
        </span>
        <span>{pct}%</span>
      </div>
      <div className="w-full bg-muted rounded-full h-2 overflow-hidden">
        <div
          className="bg-blue-500 h-2 rounded-full transition-all duration-500"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────
// Repository Card
// ─────────────────────────────────────────────────────────
function RepoCard({ repo }: { repo: Repo }) {
  const indexMutation = useIndexRepository();

  const handleIndexClick = () => {
    indexMutation.mutate(repo.id);
  };

  return (
    <div className="bg-card border border-border rounded-2xl p-6 shadow-sm hover:shadow-md transition-all flex flex-col">
      <div className="flex justify-between items-start mb-4">
        <div className="pr-4">
          <h2 className="text-lg font-bold text-card-foreground truncate">
            {repo.name}
          </h2>
          <p className="text-sm text-muted-foreground truncate">{repo.fullname}</p>
        </div>
        <span className="text-xs px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 font-semibold shrink-0">
          {repo.private ? "Private" : "Public"}
        </span>
      </div>

      <p className="text-sm text-muted-foreground mb-6 flex-1 line-clamp-2">
        {repo.description || "No description provided."}
      </p>

      <div className="grid grid-cols-2 gap-y-2 text-sm mb-6">
        <p>
          <span className="text-muted-foreground">Language:</span>{" "}
          <span className="font-medium text-foreground">
            {repo.language || "Unknown"}
          </span>
        </p>
        <p>
          <span className="text-muted-foreground">Branch:</span>{" "}
          <span className="font-medium text-foreground">
            {repo.defaultBranch}
          </span>
        </p>
        <p className="col-span-2">
          <span className="text-muted-foreground">Status:</span>{" "}
          <span
            className={`font-medium ${
              repo.indexStatus === "READY"
                ? "text-green-600 dark:text-green-400"
                : repo.indexStatus === "FAILED"
                ? "text-red-500"
                : repo.indexStatus === "INDEXING"
                ? "text-blue-500"
                : "text-muted-foreground"
            }`}
          >
            {repo.indexStatus === "INDEXING" ? (
              <span className="inline-flex items-center gap-1">
                <svg
                  className="animate-spin h-3 w-3"
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
                Indexing…
              </span>
            ) : (
              repo.indexStatus
            )}
          </span>
        </p>

        {/* Progress bar shown while indexing */}
        {repo.indexStatus === "INDEXING" && (
          <IndexingProgressBar repo={repo} />
        )}

        {repo.indexStatus === "FAILED" && repo.errorMsg && (
          <p className="col-span-2 text-red-500 text-xs mt-1">{repo.errorMsg}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-3 mt-auto">
        <a
          href={`/chat/${repo.id}`}
          className={`block text-center font-medium py-2.5 rounded-xl transition-colors shadow-sm ${
            repo.indexStatus === "READY"
              ? "bg-blue-600 hover:bg-blue-700 text-white"
              : "bg-muted text-muted-foreground cursor-not-allowed pointer-events-none"
          }`}
        >
          Start Chat
        </a>

        {repo.indexStatus === "PENDING" && (
          <button
            onClick={handleIndexClick}
            disabled={indexMutation.isPending}
            className="block text-center bg-background border-2 border-blue-600 text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20 font-medium py-2.5 rounded-xl transition-colors disabled:opacity-50"
          >
            {indexMutation.isPending ? "Starting…" : "Index Now"}
          </button>
        )}

        {repo.indexStatus === "INDEXING" && (
          <button
            disabled
            className="block text-center bg-muted border-2 border-border text-muted-foreground font-medium py-2.5 rounded-xl"
          >
            Indexing…
          </button>
        )}

        {repo.indexStatus === "READY" && (
          <button
            disabled
            className="block text-center bg-green-50 dark:bg-green-900/20 border-2 border-green-500 text-green-600 dark:text-green-400 font-medium py-2.5 rounded-xl"
          >
            Ready ✓
          </button>
        )}

        {repo.indexStatus === "FAILED" && (
          <button
            onClick={handleIndexClick}
            disabled={indexMutation.isPending}
            className="block text-center bg-red-50 dark:bg-red-900/20 border-2 border-red-500 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/40 font-medium py-2.5 rounded-xl transition-colors disabled:opacity-50"
          >
            {indexMutation.isPending ? "Retrying…" : "Retry"}
          </button>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────
// Dashboard Page
// ─────────────────────────────────────────────────────────
export default function DashboardPage() {
  const { data: user, isLoading, error } = useCurrentUser();

  // Start with a 10 s poll; tighten to 5 s once we know a repo is indexing.
  const [pollInterval, setPollInterval] = React.useState<number>(10_000);

  const {
    data: repositories,
    isLoading: repositoriesLoading,
    error: repositoriesError,
  } = useRepositories({
    refetchInterval: pollInterval,
    enabled: !!user?.id,
  });

  // Adjust polling rate based on current indexing state
  React.useEffect(() => {
    const hasIndexing = repositories?.some((r) => r.indexStatus === "INDEXING");
    setPollInterval(hasIndexing ? 5_000 : 15_000);
  }, [repositories]);

  const syncMutation = useSyncRepositories();
  const logoutMutation = useLogout();

  // Auto-sync GitHub repos when user is loaded
  useEffect(() => {
    if (user?.id && !syncMutation.isPending && !syncMutation.isSuccess) {
      syncMutation.mutate(user.id);
    }
  }, [user?.id]);

  // Redirect unauthenticated users to login
  useEffect(() => {
    if (!isLoading && (error || !user)) {
      window.location.href = "/login";
    }
  }, [isLoading, error, user]);

  const isSyncing = syncMutation.isPending;
  const showEmpty =
    !isSyncing &&
    !repositoriesLoading &&
    !syncMutation.isPending &&
    repositories?.length === 0;

  if (isLoading || error || !user) {
    return (
      <div className="flex h-screen items-center justify-center bg-background text-foreground font-sans">
        <div className="flex items-center gap-3 text-muted-foreground">
          <svg className="animate-spin h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <span className="text-sm font-medium">
            {isLoading ? "Verifying authentication…" : "Redirecting to login…"}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-background text-foreground font-sans">
      {/* Sidebar */}
      <aside className="w-72 bg-card text-card-foreground flex-col hidden md:flex border-r border-border shadow-sm">
        <div className="p-6 text-2xl font-extrabold border-b border-border text-blue-600">
          DevPilot
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <a
            href="#"
            className="block p-3 rounded-xl bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 font-semibold transition-colors"
          >
            Dashboard
          </a>
          <a
            href="#"
            className="block p-3 rounded-xl hover:bg-muted text-muted-foreground hover:text-foreground font-medium transition-colors"
          >
            Repositories
          </a>
        </nav>
        <div className="p-6 border-t border-border space-y-4">
          {/* User Info */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center overflow-hidden border border-blue-200 dark:border-blue-700 shrink-0">
              {user?.avatarUrl ? (
                <img
                  src={user.avatarUrl}
                  alt={user.displayName || "Avatar"}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-sm font-semibold text-blue-700 dark:text-blue-300">GH</span>
              )}
            </div>
            <div className="text-sm overflow-hidden">
              <p className="font-semibold text-foreground truncate">
                {user.displayName || "GitHub User"}
              </p>
              <p className="text-muted-foreground text-xs truncate">
                @{user.uname || "username"}
              </p>
            </div>
          </div>

          {/* Logout Button */}
          <button
            onClick={() => logoutMutation.mutate()}
            disabled={logoutMutation.isPending}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl border border-border hover:bg-muted text-muted-foreground hover:text-foreground text-sm font-medium transition-colors disabled:opacity-50"
          >
            {logoutMutation.isPending ? (
              <>
                <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Logging out…
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                Logout
              </>
            )}
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 p-6 md:p-10 overflow-y-auto">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-4xl font-extrabold text-foreground">Dashboard</h1>
          <button
            onClick={() => user?.id && syncMutation.mutate(user.id)}
            disabled={isSyncing || !user?.id}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSyncing ? (
              <>
                <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Syncing…
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Sync from GitHub
              </>
            )}
          </button>
        </div>
        <p className="text-muted-foreground mb-8 text-lg">
          Your GitHub repositories
        </p>

        {(repositoriesLoading || isSyncing) && (
          <div className="flex items-center gap-3 text-muted-foreground">
            <svg className="animate-spin h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            {isSyncing ? "Syncing repositories from GitHub…" : "Loading repositories…"}
          </div>
        )}

        {repositoriesError && (
          <p className="text-red-500 bg-red-50 dark:bg-red-900/20 p-4 rounded-xl border border-red-200 dark:border-red-800">
            {repositoriesError.message}
          </p>
        )}

        {syncMutation.isError && (
          <p className="text-red-500 bg-red-50 dark:bg-red-900/20 p-4 rounded-xl border border-red-200 dark:border-red-800 mb-4">
            Failed to sync from GitHub: {syncMutation.error?.message}
          </p>
        )}

        {showEmpty && (
          <div className="text-center py-16">
            <p className="text-muted-foreground text-lg mb-4">No repositories found.</p>
            <p className="text-muted-foreground text-sm">
              Make sure your GitHub OAuth app has the{" "}
              <code className="bg-muted px-1 rounded">repo</code> scope enabled.
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
