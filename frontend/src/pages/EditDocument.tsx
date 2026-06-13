import { useParams } from "react-router-dom"
import { useGetDocumentById } from "../hooks/ApiQueryHooks"
import RefreshIcon from "../assets/icons/refresh.svg"
import { IconButton } from "../components/IconButton"
import { PdfViewer } from "../components/PdfViewer"

export function EditDocument() {
    const { docId } = useParams()
    const { isLoading, data, isError, error, refetch } = useGetDocumentById(docId!);

    if (isLoading) {
        return (
            <div className="w-4/12 mx-auto pt-12">
                <div className="adobe-card flex items-center gap-3 px-6 py-4">
                    <span className="inline-block w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                    <span className="text-sm text-(--color-text-secondary)">Loading your document</span>
                </div>
            </div>
        )
    }

    if (isError) {
        return (
            <div className="w-4/12 mx-auto pt-12">
                <div className="adobe-card flex flex-col items-center gap-4 px-8 py-6">
                    <p className="text-xl text-wrap text-danger">{error.message}</p>
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
        )
    }

    return (
        <div className="flex flex-1 overflow-hidden">
            {/* ── Main: PDF Viewer ── */}
            <div className="flex flex-1 overflow-hidden">
                <PdfViewer url={data!.url} />
            </div>

            {/* ── Right Section ── */}
            <aside className="w-64 bg-surface-app border-l border-(--color-border-default) p-4">
                <h2 className="text-lg font-bold text-text-primary wrap-break-word">
                    {data!.name}
                </h2>
            </aside>
        </div>
    )
}