import { ModeToggle } from "@/components/ui/mode-toggle";
import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-slate-50 text-slate-900 font-sans p-6">
      <div className="w-full max-w-lg bg-white p-12 rounded-3xl shadow-xl border border-slate-100 text-center relative overflow-hidden">
        {/* Subtle decorative background blob */}
        <div className="absolute top-0 left-0 w-full h-32 bg-gradient-to-b from-blue-50 to-transparent -z-10"></div>
        
        <h1 className="text-5xl font-extrabold text-blue-600 tracking-tight">
          DevPilot
        </h1>
        
        <p className="mt-5 text-lg text-slate-600 font-medium">
          Welcome to your development copilot 🚀
        </p>

        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <Link 
            href="/login" 
            className="w-full sm:w-auto bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-8 rounded-xl shadow-sm transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Get Started
          </Link>
          <div className="w-full sm:w-auto">
            <ModeToggle />
          </div>
        </div>
      </div>
    </main>
  );
}