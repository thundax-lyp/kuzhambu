interface SandwichLogoProps {
    className?: string;
}

export const SandwichLogo = ({ className }: SandwichLogoProps) => {
    return (
        <svg
            className={className}
            width="96"
            height="96"
            viewBox="0 0 96 96"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            role="img"
            aria-label="Sandwich"
        >
            <rect className="logo-tile" width="96" height="96" rx="22" />
            <path
                className="logo-top"
                d="M24 35C24 27.268 30.268 21 38 21H58C65.732 21 72 27.268 72 35V38H24V35Z"
            />
            <path
                className="logo-middle"
                d="M22 43H74V53C74 56.314 71.314 59 68 59H28C24.686 59 22 56.314 22 53V43Z"
            />
            <path
                className="logo-bottom"
                d="M26 62H70V65C70 72.18 64.18 78 57 78H39C31.82 78 26 72.18 26 65V62Z"
            />
            <path
                className="logo-middle-line"
                d="M35 48H61"
                strokeWidth="4"
                strokeLinecap="round"
            />
            <path className="logo-top-line" d="M38 31H58" strokeWidth="4" strokeLinecap="round" />
        </svg>
    );
};
