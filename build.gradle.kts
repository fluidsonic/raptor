import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.10"
}

fluidJvmLibrary(name = "raptor", version = "0.9.0", prefixName = false)

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "TODO" // FIXME
}

dependencies {
	api(fluid("mongo", "1.0.0"))
	api(fluid("stdlib", "0.9.30")) {
		attributes {
			attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
		}
	}

	api(ktor("auth-jwt"))
	api(ktor("server-netty"))
	api("org.kodein.di:kodein-di-erased-jvm:6.5.0")

	implementation(fluid("graphql", "0.9.0"))
	implementation(fluid("json-basic", "1.0.3"))
	implementation("ch.qos.logback:logback-classic:1.2.3")
}

repositories {
	bintray("kotlin/ktor")
}

tasks {
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = freeCompilerArgs + "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
		}
	}
}


@Suppress("unused")
fun DependencyHandler.ktor(name: String, version: String = "1.3.1") =
	"io.ktor:ktor-$name:$version"
