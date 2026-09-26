package no.nav.sokos.oppdrag.fastedata.api.parameter

const val INVALID_KODE_FAGGRUPPE_PATH_PARAMETER_MESSAGE = "kodeFaggruppe må være mellom 2 og 8 tegn og kan kun inneholde bokstaver og tall"

fun KodeFaggruppePathParameter.validateKodeFaggruppe() {
    validateKodePathParameter(
        parameter = this,
        value = kodeFaggruppe,
        errorMessage = INVALID_KODE_FAGGRUPPE_PATH_PARAMETER_MESSAGE,
    )
}
