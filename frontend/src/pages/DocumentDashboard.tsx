import { useState, useRef, useEffect } from "react";
import { useParams } from "react-router-dom";
import { useGetDocumentById } from "../hooks/ApiQueryHooks";
import { LoadingView } from "../components/LoadingView";
import { ErrorWithRetryView } from "../components/ErrorWithRetryView";

function formatCreatedAt(dateStr: string): string {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;

    const weekdays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    const weekday = weekdays[d.getDay()];
    const month = months[d.getMonth()];
    const day = String(d.getDate()).padStart(2, "0");
    const year = d.getFullYear();

    let hours = d.getHours();
    const minutes = String(d.getMinutes()).padStart(2, "0");
    const seconds = String(d.getSeconds()).padStart(2, "0");
    const ampm = hours >= 12 ? "PM" : "AM";

    hours = hours % 12;
    hours = hours ? hours : 12; // 0 should be 12
    const hoursStr = String(hours).padStart(2, "0");

    return `${weekday}, ${month} ${day}, ${year} ${hoursStr}:${minutes}:${seconds} ${ampm}`;
}

export function DocumentDashboard() {
    const { docId } = useParams();
    const { isLoading, isError, error, data, refetch } = useGetDocumentById(docId || "");
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    const statuses: { label: string; color: string; bgColor: string }[] = [];

    if (data) {
        if (data.isPending) {
            statuses.push({
                label: "Pending",
                color: "#FF5959",
                bgColor: "rgba(255, 89, 89, 0.4)",
            });
        }
        if (data.isSigned) {
            statuses.push({
                label: "Signed",
                color: "#91D06C",
                bgColor: "rgba(145, 208, 108, 0.20)",
            });
        }
        if (data.isVerified) {
            statuses.push({
                label: "Verified",
                color: "#91D06C",
                bgColor: "rgba(145, 208, 108, 0.4)",
            });
        }
    }

    return (
        <div className="min-h-screen bg-surface-app text-text-primary py-8">
            <div className="w-full max-w-[900px] mx-auto px-4">
                <h1 className="text-2xl font-bold mb-4">Document Details</h1>

                {isLoading && <LoadingView message="Loading your document" />}

                {(isError || (!isLoading && !data)) && (
                    <ErrorWithRetryView
                        message={error?.message || "Failed to load document."}
                        onRetry={() => refetch()}
                    />
                )}

                {!isLoading && !isError && data && (
                    /* Card with 16px padding and elevation */
                    <div className="bg-surface-card border border-border-subtle rounded-lg p-4 shadow-md">
                        {/* Top of the card */}
                        <div className="flex justify-between items-start gap-4">
                            <div>
                                <h2 className="text-lg font-bold text-text-primary break-all">{data.name}</h2>
                                <span className="text-xs text-text-secondary font-mono mt-1 block">{data.id}</span>
                            </div>
                            <div className="relative inline-flex rounded-md shadow-sm shrink-0" ref={dropdownRef}>
                                <a
                                    href={data.url}
                                    download={data.name}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="adobe-btn adobe-btn-primary px-4 py-2 text-sm font-semibold select-none rounded-r-none border-r border-adobe-red-dark"
                                >
                                    Download
                                </a>
                                <button
                                    type="button"
                                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                    disabled={!data.isSigned}
                                    className={`adobe-btn adobe-btn-primary px-3 py-2 text-sm font-semibold select-none rounded-l-none flex items-center justify-center ${
                                        !data.isSigned ? "opacity-50 cursor-not-allowed" : ""
                                    }`}
                                >
                                    <svg
                                        className="w-4 h-4 fill-current"
                                        viewBox="0 0 20 20"
                                        xmlns="http://www.w3.org/2000/svg"
                                    >
                                        <path
                                            fillRule="evenodd"
                                            d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                                            clipRule="evenodd"
                                        />
                                    </svg>
                                </button>
                                {isDropdownOpen && (
                                    <div className="absolute right-0 top-full mt-1 w-48 bg-surface-card border border-border-default rounded-md shadow-lg z-10 py-1">
                                        <a
                                            href={`${import.meta.env.VITE_API_BASE_URL}/docs/${data.id}/download-signed`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="block w-full text-left px-4 py-2 text-sm text-text-primary hover:bg-gray-100 select-none cursor-pointer"
                                            onClick={() => setIsDropdownOpen(false)}
                                        >
                                            Download Signed
                                        </a>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Divider */}
                        <hr className="border-t border-border-default my-4" />

                        {/* Body: Two column grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Left Column */}
                            <div className="flex flex-col gap-3">
                                <div className="flex items-center gap-3">
                                    <span className="font-bold">Status:</span>
                                    <span className="inline-flex gap-2">
                                        {statuses.map((status, index) => (
                                            <span
                                                key={index}
                                                style={{
                                                    color: status.color,
                                                    backgroundColor: status.bgColor,
                                                    fontWeight: "bold",
                                                    padding: "2px 6px",
                                                    borderRadius: "4px",
                                                    fontSize: "0.75rem",
                                                }}
                                            >
                                                {status.label}
                                            </span>
                                        ))}
                                    </span>
                                </div>
                                
                                {/* Divider after Status */}
                                <hr className="border-t border-border-default my-1" />

                                <div className="flex items-center gap-3">
                                    <span className="font-bold">Created On:</span>
                                    <span className="text-text-secondary">{formatCreatedAt(data.createdAt)}</span>
                                </div>

                                {/* Divider after Created On */}
                                <hr className="border-t border-border-default my-1" />
                            </div>

                            {/* Right Column (Empty for now) */}
                            <div></div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}