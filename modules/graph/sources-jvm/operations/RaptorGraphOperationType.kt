package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


public enum class RaptorGraphOperationType {

	mutation,
	query;


	internal val gqlType
		get() = when (this) {
			mutation -> GOperationType.mutation
			query -> GOperationType.query
		}
}
