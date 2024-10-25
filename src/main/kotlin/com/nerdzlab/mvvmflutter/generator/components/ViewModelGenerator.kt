package com.nerdzlab.mvvmflutter.generator.components

import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator

class ViewModelGenerator(
    name: String,
    packageName: String,
) : ViewModelGenerator(name, packageName, templateName = "view_model") {
    override fun fileName() = "${snakeCase()}_screen_view_model.${fileExtension()}"
}
