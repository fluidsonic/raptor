package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class FinalizationTests {

	@Test
	fun testComponentRegistryPreventsMutations() {
		raptor {
			install(TextCollectionFeature)

			val textCollection = componentRegistry.configure(TextCollectionComponent.Key)

			install(object : RaptorFeature {

				override fun RaptorFeatureFinalizationScope.finalize() {
					assertFails {
						componentRegistry.configure(TextCollectionComponent.Key)
					}

					assertFails {
						componentRegistry.register(object : RaptorComponentKey<TextCollectionComponent> {}, TextCollectionComponent())
					}

					assertFails {
						textCollection {}
					}
				}


				override fun RaptorFeatureInstallationScope.install() = Unit
			})
		}
	}
}
