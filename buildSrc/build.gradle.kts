plugins {
	kotlin("jvm") version "2.3.20"
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
