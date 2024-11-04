package no.nav.sokos.oppdrag.common.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.sokos.oppdrag.integration.util.FnrValidator.isValidPid

internal class PidValidatorTest :
    StringSpec({

        "valid PID should return true" {
            "01010112345".isValidPid() shouldBe true
        }

        "PID with invalid length should return false" {
            "0101011234".isValidPid() shouldBe false
        }

        "PID with non-digit characters should return false" {
            "0101011234A".isValidPid() shouldBe false
        }

        "PID with invalid date should return false" {
            "32010112345".isValidPid() shouldBe false
        }
    })
