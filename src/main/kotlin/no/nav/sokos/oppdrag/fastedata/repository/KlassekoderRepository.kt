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
                    SUBSTR(XMLSERIALIZE(XMLAGG(XMLTEXT(CONCAT(',', TRIM(FK.KODE_FAGOMRAADE))) ORDER BY FK.KODE_FAGOMRAADE) AS CLOB(1K)), 2) AS KODE_FAGOMRAADE,
                    TRIM(KK.BESKR_KLASSE) AS BESKR_KLASSE,
                    TRIM(A.BESKR_ART) AS BESKR_ART,
                    TRIM(HK.HOVEDKONTO_NAVN) AS HOVEDKONTO_NAVN,
                    TRIM(HK.UNDERKONTO_NAVN) AS UNDERKONTO_NAVN
                    FROM T_KONTOREGEL KR
                          INNER JOIN T_KLASSEKODE KK ON KR.KODE_KLASSE = KK.KODE_KLASSE
                           LEFT JOIN T_FAGO_KLASSEKODE FK ON KR.KODE_KLASSE = FK.KODE_KLASSE
                           LEFT JOIN T_ART A ON KR.ART_ID = A.ART_ID
                           LEFT JOIN T_KONTO HK ON (KR.HOVEDKONTONR = HK.HOVEDKONTONR AND KR.UNDERKONTONR = HK.UNDERKONTONR)
                     WHERE KR.DATO_FOM = (
                         SELECT MAX(KR2.DATO_FOM)
                         FROM T_KONTOREGEL KR2
                         WHERE KR2.KODE_KLASSE = KR.KODE_KLASSE
                           AND KR2.DATO_FOM <= CURRENT_DATE
                     )
                     GROUP BY KR.KODE_KLASSE, KR.DATO_FOM, KR.DATO_TOM, KR.ART_ID,
                              KR.HOVEDKONTONR, KR.UNDERKONTONR, KK.BESKR_KLASSE,
                              A.BESKR_ART, HK.HOVEDKONTO_NAVN, HK.UNDERKONTO_NAVN
                    """.trimIndent(),
                ),
            ) { row ->
                Klassekoder(
                    kodeKlasse = row.string("KODE_KLASSE"),
                    kodeFagomraade = row.stringOrNull("KODE_FAGOMRAADE"),
                    artID = row.int("ART_ID"),
                    datoFom = row.string("DATO_FOM"),
                    datoTom = row.string("DATO_TOM"),
                    hovedkontoNr = row.string("HOVEDKONTONR"),
                    underkontoNr = row.stringOrNull("UNDERKONTONR"),
                    beskrKlasse = row.string("BESKR_KLASSE"),
                    beskrArt = row.stringOrNull("BESKR_ART"),
                    hovedkontoNavn = row.stringOrNull("HOVEDKONTO_NAVN"),
                    underkontoNavn = row.stringOrNull("UNDERKONTO_NAVN"),
                )
            }
        }
}
