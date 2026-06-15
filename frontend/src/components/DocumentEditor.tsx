import { PdfViewer } from "./PdfViewer"
import { DndProvider, useDrag, useDragLayer } from "react-dnd";
import { HTML5Backend, getEmptyImage } from "react-dnd-html5-backend";
import { useEffect, useRef } from "react";

interface DocumentEditorProps {
    url: string;
    name: string;
    isDialogOpen: boolean;
    signatureUrl: string | null;
    placedSignature: {
        pageNumber: number;
        x: number;
        y: number;
        width: number;
        height: number;
    } | null;
    setPlacedSignature: (signature: {
        pageNumber: number;
        x: number;
        y: number;
        width: number;
        height: number;
    } | null) => void;
}

function SignatureThumbnail({ signatureUrl }) {
    const imgRef = useRef<HTMLImageElement>(null);
    const [{ isDragging }, drag, preview] = useDrag(()=>({
        type: "SIGNATURE",
        item: () => {
            const rect = imgRef.current?.getBoundingClientRect();
            return {
                signatureUrl,
                width: rect ? rect.width : 150,
                height: rect ? rect.height : 60
            };
        },
        collect: (monitor)=> ({ isDragging: monitor.isDragging() })
    }));

    useEffect(() => {
        preview(getEmptyImage(), { captureDraggingState: true });
    }, [preview]);

    return (
        <img
            ref={(node)=> {
                drag(node);
                imgRef.current = node;
            }}
            src={signatureUrl}
            alt="Applied Signature"
            className="max-w-full max-h-24 object-contain cursor-grab active:cursor-grabbing select-none"
            style={{ opacity: isDragging ? 0.4 : 1.0 }}
        />
    )
}

function CustomDragLayer() {
    const { itemType, isDragging, item, currentOffset } = useDragLayer((monitor) => ({
        item: monitor.getItem(),
        itemType: monitor.getItemType(),
        currentOffset: monitor.getSourceClientOffset(),
        isDragging: monitor.isDragging(),
    }));

    if (!isDragging || itemType !== "SIGNATURE" || !item) {
        return null;
    }

    const transform = currentOffset
        ? `translate3d(${currentOffset.x}px, ${currentOffset.y}px, 0)`
        : "translate3d(0px, 0px, 0)";

    return (
        <div 
            style={{
                position: "fixed",
                pointerEvents: "none",
                zIndex: 9999,
                left: 0,
                top: 0,
                width: "100%",
                height: "100%",
            }}
        >
            <div
                style={{
                    transform,
                    WebkitTransform: transform,
                    display: currentOffset ? "block" : "none",
                }}
            >
                <img
                    src={item.signatureUrl}
                    alt="Dragging Signature"
                    style={{
                        width: `${item.width}px`,
                        height: `${item.height}px`,
                        objectFit: "contain",
                        opacity: 0.8,
                        border: "2px dashed var(--color-primary-500, #0066cc)",
                        borderRadius: "4px",
                        backgroundColor: "rgba(255, 255, 255, 0.85)",
                        boxShadow: "0 8px 16px rgba(0,0,0,0.15)",
                    }}
                />
            </div>
        </div>
    );
}


export function DocumentEditor({ 
    url, 
    name, 
    isDialogOpen, 
    signatureUrl, 
    placedSignature, 
    setPlacedSignature 
}: DocumentEditorProps) {

    const handleSignatureDrop = (pageNumber, size, position) => {
        const dropInfo = { pageNumber, width: size.width, height: size.height, x: position.x, y: position.y };
        setPlacedSignature(dropInfo);
        console.log("Signature Drop Completed:", dropInfo);
    };

    return (
        <DndProvider backend={HTML5Backend}>
            <div className={`flex flex-1 overflow-hidden relative ${isDialogOpen ? 'select-none pointer-events-none' : ''}`}>
                
                {/* ── Main: PDF Viewer ── */}
                <div className="flex flex-1 overflow-hidden">
                    <PdfViewer 
                        url={url} 
                        signatureUrl={signatureUrl}
                        placedSignature={placedSignature}
                        onSignatureDrop={handleSignatureDrop}
                    />
                </div>

                {/* ── Right Section ── */}
                <aside className="w-64 bg-surface-app border-l border-(--color-border-default) p-4 flex flex-col gap-2">
                    <span className="text-xs font-bold text-(--color-text-secondary) tracking-wider">
                        Document Name
                    </span>
                    <h2 className="text-lg font-bold text-text-primary wrap-break-word">
                        {name}
                    </h2>
                    {signatureUrl && (
                        <>
                            <span className="text-xs font-bold text-(--color-text-secondary) tracking-wider mt-6">
                                Your Signature
                            </span>
                            <div className="w-full bg-white rounded border border-(--color-border-subtle) p-2 flex items-center justify-center shadow-inner">
                                <SignatureThumbnail signatureUrl={signatureUrl} />
                            </div>
                            {placedSignature && (
                                <div className="mt-4 p-3 bg-white rounded border border-(--color-border-subtle) text-xs text-(--color-text-secondary) flex flex-col gap-1 shadow-sm">
                                    <span className="font-semibold text-(--color-text-primary) text-sm mb-1">
                                        Placement Details
                                    </span>
                                    <div>
                                        <span className="font-medium text-(--color-text-primary)">Page:</span> {placedSignature.pageNumber}
                                    </div>
                                    <div>
                                        <span className="font-medium text-(--color-text-primary)">Size:</span> {Math.round(placedSignature.width)}px × {Math.round(placedSignature.height)}px
                                    </div>
                                    <div>
                                        <span className="font-medium text-(--color-text-primary)">Position:</span> Left {placedSignature.x.toFixed(2)}%, Top {placedSignature.y.toFixed(2)}%
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </aside>
            </div>
            <CustomDragLayer />
        </DndProvider>
        
    )
}
