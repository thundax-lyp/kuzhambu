ARG KUZHAMBU_ELASTICSEARCH_BASE_IMAGE=kuzhambu/elasticsearch-base:8.18.8
ARG KUZHAMBU_ELASTICSEARCH_IK_VERSION=8.18.8

FROM ${KUZHAMBU_ELASTICSEARCH_BASE_IMAGE}

ARG KUZHAMBU_ELASTICSEARCH_IK_VERSION

COPY --chown=elasticsearch:root deploy/elasticsearch/elasticsearch-analysis-ik-${KUZHAMBU_ELASTICSEARCH_IK_VERSION}.zip /tmp/analysis-ik.zip

RUN elasticsearch-plugin install --batch file:///tmp/analysis-ik.zip \
    && rm /tmp/analysis-ik.zip
