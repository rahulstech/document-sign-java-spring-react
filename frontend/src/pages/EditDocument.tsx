import { useState, useEffect } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { useGetDocumentInfoById } from "../hooks/ApiQueryHooks"
import { DocumentEditor } from "../components/DocumentEditor"
import { SignatureDialog, type SingedBy } from "../components/SignatureDialog"
import { LoadingView } from "../components/LoadingView"
import { ErrorWithRetryView } from "../components/ErrorWithRetryView"

export function EditDocument() {
    const { docId } = useParams()
    const { isLoading, data, isError, error, refetch } = useGetDocumentInfoById(docId!);
    const [isDialogOpen, setIsDialogOpen] = useState(true);
    const [signatureUrl, setSignatureUrl] = useState<string | null>(null);
    const [isSave, setIsSave] = useState(false);
    const navigate = useNavigate();


    if (isSave) {
        navigate(`/docs/${docId}/dashboard`);
        return null;
    }

    if (isLoading) {
        return <LoadingView message="Loading your document" />;
    }

    if (isError) {
        return (
            <ErrorWithRetryView
                message={error.message}
                onRetry={() => refetch()}
            />
        );
    }

    if (!data || !data.canEdit) {
        return <ActionNotAllowedView docId={docId!} />;
    }

    return (
        <>
            <DocumentEditor 
                documentId={docId!}
                url={data!.url} 
                name={data!.name} 
                isDialogOpen={isDialogOpen}
                signatureUrl={signatureUrl}
                setIsSave={setIsSave}
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

function ActionNotAllowedView({ docId }: { docId: string }) {
    const navigate = useNavigate();
    const [timeLeft, setTimeLeft] = useState(5);

    useEffect(() => {
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    navigate(`/docs/${docId}/dashboard`);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [docId, navigate]);

    return (
        <div className="flex flex-col items-center justify-center min-h-[80vh] px-4 animate-adobe-fade-in">
            <div className="adobe-panel max-w-md w-full text-center shadow-lg bg-white p-8 rounded-xl flex flex-col items-center gap-6">
                {/* Visual Lock Icon */}
                <div className="w-16 h-16 rounded-full bg-[#FFEBE7] flex items-center justify-center text-adobe-red text-3xl font-bold animate-adobe-scale-in">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        strokeWidth={2}
                        stroke="currentColor"
                        className="w-8 h-8 text-adobe-red"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z"
                        />
                    </svg>
                </div>

                <div className="space-y-2">
                    <h2 className="text-2xl font-semibold text-gray-900 tracking-tight">
                        Action Not Allowed
                    </h2>
                    <p className="text-sm text-gray-500 leading-relaxed">
                        You do not have the required permissions to edit this document.
                    </p>
                </div>

                <div className="w-full bg-gray-100 rounded-full h-1.5 overflow-hidden animate-pulse">
                    <div
                        className="bg-adobe-red h-full transition-all duration-1000 ease-linear"
                        style={{ width: `${(timeLeft / 5) * 100}%` }}
                    />
                </div>

                <div className="text-xs text-gray-400 font-medium">
                    Redirecting to your dashboard in <span className="text-adobe-red font-semibold text-sm">{timeLeft}</span> seconds...
                </div>

                <button
                    onClick={() => navigate(`/docs/${docId}/dashboard`, { replace: true })}
                    className="adobe-btn adobe-btn-primary w-full transition-all duration-200"
                >
                    Go to Dashboard Now
                </button>
            </div>
        </div>
    );
}