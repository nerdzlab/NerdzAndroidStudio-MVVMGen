package com.nerdzlab.mvvmflutter.generator


import com.nerdzlab.mvvmflutter.action.BlocTemplateType
import com.nerdzlab.mvvmflutter.action.ViewModelType
import com.nerdzlab.mvvmflutter.generator.components.BlocEventGenerator
import com.nerdzlab.mvvmflutter.generator.components.BlocGenerator
import com.nerdzlab.mvvmflutter.generator.components.BlocStateGenerator

object ViewModelGeneratorFactory {
    fun getViewModelGenerators(
        name: String,
        packageName: String,
        viewModelType: ViewModelType,
    ): List<com.nerdzlab.mvvmflutter.generator.ViewModelGenerator> {

        return listOf(bloc, event, state)
    }
}
