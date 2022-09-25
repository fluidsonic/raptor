import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			@Suppress("SpellCheckingInspection")
			dependencies {
				implementation("io.github.cdimascio:dotenv-kotlin:${Versions.dotenv}")

				api(project(":raptor-core"))
			}
		}
	}
}
