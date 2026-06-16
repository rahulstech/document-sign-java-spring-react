import { useState } from "react";
import TextIcon from "../assets/icons/text.svg";
import UploadIcon from "../assets/icons/upload_file.svg";
import type { SignatureInputProps } from "./properties";

const FONTS = [
    "Herr Von Muellerhoff",
    "Hurricane",
    "Inspiration",
    "Mea Culpa",
    "Mrs Saint Delafield",
    "Sirivennela"
];

interface RenderTextSignaturesProps {
    name: string;
    selectedFont: string;
    onSignatureSelected: (font: string) => void;
}

interface RenderUploadSignatureProps {
    uploadedImage: string | null;
    errorMsg: string | null;
    onFileUpload: (e: React.ChangeEvent<HTMLInputElement>) => void;
}



function RenderTextSignatures({ name, selectedFont, onSignatureSelected }: RenderTextSignaturesProps) {
    return (
        <div className="flex-1 overflow-y-auto pr-1 flex flex-col gap-3 min-h-0">
            {FONTS.map((font) => (
                <button
                    key={font}
                    type="button"
                    onClick={() => onSignatureSelected(font)}
                    className={`flex items-center justify-center p-4 min-h-[80px] rounded-md transition-all cursor-pointer text-3xl overflow-hidden text-ellipsis whitespace-nowrap shrink-0 ${
                        selectedFont === font
                            ? "border-2 border-primary-500 bg-primary-50 text-primary-500 font-semibold shadow-sm"
                            : "border border-(--color-border-default) bg-surface-panel text-text-primary hover:border-primary-500 hover:bg-primary-50/20"
                    }`}
                    style={{ fontFamily: font }}
                >
                    {name}
                </button>
            ))}
        </div>
    );
}


function RenderUploadSignature({ uploadedImage, errorMsg, onFileUpload }: RenderUploadSignatureProps) {
    return (
        <div className="flex flex-col gap-4 w-full h-full overflow-y-auto pr-1">
            {/* File Upload Input Card */}
            <div className="flex flex-col items-center justify-center border-2 border-dashed border-(--color-border-default) hover:border-primary-500 rounded-lg p-6 bg-surface-panel transition-all shrink-0">
                <input
                    type="file"
                    accept=".jpg,.jpeg,.png"
                    className="hidden"
                    id="signature-file-upload"
                    onChange={onFileUpload}
                />
                <label
                    htmlFor="signature-file-upload"
                    className="flex flex-col items-center justify-center cursor-pointer text-center w-full"
                >
                    <svg
                        className="w-10 h-10 text-primary-500 mb-3"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
                        />
                    </svg>
                    <span className="text-sm font-semibold text-text-primary mb-1">
                        Upload your signature
                    </span>
                    <span className="text-xs text-(--color-text-tertiary)">
                        Upload only JPG, JPEG or PNG file up to 1MB
                    </span>
                </label>
            </div>

            {/* Error Message */}
            {errorMsg && (
                <div className="text-xs text-danger font-medium text-center shrink-0">
                    {errorMsg}
                </div>
            )}

            {/* Signature Preview Panel (Simple static stack layout - no nested flex heights) */}
            {uploadedImage && (
                <div className="border border-(--color-border-default) rounded-lg p-4 bg-surface-app flex flex-col items-center gap-3 shrink-0">
                    <span className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider">
                        Signature Preview
                    </span>
                    <div className="w-1/3 flex items-center justify-center bg-white rounded border border-(--color-border-subtle) p-3 shadow-sm">
                        <img
                            src={uploadedImage}
                            alt="Uploaded signature"
                            className="object-contain"
                        />
                    </div>
                </div>
            )}
        </div>
    );
}


// Utility methods for generating signature blobs
function createSignatureBlob(name: string, fontFamily: string): Promise<Blob> {
    return new Promise((resolve, reject) => {
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");
        if (!ctx) {
            reject(new Error("Canvas context is not available"));
            return;
        }

        ctx.font = "20pt " + fontFamily;
        const textMetrics = ctx.measureText(name);
        const textWidth = Math.ceil(textMetrics.width);

        const paddingX = 16;
        const paddingY = 12;
        canvas.width = textWidth + paddingX * 2;
        canvas.height = 28 + paddingY * 2; // size 20pt is approx 27px

        ctx.fillStyle = "#00000000"; // full tranparent background
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.font = "20pt " + fontFamily;
        ctx.fillStyle = "#000000";
        ctx.textBaseline = "middle";
        ctx.textAlign = "center";
        ctx.fillText(name, canvas.width / 2, canvas.height / 2);

        canvas.toBlob((blob) => {
            if (blob) {
                resolve(blob);
            } else {
                reject(new Error("Failed to convert canvas to blob"));
            }
        }, "image/png");
    });
}

async function getBlobFromDataUrl(dataUrl: string): Promise<Blob> {
    const response = await fetch(dataUrl);
    return await response.blob();
}

interface TextTabContentProps {
    fullName: string;
    setFullName: (name: string) => void;
    selectedFont: string;
    setSelectedFont: (font: string) => void;
}

