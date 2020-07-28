import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor"))
			api("org.kodein.di:kodein-di-erased-jvm:6.5.5")
			compileOnly(project(":raptor-transactions"))
		}

		testDependencies {
			implementation(project(":raptor-transactions"))
		}
	}
}
