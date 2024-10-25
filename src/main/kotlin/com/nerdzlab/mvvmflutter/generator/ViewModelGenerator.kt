package com.nerdzlab.mvvmflutter.generator

import com.fleshgrinder.extensions.kotlin.toLowerSnakeCase
import com.fleshgrinder.extensions.kotlin.toUpperCamelCase
import com.google.common.io.CharStreams
import com.nerdzlab.mvvmflutter.action.ViewModelType
import org.apache.commons.lang.text.StrSubstitutor
import java.io.InputStreamReader

abstract class ViewModelGenerator(
    private val name: String,
    private val packageName: String?,
    templateName: String
) {

    private val TEMPLATE_VIEW_MODEL_PASCAL_CASE = "view_model_pascal_case"
    private val TEMPLATE_VIEW_MODEL_SNAKE_CASE = "view_model_snake_case"
    private val TEMPLATE_PACKAGE_NAME_SNAKE_CASE = "package_name_snake_case"

    private val templateString: String
    private val templateViewModelValues: MutableMap<String, String>

    init {
        templateViewModelValues = mutableMapOf(
            TEMPLATE_VIEW_MODEL_PASCAL_CASE to pascalCase(),
            TEMPLATE_VIEW_MODEL_SNAKE_CASE to snakeCase(),
            TEMPLATE_PACKAGE_NAME_SNAKE_CASE to snakeCasePackageName()
        )
        try {
            val resource = "/templates/view_model/$templateName.dart.template"
            val resourceAsStream = BlocGenerator::class.java.getResourceAsStream(resource)
            templateString = CharStreams.toString(InputStreamReader(resourceAsStream!!, Charsets.UTF_8))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    abstract fun fileName(): String

    fun generate(): String {
        val viewModelNameSubstitutor = StrSubstitutor(templateViewModelValues, "{{", "}}", '\\')
        return viewModelNameSubstitutor.replace(templateString)
    }

    private fun pascalCase(): String = name.toUpperCamelCase()

    fun snakeCase(): String = name.toLowerSnakeCase()

    fun snakeCasePackageName(): String = packageName?.toLowerSnakeCase() ?: ""

    fun fileExtension() = "dart"
}
