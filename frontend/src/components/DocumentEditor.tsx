import { PdfViewer } from "./PdfViewer";
import { DndProvider, useDrag, useDragLayer } from "react-dnd";
import { HTML5Backend, getEmptyImage } from "react-dnd-html5-backend";
import { useEffect, useRef, useState } from "react";
import { DragType } from "./properties";
import type { DocumentEditorProps, SignatureData } from "./properties";
import { SignatureAnnotation } from "./SignatureAnnotation";
import { useSelfSign } from "../hooks/ApiQueryHooks";

function SignatureThumbnail({ signatureUrl }: { signatureUrl: string }) {
    const imgRef = useRef<HTMLImageElement>(null);
    const [{ isDragging }, drag, preview] = useDrag(()=>({
        type: DragType.SIGNATURE,
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

function DocumentEditorDragLayer() {
    const { itemType, isDragging, item, currentOffset } = useDragLayer((monitor) => ({
        item: monitor.getItem(),
        itemType: monitor.getItemType(),
        currentOffset: monitor.getSourceClientOffset(),
        isDragging: monitor.isDragging(),
    }));

    if (!isDragging || itemType !== DragType.SIGNATURE || !item) {
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
                <SignatureAnnotation
                    signatureUrl={item.signatureUrl}
                    style={{
                        width: `${item.width}px`,
                        height: `${item.height}px`,
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
    documentId,
    url, 
    name, 
    isDialogOpen, 
    signatureUrl,
    setIsSave
}: DocumentEditorProps) {

    const [placedSignature, setPlacedSignature] = useState<SignatureData | null>(null);
    const { isPending, isSuccess, mutateAsync } = useSelfSign();

    const handleSignatureDrop = (
        pageNumber: number,
        position: { left: number; top: number; width: number; height: number }
    ) => {
        const dropInfo = { pageNumber, ...position };
        setPlacedSignature(dropInfo);
        console.log("Signature Drop Completed:", dropInfo);
    };

    const handleSign = async () => {
        if (!placedSignature || !signatureUrl) return;

        try {
            const response = await fetch(signatureUrl);
            const blob = await response.blob();
            const { pageNumber, left, top, width, height } = placedSignature;

            await mutateAsync({
                documentId,
                signature: {
                    type: blob.type,
                    size: blob.size,
                    blob
                },
                pageNumber,
                bounds: { left, top, width, height }
            });

        } catch (err) {
            alert("Failed to sign document.");
        }
    };

    if (isSuccess) {
        setIsSave(isSuccess);
        return null;
    }

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
                            <p className="text-xs text-(--color-text-secondary) mt-2 leading-relaxed">
                                Drag this signature and drop in a page. Below the signature you can find a unqiue  id. You can verify signture id in document audit log for audit purpose from the document dashboard.
                            </p>
                        </>
                    )}
                    
                    <div className="mt-auto pt-4">
                        <button
                            type="button"
                            disabled={!placedSignature || isPending}
                            className={`adobe-btn adobe-btn-primary w-full py-2 flex items-center justify-center text-sm font-semibold transition-all ${
                                !placedSignature ? "opacity-50 cursor-not-allowed" : ""
                            }`}
                            onClick={handleSign}
                        >
                            Sign
                        </button>
                    </div>
                </aside>
            </div>
            <DocumentEditorDragLayer />
        </DndProvider>
        
    )
}
