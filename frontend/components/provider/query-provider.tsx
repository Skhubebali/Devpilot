"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import React from "react";

const QueryProvider = ({ children }: { children: React.ReactNode }) => {
  const [queryClient] = React.useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            // Data is considered fresh for 30 s — avoids redundant refetches
            // on component mount / window focus for rapidly re-rendering pages.
            staleTime: 30_000,
            // Only retry failed requests once to keep the UI responsive
            retry: 1,
            // Don't re-fetch on every window focus (reduces background noise)
            refetchOnWindowFocus: false,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

export default QueryProvider;
