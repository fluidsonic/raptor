import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(fluid("stdlib", "0.9.30")) {
		attributes {
			attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
		}
	}
	api("org.kodein.di:kodein-di-erased-jvm:6.5.0")

	implementation(kotlinx("atomicfu", "0.14.2", prefixName = false))
}
