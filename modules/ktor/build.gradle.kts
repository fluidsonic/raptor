import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor"))
			api(project(":raptor-lifecycle"))
			api(project(":raptor-transactions"))
			api(ktor("auth-jwt"))
			api(ktor("server-netty"))
			implementation("ch.qos.logback:logback-classic:1.2.3")
		}
	}
}

kotlin {
	sourceSets.all {
		languageSettings.useExperimentalAnnotation("io.ktor.util.KtorExperimentalAPI")
	}
}


@Suppress("unused")
fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.ktor(name: String, version: String = "1.3.2") =
	"io.ktor:ktor-$name:$version"
