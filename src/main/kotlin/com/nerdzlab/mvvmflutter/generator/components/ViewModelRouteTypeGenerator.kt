package com.nerdzlab.mvvmflutter.generator.components

import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator

class ViewModelRouteTypeGenerator(
    name: String,
    packageName: String,
) : ViewModelGenerator(name, packageName, templateName = "view_model_route_type") {
    override fun fileName() = "${snakeCase()}_screen_view_model_route_type.${fileExtension()}"
}
