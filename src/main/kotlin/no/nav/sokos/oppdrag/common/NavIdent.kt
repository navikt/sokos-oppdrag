package no.nav.sokos.oppdrag.common

import no.nav.sokos.oppdrag.security.AdGroup

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun hasAdGroupAccess(adGroup: AdGroup): Boolean = adGroup.adGroupName in roller

    fun hasAccessToAnyAdGroup(vararg adGroups: AdGroup): Boolean = adGroups.any { it.adGroupName in roller }

    fun hasAccessFortrolig(): Boolean = AdGroup.FORTROLIG.adGroupName in roller

    fun hasAccessStrengtFortrolig(): Boolean = AdGroup.STRENGT_FORTROLIG.adGroupName in roller

    fun hasAccessEgneAnsatte(): Boolean = AdGroup.EGNE_ANSATTE.adGroupName in roller
}
