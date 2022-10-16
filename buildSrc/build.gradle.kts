plugins {
	kotlin("jvm") version "1.7.20"
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
