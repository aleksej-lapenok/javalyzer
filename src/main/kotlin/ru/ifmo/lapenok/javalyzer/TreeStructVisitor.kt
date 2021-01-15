package ru.ifmo.lapenok.javalyzer

import com.github.javaparser.Range
import com.github.javaparser.ast.ArrayCreationLevel
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.AnnotationDeclaration
import com.github.javaparser.ast.body.AnnotationMemberDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.InitializerDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.ReceiverParameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.ArrayAccessExpr
import com.github.javaparser.ast.expr.ArrayCreationExpr
import com.github.javaparser.ast.expr.ArrayInitializerExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.CastExpr
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.ConditionalExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.EnclosedExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.InstanceOfExpr
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LambdaExpr
import com.github.javaparser.ast.expr.LiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.MethodReferenceExpr
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.NullLiteralExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.PatternExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.expr.SuperExpr
import com.github.javaparser.ast.expr.SwitchExpr
import com.github.javaparser.ast.expr.TextBlockLiteralExpr
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.ast.expr.TypeExpr
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.modules.ModuleDeclaration
import com.github.javaparser.ast.modules.ModuleExportsDirective
import com.github.javaparser.ast.modules.ModuleOpensDirective
import com.github.javaparser.ast.modules.ModuleProvidesDirective
import com.github.javaparser.ast.modules.ModuleRequiresDirective
import com.github.javaparser.ast.modules.ModuleUsesDirective
import com.github.javaparser.ast.stmt.AssertStmt
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.BreakStmt
import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.ast.stmt.ContinueStmt
import com.github.javaparser.ast.stmt.DoStmt
import com.github.javaparser.ast.stmt.EmptyStmt
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ForEachStmt
import com.github.javaparser.ast.stmt.ForStmt
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.LabeledStmt
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.SwitchEntry
import com.github.javaparser.ast.stmt.SwitchStmt
import com.github.javaparser.ast.stmt.SynchronizedStmt
import com.github.javaparser.ast.stmt.ThrowStmt
import com.github.javaparser.ast.stmt.TryStmt
import com.github.javaparser.ast.stmt.UnparsableStmt
import com.github.javaparser.ast.stmt.WhileStmt
import com.github.javaparser.ast.stmt.YieldStmt
import com.github.javaparser.ast.type.ArrayType
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.IntersectionType
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.TypeParameter
import com.github.javaparser.ast.type.UnionType
import com.github.javaparser.ast.type.UnknownType
import com.github.javaparser.ast.type.VarType
import com.github.javaparser.ast.type.VoidType
import com.github.javaparser.ast.type.WildcardType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

abstract class TreeStructVisitor : VoidVisitorAdapter<State>() {

    var assign = false

    val errors = mutableListOf<Range>()

    val classDefinitions = mutableMapOf<String, ClassOrInterfaceDeclaration>()
    val methods = mutableMapOf<String, MethodDeclaration>()

    open fun out(n: Any?, indentLevel: State) {
//        println(" ".repeat(5) + n?.javaClass?.simpleName)
    }

    override fun visit(n: NodeList<*>?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: AnnotationDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: AnnotationMemberDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ArrayAccessExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ArrayCreationExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ArrayCreationLevel?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ArrayInitializerExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ArrayType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: AssertStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: AssignExpr, arg: State) {
        out(n, arg)

        val variable:String
        if (n.target.isNameExpr) {
            variable = n.target.asNameExpr().nameAsString
        } else if (n.target.isFieldAccessExpr) {
            val scope = n.target.asFieldAccessExpr().scope
            val name = n.target.asFieldAccessExpr().nameAsString

            if (scope.isNameExpr && scope.asNameExpr().nameAsString == "this") {
                variable = name
            } else {
                return
            }
        } else {
            return
        }

        if (n.value.isLiteralExpr) {
            val value = n.value.asLiteralExpr()
            arg.localVars[variable]?.processAssign(value, n.operator)
        } else {
            if (variable !in arg.localVars) {
                throw IllegalArgumentException("Not found variable $variable in ${n.range}")
            }
            arg.localVars[variable] = arg.localVars[variable]!!.createEmpty()
        }
    }

