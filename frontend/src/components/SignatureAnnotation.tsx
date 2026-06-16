import { forwardRef, type ForwardedRef, type CSSProperties } from "react";

export interface SignatureAnnotationProps {
    signatureUrl: string;
    style?: CSSProperties;
    className?: string;
}

export const SignatureAnnotation = forwardRef(function SignatureAnnotation(
    { signatureUrl, style, className }: SignatureAnnotationProps,
    ref: ForwardedRef<HTMLDivElement>
) {
    return (
        <div 
            ref={ref}
            className={className}
            style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                position: "relative",
                ...style
            }}
        >
            <img 
                src={signatureUrl}
                alt="Signature"
                style={{
                    width: "100%",
                    height: "100%",
                    objectFit: "contain",
                    pointerEvents: "none"
                }}
            />
            <span 
                style={{
                    fontSize: "9px",
                    color: "var(--color-text-secondary, #666)",
                    pointerEvents: "none",
                    whiteSpace: "nowrap",
                    position: "absolute",
                    bottom: "-14px"
                }}
            >
                &lt;&lt; signature id &gt;&gt;
            </span>
        </div>
    );
});
