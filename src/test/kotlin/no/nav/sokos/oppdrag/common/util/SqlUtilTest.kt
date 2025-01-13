package no.nav.sokos.oppdrag.common.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

import no.nav.sokos.oppdrag.common.util.SqlUtil.sanitizeForSql

class SqlUtilTest :
    StringSpec({
        "skal fjerne SQL injection patterns from string" {
            val input = "SELECT * FROM users WHERE name = 'John'; DROP TABLE users; --"
            val expected = " * FROM users WHERE name = John  TABLE users "
            input.sanitizeForSql() shouldBe expected
        }

        "skal håndtere blandingsord SQL injection patterns" {
            val input = "sElEcT * fRoM users WHERE name = 'John'; DrOp TABLE users; --"
            val expected = " * fRoM users WHERE name = John  TABLE users "
            input.sanitizeForSql() shouldBe expected
        }

        "skal håndtere text uten SQL injection patterns" {
            val input = "Hello, world!"
            val expected = "Hello, world!"
            input.sanitizeForSql() shouldBe expected
        }

        "skal håndtere tomt string" {
            val input = ""
            val expected = ""
            input.sanitizeForSql() shouldBe expected
        }
    })
