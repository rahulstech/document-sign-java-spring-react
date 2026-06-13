import { useRef, useState, type ChangeEvent } from "react";
import UploadFileIcon from "../assets/icons/upload_file.svg";
import { useUploadDocument } from "../hooks/ApiQueryHooks";

const MAX_FILE_SIZE_MB = 100;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

export function UploadDocument() {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [error, setError] = useState<string | null>(null);
    const { mutate, isPending, isSuccess, data, error: mutationError } = useUploadDocument();

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
        });
    }

    if (isSuccess) {
        // TODO: navigate to document edit page
        console.log(data);
    }

    return (
        <div className="flex flex-1 justify-center">
            <div className="flex flex-col items-center gap-4">
                <input
                    ref={fileInputRef}
                    id="upload-document-input"
                    type="file"
                    accept="application/pdf"
                    className="hidden"
                    onChange={handleFileChange}
                />
                <button
                    id="upload-document-btn"
                    className="adobe-btn adobe-btn-primary text-xl px-10 py-5"
                    onClick={handleButtonClick}
                    disabled={isPending}
                >
                    {isPending ? (
                        <>
                            <span className="inline-block w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            Uploading…
                        </>
                    ) : (
                        <>
                            <img
                                src={UploadFileIcon}
                                alt="Upload"
                                className="w-6 h-6 filter-[invert(1)]"
                            />
                            Upload Document
                        </>
                    )}
                </button>
                <p className={`text-sm ${displayError ? "text-danger" : "text-(--color-text-tertiary)"}`}>
                    {displayError ?? "Upload PDF document to sign digitally"}
                </p>
            </div>
        </div>
    )
}