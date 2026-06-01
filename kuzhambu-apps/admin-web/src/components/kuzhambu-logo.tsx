interface KuzhambuLogoProps {
    className?: string;
}

export const KuzhambuLogo = ({ className }: KuzhambuLogoProps) => {
    return <img className={className} src="/kuzhambu-logo.svg" alt="Kuzhambu" />;
};
