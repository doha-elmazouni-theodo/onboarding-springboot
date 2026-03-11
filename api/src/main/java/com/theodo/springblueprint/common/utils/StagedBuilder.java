package com.theodo.springblueprint.common.utils;

import org.jilt.Builder;
import org.jilt.BuilderInterfaces;
import org.jilt.BuilderStyle;

@Builder(style = BuilderStyle.STAGED_PRESERVING_ORDER, toBuilder = "from", factoryMethod = "set")
@BuilderInterfaces(innerNames = "B_*")
public @interface StagedBuilder {
}
