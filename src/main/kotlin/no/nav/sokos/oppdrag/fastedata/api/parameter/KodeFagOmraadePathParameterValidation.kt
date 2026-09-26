package no.nav.sokos.oppdrag.fastedata.api.parameter

const val INVALID_KODE_FAGOMRAADE_PATH_PARAMETER_MESSAGE = "kodeFagomraade må være mellom 2 og 8 tegn og kan kun inneholde bokstaver og tall"

fun KodeFagOmraadePathParameter.validateKodeFagOmraade() {
    validateKodePathParameter(
        parameter = this,
        value = kodeFagOmraade,
        errorMessage = INVALID_KODE_FAGOMRAADE_PATH_PARAMETER_MESSAGE,
    )
}
