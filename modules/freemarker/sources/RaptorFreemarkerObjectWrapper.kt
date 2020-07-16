package io.fluidsonic.raptor

import freemarker.template.*


interface RaptorFreemarkerObjectWrapper<in Value : Any> {

	fun wrap(value: Value, baseWrapper: ObjectWrapper, context: RaptorContext): TemplateModel
}
