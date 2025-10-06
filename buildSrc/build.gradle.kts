plugins {
	kotlin("jvm") version "2.2.20"
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
