package com.zj.gyl.windy.toolWindow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.observable.util.addMouseListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.util.preferredHeight
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.event.CellEditorListener
import javax.swing.tree.*


class WindyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {


        val myToolWindow = WindyUIWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class WindyUIWindow(toolWindow: ToolWindow) {
        private var selectedNode: DefaultMutableTreeNode? = null
        private var parentNode: DefaultMutableTreeNode? = null
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            var root = DefaultMutableTreeNode("Windy")

            var node1 = DefaultMutableTreeNode("需求")
            node1.add(DefaultMutableTreeNode("需求1"))
            node1.add(DefaultMutableTreeNode("需求2"))
            node1.add(DefaultMutableTreeNode("需求3"))
            root.add(node1)

            var node2 = DefaultMutableTreeNode("缺陷")
            node2.add(DefaultMutableTreeNode("缺陷1"))
            node2.add(DefaultMutableTreeNode("缺陷2"))
            node2.add(DefaultMutableTreeNode("缺陷3"))
            root.add(node2)

            var node3 = DefaultMutableTreeNode("任务")
            node3.add(DefaultMutableTreeNode("任务1"))
            node3.add(DefaultMutableTreeNode("任务2"))
            node3.add(DefaultMutableTreeNode("任务3"))
            root.add(node3)

            val tree = JTree(root)
            val menu = JPopupMenu() //创建菜单
            val menuItem = JMenuItem("刷新F5") //创建菜单项(点击菜单项相当于点击一个按钮)
            menu.add(menuItem)
            menuItem.addActionListener(ActionListener {
                actionEvent -> println("假装刷新一下子吧")
                selectedNode?.let { node ->
                    println("当前选中的节点: ${node.userObject}")
                }
                parentNode?.let { node ->
                    println("当前选中父节点: ${node.userObject}")
                }
            })

            tree.addMouseListener(object : MouseAdapter(){
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e);
                    var x  = e!!.x;
                    var y = e.y;
                    if(e.getButton()==MouseEvent.BUTTON3){
                        var location = tree.getPathForLocation(e!!.x, e.y)
                        selectedNode = location?.lastPathComponent as? DefaultMutableTreeNode
                        parentNode = selectedNode?.parent as? DefaultMutableTreeNode
                        tree.setSelectionPath(location);
                        menu.show(tree, x, y);
                    }
                }
            })

            // 自定义渲染器
            tree.cellRenderer = object : DefaultTreeCellRenderer() {
                override fun getTreeCellRendererComponent(
                    tree: JTree,
                    value: Any,
                    selected: Boolean,
                    expanded: Boolean,
                    leaf: Boolean,
                    row: Int,
                    hasFocus: Boolean
                ): Component {
                    val panel = JPanel(BorderLayout())
                    val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel

                    panel.add(label, BorderLayout.CENTER)

                    if (value is DefaultMutableTreeNode) {
                        val node = value as DefaultMutableTreeNode
                        if (node.userObject == "任务") {
                            val button = JButton("+")
                            button.setSize(10,20)
                            button.addActionListener(object : ActionListener {
                                override fun actionPerformed(e: ActionEvent?) {
                                    println("Button clicked in ${node.userObject}")
                                }
                            })
                            panel.add(button, BorderLayout.EAST)
                        }
                    }
                    return panel
                }
            }

            // 自定义编辑器
            tree.cellEditor = object : TreeCellEditor {
                private val editor = JPanel(BorderLayout())

                override fun getTreeCellEditorComponent(
                    tree: JTree,
                    value: Any,
                    isSelected: Boolean,
                    expanded: Boolean,
                    leaf: Boolean,
                    row: Int
                ): Component {
                    editor.removeAll()
                    val rendererComponent = tree.cellRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true)
                    editor.add(rendererComponent, BorderLayout.CENTER)
                    return editor
                }

                override fun getCellEditorValue(): Any {
                    return (editor.components[0] as JLabel).text
                }

                override fun isCellEditable(e: EventObject?): Boolean {
                    return true
                }

                override fun shouldSelectCell(e: EventObject?): Boolean {
                    return true
                }

                override fun stopCellEditing(): Boolean {
                    return true
                }

                override fun cancelCellEditing() {}
                override fun addCellEditorListener(p0: CellEditorListener?) {
                    TODO("Not yet implemented")
                }

                override fun removeCellEditorListener(p0: CellEditorListener?) {
                    TODO("Not yet implemented")
                }
            }
            add(tree)
        }
    }
}
