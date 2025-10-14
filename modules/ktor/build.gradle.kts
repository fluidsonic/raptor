import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-di"))
				api(project(":raptor-lifecycle"))
				api(project(":raptor-transactions"))
				api(ktor("server-core"))

				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(ktor("server-call-logging"))
				implementation(ktor("server-compression"))
				implementation(ktor("server-default-headers"))
				implementation(ktor("server-forwarded-header"))
				implementation(ktor("server-netty"))
				implementation(ktor("server-websockets"))
				implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
				implementation("ch.qos.logback:logback-classic:${Versions.logback}")
			}
		}
	}
}


fun ktor(name: String, version: String = Versions.ktor) =
	"io.ktor:ktor-$name:$version"
