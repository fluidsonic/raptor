import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.*

fluidLibraryModule(description = "FIXME") {
	targets {
		js(IR)
		jvm()
	}
}
