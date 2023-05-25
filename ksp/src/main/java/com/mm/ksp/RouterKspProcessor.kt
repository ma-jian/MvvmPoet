package com.mm.ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.mm.annotation.RouterPath
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.WildcardTypeName
import javax.annotation.processing.Processor


/**
 * Date : 2023/5/16
 */
class RouterKspProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    companion object {
        const val KEY_MODULE_NAME = "moduleName"
        const val ROUTER_INTERFACE_PATH = "com.mm.common.router.IRouterRulesCreator"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val logger = env.logger
        val options = env.options
        val codeGenerator = env.codeGenerator

        val moduleName = options[KEY_MODULE_NAME]
        if (moduleName.isNullOrEmpty()) {
            logger.error("请设置 moduleName ")
            return emptyList()
        }
        //查找RouterPath
        val symbols = resolver.getSymbolsWithAnnotation(RouterPath::class.qualifiedName!!)
        //使用kotlinpoet构建类型 HashMap<String, Class<out Activity>>
        val activity = ClassName("android.app", "Activity")
        val hashMap = ClassName("java.util", "HashMap")
        val classK = ClassName("java.lang", "Class")
        val stringK = ClassName("kotlin", "String")
        val classActivity = classK.parameterizedBy(WildcardTypeName.producerOf(activity))

        val hashMapSC = hashMap.parameterizedBy(stringK, classActivity)
        //创建initRule方法
        val functionSpec = FunSpec
            .builder("initRule")
            .addAnnotation(Override::class.java)
            .addParameter(ParameterSpec.builder("rules", hashMapSC).build())
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it as KSClassDeclaration
                val activityClass = ClassName(it.packageName.asString(), it.simpleName.asString())
                //遍历该类所有的注解
                it.annotations.forEach { ann ->
                    //找到Route注解的参数route
                    val resValue = ann.arguments.find { it.name!!.asString() == "value" }!!.value
                    if (resValue is Array<*>) {
                        for (value in resValue) {
                            functionSpec.addStatement("rules[\"${value}\"] = ${activityClass}::class.java")
                        }
                    } else {
                        //写入loadInfo方法中
                        functionSpec.addStatement("rules[\"${resValue}\"] = ${activityClass}::class.java")
                    }
                }
            }

//        val file = FileSpec.builder(moduleName, "${moduleName}_AutoRouterRuleCreator")
//            .addType(
//                TypeSpec.classBuilder("Greeter")
//                    .primaryConstructor(
//                        FunSpec.constructorBuilder()
//                            .addParameter("name", String::class)
//                            .build()
//                    )
//                    .addProperty(
//                        PropertySpec.builder("name", String::class)
//                            .initializer("name")
//                            .build()
//                    )
//                    .addFunction(
//                        FunSpec.builder("greet")
//                            .addStatement("println(%P)", "Hello, \$name")
//                            .build()
//                    )
//                    .build()
//            )
//            .addFunction(
//                FunSpec.builder("main")
//                    .addParameter("args", String::class, "VARARG")
//                    .addStatement("%T(args[0]).greet()", greeterClass)
//                    .build()
//            )
//            .build()


        return symbols.filter { !it.validate() }.toList()


//        //查找注解的类
//        val elements = symbols.filterIsInstance<KSClassDeclaration>().toList()
//        val map = mutableMapOf<String, List<String>>()
//        elements.forEach {
//            it.annotations.toList().forEach { ks ->
//                // 防止多个注解的情况
//                if (ks.shortName.asString() == "RouterPath") {
//                    var path = ""
//                    ks.arguments.forEach { ksValueA ->
//                        if (ksValueA.name?.asString() == "value") {
//                            path = ksValueA.value as Arrays<String>
//                        }
//                    }
//                }
//            }
//        }
//
//        return emptyList()
    }

    inner class FindFunctionsVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            //访问 Class 节点
            classDeclaration.getDeclaredFunctions().map { it.accept(this, Unit) }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            // 访问 function 节点
        }

        override fun visitFile(file: KSFile, data: Unit) {
            //访问 file
            file.declarations.map { it.accept(this, Unit) }
        }
    }
}

@AutoService(Processor::class)
class RouterKspProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) = RouterKspProcessor(environment)
}