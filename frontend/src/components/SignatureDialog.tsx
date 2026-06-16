import { useState } from "react";
import { SignatureInput } from "./SignatureInput";
import { MembersInputList } from "./MembersInputList";
export type { SingedBy } from "./properties";
import type { SignatureDialogProps } from "./properties";

export function SignatureDialog({ isOpen, onApply }: SignatureDialogProps) {
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
                                <SignatureInput onApply={(blob, typedName) => {
                                    onApply("ONLY_ME", { name: typedName, signature: blob });
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
