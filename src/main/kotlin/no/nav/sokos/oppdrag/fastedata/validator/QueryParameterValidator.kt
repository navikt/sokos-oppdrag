package no.nav.sokos.oppdrag.fastedata.validator

const val INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE = "kodeFagomraade må være mellom 2 og 8 tegn og kan kun inneholde bokstaver og tall"

fun String.validateFagomraadeQueryParameter(): String {
    require(this.matches(Regex("^[0-9a-zA-ZæøåÆØÅ]{2,8}$"))) {
        INVALID_FAGOMRAADE_QUERY_PARAMETER_MESSAGE
    }
    return this
}
