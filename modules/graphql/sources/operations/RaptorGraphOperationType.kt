package io.fluidsonic.raptor

import io.fluidsonic.graphql.*


enum class RaptorGraphOperationType {

	query,
	mutation;


	internal val gqlType
		get() = when (this) {
			query -> GOperationType.query
			mutation -> GOperationType.mutation
		}
}
