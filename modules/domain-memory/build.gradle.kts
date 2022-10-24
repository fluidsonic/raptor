import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				api(project(":raptor-domain"))
				api(project(":raptor-mongodb"))
			}
		}

//		darwin()
//		js(KotlinJsCompilerType.BOTH)
		jvm()
	}
}
