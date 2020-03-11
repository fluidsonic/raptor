package io.fluidsonic.raptor.setup

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.Raptor.*
import kotlinx.atomicfu.*
import org.kodein.di.*


internal class RaptorImpl(
	private val config: RaptorConfig
) : Raptor {

	private val stateRef = atomic(State.initial)

	val scope = RaptorScopeImpl(
		dkodein = Kodein.direct { import(config.kodeinModule) }
	)


	override suspend fun start() {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) { "Cannot start Raptor unless it's in 'stopped' state." }

		with(scope) {
			for (callback in this@RaptorImpl.config.startCallbacks) // FIXME get rid of this@RaptorImpl.
				callback()
		}

		stateRef.value = State.started
	}


	override val state
		get() = stateRef.value


	override suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Raptor unless it's in 'started' state." }

		with(scope) {
			for (callback in this@RaptorImpl.config.stopCallbacks) // FIXME get rid of this@RaptorImpl.
				callback()
		}

		stateRef.value = State.stopped
	}
}


// FIXME
//class Test {
//
//
//	private var context: Context? = null
//	private var environment: BakuEnvironment<Context, Transaction>? = null
//	private var modules: List<BakuModule<in Context, in Transaction>>? = null
//	private val providerBasedBSONCodecRegistry = ProviderBasedBSONCodecRegistry<Context>()
//
//	val bsonCodecRegistry = CodecRegistries.fromRegistries(
//		MongoClients.defaultCodecRegistry,
//		providerBasedBSONCodecRegistry
//	)!!
//
//
//	private fun Application.configureModules() {
//		val environment = this@RaptorBuilderImpl.environment!!
//		val modules = (modules ?: error("modules() must be specified")) + StandardModule
//
//		val configurations = modules.map { it.configure() }
//
//		val context = runBlocking { environment.createContext() }
//		val idFactoriesByType = configurations.flatMap { it.idFactories }.associateBy { it.type }
//
//		val bsonCodecProviders: MutableList<BsonCodecProvider<Context>> = mutableListOf()
//		bsonCodecProviders += configurations.flatMap { it.bsonCodecProviders }
//		bsonCodecProviders += configurations.flatMap { it.idFactories }.map { EntityIdBsonCodec(factory = it) }
//		// FIXME get IDs from BSON codec providers?
//		bsonCodecProviders += TypedIdBsonCodec(idFactoryProvider = object : EntityIdFactoryProvider {
//			override fun idFactoryForType(type: String) = idFactoriesByType[type]
//		})
//
//		providerBasedBSONCodecRegistry.context = context
//		providerBasedBSONCodecRegistry.provider = BsonCodecProvider.of(bsonCodecProviders)
//		providerBasedBSONCodecRegistry.rootRegistry = bsonCodecRegistry
//
//		val entityResolverSources: MutableMap<KClass<out EntityId>, BakuModule<*, *>> = mutableMapOf()
//		val entityResolvers: MutableMap<KClass<out EntityId>, suspend Transaction.(ids: Set<EntityId>) -> Flow<Entity>> =
//			mutableMapOf()
//
//		for (configuration in configurations) {
//			for ((idClass, entityResolver) in configuration.entities.resolvers) {
//				val previousModule = entityResolverSources.putIfAbsent(idClass, configuration.module)
//				if (previousModule != null) {
//					error("Cannot add entity resolver for $idClass of ${configuration.module} because $previousModule already provides one")
//				}
//
//				entityResolvers[idClass] = entityResolver
//			}
//		}
//
//		configurations.forEach { it.customConfigurations.forEach { it() } }
//
//	}
//}
//
//
//private class ProviderBasedBSONCodecRegistry<Context : BakuContext<*>> : CodecRegistry {
//
//	lateinit var context: Context
//	lateinit var provider: BsonCodecProvider<Context>
//	lateinit var rootRegistry: CodecRegistry
//
//
//	override fun <Value : Any> get(clazz: Class<Value>, registry: CodecRegistry) =
//		get(clazz)
//
//
//	override fun <Value : Any> get(clazz: Class<Class>): BsonCodec<Value, Context> {
//		val codec = provider.codecForClass(clazz.kotlin) ?: throw CodecConfigurationException("No BSON codec provided for $clazz")
//		if (codec is AbstractBsonCodec<Value, Context>) {
//			codec.configure(context = context, rootRegistry = rootRegistry)
//		}
//
//		return codec
//	}
//}
