package com.nerdzlab.mvvmflutter.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.yaml.snakeyaml.Yaml
import java.io.File


class ExtractToArbAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private lateinit var dataContext: DataContext

    override fun actionPerformed(e: AnActionEvent) {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return

        val selectionModel: SelectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return

        // Step 1: Prompt for key
        val key = Messages.showInputDialog(
            project,
            "Enter the localization key:",
            "New Localization Key",
            null
        )
            ?: return // Cancelled

        // Step 2: Find `l10n.yaml` and get `arb-dir`
        val l10nYamlFile = findL10nYaml(project)
        val arbDir = l10nYamlFile?.let { project.basePath + File.separator + getArbDir(it) } ?: return

        // Step 3: Insert key-value in all ARB files
        insertKeyValueInArbFiles(arbDir, key, selectedText)

        // Run `flutter l10n-gen` command
        runCommand(project.basePath!!)

        val document = editor.document
        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(
                project
            ) {
                document.replaceString(start, end, "context.locale.${key}")
            }
        }

    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        e.dataContext.let {
            this.dataContext = it
            val presentation = e.presentation
            presentation.isEnabled = true
            presentation.isEnabledAndVisible = editor != null && editor.selectionModel.hasSelection()
        }
    }

    private fun findL10nYaml(project: Project): File? {
        // Search for `l10n.yaml` in the project files
        val baseDir = project.basePath ?: return null
        val l10nYamlPath = File(baseDir, "l10n.yaml")

        return if (l10nYamlPath.exists()) l10nYamlPath else null
    }

    private fun getArbDir(l10nYamlFile: File): String? {
        // Read `l10n.yaml` and extract `arb-dir`
        val yaml = Yaml()
        val inputStream = l10nYamlFile.inputStream()
        val data = yaml.load<Map<String, Any>>(inputStream)

        return data["arb-dir"] as? String
    }

    private fun insertKeyValueInArbFiles(arbDir: String, key: String, value: String) {
        // Find ARB files in the specified directory and insert the key-value pair
        val arbFilesDir = File(arbDir)

        if (arbFilesDir.exists() && arbFilesDir.isDirectory) {
            arbFilesDir.listFiles { file -> file.extension == "arb" }?.forEach { arbFile ->
                // Read the existing ARB content
                val existingContent = arbFile.readText()
                val decodedText = Json.decodeFromString<Map<String, String>>(existingContent)
                if (decodedText.containsKey(key)) return

                // Write back to the ARB file
                val prettyJson = Json {
                    prettyPrint = true
                }
                arbFile.writeText(prettyJson.encodeToString(decodedText.plus(Pair(key, value))))
            }
        }
    }

    private fun runCommand(directory: String) {
        ProcessBuilder("flutter", "l10n-gen")
            .directory(File(directory))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .waitFor()
    }
}
