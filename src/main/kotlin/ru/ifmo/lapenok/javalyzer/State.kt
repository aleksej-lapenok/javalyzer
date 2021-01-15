package ru.ifmo.lapenok.javalyzer

import com.github.javaparser.ast.type.Type

class State {
    val localVars = mutableMapOf<String, VarState<*>>()
    val classesVars = mutableMapOf<String, State>()
    val types = mutableMapOf<String, Type>()

    var parentState:State? = null
}
