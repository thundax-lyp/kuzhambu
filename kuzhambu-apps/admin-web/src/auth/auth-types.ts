export interface LoginFormRecord {
    loginToken: string;
    refreshToken: string;
    expiredAt: number;
    publicKey: string;
}

export interface AccessTokenRecord {
    token: string;
    refreshToken?: string;
    expireAt?: number;
}
