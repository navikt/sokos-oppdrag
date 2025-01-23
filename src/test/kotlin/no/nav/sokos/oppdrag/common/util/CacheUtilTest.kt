package no.nav.sokos.oppdrag.common.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CacheUtilTest : StringSpec({

    "skal fagSystemId være en del av cache key returere true" {
        val fagSystemId = "4567-H4"
        CacheUtil.isFagSystemIdPartOfCacheKey("123-abc-456-true", fagSystemId) shouldBe true
        CacheUtil.isFagSystemIdPartOfCacheKey("123-abc-4567-true", fagSystemId) shouldBe true
        CacheUtil.isFagSystemIdPartOfCacheKey("123-000-4567-H-false", fagSystemId) shouldBe true
        CacheUtil.isFagSystemIdPartOfCacheKey("123-000-4567-H4-false", fagSystemId) shouldBe true
        CacheUtil.isFagSystemIdPartOfCacheKey("-000-4567-H4-false", fagSystemId) shouldBe true
    }

    "skal fagSystemId være en del av cache key retur false" {
        val fagSystemId = "4567-H4"
        CacheUtil.isFagSystemIdPartOfCacheKey("123-abc-457-true", fagSystemId) shouldBe false
        CacheUtil.isFagSystemIdPartOfCacheKey("123-abc-H567-true", fagSystemId) shouldBe false
        CacheUtil.isFagSystemIdPartOfCacheKey("123-000-45678-false", fagSystemId) shouldBe false
        CacheUtil.isFagSystemIdPartOfCacheKey("123-4567", fagSystemId) shouldBe false
        CacheUtil.isFagSystemIdPartOfCacheKey("123-000-4567H4-false", fagSystemId) shouldBe false
    }
})
