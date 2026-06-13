import { QueryClient, useMutation } from "@tanstack/react-query";
import { uploadDocument, type UploadDocumentParams, type UploadDocumentResult } from "../service/DocApi";

export const apiQueryClient = new QueryClient()


export function useUploadDocument() {
    return useMutation<UploadDocumentResult, Error, UploadDocumentParams>({
        mutationFn: uploadDocument,
    });
}