    override fun visit(n: BinaryExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: BlockComment?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: BlockStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: BooleanLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: BreakStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: CastExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: CatchClause?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: CharLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ClassExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ClassOrInterfaceDeclaration, arg: State) {
        out(n, arg)

        classDefinitions[n.nameAsString] = n

//        methods.putAll(n.methods.filter { it.isStatic }.map { n.nameAsString + "::" + it.nameAsString + "::" +
//                it.parameters.map { parameter -> parameter.typeAsString }.joinToString { "::" } to it })
//
//        println(methods)

//        n.getMethodsByParameterTypes("String[]").firstOrNull {
//            it.isStatic && it.nameAsString == "main" && it.typeAsString == "void"
//        }?.apply { visit(this, State()) }

        n.methods.filter { it.isStatic }.forEach { it.accept(this, arg) }

        n.members.filter { it.isFieldDeclaration }.flatMap { it.asFieldDeclaration().variables }.forEach {//save parameters of class

            arg.types[it.nameAsString] = it.type

            val varState = if (it.type.isPrimitiveType) {
                val type = it.type.asPrimitiveType().type
                when (type!!) {
                    PrimitiveType.Primitive.BOOLEAN -> BooleanVarState()
                    PrimitiveType.Primitive.CHAR,
                    PrimitiveType.Primitive.BYTE,
                    PrimitiveType.Primitive.SHORT,
                    PrimitiveType.Primitive.INT,
                    PrimitiveType.Primitive.LONG ->
                        NumberVarState()
                    PrimitiveType.Primitive.FLOAT, PrimitiveType.Primitive.DOUBLE -> DoubleVarState()
                }
            } else {
                ClassVarState()
            }

            arg.localVars[it.nameAsString] = varState
        }

        n.constructors.forEach { it.accept(this, arg) }
        n.methods.filter { !it.isStatic }.forEach { it.accept(this, arg) }

    }

    override fun visit(n: ClassOrInterfaceType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: CompilationUnit?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ConditionalExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ConstructorDeclaration, arg: State) {
        out(n, arg)
        n.parameters.forEach {
            arg.types[it.nameAsString] = it.type

            val varState = if (it.type.isPrimitiveType) {
                val type = it.type.asPrimitiveType().type
                when (type!!) {
                    PrimitiveType.Primitive.BOOLEAN -> BooleanVarState()
                    PrimitiveType.Primitive.CHAR,
                    PrimitiveType.Primitive.BYTE,
                    PrimitiveType.Primitive.SHORT,
                    PrimitiveType.Primitive.INT,
                    PrimitiveType.Primitive.LONG ->
                        NumberVarState()
                    PrimitiveType.Primitive.FLOAT, PrimitiveType.Primitive.DOUBLE -> DoubleVarState()
                }
            } else {
                ClassVarState()
            }

            arg.localVars[it.nameAsString] = varState
        }
        super.visit(n, arg)
    }

