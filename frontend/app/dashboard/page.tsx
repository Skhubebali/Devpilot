"use client";
import React from 'react';

import { useCurrentUser } from '@/hooks/use-current-user';
import { useIndexRepository, useRepositories, Repo } from "@/hooks/use-repositories";

function RepoCard({ repo }: { repo: Repo }) {
  const indexMutation = useIndexRepository();

  const handleIndexClick = () => {
    indexMutation.mutate(repo.id);
  };

  return (
    <div
      className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all flex flex-col"
    >
      <div className="flex justify-between items-start mb-4">
        <div className="pr-4">
          <h2 className="text-lg font-bold text-slate-900 truncate">
            {repo.name}
          </h2>
          <p className="text-sm text-slate-500 truncate">
            {repo.fullname}
          </p>
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
          <span className="font-medium text-slate-700">{repo.language || "Unknown"}</span>
        </p>
        <p>
          <span className="text-slate-500">Branch:</span>{" "}
          <span className="font-medium text-slate-700">{repo.defaultBranch}</span>
        </p>
        <p className="col-span-2">
          <span className="text-slate-500">Status:</span>{" "}
          <span className="font-medium text-slate-700">{repo.indexStatus}</span>
        </p>
        {repo.indexStatus === "INDEXING" && (
          <p className="col-span-2">
            <span className="text-slate-500">Progress:</span>{" "}
            <span className="font-medium text-blue-600">
              {repo.fileProcessed} / {repo.totalFiles} files (Chunks: {repo.chunkCount})
            </span>
          </p>
        )}
        {repo.indexStatus === "FAILED" && repo.errorMsg && (
          <p className="col-span-2 text-red-500 text-xs">
            {repo.errorMsg}
          </p>
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

  return (
    <div className="flex h-screen bg-slate-50 text-slate-900 font-sans">
      {/* Sidebar */}
      <aside className="w-72 bg-white text-slate-900 flex-col hidden md:flex border-r border-slate-200 shadow-sm">
        <div className="p-6 text-2xl font-extrabold border-b border-slate-100 text-blue-600">
          DevPilot
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <a href="#" className="block p-3 rounded-xl bg-blue-50 text-blue-700 font-semibold transition-colors">
            Dashboard
          </a>
          <a href="#" className="block p-3 rounded-xl hover:bg-slate-50 text-slate-600 hover:text-slate-900 font-medium transition-colors">
            Repositories
          </a>
        </nav>
        <div className="p-6 border-t border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center overflow-hidden border border-blue-200 shrink-0">
              {user?.avatarUrl ? (
                <img src={user.avatarUrl} alt={user.displayName || 'Avatar'} className="w-full h-full object-cover" />
              ) : (
                <span className="text-sm font-semibold text-blue-700">GH</span>
              )}
            </div>
            <div className="text-sm overflow-hidden">
              {error ? (
                <p className="font-medium text-red-500 text-xs truncate">Error: {error.message}</p>
              ) : (
                <>
                  <p className="font-semibold text-slate-900 truncate">{isLoading ? "Loading..." : user?.displayName || "GitHub User"}</p>
                  <p className="text-slate-500 text-xs truncate">@{user?.uname || "username"}</p>
                </>
              )}
            </div>
          </div>
        </div>
      </aside>

      <div className="flex-1 p-6 md:p-10 overflow-y-auto">
        <h1 className="text-4xl font-extrabold mb-2 text-slate-900">
          Dashboard
        </h1>
        <p className="text-slate-500 mb-8 text-lg">
          Your GitHub repositories
        </p>

        {repositoriesLoading && (
          <p className="text-slate-500">Loading repositories...</p>
        )}

        {repositoriesError && (
          <p className="text-red-500 bg-red-50 p-4 rounded-xl border border-red-100">
            {repositoriesError.message}
          </p>
        )}

        {repositories && repositories.length === 0 && (
          <p className="text-slate-500">
            No repositories found.
          </p>
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