import io.fluidsonic.raptor.*
import kotlin.test.*


@Suppress("RemoveExplicitTypeArguments")
class DITests {

	@Test
	fun testExplicit() {
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide<Foo> { FooImpl1 }
				provide<Bar>(BarImpl1)
			}
		}.context.di

		assertEquals(expected = FooImpl1, actual = di.get<Foo>())
		assertEquals(expected = FooImpl1, actual = di.get<Foo?>())
		assertFails { di.get<FooImpl1>() }
		assertNull(di.get<FooImpl1?>())

		assertEquals(expected = BarImpl1, actual = di.get<Bar>())
		assertEquals(expected = BarImpl1, actual = di.get<Bar?>())
		assertFails { di.get<BarImpl1>() }
		assertNull(di.get<BarImpl1?>())
	}


	@Test
	fun testImplicit() {
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide { FooImpl1 }
				provide(BarImpl1)
			}
		}.context.di

		assertEquals(expected = FooImpl1, actual = di.get<Foo>())
		assertEquals(expected = FooImpl1, actual = di.get<Foo?>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1?>())

		assertEquals(expected = BarImpl1, actual = di.get<Bar>())
		assertEquals(expected = BarImpl1, actual = di.get<Bar?>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1?>())
	}


	@Test
	fun testLazyResolutionExplicit() {
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide<Foo> { resolutionCount += 1; FooImpl1 }
				provide(BarImpl1)
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.get<Foo?>()
		di.get<Foo>()
		di.get<Foo?>()

		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Test
	fun testLazyResolutionImplicit() {
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide { resolutionCount += 1; FooImpl1 }
				provide(BarImpl1)
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.get<Foo?>()
		di.get<Foo>()
		di.get<Foo?>()
		di.get<FooImpl1>()
		di.get<FooImpl1?>()
		di.get<FooImpl1>()
		di.get<FooImpl1?>()

		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Test
	fun testLazyResolutionWithOverrides() {
		var implResolutionCount = 0
		var overriddenResolutionCount = 0
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide<Foo> { overriddenResolutionCount += 1; FooImpl2 }
				provide { implResolutionCount += 1; FooImpl1 }
				provide<Foo> { resolutionCount += 1; get<FooImpl1>() }
				provide(BarImpl1)
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = implResolutionCount)
		assertEquals(expected = 0, actual = overriddenResolutionCount)
		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.get<Foo?>()
		di.get<Foo>()
		di.get<Foo?>()
		di.get<FooImpl1>()
		di.get<FooImpl1?>()
		di.get<FooImpl1>()
		di.get<FooImpl1?>()

		assertEquals(expected = 1, actual = implResolutionCount)
		assertEquals(expected = 0, actual = overriddenResolutionCount)
		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Suppress("USELESS_IS_CHECK")
	@Test
	fun testOverrideExplicit() {
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide<Foo> { FooImpl1 }
				provide<Foo> { FooImpl2 }
				provide<Bar>(BarImpl1)
				provide<Bar>(BarImpl2)
			}
		}.context.di

		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.get<Foo?>())

		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.get<Bar?>())
	}


	@Test
	fun testOverrideImplicit() {
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide { FooImpl1 }
				provide { FooImpl2 }
				provide(BarImpl1)
				provide(BarImpl2)
			}
		}.context.di

		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.get<Foo?>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1?>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2?>())

		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.get<Bar?>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1?>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2?>())
	}


	@Test
	fun testOverrideImplicitWithReverseResolution() {
		val di = raptor {
			install(RaptorDIFeature)
			di {
				provide { FooImpl1 }
				provide { FooImpl2 }
				provide(BarImpl1)
				provide(BarImpl2)
			}
		}.context.di

		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1?>())
		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.get<Foo?>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2?>())

		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1?>())
		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.get<Bar?>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2?>())
	}


	interface Bar
	object BarImpl1 : Bar
	object BarImpl2 : Bar

	interface Foo
	object FooImpl1 : Foo
	object FooImpl2 : Foo
}
