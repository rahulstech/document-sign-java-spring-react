import RefreshIcon from "../assets/icons/refresh.svg";
import { IconButton } from "./IconButton";

interface ErrorWithRetryViewProps {
    message: string;
    onRetry: () => void;
}

export function ErrorWithRetryView({ message, onRetry }: ErrorWithRetryViewProps) {
    return (
        <div className="w-4/12 mx-auto pt-12">
            <div className="adobe-card flex flex-col items-center gap-4 px-8 py-6">
                <p className="text-xl text-wrap text-danger">{message}</p>
                <IconButton
                    className="adobe-btn-primary rounded-full"
                    icon={RefreshIcon}
                    iconAlt="Retry"
                    iconClassName="w-4 h-4 filter-[invert(1)]"
                    onClick={onRetry}
                >
                    Retry
                </IconButton>
            </div>
        </div>
    );
}
