import { useState, useCallback, useRef, useEffect } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/AnnotationLayer.css";
import "react-pdf/dist/Page/TextLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
    "pdfjs-dist/build/pdf.worker.min.mjs",
    import.meta.url,
).toString();

interface PdfViewerProps {
    url: string;
}

export function PdfViewer({ url }: PdfViewerProps) {
    const [numPages, setNumPages] = useState<number>(0);
    const [currentPage, setCurrentPage] = useState<number>(1);
    const pagesRef = useRef<HTMLDivElement>(null);

    const onDocumentLoadSuccess = useCallback(({ numPages }: { numPages: number }) => {
        setNumPages(numPages);
    }, []);

    // Track which page is visible while scrolling
    useEffect(() => {
        const container = pagesRef.current;
        if (!container || numPages === 0) return;

        const observer = new IntersectionObserver(
            (entries) => {
                for (const entry of entries) {
                    if (entry.isIntersecting) {
                        const id = entry.target.id; // "page-N"
                        const pageNum = parseInt(id.replace("page-", ""), 10);
                        if (!isNaN(pageNum)) {
                            setCurrentPage(pageNum);
                        }
                    }
                }
            },
            {
                root: container,
                threshold: 0.3, // Lower threshold to trigger intersection earlier/more reliably
            }
        );

        // Find and observe page elements
        const observePages = () => {
            const pageElements = container.querySelectorAll('[id^="page-"]');
            pageElements.forEach((el) => observer.observe(el));
        };

        observePages();

        // Use MutationObserver to observe pages dynamically rendered by react-pdf
        const mutationObserver = new MutationObserver(() => {
            observePages();
        });

        mutationObserver.observe(container, {
            childList: true,
            subtree: true,
        });

        return () => {
            observer.disconnect();
            mutationObserver.disconnect();
        };
    }, [numPages]);

    function scrollToPage(page: number) {
        setCurrentPage(page);
        const el = document.getElementById(`page-${page}`);
        if (el && pagesRef.current) {
            pagesRef.current.scrollTo({
                top: el.offsetTop - pagesRef.current.offsetTop,
                behavior: "smooth",
            });
        }
    }

    return (
        <div className="flex h-screen w-full overflow-hidden">
            {/* Left sidebar matching app background */}
            <aside className="w-[144px] bg-surface-app border-r border-(--color-border-default) overflow-y-auto flex flex-col p-2 shrink-0">
                <Document 
                    file={url} 
                    loading={
                        <div className="flex items-center gap-3 py-12">
                            <span className="inline-block w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                            <span className="text-sm text-(--color-text-secondary)">Loading Thumbnails</span>
                        </div>
                    }
                >
                    {Array.from({ length: numPages }, (_, i) => (
                        <button
                            key={i + 1}
                            className={`relative cursor-pointer rounded-md p-1 m-1 w-[108px] transition-all duration-200 ${
                                currentPage === i + 1
                                    ? "ring-2 ring-primary-500 bg-surface-active shadow-sm"
                                    : "hover:bg-surface-hover"
                            }`}
                            onClick={() => scrollToPage(i + 1)}
                        >
                            <Page
                                pageNumber={i + 1}
                                width={100}
                                renderTextLayer={false}
                                renderAnnotationLayer={false}
                            />
                            <span className={`block text-center text-xs mt-1 ${
                                currentPage === i + 1
                                    ? "text-primary-500 font-semibold"
                                    : "text-(--color-text-tertiary)"
                            }`}>
                                {i + 1}
                            </span>
                        </button>
                    ))}
                </Document>
            </aside>

            {/* ── Pages Panel matching app background ── */}
            <main ref={pagesRef} className="flex-1 overflow-y-auto bg-surface-app flex flex-col items-center py-6 gap-6">
                <Document
                    file={url}
                    onLoadSuccess={onDocumentLoadSuccess}
                    loading={
                        <div className="flex items-center gap-3 py-12">
                            <span className="inline-block w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                            <span className="text-sm text-(--color-text-secondary)">Loading Pages</span>
                        </div>
                    }
                >
                    {Array.from({ length: numPages }, (_, i) => (
                        <div key={i + 1} id={`page-${i + 1}`} className="adobe-page-shadow mb-6">
                            <Page
                                pageNumber={i + 1}
                                renderTextLayer={true}
                                renderAnnotationLayer={true}
                            />
                        </div>
                    ))}
                </Document>
            </main>
        </div>
    );
}