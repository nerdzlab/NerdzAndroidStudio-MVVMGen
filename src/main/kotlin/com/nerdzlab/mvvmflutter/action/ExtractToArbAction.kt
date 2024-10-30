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

        if (selectedText.isBlank() || selectedText == "''" || selectedText == "\"\"") {
            Messages.showErrorDialog(
                "Can't extract. Please select a string enclosed in either single (' ') or double (' \\\" ') quotes.",
                "Error"
            )
            return
        }

        // Step 1: Prompt for key
        val key = Messages.showInputDialog(
            project,
            "Enter the localization key:",
            "New Localization Key",
            null
        )

        val validARBKey = Regex("^[a-z][a-zA-Z0-9]*([A-Z][a-zA-Z0-9]*)*$")
        if (key == null) {
            Messages.showErrorDialog(
                "ARB key must be not empty",
                "Error"
            )
            return
        } else if (!validARBKey.matches(key)) {
            Messages.showErrorDialog(
                "ARB key must start with a lowercase letter and can contain numbers, e.g., exampleKey123",
                "Error"
            )
            return

        }

        // Step 2: Find `l10n.yaml` and get `arb-dir`
        val l10nYamlFile = findL10nYaml(project)
        if (l10nYamlFile == null) {
            Messages.showErrorDialog(
                "File l10n.yaml not found in the workspace.",
                "Error"
            )
            return
        }

        val arbDir = l10nYamlFile.let { project.basePath + File.separator + getArbDir(it) }

        // Step 3: Insert key-value in all ARB files
        val result = insertKeyValueInArbFiles(arbDir, key, selectedText.trim('\'', '\"'))
        if (!result) return

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

    private fun insertKeyValueInArbFiles(arbDir: String, key: String, value: String): Boolean {
        // Find ARB files in the specified directory and insert the key-value pair
        val arbFilesDir = File(arbDir)

        if (arbFilesDir.exists() && arbFilesDir.isDirectory) {
            arbFilesDir.listFiles { file -> file.extension == "arb" }?.forEach { arbFile ->
                // Read the existing ARB content
                val existingContent = arbFile.readText()
                val decodedText = Json.decodeFromString<Map<String, String>>(existingContent)
                if (decodedText.containsKey(key)) {
                    Messages.showErrorDialog(
                        "ARB already contains key $key",
                        "Error"
                    )
                    return false
                }

                // Write back to the ARB file
                val prettyJson = Json {
                    prettyPrint = true
                }
                arbFile.writeText(prettyJson.encodeToString(decodedText.plus(Pair(key, value))))
            }

            return true
        }

        Messages.showErrorDialog(
            "ARB directory specified in l10n.yaml does not exist.",
            "Error"
        )
        return false
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
