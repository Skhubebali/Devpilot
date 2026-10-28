"use client";

import React, { useState, useRef, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useRepository } from "@/hooks/use-repositories";
import { BACKEND_URL } from "@/lib/api";
import { ArrowLeft, Send, Loader2, Bot, User } from "lucide-react";

type Message = {
  id: string;
  role: "user" | "assistant";
  content: string;
};

export default function ChatPage() {
  const params = useParams();
  const router = useRouter();
  const repoId = params.id as string;
  
  const { data: repo, isLoading: isRepoLoading, error: repoError } = useRepository(repoId);
  
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "welcome",
      role: "assistant",
      content: "Hello! I've indexed this repository. Ask me anything about the code."
    }
  ]);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isTyping) return;

    const userMsg = input.trim();
    setInput("");
    
    // Add user message to UI
    const newUserMsg: Message = {
      id: Date.now().toString(),
      role: "user",
      content: userMsg,
    };
    setMessages((prev) => [...prev, newUserMsg]);
    setIsTyping(true);

    try {
      const response = await fetch(`${BACKEND_URL}/api/repositories/${repoId}/ask`, {
        method: "POST",
        headers: {
          "Content-Type": "text/plain",
        },
        body: userMsg,
        credentials: "include",
      });

      if (!response.ok) {
        const errorText = await response.text().catch(() => `HTTP ${response.status}`);
        throw new Error(`Server error (${response.status}): ${errorText}`);
      }

      const answer = await response.text();
      
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          role: "assistant",
          content: answer,
        },
      ]);
    } catch (error) {
      console.error("Chat error:", error);
      const errorMsg = error instanceof Error ? error.message : "Unknown error";
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          role: "assistant",
          content: `⚠️ Error: ${errorMsg}\n\nPlease try again.`,
        },
      ]);
    } finally {
      setIsTyping(false);
    }
  };

  if (isRepoLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-slate-50">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (repoError || !repo) {
    return (
      <div className="flex h-screen flex-col items-center justify-center bg-slate-50 p-6">
        <p className="text-red-500 mb-4">Error loading repository: {repoError?.message || "Not found"}</p>
        <button
          onClick={() => router.push("/dashboard")}
          className="text-blue-600 hover:underline"
        >
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="flex h-screen flex-col bg-slate-50 text-slate-900 font-sans">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center shadow-sm shrink-0">
        <button
          onClick={() => router.push("/dashboard")}
          className="mr-4 p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500 hover:text-slate-900"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            {repo.name}
            <span className="text-xs px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 font-semibold align-middle">
              Chat
            </span>
          </h1>
          <p className="text-sm text-slate-500 truncate max-w-md">
            {repo.fullname}
          </p>
        </div>
      </header>

      {/* Chat Messages */}
      <div className="flex-1 overflow-y-auto p-4 md:p-6 space-y-6">
        {messages.map((message) => (
          <div
            key={message.id}
            className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div className={`flex max-w-[85%] md:max-w-[75%] gap-3 ${message.role === "user" ? "flex-row-reverse" : "flex-row"}`}>
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-1 ${
                message.role === "user" ? "bg-blue-600 text-white" : "bg-emerald-500 text-white"
              }`}>
                {message.role === "user" ? <User size={16} /> : <Bot size={16} />}
              </div>
              
              {/* Message Bubble */}
              <div
                className={`p-4 rounded-2xl ${
                  message.role === "user"
                    ? "bg-blue-600 text-white rounded-tr-sm"
                    : "bg-white border border-slate-200 text-slate-800 shadow-sm rounded-tl-sm whitespace-pre-wrap"
                }`}
              >
                {message.content}
              </div>
            </div>
          </div>
        ))}
        
        {isTyping && (
          <div className="flex justify-start">
            <div className="flex gap-3 flex-row">
              <div className="w-8 h-8 rounded-full bg-emerald-500 text-white flex items-center justify-center shrink-0 mt-1">
                <Bot size={16} />
              </div>
              <div className="p-4 rounded-2xl bg-white border border-slate-200 text-slate-800 shadow-sm rounded-tl-sm flex items-center gap-1">
                <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce [animation-delay:-0.3s]"></div>
                <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce [animation-delay:-0.15s]"></div>
                <div className="w-2 h-2 bg-slate-400 rounded-full animate-bounce"></div>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-white border-t border-slate-200 p-4 shrink-0">
        <form
          onSubmit={handleSubmit}
          className="max-w-4xl mx-auto relative flex items-center"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder={`Ask a question about ${repo.name}...`}
            disabled={isTyping}
            className="w-full bg-slate-50 border border-slate-200 text-slate-900 rounded-full pl-6 pr-14 py-3.5 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-shadow disabled:opacity-50"
          />
          <button
            type="submit"
            disabled={!input.trim() || isTyping}
            className="absolute right-2 p-2 rounded-full bg-blue-600 text-white hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:hover:bg-blue-600 flex items-center justify-center"
          >
            <Send size={18} />
          </button>
        </form>
        <p className="text-center text-xs text-slate-400 mt-3">
          AI can make mistakes. Verify important information.
        </p>
      </div>
    </div>
  );
}
