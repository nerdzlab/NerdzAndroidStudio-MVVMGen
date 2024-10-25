package com.nerdzlab.mvvmflutter.generator.components

import com.nerdzlab.mvvmflutter.action.BlocTemplateType
import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.BlocGenerator
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator

class ViewModelRouteTypeGenerator(
    name: String,
    packageName: String,
    viewModelType: ViewModelType,
) : ViewModelGenerator(name, packageName, viewModelType, templateName = "view_model_route_type") {
    override fun fileName() = "${snakeCase()}_view_model_route_type.${fileExtension()}"
}
