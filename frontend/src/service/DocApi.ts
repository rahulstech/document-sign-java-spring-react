import axios from "axios";

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
});

// ── Request / Response types ────────────────────────────────────────────

/** Parameters accepted by the upload mutation */
export interface UploadDocumentParams {
    type: string;
    size: number;
    blob: Blob;
    name: string;
}

/** Response from POST /docs/upload/url */
interface UploadUrlResponse {
    url: string;
    key: string;
}

/** Body sent to POST /docs/upload/confirm */
interface UploadConfirmRequest {
    key: string;
    name: string;
}

/** Final response from POST /docs/upload/confirm */
export interface UploadDocumentResult {
    docId: string;
    docName: string;
    docType: string;
    docUrl: string;
}

export interface Document {
    id: string,
    url: string,
    type: string,
    name: string,
    isPublished: boolean,
    isSigned: boolean,
    isVerified: boolean,
}

// ── Multi-step upload logic ─────────────────────────────────────────────

export async function uploadDocument(params: UploadDocumentParams): Promise<UploadDocumentResult> {
    const { type, size, blob, name } = params;

    // Step 1 – obtain a signed upload URL and key
    const { data: urlData } = await client.post<UploadUrlResponse>(
        "/docs/upload/url",
        { type, size },
    );

    const { url, key } = urlData;

    // Step 2 – PUT the blob to the signed URL
    await axios.put(url, blob, {
        headers: { "Content-Type": type },
    });

    // Step 3 – confirm the upload
    const { data: confirmData } = await client.post<UploadDocumentResult>(
        "/docs/upload/confirm",
        { key, name } as UploadConfirmRequest,
    );

    return confirmData;
}

// Get document by id
export async function getDocumentById(docId: string): Promise<Document> {
    // await new Promise(resolve => setTimeout(resolve, 3000));
    // throw new Error("some error");
    const { data } = await client.get<Document>(`/docs/${docId}`);
    return data;
}


