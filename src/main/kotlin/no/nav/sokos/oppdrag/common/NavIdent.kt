package no.nav.sokos.oppdrag.common

data class NavIdent(
    val ident: String,
    val roller: List<String> = emptyList(),
) {
    fun hasAdGroupAccess(adGroupName: String): Boolean = adGroupName in roller

    fun hasAccessToAnyAdGroup(vararg roles: String): Boolean = roles.any { it in roller }

    fun hasAccessToAllAdGroups(vararg roles: String): Boolean = roles.all { it in roller }
}
