import { useState } from "react";
import AddIcon from "../assets/icons/add.svg";
import DeleteIcon from "../assets/icons/delete.svg";

type MemberRole = "SIGNER" | "WITNESS" | "VERIFIER";

interface MemberItem {
    key: string;
    name: string;
    email: string;
    role: MemberRole;
    nameError?: string;
    emailError?: string;
}

interface MemberInputProps {
    member: MemberItem;
    onChange: (key: string, updatedFields: Partial<MemberItem>) => void;
    onRemove: (key: string) => void;
}

interface MembersInputListProps {
    onApply?: (members: { name: string; email: string; role: MemberRole }[]) => void;
}



function MemberInput({ member, onChange, onRemove }: MemberInputProps) {
    return (
        <div className="flex gap-4 items-start w-full bg-surface-card p-4 border border-(--color-border-default) rounded-md shadow-sm shrink-0">
            {/* Full Name Input */}
            <div className="flex-1 min-w-0 flex flex-col gap-1.5">
                <label className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider">
                    Full Name
                </label>
                <input
                    type="text"
                    value={member.name}
                    onChange={(e) => onChange(member.key, { name: e.target.value, nameError: undefined })}
                    placeholder="Enter full name"
                    className={`adobe-input ${member.nameError ? "border-danger focus:border-danger focus:ring-danger" : ""}`}
                />
                {member.nameError && (
                    <span className="text-xs text-danger font-medium mt-1">
                        {member.nameError}
                    </span>
                )}
            </div>

            {/* Email Input */}
            <div className="flex-1 min-w-0 flex flex-col gap-1.5">
                <label className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider">
                    Email
                </label>
                <input
                    type="email"
                    value={member.email}
                    onChange={(e) => onChange(member.key, { email: e.target.value, emailError: undefined })}
                    placeholder="Enter email address"
                    className={`adobe-input ${member.emailError ? "border-danger focus:border-danger focus:ring-danger" : ""}`}
                />
                {member.emailError && (
                    <span className="text-xs text-danger font-medium mt-1">
                        {member.emailError}
                    </span>
                )}
            </div>

            {/* Role Drop Down */}
            <div className="w-48 shrink-0 flex flex-col gap-1.5">
                <label className="text-xs font-bold text-(--color-text-secondary) uppercase tracking-wider">
                    Role
                </label>
                <select
                    value={member.role}
                    onChange={(e) => onChange(member.key, { role: e.target.value as MemberRole })}
                    className="adobe-input cursor-pointer appearance-none bg-[url('data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%2020%2020%22%20fill%3D%22none%22%3E%3Cpath%20d%3D%22M7%209l3%203%203-3%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%221.5%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%2F%3E%3C%2Fsvg%3E')] bg-position-[right_0.5rem_center] bg-size-[1.25rem_1.25rem] bg-no-repeat pr-8"
                >
                    <option value="SIGNER">Signer</option>
                    <option value="WITNESS">Witness</option>
                    <option value="VERIFIER">Verifier</option>
                </select>
            </div>

            {/* Remove Button */}
            <button
                type="button"
                onClick={() => onRemove(member.key)}
                className="mt-6 p-2 text-danger hover:bg-danger/10 rounded-full transition-all cursor-pointer flex items-center justify-center shrink-0 animate-adobe-fade-in"
                title="Remove member"
            >
                <img src={DeleteIcon} alt="delete" className="w-5 h-5" />
            </button>
        </div>
    );
}


export function MembersInputList({ onApply }: MembersInputListProps) {
    const [members, setMembers] = useState<MemberItem[]>([
        { key: Date.now().toString(), name: "", email: "", role: "SIGNER" }
    ]);

    const handleAddMember = () => {
        const newMember: MemberItem = {
            key: Date.now().toString(),
            name: "",
            email: "",
            role: "SIGNER"
        };
        // Add new member at the top of the list
        setMembers([newMember, ...members]);
    };

    const handleRemoveMember = (key: string) => {
        setMembers(members.filter((m) => m.key !== key));
    };

    const handleMemberChange = (key: string, updatedFields: Partial<MemberItem>) => {
        setMembers(
            members.map((m) => (m.key === key ? { ...m, ...updatedFields } : m))
        );
    };

    const handleApply = () => {
        let hasErrors = false;
        const updatedMembers = [...members];
        const seenEmails = new Set<string>();

        for (let i = 0; i < updatedMembers.length; i++) {
            const member = { ...updatedMembers[i] };
            let nameError: string | undefined = undefined;
            let emailError: string | undefined = undefined;

            // Validate Name
            if (!member.name.trim()) {
                nameError = "Full Name is required";
                hasErrors = true;
            }

            // Validate Email
            const emailVal = member.email.trim();
            if (!emailVal) {
                emailError = "Email is required";
                hasErrors = true;
            } else {
                // Check duplicate emails
                const lowerEmail = emailVal.toLowerCase();
                if (seenEmails.has(lowerEmail)) {
                    emailError = "Duplicate email";
                    hasErrors = true;
                } else {
                    seenEmails.add(lowerEmail);
                }
            }

            member.nameError = nameError;
            member.emailError = emailError;
            updatedMembers[i] = member;
        }

        // Set state with validation messages
        setMembers(updatedMembers);

        if (!hasErrors) {
            // Clean keys and errors from array
            const cleanMembers = updatedMembers.map(({ name, email, role }) => ({
                name: name.trim(),
                email: email.trim(),
                role
            }));
            console.log("Applying members list:", cleanMembers);
            if (onApply) {
                onApply(cleanMembers);
            }
        }
    };

    return (
        <div className="flex flex-col h-full w-full gap-4 overflow-hidden">
            {/* Top Area: Add Member Button */}
            <div className="flex justify-end items-center shrink-0 pl-8 pr-8">
                <button
                    type="button"
                    onClick={handleAddMember}
                    className="adobe-btn adobe-btn-primary px-4 py-2 flex items-center gap-2"
                >
                    <img src={AddIcon} alt="add" className="w-4 h-4 filter-[invert(1)]" />
                    Add New Member
                </button>
            </div>

            {/* Scrollable list of MemberInput items */}
            <div className="flex-1 overflow-y-auto flex flex-col gap-3 min-h-0 pl-8 pr-8 pt-4">
                {members.length === 0 ? (
                    <div className="flex flex-col items-center justify-center p-8 border border-dashed border-(--color-border-default) rounded-md text-(--color-text-tertiary) text-sm bg-surface-card flex-1">
                        No members added yet. Click "Add New Member" to begin.
                    </div>
                ) : (
                    members.map((member) => (
                        <MemberInput
                            key={member.key}
                            member={member}
                            onChange={handleMemberChange}
                            onRemove={handleRemoveMember}
                        />
                    ))
                )}
            </div>

            {/* Bottom Right: Apply Button */}
            <div className="flex justify-end shrink-0 p-6 border-t border-(--color-border-default)">
                <button
                    type="button"
                    onClick={handleApply}
                    className="adobe-btn adobe-btn-primary px-6"
                >
                    Apply
                </button>
            </div>
        </div>
    );
}
