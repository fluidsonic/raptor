import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-core"))
	implementation(kotlinx("atomicfu", "0.14.3", prefixName = false))

	testImplementation(kotlinx("coroutines-test", "1.3.6"))
}