    override fun visit(n: ContinueStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: DoStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: DoubleLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: EmptyStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: EnclosedExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: EnumConstantDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: EnumDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ExplicitConstructorInvocationStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ExpressionStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: FieldAccessExpr, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: FieldDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ForStmt, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ForEachStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: IfStmt, arg: State) {
        out(n, arg)

        if (n.condition.isBinaryExpr) {
            val condition = n.condition.asBinaryExpr()

            val operator: BinaryExpr.Operator
            val left: NameExpr
            val right: LiteralExpr
            if (condition.right.isNameExpr && condition.left.isLiteralExpr) {
                left = condition.right.asNameExpr()
                right = condition.left.asLiteralExpr()
                operator = when (condition.operator) {
                    BinaryExpr.Operator.EQUALS -> BinaryExpr.Operator.EQUALS
                    BinaryExpr.Operator.LESS -> BinaryExpr.Operator.GREATER
                    BinaryExpr.Operator.GREATER -> BinaryExpr.Operator.LESS
                    BinaryExpr.Operator.LESS_EQUALS -> BinaryExpr.Operator.GREATER_EQUALS
                    BinaryExpr.Operator.GREATER_EQUALS -> BinaryExpr.Operator.LESS_EQUALS
                    else -> return //not supported
                }
            } else if (condition.left.isNameExpr && condition.right.isLiteralExpr) {
                left = condition.left.asNameExpr()
                right = condition.right.asLiteralExpr()
                operator = condition.operator
            } else {
                super.visit(n, arg)
                return
            }

            val varState = arg.localVars[left.nameAsString]
            if (varState == null) {
                throw IllegalArgumentException("Not found variable ${left.nameAsString}")
            }

            if (!varState.checkState(right, operator)) {
//                println("ERROR: ${n.thenStmt.range} will not executed")
                n.thenStmt.range.ifPresent { errors.add(it) }
            }

            val newVarState = varState.copy()
            arg.localVars[left.nameAsString] = newVarState

            when (operator) {
                BinaryExpr.Operator.EQUALS ->
                    when (newVarState) {
                        is ClassVarState -> newVarState.exactlyValues.add(right.toString())
                        is NumberVarState -> newVarState.exactlyValues.add(
                            right.asIntegerLiteralExpr().asNumber().toLong()
                        )
                        is DoubleVarState -> newVarState.exactlyValues.add(right.asDoubleLiteralExpr().asDouble())
                        is BooleanVarState -> newVarState.exactlyValues.add(right.asBooleanLiteralExpr().value)
                    }
                BinaryExpr.Operator.NOT_EQUALS ->
                    when (newVarState) {
                        is ClassVarState -> newVarState.exceptValues.add(right.toString())
                        is NumberVarState -> newVarState.exceptValues.add(
                            right.asIntegerLiteralExpr().asNumber().toLong()
                        )
                        is DoubleVarState -> newVarState.exceptValues.add(right.asDoubleLiteralExpr().asDouble())
                        is BooleanVarState -> newVarState.exceptValues.add(right.asBooleanLiteralExpr().value)
                    }
            }

            n.thenStmt.accept(this, arg)
            if (n.elseStmt.isPresent) {
                val differentOperator = when(operator) {
                    BinaryExpr.Operator.EQUALS -> BinaryExpr.Operator.NOT_EQUALS
                    BinaryExpr.Operator.NOT_EQUALS -> BinaryExpr.Operator.EQUALS
                    BinaryExpr.Operator.LESS -> BinaryExpr.Operator.GREATER_EQUALS
                    BinaryExpr.Operator.LESS_EQUALS -> BinaryExpr.Operator.GREATER
                    BinaryExpr.Operator.GREATER -> BinaryExpr.Operator.LESS_EQUALS
                    BinaryExpr.Operator.GREATER_EQUALS -> BinaryExpr.Operator.GREATER_EQUALS
                    else -> throw IllegalArgumentException()//not supported
                }

                if (!varState.checkState(right, differentOperator)) {
//                    println("ERROR: ${n.elseStmt.get().range} will not executed")
                    n.elseStmt.get().range.ifPresent { errors.add(it) }
                }

                val newElseVarState = varState.copy()
                arg.localVars[left.nameAsString] = newElseVarState

                when (operator) {
                    BinaryExpr.Operator.EQUALS ->
                        when (newElseVarState) {
                            is ClassVarState -> newElseVarState.exceptValues.add(right.toString())
                            is NumberVarState -> newElseVarState.exceptValues.add(
                                right.asIntegerLiteralExpr().asNumber().toLong()
                            )
                            is DoubleVarState -> newElseVarState.exceptValues.add(
                                right.asDoubleLiteralExpr().asDouble()
                            )
                            is BooleanVarState -> newElseVarState.exceptValues.add(right.asBooleanLiteralExpr().value)
                        }
                    BinaryExpr.Operator.NOT_EQUALS ->
                        when (newElseVarState) {
                            is ClassVarState -> newElseVarState.exactlyValues.add(right.toString())
                            is NumberVarState -> newElseVarState.exactlyValues.add(
                                right.asIntegerLiteralExpr().asNumber().toLong()
                            )
                            is DoubleVarState -> newElseVarState.exactlyValues.add(
                                right.asDoubleLiteralExpr().asDouble()
                            )
                            is BooleanVarState -> newElseVarState.exactlyValues.add(right.asBooleanLiteralExpr().value)
                        }
                }
                n.elseStmt.ifPresent { it.accept(this, arg) }
            }
            arg.localVars[left.nameAsString] = varState
        }
//
//        if (n.condition.toBooleanLiteralExpr().takeIf { it.isPresent }?.get()?.value == false) {//if(false){}
//            println("ERROR")
//        }
//
//        if (n.condition.toBooleanLiteralExpr().takeIf { it.isPresent }?.get()?.value == true &&//if(true){}else{}
//                    n.hasElseBranch()) {
//            println("ERROR")
//        }

//        super.visit(n, arg)
    }

    override fun visit(n: ImportDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: InitializerDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: InstanceOfExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: IntegerLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: IntersectionType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: JavadocComment?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: LabeledStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: LambdaExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: LineComment?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: LocalClassDeclarationStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: LongLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: MarkerAnnotationExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: MemberValuePair?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: MethodDeclaration, arg: State) {
        out(n, arg)
        n.parameters.forEach {
            arg.types[it.nameAsString] = it.type

            val varState = if (it.type.isPrimitiveType) {
                val type = it.type.asPrimitiveType().type
                 when (type!!) {
                    PrimitiveType.Primitive.BOOLEAN -> BooleanVarState()
                    PrimitiveType.Primitive.CHAR,
                    PrimitiveType.Primitive.BYTE,
                    PrimitiveType.Primitive.SHORT,
                    PrimitiveType.Primitive.INT,
                    PrimitiveType.Primitive.LONG ->
                        NumberVarState()
                    PrimitiveType.Primitive.FLOAT, PrimitiveType.Primitive.DOUBLE -> DoubleVarState()
                }
            } else {
                ClassVarState()
            }

            arg.localVars[it.nameAsString] = varState
        }
        super.visit(n, arg)
    }

