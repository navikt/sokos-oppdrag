package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.attestasjon.repository.AttestasjonRepository
import no.nav.sokos.oppdrag.attestasjon.service.zos.PostOSAttestasjonResponse200
import no.nav.sokos.oppdrag.attestasjon.service.zos.ZOSConnectService
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.security.AuthToken.getSaksbehandler

private val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class AttestasjonService(
    private val attestasjonRepository: AttestasjonRepository = AttestasjonRepository(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val zosConnectService: ZOSConnectService = ZOSConnectService(),
) {
    fun getOppdrag(
        applicationCall: ApplicationCall,
        attestert: Boolean? = null,
        fagsystemId: String? = null,
        gjelderId: String? = null,
        kodeFagGruppe: String? = null,
        kodeFagOmraade: String? = null,
    ): List<Oppdrag> {
        if (!gjelderId.isNullOrBlank()) {
            val saksbehandler = getSaksbehandler(applicationCall)
            secureLogger.info { "Henter attestasjonsdata for gjelderId: $gjelderId" }
            auditLogger.auditLog(
                AuditLogg(
                    navIdent = saksbehandler.ident,
                    gjelderId = gjelderId,
                    brukerBehandlingTekst = "NAV-ansatt har gjort et oppslag på navn",
                ),
            )
        }

        return attestasjonRepository.getOppdrag(
            gjelderId = gjelderId ?: "",
            fagsystemId = fagsystemId ?: "",
            kodeFaggruppe = kodeFagGruppe ?: "",
            kodeFagomraade = kodeFagOmraade ?: "",
            attestert = attestert,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
    }

    fun getOppdragsDetaljer(oppdragsId: Int): List<OppdragsDetaljer> {
        val oppdragslinjerUtenFluff = attestasjonRepository.getOppdragslinjerWithoutFluff(oppdragsId)

        val linjerMedDatoVedtakTomSattDerDeManglerUnntattDenSisteAvHverKlassekode =
            oppdragslinjerUtenFluff
                .groupBy { l -> l.kodeKlasse }
                .values.flatMap { l ->
                    l.zipWithNext()
                        .map { (curr, next) -> curr.copy(datoVedtakTom = curr.datoVedtakTom ?: next.datoVedtakFom.minusDays(1)) }
                        .toList() + l.last()
                }

        val oppdragsdetaljer =
            linjerMedDatoVedtakTomSattDerDeManglerUnntattDenSisteAvHverKlassekode.map {
                    l ->
                OppdragsDetaljer(
                    ansvarsStedForOppdrag = "kek",
                    ansvarsStedForOppdragsLinje = "kek",
                    antallAttestanter = 1,
                    attestant = "kek",
                    datoUgyldigFom = "kek",
                    datoVedtakFom = l.datoVedtakFom.toString(),
                    datoVedtakTom = l.datoVedtakTom.toString(),
                    delytelsesId = l.delytelseId.toString(),
                    fagGruppe = "kek",
                    fagOmraade = "kek",
                    fagSystemId = "kek",
                    gjelderId = "kek",
                    kodeFagOmraade = "kek",
                    kodeKlasse = l.kodeKlasse,
                    kostnadsStedForOppdrag = "kek",
                    kostnadsStedForOppdragsLinje = "kek",
                    linjeId = l.linjeId.toString(),
                    oppdragsId = l.oppdragsId.toString(),
                    sats = l.sats,
                    satstype = l.typeSats,
                )
            }

        return emptyList()
//        return attestasjonRepository.getOppdragsDetaljer(oppdragsId)
    }

    suspend fun attestereOppdrag(
        applicationCall: ApplicationCall,
        attestasjonRequest: AttestasjonRequest,
    ): PostOSAttestasjonResponse200 {
        val saksbehandler = getSaksbehandler(applicationCall)
        return zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
    }
}
