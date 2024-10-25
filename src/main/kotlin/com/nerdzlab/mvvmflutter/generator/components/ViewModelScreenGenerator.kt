package com.nerdzlab.mvvmflutter.generator.components

import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator

class ViewModelScreenGenerator(
    name: String,
    packageName: String,
    viewModelType: ViewModelType,
) : ViewModelGenerator(
    name,
    packageName,
    templateName = if (viewModelType == ViewModelType.STATELESS) "view_model_screen_stateless" else "view_model_screen_stateful"
) {
    override fun fileName() = "${snakeCase()}_screen.${fileExtension()}"
}
