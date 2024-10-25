package com.nerdzlab.mvvmflutter.action

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.impl.toolkit.IdeDesktopPeer.Companion.logger
import com.intellij.psi.*
import com.nerdzlab.mvvmflutter.generator.ViewModelGenerator
import com.nerdzlab.mvvmflutter.generator.ViewModelGeneratorFactory
import java.awt.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.*

class GenerateViewModelAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private lateinit var dataContext: DataContext

    override fun actionPerformed(e: AnActionEvent) {
        // Get package name
        if (e.project == null) return
        val packageName = getPackageName(e.project!!)
        if (packageName == null) {
            Messages.showErrorDialog(e.project, "Project don't have pubspec.yaml.", "Error")
            return
        }

        // Get settings data
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // Create input field for ViewModel name
        val nameField = JTextField(20)
        panel.add(JLabel("Enter ViewModel Name:"))
        panel.add(nameField)

        // Create a combo box for widget type selection
        val widgetOptions = arrayOf("StatelessWidget", "StatefulWidget")
        val widgetComboBox = ComboBox(widgetOptions)
        panel.add(JLabel("Select Widget Type:"))
        panel.add(widgetComboBox)


        val dialogBuilder = DialogBuilder()
        dialogBuilder.setTitle("New ViewModel")
        dialogBuilder.setCenterPanel(panel)
        dialogBuilder.setOkActionEnabled(true)
        if (dialogBuilder.show() != DialogWrapper.OK_EXIT_CODE) return

        val className = nameField.text.trim()
        if (className.isEmpty()) {
            Messages.showErrorDialog(e.project, "ViewModel name cannot be empty.", "Error")
            return
        }
        val selectedWidgetType =
            if ("StatelessWidget" == (widgetComboBox.selectedItem as String)) ViewModelType.STATELESS else ViewModelType.STATEFUL


        val generators = ViewModelGeneratorFactory.getViewModelGenerators(className, packageName, selectedWidgetType)
        generate(generators)
    }

    override fun update(e: AnActionEvent) {
        e.dataContext.let {
            this.dataContext = it
            val presentation = e.presentation
            presentation.isEnabled = true
        }
    }

    private fun generate(mainSourceGenerators: List<ViewModelGenerator>) {
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val view = LangDataKeys.IDE_VIEW.getData(dataContext)
        val directory = view?.orChooseDirectory
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(
                project, {
                    mainSourceGenerators.forEach { createSourceFile(project!!, it, directory!!) }
                }, "Generate a new Bloc", null
            )
        }
    }

    private fun createSourceFile(project: Project, generator: ViewModelGenerator, directory: PsiDirectory) {
        val fileName = generator.fileName()
        val existingPsiFile = directory.findFile(fileName)
        if (existingPsiFile != null) {
            val document = PsiDocumentManager.getInstance(project).getDocument(existingPsiFile)
            document?.insertString(document.textLength, "\n" + generator.generate())
            return
        }
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, JavaLanguage.INSTANCE, generator.generate())
        directory.add(psiFile)
    }

    private fun getPackageName(project: Project): String? {
        // Find the pubspec.yaml file in the project's root directory
        val baseDir = project.guessProjectDir()
        val pubspecFile: VirtualFile? = baseDir?.findChild("pubspec.yaml")
        logger.info("Package name retrieved: $baseDir")
        if (pubspecFile != null) {
            // Read the contents of the pubspec.yaml file
            val inputStream = pubspecFile.inputStream
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Look for the line starting with "name:"
                    if (line!!.startsWith("name:")) {
                        // Extract the package name (trim whitespace and any comments)
                        return line!!.substringAfter("name:").trim()
                    }
                }
            }
        }

        return null // Return null if the package name is not found
    }
}
