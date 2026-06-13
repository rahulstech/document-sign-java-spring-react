import type { ButtonHTMLAttributes } from "react";

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    /** Path to the icon SVG */
    icon: string;
    /** Alt text for the icon */
    iconAlt?: string;
    /** Icon size classes (default: "w-5 h-5") */
    iconClassName?: string;
    /** Show a spinner instead of the icon */
    loading?: boolean;
    /** Spinner color classes (default: "border-white border-t-transparent") */
    spinnerClassName?: string;
}

export function IconButton({
    icon,
    iconAlt = "",
    iconClassName = "w-5 h-5",
    loading = false,
    spinnerClassName = "border-white border-t-transparent",
    children,
    className = "",
    disabled,
    ...rest
}: IconButtonProps) {
    return (
        <button
            className={`adobe-btn ${className}`}
            disabled={disabled || loading}
            {...rest}
        >
            {loading ? (
                <span className={`inline-block w-5 h-5 border-2 rounded-full animate-spin ${spinnerClassName}`} />
            ) : (
                <img src={icon} alt={iconAlt} className={iconClassName} />
            )}
            {children}
        </button>
    );
}
