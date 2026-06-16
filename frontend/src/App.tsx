import { QueryClientProvider } from "@tanstack/react-query";
import { UploadDocument } from "./pages/UploadDocument";
import { apiQueryClient } from "./hooks/ApiQueryHooks";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { EditDocument } from "./pages/EditDocument";
import { DocumentDashboard } from "./pages/DocumentDashboard";



const routes = createBrowserRouter([
  {
    path: "/docs/upload",
    element: <UploadDocument />
  },
  {
    path: "/docs/:docId/dashboard",
    element: <DocumentDashboard />
  },
  {
    path: "/docs/:docId/edit",
    element: <EditDocument />
  },
]);


export function App() {

  return (
      <QueryClientProvider client={apiQueryClient} >
        <RouterProvider router={routes} />
      </QueryClientProvider>
  )
}