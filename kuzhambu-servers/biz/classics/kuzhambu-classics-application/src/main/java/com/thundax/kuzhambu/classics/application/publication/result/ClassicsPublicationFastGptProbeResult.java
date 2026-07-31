package com.thundax.kuzhambu.classics.application.publication.result;

public record ClassicsPublicationFastGptProbeResult(boolean present, Boolean forbidden) {

    public static ClassicsPublicationFastGptProbeResult missing() {
        return new ClassicsPublicationFastGptProbeResult(false, null);
    }
}
