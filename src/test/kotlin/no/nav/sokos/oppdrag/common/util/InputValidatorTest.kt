package no.nav.sokos.oppdrag.common.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

import no.nav.sokos.oppdrag.common.util.GjelderIdValidator.isValidGjelderId

internal class InputValidatorTest :
    FunSpec({

        test("gjelderId er gyldig med 9 siffer") {
            val validGjelderId = "123456789"
            val result = isValidGjelderId(validGjelderId)
            result shouldBe true
        }

        test("gjelderId er gyldig med 11 siffer") {
            val invalidGjelderId = "12345678901"
            val result = isValidGjelderId(invalidGjelderId)
            result shouldBe true
        }

        test("gjelderId er ugyldig med 8 siffer") {
            val invalidGjelderId = "12345678"
            val result = isValidGjelderId(invalidGjelderId)
            result shouldBe false
        }

        test("gjelderId er ugyldig med 10 siffer") {
            val invalidGjelderId = "123456789012"
            val result = isValidGjelderId(invalidGjelderId)
            result shouldBe false
        }

        test("gjelderId er ugyldig med bokstaver") {
            val invalidGjelderId = "12345678a"
            val result = isValidGjelderId(invalidGjelderId)
            result shouldBe false
        }

        test("gjelderId er blank") {
            val invalidGjelderId = ""
            val result = isValidGjelderId(invalidGjelderId)
            result shouldBe false
        }
    })
