package no.nav.sokos.oppdrag.attestasjon.domain

import java.time.LocalDate

data class Attestasjon(val attestant: String, val datoUgyldigFom: LocalDate)
