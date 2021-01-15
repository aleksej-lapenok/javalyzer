package ru.ifmo.lapenok.javalyzer

import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.LiteralExpr

sealed class VarState<T> {
    val exceptValues = mutableListOf<T>()
    val exactlyValues = mutableListOf<T>()

    protected fun checkState(value: T, operator: BinaryExpr.Operator): Boolean {
        return if (operator == BinaryExpr.Operator.EQUALS) {
            if (exactlyValues.isNotEmpty() && value !in exactlyValues) {
                false
            } else if (value in exceptValues) {
                false
            } else {
                customCheck(value, operator)
            }
        } else if (operator == BinaryExpr.Operator.NOT_EQUALS) {
            if (value in exactlyValues) {
                false
            } else {
                customCheck(value, operator)
            }
        } else {
            customCheck(value, operator)
        }
    }

    fun checkState(value: LiteralExpr, operator: BinaryExpr.Operator): Boolean {
        return checkState(convertValue(value), operator)
    }

    abstract fun convertValue(expr: LiteralExpr): T

    abstract fun customCheck(value: T, operator: BinaryExpr.Operator): Boolean

    abstract fun copy(): VarState<T>

    protected fun baseCope(baseState: VarState<T>) {
        exceptValues.addAll(baseState.exceptValues)
        exactlyValues.addAll(baseState.exactlyValues)
    }

    fun elseState(): VarState<T> {
        return copy().also {
            it.exactlyValues.clear()
            it.exceptValues.clear()

            it.exceptValues.addAll(exactlyValues)
            it.exactlyValues.addAll(exceptValues)
        }
    }

    fun processAssign(value:LiteralExpr, operator: AssignExpr.Operator) {
        when(operator) {
            AssignExpr.Operator.ASSIGN -> {
                exactlyValues.clear()
                exactlyValues.add(convertValue(value))
                exceptValues.clear()
            }
        }
        doProcessAssign(convertValue(value), operator)
    }

    protected abstract fun doProcessAssign(value: T, operator: AssignExpr.Operator)

    abstract fun createEmpty():VarState<T>

}

class NumberVarState : VarState<Long>() {
    var lowerBound: Long = Long.MIN_VALUE
    var upperBound: Long = Long.MAX_VALUE

    override fun customCheck(value: Long, operator: BinaryExpr.Operator): Boolean {

        return when (operator) {
            BinaryExpr.Operator.EQUALS -> value in lowerBound..upperBound
            BinaryExpr.Operator.LESS -> upperBound > value
            BinaryExpr.Operator.LESS_EQUALS -> upperBound >= value
            BinaryExpr.Operator.GREATER -> lowerBound < value
            BinaryExpr.Operator.GREATER_EQUALS -> lowerBound <= value
            else -> true
        }
    }

    override fun convertValue(expr: LiteralExpr): Long {
        if (expr.isIntegerLiteralExpr) {
            return expr.asIntegerLiteralExpr().asNumber().toLong()
        } else {
            return expr.asLongLiteralExpr().asNumber().toLong()
        }
    }

    override fun copy(): NumberVarState {
        return NumberVarState().also {
            it.lowerBound = lowerBound
            it.upperBound = upperBound
            it.baseCope(this)
        }
    }

    override fun doProcessAssign(value: Long, operator: AssignExpr.Operator) {
        when (operator) {
            AssignExpr.Operator.ASSIGN -> {
                lowerBound = value
                upperBound = value
            }
            AssignExpr.Operator.PLUS -> {
                lowerBound += value
                upperBound += value
            }
            AssignExpr.Operator.MINUS -> {
                lowerBound -= value
                upperBound -= value
            }
            AssignExpr.Operator.MULTIPLY -> {
                lowerBound *= value
                upperBound *= value
            }
            AssignExpr.Operator.DIVIDE -> {
                lowerBound/=value
                upperBound/=value
            }
            else -> {
                lowerBound = Long.MIN_VALUE
                upperBound = Long.MAX_VALUE
            }
        }
    }

    override fun createEmpty(): VarState<Long> {
        return NumberVarState()
    }
}

class DoubleVarState : VarState<Double>() {
    var lowerBound = Double.MIN_VALUE
    var upperBound = Double.MAX_VALUE

    override fun customCheck(value: Double, operator: BinaryExpr.Operator): Boolean {

        return when (operator) {
            BinaryExpr.Operator.EQUALS -> value in upperBound..lowerBound
            BinaryExpr.Operator.LESS -> upperBound > value
            BinaryExpr.Operator.LESS_EQUALS -> upperBound >= value
            BinaryExpr.Operator.GREATER -> lowerBound < value
            BinaryExpr.Operator.GREATER_EQUALS -> lowerBound <= value
            else -> true
        }
    }

    override fun convertValue(expr: LiteralExpr): Double {
        return expr.asDoubleLiteralExpr().value.toDouble()
    }

    override fun copy(): DoubleVarState {
        return DoubleVarState().also {
            it.lowerBound = lowerBound
            it.upperBound = upperBound
            it.baseCope(this)
        }
    }

    override fun doProcessAssign(value: Double, operator: AssignExpr.Operator) {
        when (operator) {
            AssignExpr.Operator.ASSIGN -> {
                lowerBound = value
                upperBound = value
            }
            AssignExpr.Operator.PLUS -> {
                lowerBound += value
                upperBound += value
            }
            AssignExpr.Operator.MINUS -> {
                lowerBound -= value
                upperBound -= value
            }
            AssignExpr.Operator.MULTIPLY -> {
                lowerBound *= value
                upperBound *= value
            }
            AssignExpr.Operator.DIVIDE -> {
                lowerBound/=value
                upperBound/=value
            }
            else -> {
                lowerBound = Double.MIN_VALUE
                upperBound = Double.MAX_VALUE
            }
        }
    }

    override fun createEmpty(): VarState<Double> {
        return DoubleVarState()
    }
}

class BooleanVarState : VarState<Boolean>() {
    override fun customCheck(value: Boolean, operator: BinaryExpr.Operator): Boolean {
        return value
    }

    override fun convertValue(expr: LiteralExpr): Boolean {
        return expr.asBooleanLiteralExpr().value
    }

    override fun copy(): BooleanVarState {
        return BooleanVarState().also { it.baseCope(this) }
    }

    override fun doProcessAssign(value: Boolean, operator: AssignExpr.Operator) {

    }

    override fun createEmpty(): VarState<Boolean> {
        return BooleanVarState()
    }
}

class ClassVarState : VarState<String>() {
    override fun customCheck(value: String, operator: BinaryExpr.Operator): Boolean {
        return true
    }

    override fun convertValue(expr: LiteralExpr): String {
        return expr.toString()
    }

    override fun copy(): ClassVarState {
        return ClassVarState().also { it.baseCope(this) }
    }

    override fun doProcessAssign(value: String, operator: AssignExpr.Operator) {

    }

    override fun createEmpty(): VarState<String> {
        return ClassVarState()
    }
}
