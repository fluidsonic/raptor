package io.fluidsonic.raptor

import io.fluidsonic.graphql.*


internal sealed class GraphOperation {

	object Mutation : GraphOperation()
	object Query : GraphOperation()


	companion object {

		fun forType(type: Type) = when (type) {
			Type.mutation -> Mutation
			Type.query -> Query
		}
	}


	internal enum class Type {

		query,
		mutation;


		internal val gqlType
			get() = when (this) {
				query -> GOperationType.query
				mutation -> GOperationType.mutation
			}
	}
}
