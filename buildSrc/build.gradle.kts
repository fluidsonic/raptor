plugins {
	kotlin("jvm") version "1.9.23"
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
