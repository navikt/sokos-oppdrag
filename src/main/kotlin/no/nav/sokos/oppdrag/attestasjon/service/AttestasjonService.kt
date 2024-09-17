package no.nav.sokos.oppdrag.attestasjon.service

import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import no.nav.sokos.oppdrag.attestasjon.api.model.AttestasjonRequest
import no.nav.sokos.oppdrag.attestasjon.domain.Attestasjon
import no.nav.sokos.oppdrag.attestasjon.domain.FagOmraade
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdrag
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragsDetaljer
import no.nav.sokos.oppdrag.attestasjon.domain.Oppdragslinje
import no.nav.sokos.oppdrag.attestasjon.domain.OppdragslinjePlain
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
        fagSystemId: String? = null,
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
            fagSystemId = fagSystemId ?: "",
            kodeFaggruppe = kodeFagGruppe ?: "",
            kodeFagomraade = kodeFagOmraade ?: "",
            attestert = attestert,
        )
    }

    fun getFagOmraade(): List<FagOmraade> {
        return attestasjonRepository.getFagOmraader()
    }

    fun getOppdragsDetaljer(oppdragsId: Int): OppdragsDetaljer {
        val oppdragslinjerPlain: List<OppdragslinjePlain> = attestasjonRepository.getOppdragslinjerPlain(oppdragsId)

        val oppdragsInfo = attestasjonRepository.getEnkeltOppdrag(oppdragsId)

        val linjerMedDatoVedtakTomSattDerDeManglerUnntattDenSisteAvHverKlassekode: List<OppdragslinjePlain> =
            oppdragslinjerPlain
                .groupBy { l -> l.kodeKlasse }
                .values.flatMap { l ->
                    l.zipWithNext()
                        .map { (curr, next) ->
                            curr.copy(
                                datoVedtakTom = curr.datoVedtakTom ?: next.datoVedtakFom.minusDays(1),
                            )
                        }
                        .toList() + l.last()
                }

        val linjeIder = oppdragslinjerPlain.map { l -> l.linjeId }.toList()

        val kostnadssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BOS")
        val ansvarssteder = attestasjonRepository.getEnhetForLinjer(oppdragsId, linjeIder, "BEH")
        val attestasjoner: Map<Int, List<Attestasjon>> = attestasjonRepository.getAttestasjonerForLinjer(oppdragsId, linjeIder)

        val oppdragsdetaljer =
            OppdragsDetaljer(
                ansvarsStedForOppdrag = oppdragsInfo.ansvarsSted,
                oppdragsId = oppdragsInfo.oppdragsId.toString(),
                antallAttestanter = oppdragsInfo.antallAttestanter,
                fagGruppe = oppdragsInfo.fagGruppe,
                fagOmraade = oppdragsInfo.fagOmraade,
                fagSystemId = oppdragsInfo.fagSystemId,
                gjelderId = oppdragsInfo.gjelderId,
                kostnadsStedForOppdrag = oppdragsInfo.kostnadsSted,
                kodeFagOmraade = oppdragsInfo.kodeFagOmraade,
                linjer =
                    linjerMedDatoVedtakTomSattDerDeManglerUnntattDenSisteAvHverKlassekode.map { l ->
                        Oppdragslinje(
                            oppdragsLinje = l,
                            ansvarsStedForOppdragsLinje = ansvarssteder[l.linjeId],
                            kostnadsStedForOppdragsLinje = kostnadssteder[l.linjeId],
                            attestasjoner = attestasjoner[l.linjeId] ?: emptyList(),
                        )
                    },
            )
        return oppdragsdetaljer
    }

    suspend fun attestereOppdrag(
        applicationCall: ApplicationCall,
        attestasjonRequest: AttestasjonRequest,
    ): PostOSAttestasjonResponse200 {
        val saksbehandler = getSaksbehandler(applicationCall)
        return zosConnectService.attestereOppdrag(attestasjonRequest, saksbehandler.ident)
    }
}
