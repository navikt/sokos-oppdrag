FROM valkey/valkey:9-alpine3.23@sha256:d1cc70645bbcef743615463a2fa4616e841407545e18f560aed0c49671a90147
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey-entrypoint.sh /app/valkey-entrypoint.sh
COPY --chown=valkey:valkey --chmod=0077 /.nais/valkey/valkey.conf /app/valkey.conf
ENTRYPOINT ["/app/valkey-entrypoint.sh"]