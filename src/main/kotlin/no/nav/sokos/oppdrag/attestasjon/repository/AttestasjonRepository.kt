package no.nav.sokos.oppdrag.attestasjon.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.oppdragsinfo.domain.Oppdragsegenskaper

class AttestasjonRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource(),
) {
    fun sok(gjelderId: String): List<Oppdragsegenskaper> {
        return using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT OP.OPPDRAGS_ID,
                        OP.FAGSYSTEM_ID,
                        FO.NAVN_FAGOMRAADE,
                        OP.OPPDRAG_GJELDER_ID,
                        OP.KJOR_IDAG,
                        FG.NAVN_FAGGRUPPE,
                        OS.KODE_STATUS,
                        OS.TIDSPKT_REG
                    FROM T_OPPDRAG OP,
                        T_FAGOMRAADE FO, 
                        T_FAGGRUPPE FG,
                        T_OPPDRAG_STATUS OS
                    WHERE OP.OPPDRAG_GJELDER_ID = :gjelderId
                    AND FO.KODE_FAGOMRAADE = OP.KODE_FAGOMRAADE
                    AND FG.KODE_FAGGRUPPE = FO.KODE_FAGGRUPPE
                    AND OS.OPPDRAGS_ID = OP.OPPDRAGS_ID
                    AND OS.TIDSPKT_REG = (
                    SELECT MAX(OS2.TIDSPKT_REG)
                    FROM T_OPPDRAG_STATUS OS2
                    WHERE OS2.OPPDRAGS_ID = OS.OPPDRAGS_ID)
                    ORDER BY OS.KODE_STATUS
                    """.trimIndent(),
                    mapOf(
                        "gjelderId" to gjelderId,
                    ),
                ),
                mapToOppdragsegenskaper,
            )
        }
    }

    private val mapToOppdragsegenskaper: (Row) -> Oppdragsegenskaper = { row ->
        Oppdragsegenskaper(
            fagsystemId = row.string("FAGSYSTEM_ID").trim(),
            oppdragsId = row.int("OPPDRAGS_ID"),
            navnFagGruppe = row.string("NAVN_FAGGRUPPE").trim(),
            navnFagOmraade = row.string("NAVN_FAGOMRAADE").trim(),
            kjorIdag = row.string("KJOR_IDAG"),
            kodeStatus = row.string("KODE_STATUS"),
        )
    }
}
