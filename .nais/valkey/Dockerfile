FROM valkey/valkey:9-alpine3.23@sha256:e1095c6c76ee982cb2d1e07edbb7fb2a53606630a1d810d5a47c9f646b708bf5
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey-entrypoint.sh /app/valkey-entrypoint.sh
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey.conf /app/valkey.conf
ENTRYPOINT ["/app/valkey-entrypoint.sh"]