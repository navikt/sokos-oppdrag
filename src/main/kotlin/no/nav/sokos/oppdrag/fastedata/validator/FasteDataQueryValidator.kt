package no.nav.sokos.oppdrag.fastedata.validator

val validateFagomraade: (String) -> (Boolean) = { fagomraade ->
    !fagomraade.isEmpty() && fagomraade.matches(Regex("^[0-9a-zA-ZæøåÆØÅ]{0,8}$"))
}
