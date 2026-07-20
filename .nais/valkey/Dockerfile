FROM valkey/valkey:9-alpine3.23@sha256:c9b77919daeba2c02ad954d0c844cc4e7142069d177b89c5fd771f405daf9e02
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey-entrypoint.sh /app/valkey-entrypoint.sh
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey.conf /app/valkey.conf
ENTRYPOINT ["/app/valkey-entrypoint.sh"]