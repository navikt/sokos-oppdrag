package no.nav.sokos.oppdrag.fastedata.validator

fun validateFagomraadeQueryParameter(kodeFagomraade: String) {
    require(kodeFagomraade.matches(Regex("^[0-9a-zA-ZæøåÆØÅ]{2,8}$"))) {
        "Kode for fagområde må være en streng på min 2 og maks 8 tegn"
    }
}
