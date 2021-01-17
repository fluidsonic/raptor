package io.fluidsonic.raptor

import freemarker.template.*


public interface RaptorFreemarkerObjectWrapper<in Value : Any> {

	public fun wrap(value: Value, baseWrapper: ObjectWrapper, context: RaptorContext): TemplateModel
}