    override fun visit(n: MethodReferenceExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: NameExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: Name?, arg: State) {
        out(n, arg)
//        super.visit(n, arg)
    }

    override fun visit(n: NormalAnnotationExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: NullLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ObjectCreationExpr, arg: State) {
        out(n, arg)
//        if (n.typeAsString in classDefinitions) {
//            val classDefinition = classDefinitions[n.typeAsString]!!
//
//            if (classDefinition.constructors.size == 1) {
//                classDefinition.constructors.forEach { it.accept(this, arg) }
//            }
//        }
        super.visit(n, arg)
    }

    override fun visit(n: PackageDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: Parameter?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: PrimitiveType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ReturnStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: SimpleName, arg: State) {
        out(n, arg)

        super.visit(n, arg)
    }

    override fun visit(n: SingleMemberAnnotationExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: StringLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: SuperExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: SwitchEntry?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: SwitchStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: SynchronizedStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ThisExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ThrowStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: TryStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: TypeExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: TypeParameter?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: UnaryExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: UnionType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: UnknownType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: VariableDeclarationExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: VariableDeclarator, arg: State) {
        out(n, arg)

        arg.types[n.nameAsString] = n.type

        if (n.initializer.isPresent) {
            val initializer = n.initializer.get()

            if (n.type.isPrimitiveType || n.type.isArrayType) {
                initializer.accept(this, arg)
                val type = n.type.asPrimitiveType()

                val varState = if (initializer.isLiteralExpr) {
                    val constInitializer = initializer.asLiteralExpr()
                    when (type.type!!) {
                        PrimitiveType.Primitive.BOOLEAN ->
                            BooleanVarState().apply { exactlyValues.add(convertValue(constInitializer)) }
                        PrimitiveType.Primitive.CHAR,
                        PrimitiveType.Primitive.BYTE,
                        PrimitiveType.Primitive.SHORT,
                        PrimitiveType.Primitive.INT,
                        PrimitiveType.Primitive.LONG ->
                            NumberVarState().apply { exactlyValues.add(convertValue(constInitializer)) }
                        PrimitiveType.Primitive.FLOAT, PrimitiveType.Primitive.DOUBLE ->
                            DoubleVarState().apply { exactlyValues.add(convertValue(constInitializer)) }
//                        else -> throw IllegalArgumentException()
                    }
                } else {
                    when (type.type!!) {
                        PrimitiveType.Primitive.BOOLEAN -> BooleanVarState()
                        PrimitiveType.Primitive.CHAR,
                        PrimitiveType.Primitive.BYTE,
                        PrimitiveType.Primitive.SHORT,
                        PrimitiveType.Primitive.INT,
                        PrimitiveType.Primitive.LONG ->
                            NumberVarState()
                        PrimitiveType.Primitive.FLOAT, PrimitiveType.Primitive.DOUBLE -> DoubleVarState()
//                        else -> throw IllegalArgumentException()
                    }
                }
                arg.localVars[n.nameAsString] = varState

            } else {
                val stateForInitializer = State().apply { this.parentState = arg }
                initializer.accept(this, stateForInitializer)
                arg.localVars[n.nameAsString] = ClassVarState()
                arg.classesVars[n.nameAsString] = stateForInitializer
            }
        }

//        if (inClass) {
////            classes[className]?.add(n)
////            super.visit(n, arg)
//        } else {
//            val prefPrefix = prefixName
//            if (n.type.isPrimitiveType) {
//                vars[prefixName + "." + n.name.asString()] = false
//            } else if (n.type.isArrayType) {
//                vars[prefixName + "." + n.name.asString()] = false
//            } else if (n.type.isClassOrInterfaceType && n.type.asClassOrInterfaceType().nameWithScope in classes) {
//                classes[n.type.asClassOrInterfaceType().nameWithScope]?.forEach { visit(it, arg) }
//            }
////            super.visit(n, arg)
//            prefixName = prefPrefix
//        }

        super.visit(n, arg)
    }

    override fun visit(n: VoidType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: WhileStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: WildcardType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleDeclaration?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleRequiresDirective?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleExportsDirective?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleProvidesDirective?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleUsesDirective?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ModuleOpensDirective?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: UnparsableStmt?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: ReceiverParameter?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: VarType?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(n: Modifier?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(switchExpr: SwitchExpr?, arg: State) {
        out(switchExpr, arg)
        super.visit(switchExpr, arg)
    }

    override fun visit(n: TextBlockLiteralExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }

    override fun visit(yieldStmt: YieldStmt?, arg: State) {
        out(yieldStmt, arg)
        super.visit(yieldStmt, arg)
    }

    override fun visit(n: PatternExpr?, arg: State) {
        out(n, arg)
        super.visit(n, arg)
    }
}
