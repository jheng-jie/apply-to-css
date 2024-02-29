package org.jetbrains.plugins.apply2css

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import java.awt.Insets
import javax.swing.*
import com.intellij.openapi.project.Project


@Service
@State(
        name = "org.jetbrains.plugins.apply2css.ProjectSettingsService",
        storages = [Storage("ApplySettingsPlugin.xml")]
)
class ProjectSettingsService : PersistentStateComponent<ProjectSettingsService.State> {
    data class State(var configPath: String = "", var unitRate: String = "0.0625")

    var configurationSchemaKey: String;

    init {
        configurationSchemaKey = "Apply2CssSetting";
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var configPath: String
        get() = myState.configPath
        set(value) {
            myState.configPath = value
        }
    var unitRate: String
        get() = myState.unitRate
        set(value) {
            myState.unitRate = value
        }
}


class ApplicationSettingsComponent {
    val panel: JPanel = JPanel()
    private val configPathTextField = JTextField(1)
    private val unitRateTextField = JTextField(1)

    init {
        panel.layout = GridLayoutManager(5, 3, Insets(0, 0, 0, 0), -1, -1)

        // 說明
        val ConfigTitleRow = JPanel()
        val desc = JLabel("Set the path for your tailwind.config.js")
        ConfigTitleRow.add(desc)
        panel.add(ConfigTitleRow, GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))

        // 輸入框
        val label = JLabel("Config Path:")
        panel.add(label, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        val labeledComponent = LabeledComponent.create(configPathTextField, "")
        panel.add(labeledComponent, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        // 選擇檔案按鈕
        val browseButton = JButton("Browse")
        browseButton.addActionListener {
            val fileChooserDescriptor = object : FileChooserDescriptor(true, false, false, false, false, false) {
                override fun isFileSelectable(file: VirtualFile?): Boolean {
                    if (!file?.isDirectory!!) {
                        val extension = file.extension
                        return extension != null && setOf("js", "ts", "cjs", "mjs").contains(extension.lowercase())
                    }
                    return false
                }
            }
            fileChooserDescriptor.title = "Select Config Directory"
            val file = FileChooser.chooseFile(fileChooserDescriptor, panel, null, null)
            file?.let {
                configPathTextField.text = it.path
            }
        }
        panel.add(browseButton, GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))

        // 單位轉換說明
        val unitConfigTitleRow = JPanel()
        val unitDesc = JLabel("Set the ratio of px to rem conversion")
        unitConfigTitleRow.add(unitDesc)
        panel.add(unitConfigTitleRow, GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        // 輸入框
        val unitLabel = JLabel("Ratio:")
        panel.add(unitLabel, GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        val unitLabeledComponent = LabeledComponent.create(unitRateTextField, "")
        panel.add(unitLabeledComponent, GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))

        // 第三行，撐滿剩餘空間
        val emptyPanel = JPanel()
        panel.add(emptyPanel, GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
    }


    var configPath: String
        get() = configPathTextField.text
        set(value) {
            configPathTextField.text = value
        }

    var unitRate: String
        get() = unitRateTextField.text
        set(value) {
            unitRateTextField.text = value
        }
}

class ApplicationSettingsConfigurable(private val project: Project) : Configurable {
    private var mySettingsComponent: ApplicationSettingsComponent? = null

    override fun getDisplayName(): String = "ApplyToCSS Settings"

    override fun createComponent(): JComponent {
        mySettingsComponent = ApplicationSettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = project.getService(ProjectSettingsService::class.java)
        val configModified = mySettingsComponent?.configPath != settings.configPath
        val unitModified = mySettingsComponent?.unitRate != settings.unitRate
        return configModified || unitModified
    }

    override fun apply() {
        val settings = project.getService(ProjectSettingsService::class.java)
        mySettingsComponent?.configPath?.let {
            settings.configPath = it
        }
        mySettingsComponent?.unitRate?.let {
            settings.unitRate = it
        }
    }

    override fun reset() {
        val settings = project.getService(ProjectSettingsService::class.java)
        mySettingsComponent?.configPath = settings.configPath
        mySettingsComponent?.unitRate = settings.unitRate
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}

