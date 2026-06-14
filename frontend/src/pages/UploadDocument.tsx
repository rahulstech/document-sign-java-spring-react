import { useRef, useState, type ChangeEvent } from "react";
import UploadFileIcon from "../assets/icons/upload_file.svg";
import { useUploadDocument } from "../hooks/ApiQueryHooks";
import { useNavigate } from "react-router-dom";
import { IconButton } from "../components/IconButton";

const MAX_FILE_SIZE_MB = 100;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

export function UploadDocument() {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [error, setError] = useState<string | null>(null);
    const { mutate, isPending, error: mutationError } = useUploadDocument();
    const navigate = useNavigate();

    const displayError = error ?? (mutationError ? mutationError.message : null);

    function handleButtonClick() {
        fileInputRef.current?.click();
    }

    function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0];
        if (!file) return;

        if (file.type !== "application/pdf") {
            setError("Only PDF files are allowed.");
            e.target.value = "";
            return;
        }

        if (file.size > MAX_FILE_SIZE_BYTES) {
            setError(`File size exceeds ${MAX_FILE_SIZE_MB}MB limit.`);
            e.target.value = "";
            return;
        }

        setError(null);

        mutate({
            type: file.type,
            size: file.size,
            blob: file,
            name: file.name,
        }, {
            onSuccess: (data) => {
                navigate(`/docs/${data.docId}/edit`);
            },
        });
    }

    return (
        <div className="flex flex-1 justify-center p-4">
            <div className="flex flex-col items-center gap-4">
                <h2 className="text-3xl font-bold">Sign Document</h2>
                
                <p className="text-xl">eSign your Document or send sign request to others</p>

                <input
                    ref={fileInputRef}
                    id="upload-document-input"
                    type="file"
                    accept="application/pdf"
                    className="hidden"
                    onChange={handleFileChange}
                />
                <IconButton
                    id="upload-document-btn"
                    className="adobe-btn-primary text-xl px-10 py-5"
                    icon={UploadFileIcon}
                    iconAlt="Upload"
                    iconClassName="w-6 h-6 filter-[invert(1)]"
                    loading={isPending}
                    onClick={handleButtonClick}
                >
                    {isPending ? "Uploading" : "Upload Document"}
                </IconButton>
                <p className={`text-sm ${displayError ? "text-danger" : "text-(--color-text-tertiary)"}`}>
                    {displayError ?? "Upload PDF document to sign digitally"}
                </p>
            </div>
        </div>
    )
}