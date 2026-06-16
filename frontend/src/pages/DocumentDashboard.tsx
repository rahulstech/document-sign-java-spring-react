import { useParams } from "react-router-dom";
import { useGetDocumentById } from "../hooks/ApiQueryHooks";
import RefreshIcon from "../assets/icons/refresh.svg";
import { IconButton } from "../components/IconButton";

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

    const statuses: { label: string; color: string; bgColor: string }[] = [];

    if (data) {
        if (data.isSigned || data.isVerified) {
            if (data.isSigned) {
                statuses.push({
                    label: "Singed",
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
        } else if (data.isPending) {
            statuses.push({
                label: "Pending",
                color: "#FF5959",
                bgColor: "rgba(255, 89, 89, 0.4)",
            });
        }
    }

    return (
        <div className="min-h-screen bg-surface-app text-text-primary py-8">
            <div className="w-full max-w-[900px] mx-auto px-4">
                <h1 className="text-2xl font-bold mb-4">Document Details</h1>

                {isLoading && (
                    <div className="w-4/12 mx-auto pt-12">
                        <div className="adobe-card flex items-center gap-3 px-6 py-4">
                            <span className="inline-block w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                            <span className="text-sm text-(--color-text-secondary)">Loading your document</span>
                        </div>
                    </div>
                )}

                {(isError || (!isLoading && !data)) && (
                    <div className="w-4/12 mx-auto pt-12">
                        <div className="adobe-card flex flex-col items-center gap-4 px-8 py-6">
                            <p className="text-xl text-wrap text-danger">{error?.message || "Failed to load document."}</p>
                            <IconButton
                                className="adobe-btn-primary rounded-full"
                                icon={RefreshIcon}
                                iconAlt="Retry"
                                iconClassName="w-4 h-4 filter-[invert(1)]"
                                onClick={() => refetch()}
                            >
                                Retry
                            </IconButton>
                        </div>
                    </div>
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
                            <a
                                href={data.url}
                                download={data.name}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="adobe-btn adobe-btn-primary px-4 py-2 text-sm font-semibold select-none shrink-0"
                            >
                                Download
                            </a>
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