import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				api(project(":raptor-lifecycle"))
				api(fluid("time", Versions.fluid_time))
				implementation(project(":raptor-di"))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}

//		darwin()
//		js(KotlinJsCompilerType.BOTH)
		jvm()
	}
}
