package com.nerdzlab.mvvmflutter.generator.components

import com.nerdzlab.mvvmflutter.action.BlocTemplateType
import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.BlocGenerator
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator

class ViewModelTypeGenerator(
    name: String,
    packageName: String,
    viewModelType: ViewModelType,
) : ViewModelGenerator(name, packageName, viewModelType, templateName = "view_model_type") {
    override fun fileName() = "${snakeCase()}_view_model_type.${fileExtension()}"
}
