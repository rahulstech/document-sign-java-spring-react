import axios from "axios";

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
});

// ── Request / Response types ────────────────────────────────────────────

/** Parameters accepted by the upload mutation */
export interface UploadDocumentRequest {
    type: string;
    size: number;
    blob: Blob;
    name: string;
}

export interface SelfSignRequest {
    documentId: string,
    signature: {
        type: string;
        size: number;
        blob: Blob;
    };
    pageNumber: number,
    bounds: {
        x: number; // percentage
        y: number; // percentage
        width: number; // percentage
        height: number; // percentage
    }
}


/** Final response from POST /docs/upload/confirm */
export interface UploadDocumentResult {
    docId: string;
    docName: string;
    docType: string;
    docUrl: string;
}

export interface Document {
    id: string;
    url: string;
    mimeType: string;
    name: string;
    createdAt: string,
    isPending: boolean;
    isSigned: boolean;
    isVerified: boolean;
}

export interface DocumentInfo {
    url: string;
    mimeType: string;
    name: string;
    canEdit: boolean;
}

// ── Multi-step upload logic ─────────────────────────────────────────────

export async function uploadDocument({ name, type, size, blob }: UploadDocumentRequest): Promise<UploadDocumentResult> {

    // Step 1 – obtain a signed upload URL and key
    const { data: urlData } = await client.post<{ url: string, key: string }>(
        "/upload",
        { type, size },
    );

    const { url, key } = urlData;

    // Step 2 – PUT the blob to the signed URL
    await axios.put(url, blob, {
        headers: { "Content-Type": type },
    });

    // Step 3 – confirm the upload
    const { data: confirmData } = await client.post<UploadDocumentResult>(
        "/docs/new",
        { key, name },
    );

    return confirmData;
}

// Get document by id
export async function getDocumentById(docId: string): Promise<Document> {
    const { data } = await client.get<Document>(`/docs/${docId}`);
    return data;
}


// Get document info by id
export async function getDocumentInfoById(docId: string): Promise<DocumentInfo> {
    const { data } = await client.get<DocumentInfo>(`/docs/${docId}/info`);
    return data;
}


export async function selfSign({ documentId, signature, pageNumber, bounds }: SelfSignRequest): Promise<void> {
    // Step 1 – obtain a signed upload URL and key
    const { type, size, blob } = signature;
    const { data: urlData } = await client.post<{ url: string, key: string }>(
        "/upload",
        { type, size },
    );

    const { url, key: uploadKey } = urlData;

    // Step 2 – PUT the blob to the signed URL
    await axios.put(url, blob, {
        headers: { "Content-Type": type },
    });

    // Step 3 – confirm the upload
    const { x,y, width, height } = bounds;
    await client.post<void>(
        `/docs/${documentId}/signature/self`,
        { 
            uploadKey, pageNumber, x, y, width, height,
        },
    );
}

