plugins {
	kotlin("jvm") version "2.3.10"
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

kotlin.sourceSets {
	getByName("main") {
		kotlin.srcDirs("sources")
	}
}
