package no.nav.sokos.oppdrag.oppdragsinfo.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import mu.KotlinLogging
import no.nav.sokos.oppdrag.common.audit.AuditLogg
import no.nav.sokos.oppdrag.common.audit.AuditLogger
import no.nav.sokos.oppdrag.common.audit.Saksbehandler
import no.nav.sokos.oppdrag.common.config.DatabaseConfig
import no.nav.sokos.oppdrag.common.config.SECURE_LOGGER
import no.nav.sokos.oppdrag.common.security.JwtClaimHandler.getSaksbehandler
import no.nav.sokos.oppdrag.integration.ereg.EregService
import no.nav.sokos.oppdrag.integration.pdl.PdlService
import no.nav.sokos.oppdrag.integration.tp.TpService
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererEnheter
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererGrader
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererKidliste
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererKravhavere
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererMaksdatoer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererOmposteringer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererSkyldnere
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererTekster
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.eksistererValutaer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.erOppdragTilknyttetBruker
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentEnheter
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentFaggrupper
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentGrader
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentKidliste
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentKorreksjoner
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentKravhavere
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentMaksdatoer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsEnhetsHistorikk
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsLinjeAttestanter
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsLinjeStatuser
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsLinjer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsListe
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsOmposteringer
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOppdragsStatusHistorikk
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentOvrige
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentSkyldnere
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentTekster
import no.nav.sokos.oppdrag.oppdragsinfo.database.OppdragsInfoRepository.hentValutaer
import no.nav.sokos.oppdrag.oppdragsinfo.database.RepositoryExtensions.useAndHandleErrors
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Attestant
import no.nav.sokos.oppdrag.oppdragsinfo.domain.FagGruppe
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Grad
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kid
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Kravhaver
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.LinjeStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Maksdato
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ompostering
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragDetaljer
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragStatus
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsEnhet
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsInfo
import no.nav.sokos.oppdrag.oppdragsinfo.domain.OppdragsLinjeDetaljer
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Ovrig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Skyldner
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Tekst
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Valuta

private val logger = KotlinLogging.logger {}
val secureLogger = KotlinLogging.logger(SECURE_LOGGER)

