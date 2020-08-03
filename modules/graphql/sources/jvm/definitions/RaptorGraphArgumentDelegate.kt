package io.fluidsonic.raptor

import kotlin.properties.*


public interface RaptorGraphArgumentDelegate<out Type> : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Type>>
