import { QueryClient, useMutation, useQuery } from "@tanstack/react-query";
import { getDocumentById, uploadDocument, type Document, type UploadDocumentParams, type UploadDocumentResult } from "../service/DocApi";

export const apiQueryClient = new QueryClient()


export function useUploadDocument() {
    return useMutation<UploadDocumentResult, Error, UploadDocumentParams>({
        mutationFn: uploadDocument,
    });
}

export function useGetDocumentById(docId: string) {
    return useQuery<Document, Error>({
        queryKey: ["document", docId],
        queryFn: () => getDocumentById(docId),
        enabled: !!docId,
    })
}