function TextTabContent({ fullName, setFullName, selectedFont, setSelectedFont }: TextTabContentProps) {
    return (
        <div className="flex-1 min-h-0 overflow-hidden flex flex-col gap-4">
            <div className="flex flex-col gap-1.5 shrink-0">
                <label className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider">
                    Full Name
                </label>
                <input
                    type="text"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Enter your full name"
                    className="w-full px-3 py-2 border border-(--color-border-default) rounded-md text-sm text-text-primary focus:outline-none focus:border-primary-500 bg-surface-panel transition-all"
                />
            </div>
            <span className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider shrink-0">
                Select Style
            </span>
            <RenderTextSignatures
                name={fullName}
                selectedFont={selectedFont}
                onSignatureSelected={setSelectedFont}
            />
        </div>
    );
}

interface UploadTabContentProps {
    uploadedImage: string | null;
    errorMsg: string | null;
    handleFileUpload: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

function UploadTabContent({ uploadedImage, errorMsg, handleFileUpload }: UploadTabContentProps) {
    return (
        <div className="flex-1 min-h-0 overflow-hidden flex flex-col">
            <RenderUploadSignature
                uploadedImage={uploadedImage}
                errorMsg={errorMsg}
                onFileUpload={handleFileUpload}
            />
        </div>
    );
}

export function SignatureInput({ onApply }: SignatureInputProps) {
    const [activeTab, setActiveTab] = useState<"text" | "upload">("text");
    const [fullName, setFullName] = useState<string>("Your Name");
    const [selectedFont, setSelectedFont] = useState<string>("Herr Von Muellerhoff");
    const [uploadedImage, setUploadedImage] = useState<string | null>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const handleApply = async () => {

        if (activeTab === "text") {
            try {
                const blob = await createSignatureBlob(fullName, selectedFont);
                onApply(blob, fullName);
            } catch (err) {
                setErrorMsg("Failed to generate signature.");
            }
        } else {
            if (!uploadedImage) {
                setErrorMsg("Please upload a signature image first.");
                return;
            }
            try {
                const blob = await getBlobFromDataUrl(uploadedImage);
                onApply(blob);
            } catch (err) {
                setErrorMsg("Failed to process uploaded image.");
            }
        }
    };

    const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        setErrorMsg(null);
        const file = e.target.files?.[0];
        if (!file) return;

        // Check type: jpeg, jpg, png
        const validTypes = ["image/jpeg", "image/jpg", "image/png"];
        if (!validTypes.includes(file.type)) {
            setErrorMsg("Invalid file type. Please upload a JPG, JPEG, or PNG image.");
            return;
        }

        // Check size: up to 1MB (1,048,576 bytes)
        const maxSize = 1 * 1024 * 1024;
        if (file.size > maxSize) {
            setErrorMsg("File size exceeds 1MB. Please upload a smaller image.");
            return;
        }

        // Read file as Data URL
        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result === "string") {
                setUploadedImage(reader.result);
            }
        };
        reader.readAsDataURL(file);
    };

    return (
        <div className="flex flex-col h-full w-full gap-4 overflow-hidden">
            {/* Top Area: Left tabs & Right content */}
            <div className="flex gap-4 flex-1 min-h-0 pl-8 pr-8 pb-6 overflow-hidden">
                {/* Left Column: Vertical Tabs */}
                <div className="flex flex-col gap-2 shrink-0">
                    <button
                        type="button"
                        onClick={() => {
                            setActiveTab("text");
                            setErrorMsg(null);
                        }}
                        className={`w-12 h-12 rounded-md border-2 transition-all flex items-center justify-center cursor-pointer ${
                            activeTab === "text"
                                ? "border-primary-500 bg-primary-50"
                                : "border-(--color-border-default) bg-surface-card hover:bg-surface-hover"
                        }`}
                        aria-label="Text tab"
                        title="Type Signature"
                    >
                        <img src={TextIcon} alt="text" className="w-5 h-5" />
                    </button>
                    <button
                        type="button"
                        onClick={() => {
                            setActiveTab("upload");
                            setErrorMsg(null);
                        }}
                        className={`w-12 h-12 rounded-md border-2 transition-all flex items-center justify-center cursor-pointer ${
                            activeTab === "upload"
                                ? "border-primary-500 bg-primary-50"
                                : "border-(--color-border-default) bg-surface-card hover:bg-surface-hover"
                        }`}
                        aria-label="Upload file tab"
                        title="Upload Signature File"
                    >
                        <img src={UploadIcon} alt="upload_file" className="w-5 h-5" />
                    </button>
                </div>

                {/* Right Column: Tab Contents (overflow-hidden) */}
                <div className="flex-1 border border-(--color-border-default) rounded-md bg-surface-card overflow-hidden p-6 flex flex-col min-h-0">
                    {activeTab === "text" ? (
                        <TextTabContent
                            fullName={fullName}
                            setFullName={setFullName}
                            selectedFont={selectedFont}
                            setSelectedFont={setSelectedFont}
                        />
                    ) : (
                        <UploadTabContent
                            uploadedImage={uploadedImage}
                            errorMsg={errorMsg}
                            handleFileUpload={handleFileUpload}
                        />
                    )}
                </div>
            </div>

            {/* Bottom Right: Apply Button */}
            <div className="flex justify-end shrink-0 p-6 border-t border-(--color-border-default)">
                <button
                    type="button"
                    onClick={handleApply}
                    className="adobe-btn adobe-btn-primary px-6"
                >
                    Apply
                </button>
            </div>
        </div>
    );
}
