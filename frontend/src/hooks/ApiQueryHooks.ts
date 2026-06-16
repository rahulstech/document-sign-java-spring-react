import { QueryClient, useMutation, useQuery, useQueryClient, type UseQueryResult } from "@tanstack/react-query";
import { getDocumentById, getDocumentInfoById, selfSign, uploadDocument, type Document, type DocumentInfo, type SelfSignRequest, type UploadDocumentRequest, type UploadDocumentResult } from "../service/DocApi";

export const apiQueryClient = new QueryClient()


export function useUploadDocument() {
    return useMutation<UploadDocumentResult, Error, UploadDocumentRequest>({
        mutationFn: uploadDocument,
    });
}

export function useGetDocumentById(docId: string): UseQueryResult<Document,Error> {
    return useQuery<Document, Error>({
        queryKey: ["document", docId],
        queryFn: () => getDocumentById(docId),
        enabled: !!docId,
    })
}

export function useGetDocumentInfoById(docId: string): UseQueryResult<DocumentInfo,Error> {
    return useQuery<DocumentInfo, Error>({
        queryKey: ["document", docId],
        queryFn: () => getDocumentInfoById(docId),
        enabled: !!docId,
    })
}

export function useSelfSign() {
    const client = useQueryClient();
    return useMutation<void,Error,SelfSignRequest>({
        mutationFn: selfSign,
        onSuccess: (_,{ documentId })=> {
            client.fetchQuery({ queryKey: ["document", documentId] })
        },
    });
}