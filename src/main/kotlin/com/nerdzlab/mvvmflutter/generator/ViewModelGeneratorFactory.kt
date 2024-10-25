package com.nerdzlab.mvvmflutter.generator


import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.components.ViewModelGenerator
import com.nerdzlab.mvvmflutter.generator.components.ViewModelRouteTypeGenerator
import com.nerdzlab.mvvmflutter.generator.components.ViewModelScreenGenerator
import com.nerdzlab.mvvmflutter.generator.components.ViewModelTypeGenerator

object ViewModelGeneratorFactory {
    fun getViewModelGenerators(
        name: String,
        packageName: String,
        viewModelType: ViewModelType,
    ): List<com.nerdzlab.mvvmflutter.generator.ViewModelGenerator> {
        val type = ViewModelTypeGenerator(name, packageName)
        val routeType = ViewModelRouteTypeGenerator(name, packageName)
        val viewModel = ViewModelGenerator(name, packageName)
        val screen = ViewModelScreenGenerator(name, packageName, viewModelType)
        return listOf(type, routeType, viewModel, screen)
    }
}
