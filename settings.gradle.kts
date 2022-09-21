import org.gradle.kotlin.dsl.support.*

rootProject.name = "raptor"

file("modules")
	.listFilesOrdered(File::isDirectory)
	.forEach { directory ->
		val name = directory.name

		include(name)

		project(":$name").apply {
			this.name = "raptor-$name"
			this.projectDir = directory
		}
	}
