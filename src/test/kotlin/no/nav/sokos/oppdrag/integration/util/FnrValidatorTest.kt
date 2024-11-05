package no.nav.sokos.oppdrag.integration.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.sokos.oppdrag.integration.util.FnrValidator.isValidPid

internal class FnrValidatorTest : StringSpec({

    "gyldig FNR skal returnere true" {
        "01010112345".isValidPid() shouldBe true
    }

    "FNR med ugyldig lengde skal returnere false" {
        "0101011234".isValidPid() shouldBe false
    }

    "FNR med ikke tall verdi skal returnere false" {
        "0101011234A".isValidPid() shouldBe false
    }

    "FNR med ugyldig dato skal returnere false" {
        "12140112345".isValidPid() shouldBe false
        // Ugyldig D-nummer
        "32120112345".isValidPid() shouldBe false
        "72120112345".isValidPid() shouldBe false
        // Ugyldig Dolly-nummer
        "12400112345".isValidPid() shouldBe false
        "12531112345".isValidPid() shouldBe false
    }

    "FNR som er en D-nummer skal returnere true" {
        "42010112345".isValidPid() shouldBe true
        "71010112345".isValidPid() shouldBe true
    }

    "FNR som er en Dolly-nummer skal returnere true" {
        "12410112345".isValidPid() shouldBe true
        "12521112345".isValidPid() shouldBe true
    }
})
