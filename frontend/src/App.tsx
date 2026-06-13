import { QueryClientProvider } from "@tanstack/react-query";
import { UploadDocument } from "./pages/UploadDocument";
import { apiQueryClient } from "./hooks/ApiQueryHooks";


export function App() {

  return (
    <div className="p-4">
      <QueryClientProvider client={apiQueryClient} >
        <UploadDocument />
      </QueryClientProvider>
      
    </div>
  )
}