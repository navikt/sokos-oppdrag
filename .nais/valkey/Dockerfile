FROM valkey/valkey:8.1-alpine
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey-entrypoint.sh /app/valkey-entrypoint.sh
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey.conf /app/valkey.conf
ENTRYPOINT ["/app/valkey-entrypoint.sh"]