rootProject.name = "raptor"

//includeBuild("../fluid-graphql") // https://youtrack.jetbrains.com/issue/KT-41370

file("modules")
	.listFiles()!!
	.filter(File::isDirectory)
	.forEach { directory ->
		val name = directory.name

		include(name)

		project(":$name").apply {
			this.name = "raptor-$name"
			this.projectDir = directory
		}
	}
