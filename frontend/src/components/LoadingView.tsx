interface LoadingViewProps {
    message?: string;
}

export function LoadingView({ message = "Loading your document" }: LoadingViewProps) {
    return (
        <div className="w-4/12 mx-auto pt-12">
            <div className="adobe-card flex items-center gap-3 px-6 py-4">
                <span className="inline-block w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                <span className="text-sm text-(--color-text-secondary)">{message}</span>
            </div>
        </div>
    );
}
