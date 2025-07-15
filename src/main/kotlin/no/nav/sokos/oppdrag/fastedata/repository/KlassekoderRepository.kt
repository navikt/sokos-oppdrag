package no.nav.sokos.oppdrag.fastedata.repository

import com.zaxxer.hikari.HikariDataSource
import kotliquery.LoanPattern.using
import kotliquery.queryOf
import kotliquery.sessionOf

import no.nav.sokos.oppdrag.config.DatabaseConfig
import no.nav.sokos.oppdrag.fastedata.domain.Klassekoder

class KlassekoderRepository(
    private val dataSource: HikariDataSource = DatabaseConfig.db2DataSource,
) {
    fun getAllKlassekoder(): List<Klassekoder> =
        using(sessionOf(dataSource)) { session ->
            session.list(
                queryOf(
                    """
                    SELECT TRIM(KR.KODE_KLASSE) AS KODE_KLASSE,
                           KR.DATO_FOM AS DATO_FOM,
                           KR.DATO_TOM AS DATO_TOM,
                           KR.ART_ID AS ART_ID,
                           TRIM(KR.HOVEDKONTONR) AS HOVEDKONTONR,
                           TRIM(KR.UNDERKONTONR) AS UNDERKONTONR,
                           TRIM(FK.KODE_FAGOMRAADE) AS KODE_FAGOMRAADE
                    FROM T_KONTOREGEL KR
                    INNER JOIN T_KLASSEKODE KK ON KR.KODE_KLASSE = KK.KODE_KLASSE
                    LEFT JOIN T_FAGO_KLASSEKODE FK ON KR.KODE_KLASSE = FK.KODE_KLASSE
                    WHERE KR.DATO_FOM = (
                        SELECT MAX(KR2.DATO_FOM)
                        FROM T_KONTOREGEL KR2
                        WHERE KR2.KODE_KLASSE = KR.KODE_KLASSE
                        AND KR2.DATO_FOM < CURRENT DATE
                    )
                    ORDER BY COLLATION_KEY(KR.KODE_KLASSE, 'UCA400R1_AN_LNB_S3_CU')
                    """.trimIndent(),
                ),
            ) { row ->
                Klassekoder(
                    kodeKlasse = row.string("KODE_KLASSE"),
                    kodeFagomraade = row.stringOrNull("KODE_FAGOMRAADE") ?: "",
                    artID = row.int("ART_ID"),
                    datoFom = row.string("DATO_FOM"),
                    datoTom = row.stringOrNull("DATO_TOM"),
                    hovedkontoNr = row.string("HOVEDKONTONR"),
                    underkontoNr = row.string("UNDERKONTONR"),
                )
            }
        }
}
