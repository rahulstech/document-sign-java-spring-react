import type { ButtonHTMLAttributes } from "react";

export const DragType = {
    SIGNATURE: "SIGNATURE"
} as const;

export interface SignatureData {
    pageNumber: number;
    left: number; // percentage
    top: number; // percentage
    width: number; // percentage
    height: number; // percentage
}

export interface PdfViewerProps {
    url: string;
    signatureUrl?: string | null;
    placedSignature?: SignatureData | null;
    onSignatureDrop?: (
        pageNumber: number,
        position: { left: number; top: number; width: number; height: number }
    ) => void;
}

export interface SignatureAnnotationProps {
    signatureUrl: string;
    left: number; // percentage
    top: number; // percentage
    width: number; // percentage
    height: number; // percentage
}

export interface PdfPageProps {
    pageNum: number;
    signatureUrl?: string | null;
    placedSignature?: SignatureData | null;
    onSignatureDrop?: (
        pageNumber: number,
        position: { left: number; top: number; width: number; height: number }
    ) => void;
}

export interface DocumentEditorProps {
    documentId: string,
    url: string;
    name: string;
    isDialogOpen: boolean;
    signatureUrl: string | null;
    setIsSave: (value: boolean)=> void;
}

export type SingedBy = "ONLY_ME" | "MULTIPLE";

export interface SignatureDialogProps {
    isOpen: boolean;
    onApply: (type: SingedBy, data: any) => void;
}

export interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    /** Path to the icon SVG */
    icon: string;
    /** Alt text for the icon */
    iconAlt?: string;
    /** Icon size classes (default: "w-5 h-5") */
    iconClassName?: string;
    /** Show a spinner instead of the icon */
    loading?: boolean;
    /** Spinner color classes (default: "border-white border-t-transparent") */
    spinnerClassName?: string;
}

export type MemberRole = "SIGNER" | "WITNESS" | "VERIFIER";

export interface MemberItem {
    key: string;
    name: string;
    email: string;
    role: MemberRole;
    nameError?: string;
    emailError?: string;
}

export interface MemberInputProps {
    member: MemberItem;
    onChange: (key: string, updatedFields: Partial<MemberItem>) => void;
    onRemove: (key: string) => void;
}

export interface MembersInputListProps {
    onApply?: (members: { name: string; email: string; role: MemberRole }[]) => void;
}

export interface SignatureInputProps {
    onApply: (signatureBlob: Blob, typedName?: string) => void;
}
