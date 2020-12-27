import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		js(IR)
		jvm()
	}
}
