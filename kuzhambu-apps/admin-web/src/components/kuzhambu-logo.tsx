interface KuzhambuLogoProps {
    className?: string;
}

// AI NOTE: This is the single shared logo renderer.
// Do not duplicate logo paths or alt text in page code.
export const KuzhambuLogo = ({ className }: KuzhambuLogoProps) => {
    return <img className={className} src="/kuzhambu-logo.svg" alt="Kuzhambu" />;
};
