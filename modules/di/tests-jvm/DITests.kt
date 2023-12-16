import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.test.*

class DITests {

	@Test
	fun testExplicit() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Foo> { FooImpl1 }
				provide<Bar>(BarImpl1)
			}
		}.context.di

		assertEquals(expected = FooImpl1, actual = di.get<Foo>())
		assertEquals(expected = FooImpl1, actual = di.getOrNull<Foo>())
		assertFails { di.get<FooImpl1>() }
		assertNull(di.getOrNull<FooImpl1>())

		assertEquals(expected = BarImpl1, actual = di.get<Bar>())
		assertEquals(expected = BarImpl1, actual = di.getOrNull<Bar>())
		assertFails { di.get<BarImpl1>() }
		assertNull(di.getOrNull<BarImpl1>())
	}


	@Test
	fun testLazyResolutionExplicit() {
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Foo> { resolutionCount += 1; FooImpl1 }
				provide<BarImpl1>(BarImpl1)
				provide<Bar> { get<BarImpl1>() }
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.getOrNull<Foo>()
		di.get<Foo>()
		di.getOrNull<Foo>()

		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Test
	fun testLazyResolutionImplicit() {
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<FooImpl1> { resolutionCount += 1; FooImpl1 }
				provide<Foo> { get<FooImpl1>() }
				provide<BarImpl1>(BarImpl1)
				provide<Bar> { get<BarImpl1>() }
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.getOrNull<Foo>()
		di.get<Foo>()
		di.getOrNull<Foo>()
		di.get<FooImpl1>()
		di.getOrNull<FooImpl1>()
		di.get<FooImpl1>()
		di.getOrNull<FooImpl1>()

		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Test
	fun testLazyResolutionWithOverrides() {
		var implResolutionCount = 0
		var overriddenResolutionCount = 0
		var resolutionCount = 0
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Foo> { overriddenResolutionCount += 1; FooImpl2 }
				provide<FooImpl1> { implResolutionCount += 1; FooImpl1 }
				provide<Foo> { resolutionCount += 1; get<FooImpl1>() }
				provide<BarImpl1>(BarImpl1)
				provide<Bar> { get<BarImpl1>() }
			}
		}.context.di

		di.get<Bar>()

		assertEquals(expected = 0, actual = implResolutionCount)
		assertEquals(expected = 0, actual = overriddenResolutionCount)
		assertEquals(expected = 0, actual = resolutionCount)

		di.get<Foo>()
		di.getOrNull<Foo>()
		di.get<Foo>()
		di.getOrNull<Foo>()
		di.get<FooImpl1>()
		di.getOrNull<FooImpl1>()
		di.get<FooImpl1>()
		di.getOrNull<FooImpl1>()

		assertEquals(expected = 1, actual = implResolutionCount)
		assertEquals(expected = 0, actual = overriddenResolutionCount)
		assertEquals(expected = 1, actual = resolutionCount)
	}


	@Test
	fun testOverrideExplicit() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Foo> { FooImpl1 }
				provide<Foo> { FooImpl2 }
				provide<Bar>(BarImpl1)
				provide<Bar>(BarImpl2)
			}
		}.context.di

		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.getOrNull<Foo>())

		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.getOrNull<Bar>())
	}


	@Test
	fun testOverrideImplicit() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<FooImpl1> { FooImpl1 }
				provide<Foo> { get<FooImpl1>() }
				provide<FooImpl2> { FooImpl2 }
				provide<Foo> { get<FooImpl2>() }

				provide<BarImpl1>(BarImpl1)
				provide<Bar> { get<BarImpl1>() }
				provide<BarImpl2>(BarImpl2)
				provide<Bar> { get<BarImpl2>() }
			}
		}.context.di

		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.getOrNull<Foo>())
		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1>())
		assertEquals(expected = FooImpl1, actual = di.getOrNull<FooImpl1>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2>())
		assertEquals(expected = FooImpl2, actual = di.getOrNull<FooImpl2>())

		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.getOrNull<Bar>())
		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1>())
		assertEquals(expected = BarImpl1, actual = di.getOrNull<BarImpl1>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2>())
		assertEquals(expected = BarImpl2, actual = di.getOrNull<BarImpl2>())
	}


	@Test
	fun testOverrideImplicitWithReverseResolution() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<FooImpl1> { FooImpl1 }
				provide<Foo> { get<FooImpl1>() }
				provide<FooImpl2> { FooImpl2 }
				provide<Foo> { get<FooImpl2>() }
				provide<BarImpl1>(BarImpl1)
				provide<Bar> { get<BarImpl1>() }
				provide<BarImpl2>(BarImpl2)
				provide<Bar> { get<BarImpl2>() }
			}
		}.context.di

		assertEquals(expected = FooImpl1, actual = di.get<FooImpl1>())
		assertEquals(expected = FooImpl1, actual = di.getOrNull<FooImpl1>())
		assertEquals(expected = FooImpl2, actual = di.get<Foo>())
		assertEquals(expected = FooImpl2, actual = di.getOrNull<Foo>())
		assertEquals(expected = FooImpl2, actual = di.get<FooImpl2>())
		assertEquals(expected = FooImpl2, actual = di.getOrNull<FooImpl2>())

		assertEquals(expected = BarImpl1, actual = di.get<BarImpl1>())
		assertEquals(expected = BarImpl1, actual = di.getOrNull<BarImpl1>())
		assertEquals(expected = BarImpl2, actual = di.get<Bar>())
		assertEquals(expected = BarImpl2, actual = di.getOrNull<Bar>())
		assertEquals(expected = BarImpl2, actual = di.get<BarImpl2>())
		assertEquals(expected = BarImpl2, actual = di.getOrNull<BarImpl2>())
	}


	@Test
	fun testFunctionReference() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Foo>(FooImpl1)
				provide<Bar>(BarImpl1)
				provide<Composite>(::Composite)
			}
		}.context.di

		assertIs<Composite>(di.get<Composite>())
	}


	@Test
	fun testOptional() {
		val di = raptor {
			install(RaptorDIPlugin)
			di {
				provide<Bar>(BarImpl1)
			}
		}.context.di

		assertNull(di.get<Foo?>())
		assertNull(di.getOrNull<Foo>())
		assertEquals(actual = di.get<Bar?>(), expected = BarImpl1)
		assertEquals(actual = di.getOrNull<Bar>(), expected = BarImpl1)
	}


	interface Bar
	object BarImpl1 : Bar
	object BarImpl2 : Bar

	interface Foo
	object FooImpl1 : Foo
	object FooImpl2 : Foo

	class Composite(
		val bar: Bar,
		val foo: Foo,
	)
}
