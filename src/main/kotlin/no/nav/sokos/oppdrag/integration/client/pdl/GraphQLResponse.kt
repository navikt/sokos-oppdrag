package no.nav.sokos.oppdrag.integration.client.pdl

import com.expediagroup.graphql.client.types.GraphQLClientError
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.client.types.GraphQLClientSourceLocation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphQLResponse<T>(
    override val data: T? = null,
    override val errors: List<GraphQLError>? = null,
    override val extensions: Map<String, JsonElement?>? = null,
) : GraphQLClientResponse<T>

@Serializable
data class GraphQLError(
    override val message: String,
    override val locations: List<GraphQLSourceLocation>? = null,
    override val extensions: Map<String, JsonElement?>? = null,
    override val path: List<JsonElement>? = null,
) : GraphQLClientError

@Serializable
data class GraphQLSourceLocation(
    override val line: Int,
    override val column: Int,
) : GraphQLClientSourceLocation
