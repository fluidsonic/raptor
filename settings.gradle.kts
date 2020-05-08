includeBuild("../fluid-graphql")

file("modules")
	.listFiles()
	.filter(File::isDirectory)
	.forEach { directory ->
		val name = directory.name

		include(name)

		project(":$name").apply {
			this.name = when (name) {
				"default" -> "raptor"
				else -> "raptor-$name"
			}
			this.projectDir = directory
		}
	}