class OppdragsInfoService(
    private val databaseConfig: DatabaseConfig = DatabaseConfig(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val pdlService: PdlService = PdlService(),
    private val eregService: EregService = EregService(),
    private val tpService: TpService = TpService(),
) {
    suspend fun sokOppdrag(
        gjelderId: String,
        faggruppeKode: String?,
        applicationCall: ApplicationCall,
    ): List<OppdragsInfo> {
        val saksbehandler = hentSaksbehandler(applicationCall)
        logger.info(
            "Søker etter oppdrag med gjelderId: $gjelderId",
        )
        secureLogger.info("Søker etter oppdrag med gjelderId: $gjelderId")
        auditLogger.auditLog(
            AuditLogg(
                saksbehandler = saksbehandler.ident,
                gjelderId = gjelderId,
                brukerBehandlingTekst = "NAV-ansatt har gjort et søk på oppdrag",
            ),
        )

        val oppdragsInfo =
            databaseConfig.connection.useAndHandleErrors {
                it.hentOppdragsInfo(gjelderId).firstOrNull()
            } ?: return emptyList()

        val oppdragsListe =
            databaseConfig.connection.useAndHandleErrors { it.hentOppdragsListe(oppdragsInfo.gjelderId, faggruppeKode) }

        val gjelderNavn = hentNavnForGjelderId(oppdragsInfo.gjelderId)

        return listOf(
            OppdragsInfo(
                gjelderId = oppdragsInfo.gjelderId,
                gjelderNavn = gjelderNavn,
                oppdragsListe = oppdragsListe,
            ),
        )
    }

    fun hentFaggrupper(): List<FagGruppe> {
        return databaseConfig.connection.useAndHandleErrors {
            it.hentFaggrupper().toList()
        }
    }

    fun hentOppdragsOmposteringer(
        gjelderId: String,
        oppdragsId: String,
    ): List<Ompostering> {
        secureLogger.info("Henter omposteringer for gjelderId: $gjelderId")
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOppdragsOmposteringer(gjelderId, oppdragsId.toInt()).toList()
        }
    }

    fun hentOppdrag(
        gjelderId: String,
        oppdragsId: String,
    ): OppdragDetaljer {
        secureLogger.info("Henter oppdragslinjer med oppdragsId: $oppdragsId")

        val oppdragKnyttetTilbruker =
            databaseConfig.connection.useAndHandleErrors {
                it.erOppdragTilknyttetBruker(gjelderId, oppdragsId.toInt())
            }

        if (!oppdragKnyttetTilbruker) {
            throw RequestValidationException(
                HttpStatusCode.BadRequest.value,
                listOf("Oppdraget er ikke knyttet til bruker"),
            )
        }

        return OppdragDetaljer(
            enhet =
                databaseConfig.connection.useAndHandleErrors {
                    it.hentOppdragsEnhet(
                        oppdragsId = oppdragsId.toInt(),
                    ).first()
                },
            behandlendeEnhet =
                databaseConfig.connection.useAndHandleErrors {
                    it.hentOppdragsEnhet(
                        "BEH",
                        oppdragsId.toInt(),
                    ).firstOrNull()
                },
            harOmposteringer =
                databaseConfig.connection.useAndHandleErrors {
                    it.eksistererOmposteringer(
                        gjelderId,
                        oppdragsId.toInt(),
                    )
                },
            oppdragsLinjer =
                databaseConfig.connection.useAndHandleErrors {
                    it.hentOppdragsLinjer(oppdragsId.toInt()).toList()
                },
        )
    }

    fun hentOppdragsEnhetsHistorikk(oppdragsId: String): List<OppdragsEnhet> {
        secureLogger.info("Henter oppdragsEnhetsHistorikk for oppdrag: $oppdragsId")
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOppdragsEnhetsHistorikk(oppdragsId.toInt()).toList()
        }
    }

    fun hentOppdragsStatusHistorikk(oppdragsId: String): List<OppdragStatus> {
        secureLogger.info("Henter oppdragsStatusHistorikk for oppdrag: $oppdragsId")
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOppdragsStatusHistorikk(oppdragsId.toInt()).toList()
        }
    }

    fun hentOppdragsLinjeStatuser(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeStatus> {
        secureLogger.info("Henter oppdragslinjstatuser for oppdrag : $oppdragsId, linje : $linjeId")
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOppdragsLinjeStatuser(oppdragsId.toInt(), linjeId.toInt()).toList()
        }
    }

    fun hentOppdragsLinjeAttestanter(
        oppdragsId: String,
        linjeId: String,
    ): List<Attestant> {
        secureLogger.info("Henter oppdragslinjstatuser for oppdrag : $oppdragsId, linje : $linjeId")
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOppdragsLinjeAttestanter(oppdragsId.toInt(), linjeId.toInt()).toList()
        }
    }

    fun hentOppdragsLinjeDetaljer(
        oppdragsId: String,
        linjeId: String,
    ): List<OppdragsLinjeDetaljer> {
        secureLogger.info("Henter oppdragslinjedetaljer for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            listOf(
                OppdragsLinjeDetaljer(
                    korrigerteLinjeIder = korrigerteLinjeIder,
                    harValutaer = it.eksistererValutaer(oppdragsId.toInt(), linjeId.toInt()),
                    harSkyldnere = it.eksistererSkyldnere(oppdragsId.toInt(), linjeId.toInt()),
                    harKravhavere = it.eksistererKravhavere(oppdragsId.toInt(), linjeId.toInt()),
                    harEnheter = it.eksistererEnheter(oppdragsId.toInt(), linjeId.toInt()),
                    harGrader = it.eksistererGrader(oppdragsId.toInt(), linjeId.toInt()),
                    harTekster = it.eksistererTekster(oppdragsId.toInt(), linjeId.toInt()),
                    harKidliste = it.eksistererKidliste(oppdragsId.toInt(), linjeId.toInt()),
                    harMaksdatoer = it.eksistererMaksdatoer(oppdragsId.toInt(), linjeId.toInt()),
                ),
            )
        }
    }

    fun hentOppdragsLinjeValuta(
        oppdragsId: String,
        linjeId: String,
    ): List<Valuta> {
        secureLogger.info("Henter oppdragslinjevaluta for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentValutaer(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeSkyldner(
        oppdragsId: String,
        linjeId: String,
    ): List<Skyldner> {
        secureLogger.info("Henter oppdragslinjeSkyldner for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentSkyldnere(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeKravhaver(
        oppdragsId: String,
        linjeId: String,
    ): List<Kravhaver> {
        secureLogger.info("Henter oppdragslinjeKravhaver for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentKravhavere(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeEnheter(
        oppdragsId: String,
        linjeId: String,
    ): List<LinjeEnhet> {
        secureLogger.info("Henter oppdragslinjeEnheter for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentEnheter(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeGrad(
        oppdragsId: String,
        linjeId: String,
    ): List<Grad> {
        secureLogger.info("Henter oppdragslinjeGrad for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentGrader(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeTekst(
        oppdragsId: String,
        linjeId: String,
    ): List<Tekst> {
        secureLogger.info("Henter oppdragslinjeTekst for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentTekster(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeKidListe(
        oppdragsId: String,
        linjeId: String,
    ): List<Kid> {
        secureLogger.info("Henter oppdragslinjeKidliste for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentKidliste(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeMaksdato(
        oppdragsId: String,
        linjeId: String,
    ): List<Maksdato> {
        secureLogger.info("Henter oppdragslinjeMaksdato for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentMaksdatoer(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    fun hentOppdragsLinjeOvrig(
        oppdragsId: String,
        linjeId: String,
    ): List<Ovrig> {
        secureLogger.info("Henter oppdragslinjeOvrig for oppdrag : $oppdragsId, linje : $linjeId")
        val korrigerteLinjeIder: MutableList<Int> = finnKorrigerteLinjer(oppdragsId, linjeId)
        return databaseConfig.connection.useAndHandleErrors {
            it.hentOvrige(oppdragsId.toInt(), korrigerteLinjeIder.joinToString(",")).toList()
        }
    }

    private fun finnKorrigerteLinjer(
        oppdragsId: String,
        linjeId: String,
    ): MutableList<Int> {
        val korrigerteLinjer = databaseConfig.connection.useAndHandleErrors { it.hentKorreksjoner(oppdragsId) }
        val korrigerteLinjeIder: MutableList<Int> = ArrayList()
        if (korrigerteLinjer.isNotEmpty()) {
            var linje = linjeId.toInt()
            for (korreksjon in korrigerteLinjer) {
                val korrLinje = korreksjon.linje
                if (korrLinje == linje) {
                    korrigerteLinjeIder.add(korrLinje)
                    linje = korreksjon.korrigertLinje
                }
            }
            korrigerteLinjeIder.add(linje)
        } else {
            korrigerteLinjeIder.add(linjeId.toInt())
        }
        return korrigerteLinjeIder
    }

    private suspend fun hentNavnForGjelderId(gjelderId: String): String =
        when {
            gjelderId.toLong() > 80000000000 -> tpService.getLeverandorNavn(gjelderId).navn
            gjelderId.toLong() < 80000000000 ->
                pdlService.getPersonNavn(gjelderId)?.navn?.firstOrNull()
                    ?.run { mellomnavn?.let { "$fornavn $mellomnavn $etternavn" } ?: "$fornavn $etternavn" } ?: ""

            else -> eregService.getOrganisasjonsNavn(gjelderId).navn.sammensattnavn
        }

    private fun hentSaksbehandler(call: ApplicationCall): Saksbehandler {
        return getSaksbehandler(call)
    }
}
