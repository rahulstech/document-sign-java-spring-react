import { useState } from "react"
import { useParams } from "react-router-dom"
import { useGetDocumentById } from "../hooks/ApiQueryHooks"
import RefreshIcon from "../assets/icons/refresh.svg"
import { IconButton } from "../components/IconButton"
import { PdfViewer } from "../components/PdfViewer"
import { SignatureInput } from "../components/SignatureInput"
import { MembersInputList } from "../components/MembersInputList"

type SingedBy = "ONLY_ME" | "MULTIPLE";

interface SuccessContentProps {
    url: string;
    name: string;
    isDialogOpen: boolean;
    signatureUrl: string | null;
}

interface SignatureDialogProps {
    isOpen: boolean;
    onApply: (type: SingedBy, data: any)=> void;
}

function SuccessContent({ url, name, isDialogOpen, signatureUrl }: SuccessContentProps) {
    return (
        <div className={`flex flex-1 overflow-hidden relative ${isDialogOpen ? 'select-none pointer-events-none' : ''}`}>
            {/* ── Main: PDF Viewer ── */}
            <div className="flex flex-1 overflow-hidden">
                <PdfViewer url={url} />
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
                            <img
                                src={signatureUrl}
                                alt="Applied Signature"
                                className="max-w-full max-h-24 object-contain"
                            />
                        </div>
                    </>
                )}
            </aside>
        </div>
    )
}


function SignatureDialog({ isOpen, onApply }: SignatureDialogProps) {
    const [activeTab, setActiveTab] = useState<"me" | "others">("me");

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-modal flex items-center justify-center p-4 bg-transparent select-none pointer-events-auto">
            {/* Click/select-blocking transparent wrapper */}
            <div className="absolute inset-0 cursor-default" />
            
            {/* Dialog Content Area (using card style: adobe-card) */}
            <div className="adobe-card relative z-10 w-[75vw] h-[90vh] flex flex-col overflow-hidden p-0 shadow-xl border border-(--color-border-default) select-text pointer-events-auto bg-surface-panel">
                {/* Top Section: Tabs (no close button) */}
                <div className="flex justify-between items-center border-b border-(--color-border-default) px-6 bg-surface-toolbar shrink-0">
                    <div className="flex gap-4">
                        <button
                            onClick={() => setActiveTab("me")}
                            className={`adobe-tab ${
                                activeTab === "me"
                                    ? "adobe-tab-active border-b-4 border-primary-500 font-semibold"
                                    : ""
                            }`}
                        >
                            Singed by Me
                        </button>
                        <button
                            onClick={() => setActiveTab("others")}
                            className={`adobe-tab ${
                                activeTab === "others"
                                    ? "adobe-tab-active border-b-4 border-primary-500 font-semibold"
                                    : ""
                            }`}
                        >
                            Singed by Others
                        </button>
                    </div>
                </div>

                {/* Bottom Section: Tab Content (overflow hidden for both x and y) */}
                <div className="flex-1 overflow-hidden bg-surface-panel flex flex-col">
                    {activeTab === "me" ? (
                        <div className="flex flex-col h-full overflow-hidden">
                            <p className="p-6 text-lg font-medium text-(--color-text-secondary) mb-4 shrink-0 leading-relaxed">
                                Sign the document as the only signer. You can choose one of the signature styles for digital signature or upload your signature as an image file.
                            </p>
                            <div className="flex-1 min-h-0 overflow-hidden flex flex-col">
                                {/* TODO: supply the current loggedin user name */}
                                <SignatureInput name="Rahul Bagchi" onApply={(blob) => {
                                    onApply("ONLY_ME", { name: "Rahul Bagchi", email: "domain@email.com", signature: blob });
                                }} />
                            </div>
                        </div>
                    ) : (
                        <div className="flex flex-col h-full overflow-hidden">
                            <p className="p-6 text-lg font-medium text-(--color-text-secondary) mb-4 shrink-0 leading-relaxed">
                                Add multiple people with different roles like Signer, Witness and Verifier. Only People with Signer role can sign. Add name, email and role for each person. Those people will get an email with a special link exclusively for him/her. Later he/she can click on the link to perform the designated action. Each actions are audited and only you, the creator of this document, can see the audit log from the dashboard navigating to this document.
                            </p>
                            <div className="flex-1 min-h-0 overflow-hidden flex flex-col">
                                <MembersInputList onApply={(members) => {
                                    onApply("MULTIPLE", members);
                                }} />
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}

export function EditDocument() {
    const { docId } = useParams()
    const { isLoading, data, isError, error, refetch } = useGetDocumentById(docId!);
    const [isDialogOpen, setIsDialogOpen] = useState(true);
    const [signatureUrl, setSignatureUrl] = useState<string | null>(null);

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
        <>
            <SuccessContent 
                url={data!.url} 
                name={data!.name} 
                isDialogOpen={isDialogOpen}
                signatureUrl={signatureUrl}
            />
            <SignatureDialog 
                isOpen={isDialogOpen} 
                onApply={(type: SingedBy, applyData: any)=>{
                    setIsDialogOpen(false);
                    if (type === "ONLY_ME" && applyData?.signature instanceof Blob) {
                        setSignatureUrl(URL.createObjectURL(applyData.signature));
                    }
                }}
            />
        </>
    )
}