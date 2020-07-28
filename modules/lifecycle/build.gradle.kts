import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor"))
			implementation(kotlinx("atomicfu", "0.14.3", prefixName = false))
		}

		testDependencies {
			implementation(kotlinx("coroutines-test", "1.3.8"))
		}
	}
}
