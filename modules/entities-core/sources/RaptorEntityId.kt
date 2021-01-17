package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorEntityId.*
import kotlin.reflect.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*


public interface RaptorEntityId {

	public val definition: Definition<*>

	public fun encode(includeDiscriminator: Boolean = false): String


	public companion object;


	public interface Definition<Id : RaptorEntityId> : KSerializer<Id> {

		public val idDescriptor: Descriptor<Id>

		public fun decodeOrNull(value: String, requireDiscriminator: Boolean = false): Id?
		public fun encode(id: Id, includeDiscriminator: Boolean = false): String
	}


	public class Descriptor<Id : RaptorEntityId>(
		public val discriminator: String,
		public val factory: (String) -> Id,
		public val instanceClass: KClass<Id>,
	)


	public abstract class Typed<Id : Typed<Id>>(protected val value: String) : RaptorEntityId {

		abstract override val definition: Definition<Id>


		@Suppress("UNCHECKED_CAST")
		override fun encode(includeDiscriminator: Boolean): String =
			definition.encode(this as Id, includeDiscriminator = includeDiscriminator)


		override fun equals(other: Any?): Boolean =
			this === other || (other is Typed<*> && this::class == other::class && value == other.value)


		override fun hashCode(): Int =
			value.hashCode()


		override fun toString(): String =
			value


		public abstract class Definition<Id : Typed<Id>>(
			final override val idDescriptor: Descriptor<Id>,
		) : RaptorEntityId.Definition<Id> {

			final override val descriptor: SerialDescriptor =
				PrimitiveSerialDescriptor("${idDescriptor.discriminator} id", PrimitiveKind.STRING)


			final override fun decodeOrNull(value: String, requireDiscriminator: Boolean): Id? {
				val separatorIndex = value.indexOf(':')
				val actualValue = when {
					separatorIndex >= 0 -> {
						val discriminator = value.substring(0, separatorIndex)
						if (discriminator != this.idDescriptor.discriminator)
							return null

						value.substring(separatorIndex + 1)
					}
					requireDiscriminator -> return null
					else -> value
				}

				if (actualValue.isEmpty())
					return null

				return idDescriptor.factory(actualValue)
			}


			final override fun deserialize(decoder: Decoder): Id =
				idDescriptor.factory(decoder.decodeString())


			final override fun encode(id: Id, includeDiscriminator: Boolean): String =
				if (includeDiscriminator) "${idDescriptor.discriminator}:${id.value}" else id.value


			final override fun serialize(encoder: Encoder, value: Id) {
				encoder.encodeString(value.value)
			}
		}
	}
}


@RaptorDsl
public inline infix fun <reified Id : RaptorEntityId> KFunction1<String, Id>.by(discriminator: String): Descriptor<Id> =
	Descriptor(
		discriminator = discriminator,
		factory = this,
		instanceClass = Id::class,
	)
