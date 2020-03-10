import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.tasks.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":"))

	api(ktor("auth-jwt"))
	api(ktor("server-netty"))

	implementation("ch.qos.logback:logback-classic:1.2.3")
}

tasks {
	withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = freeCompilerArgs + "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
		}
	}
}

// FIXME
//repositories {
//	bintray("kotlin/ktor")
//}


@Suppress("unused")
fun DependencyHandler.ktor(name: String, version: String = "1.3.1") =
	"io.ktor:ktor-$name:$version"
