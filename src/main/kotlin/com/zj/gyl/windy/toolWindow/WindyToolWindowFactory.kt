package com.zj.gyl.windy.toolWindow

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.zj.gyl.windy.services.MyProjectService
import com.zj.gyl.windy.services.WorkTask
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.stream.Collectors
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import com.intellij.ui.content.Content
import org.jetbrains.annotations.NonNls


class WindyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val propertiesComponent = PropertiesComponent.getInstance()
        val token = propertiesComponent.getValue("user.token")
        val window: JPanel
        if (token != null && validateTokenWithServer(token)) {
            // Token is valid, show business page
            window = LoginUIWindow(toolWindow).getContent()
        } else {
            window = WindyUIWindow(toolWindow).getContent()
        }
        val content = ContentFactory.getInstance().createContent(window, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun validateTokenWithServer(token: String): Boolean {
        if ("" != token){
            return true
        }
        return false
    }


    override fun shouldBeAvailable(project: Project) = true


    class LoginUIWindow(val toolWindow: ToolWindow) {
        private val project: Project = toolWindow.project

        private fun resizeIcon(icon: Icon, width: Int, height: Int): Icon {
            val img = (icon as ImageIcon).image
            val resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            return ImageIcon(resizedImg)
        }
        fun getContent(): JPanel {
            val mainPanel = JPanel(GridBagLayout())
            val gbc = GridBagConstraints()

            // 增加填充使内容上下居中
            gbc.weightx = 1.0
            gbc.weighty = 0.1
            gbc.gridx = 0
            gbc.gridy = 0
            mainPanel.add(Box.createGlue(), gbc)

            // Image row
            val imageLabel = JLabel()
            gbc.gridx = 0
            gbc.gridy = 1
            gbc.gridwidth = 2  // 跨越两列以确保居中
            gbc.anchor = GridBagConstraints.CENTER  // 水平居中
            gbc.fill = GridBagConstraints.NONE  // 不填充水平空间
            gbc.insets = Insets(10, 10, 10, 10)
            mainPanel.add(imageLabel, gbc)
            // 添加窗口大小监听器以调整图片大小
            mainPanel.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    val with = mainPanel.width / 3
                    val image = resizeIcon(ImageIcon(javaClass.classLoader.getResource("icons/windy.png")), with, with)
                    imageLabel.icon = image
                }
            })

            // 表单部分设置为水平居中
            val formPanel = JPanel(GridBagLayout())
            val formGbc = GridBagConstraints()
            formGbc.insets = Insets(5, 5, 5, 5)
            formGbc.fill = GridBagConstraints.NONE  // 表单不填满整个面板
            formGbc.anchor = GridBagConstraints.CENTER  // 水平居中

            formGbc.gridx = 0
            formGbc.gridy = 0
            formPanel.add(JLabel("姓名"), formGbc)

            formGbc.gridx = 1
            val nameField = JTextField(20)
            formPanel.add(nameField, formGbc)

            formGbc.gridx = 0
            formGbc.gridy = 1
            formPanel.add(JLabel("密码"), formGbc)

            formGbc.gridx = 1
            val passwordField = JPasswordField(20)
            formPanel.add(passwordField, formGbc)

            formGbc.gridx = 0
            formGbc.gridy = 2
            formGbc.gridwidth = 2
            formGbc.fill = GridBagConstraints.NONE
            formGbc.anchor = GridBagConstraints.CENTER
            val confirmButton = JButton("确认")
            formPanel.add(confirmButton, formGbc)

            gbc.gridy = 2
            gbc.gridx = 0
            gbc.gridwidth = 2  // 跨越两列以确保居中
            gbc.anchor = GridBagConstraints.CENTER  // 水平居中
            gbc.insets = Insets(5, 10, 5, 10)
            gbc.fill = GridBagConstraints.NONE  // 不填充水平方向空间
            mainPanel.add(formPanel, gbc)

            // 增加填充使内容上下居中
            gbc.weightx = 1.0
            gbc.weighty = 0.1
            gbc.gridx = 0
            gbc.gridy = 3
            mainPanel.add(Box.createGlue(), gbc)

            // 设置按钮放置在底部右下角，宽度保持为50
            val settingsButton = JButton("设置")
            gbc.gridx = 1
            gbc.gridy = 4
            gbc.anchor = GridBagConstraints.SOUTHEAST
            gbc.insets = Insets(10, 10, 10, 10)
            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.NONE
            mainPanel.add(settingsButton, gbc)
            settingsButton.preferredSize = Dimension(50, settingsButton.preferredSize.height)

            // Confirm Button Action
            confirmButton.addActionListener {
                // Implement your authentication logic here
                if (authenticateUser(nameField.text, String(passwordField.password))) {
                    // On success, switch to the business page
                    PropertiesComponent.getInstance().setValue("user.token", "hhhhhhh")
                    switchToBusinessPage()
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "登录失败，请重试")
                }
            }

            // Settings Button Action
            settingsButton.addActionListener {
                showSettingsDialog()
            }

            return mainPanel
        }

        private fun authenticateUser(username: String, password: String): Boolean {
            // Replace with actual authentication logic
            return username == "admin" && password == "admin"
        }

        private fun switchToBusinessPage() {
            val contentManager = toolWindow.contentManager
            val content = contentManager.factory.createContent(WindyUIWindow(toolWindow).getContent(), null, false)
            contentManager.removeAllContents(true)
            contentManager.addContent(content)
        }

        private fun showSettingsDialog() {
            val dialog = JDialog()
            dialog.title = "服务配置"
            dialog.isModal = true

            // 设置弹框的大小
            dialog.setSize(500, 250)
            dialog.setLocationRelativeTo(null) // 窗口居中

            val panel = JPanel(GridBagLayout())
            val gbc = GridBagConstraints()

            // 主要内容区域（居中显示）
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 1.0
            gbc.fill = GridBagConstraints.BOTH
            gbc.insets = Insets(10, 10, 10, 10)
            val contentPanel = JPanel(GridBagLayout())
            val contentGbc = GridBagConstraints()
            contentGbc.insets = Insets(5, 5, 5, 5)
            contentGbc.anchor = GridBagConstraints.CENTER

            contentGbc.gridx = 0
            contentGbc.gridy = 0
            contentPanel.add(JLabel("服务地址"), contentGbc)

            contentGbc.gridx = 1
            val addressField = JTextField(30)
            contentPanel.add(addressField, contentGbc)

            gbc.anchor = GridBagConstraints.CENTER
            panel.add(contentPanel, gbc)

            // 底部确认按钮
            gbc.gridx = 0
            gbc.gridy = 1
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.anchor = GridBagConstraints.SOUTH
            val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
            val confirmButton = JButton("确认")
            buttonPanel.add(confirmButton)
            panel.add(buttonPanel, gbc)

            confirmButton.addActionListener {
                dialog.dispose()
            }

            dialog.contentPane.add(panel)
            dialog.isVisible = true
        }
    }

    class WindyUIWindow(val toolWindow: ToolWindow) {
        private val project: Project = toolWindow.project
        private var selectedNode: DefaultMutableTreeNode? = null
        private var parentNode: DefaultMutableTreeNode? = null
        private var dataMap: HashMap<String, Any> = HashMap()
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            //创建树节点
            val root = DefaultMutableTreeNode("Windy")
            val demandNode = DefaultMutableTreeNode("需求")
            val bugNode = DefaultMutableTreeNode("缺陷")
            val workNode = DefaultMutableTreeNode("任务")
            root.add(demandNode)
            root.add(bugNode)
            root.add(workNode)

            val tree = JTree(root)
            bindSubTreeNode(tree, demandNode, bugNode, workNode)
            tree.cellRenderer = CustomTreeCellRenderer()

            //绑定右侧菜单
            bindRightMenu(tree)
            val scrollPane = JScrollPane(tree)
            scrollPane.border = BorderFactory.createEmptyBorder()
            background = UIUtil.getPanelBackground()
            add(scrollPane, BorderLayout.CENTER)


            //开始添加底部按钮
            val (addWorkBtn, btnRefresh) = addBottomButtons(toolWindow)
            addWorkBtn.addActionListener {
                //新增创建工作项的表单
                createWorkFormUI(tree, demandNode, bugNode, workNode)
            }
            btnRefresh.addActionListener {
                //点击刷新按钮，重新绑定下树得到子节点
                bindSubTreeNode(tree, demandNode, bugNode, workNode)
            }
        }

        class CustomTreeCellRenderer : DefaultTreeCellRenderer() {
            private fun resizeIcon(icon: Icon, width: Int, height: Int): Icon {
                val img = (icon as ImageIcon).image
                val resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
                return ImageIcon(resizedImg)
            }

            private val parentIconMap: Map<String, Icon> = mapOf(
                "缺陷" to resizeIcon(ImageIcon(javaClass.classLoader.getResource("icons/bug.png")), 12, 12),
                "需求" to resizeIcon(ImageIcon(javaClass.classLoader.getResource("icons/demand.png")), 12, 12),
                "任务" to resizeIcon(ImageIcon(javaClass.classLoader.getResource("icons/task.png")), 12, 12)
            )

            override fun getTreeCellRendererComponent(
                tree: JTree?,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ): Component {
                val component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
                if (leaf) {
                    if (component is JLabel && value is DefaultMutableTreeNode) {
                        val originalText = value.userObject.toString()
                        component.toolTipText = originalText
                        val parentNode = value.parent as? DefaultMutableTreeNode
                        val parentName = ((parentNode?.userObject as? String) ?: "任务").split(" ")[0]
                        val icon = parentIconMap[parentName] ?: parentIconMap["任务"]
                        component.icon = icon
                        if (parentIconMap.keys.contains(value.userObject as? String)) {
                            component.icon = openIcon
                        }
                    }
                }

                return component
            }
        }

        private fun JBPanel<JBPanel<*>>.addBottomButtons(toolWindow: ToolWindow): Pair<JButton, JButton> {
            val panel = JPanel(GridBagLayout())
            val addWorkBtn = JButton("+ 新增工作项")
            val btnRefresh = JButton("刷新")

            // Create a popup menu
            val popupMenu = JPopupMenu().apply {
                add(JMenuItem("退出登录")).addActionListener {
                    // Handle logout action
                    PropertiesComponent.getInstance().setValue("user.token","")
                    val contentManager = toolWindow.contentManager
                    val content = contentManager.factory.createContent(LoginUIWindow(toolWindow).getContent(), null, false)
                    contentManager.removeAllContents(true)
                    contentManager.addContent(content)
                }
            }

//            val icon = resizeIcon(ImageIcon(javaClass.classLoader.getResource("icons/setting.png")), 24, 24)
            // Create a button that shows the popup menu
            val popupButton = JButton("设置").apply {
                addActionListener {
                    popupMenu.show(this, 0, -popupMenu.preferredSize.height) // Position above the button
                }
            }

            val gbc = GridBagConstraints()
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            gbc.gridx = 1
            gbc.gridy = 0
            panel.add(btnRefresh, gbc)

            gbc.weightx = 0.0
            gbc.gridx = 2
            gbc.gridy = 0
            panel.add(popupButton, gbc)

            gbc.weightx = 0.0
            gbc.gridx = 0
            gbc.gridy = 0
            panel.add(addWorkBtn, gbc)

            add(panel, BorderLayout.SOUTH)
            return Pair(addWorkBtn, btnRefresh)
        }


        private fun createWorkFormUI(
            tree: JTree,
            demandNode: DefaultMutableTreeNode,
            bugNode: DefaultMutableTreeNode,
            workNode: DefaultMutableTreeNode
        ) {
            val ideFrame = WindowManager.getInstance().getIdeFrame(project)
            val frame = ideFrame!!.component as? JFrame

            val dialog = JDialog(frame, "新增工作项", true)
            dialog.layout = GridBagLayout()
            val gbc = GridBagConstraints()

            // 设置默认权重和填充
            gbc.insets = JBUI.insets(5)
            gbc.fill = GridBagConstraints.NONE
            gbc.anchor = GridBagConstraints.NORTH

            // 第一行：工作项名称和输入框
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            dialog.add(JLabel("工作项名称:"), gbc)

            gbc.gridx = 1
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            val nameField = JTextArea(4, 20) // 设定较大的输入框
            val scrollNamePane = JScrollPane(nameField)
            dialog.add(scrollNamePane, gbc)

            // 第二行：关联需求/缺陷ID和下拉框
            gbc.gridx = 0
            gbc.gridy = 1
            gbc.fill = GridBagConstraints.NONE
            gbc.weightx = 0.0
            dialog.add(JLabel("关联需求/缺陷ID:"), gbc)

            gbc.gridx = 1
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            class SelectItem(var id: String, var name: String){
                override fun toString(): String {
                    return name
                }
            }
            val myService = project.getService(MyProjectService::class.java)
            val items = myService.demandPage!!.getList().stream().map {
                SelectItem(it.demandId, it.demandName)
            }.collect(Collectors.toList())
            val bugItems = myService.bugPage!!.getList().stream().map {
                SelectItem(it.bugId, it.bugName)
            }.collect(Collectors.toList())
            items.addAll(bugItems)

            val idComboBox = JComboBox(items.toTypedArray())
            dialog.add(idComboBox, gbc)

            // 第三行：工作项描述和输入框
            gbc.gridx = 0
            gbc.gridy = 2
            gbc.fill = GridBagConstraints.NONE
            gbc.weightx = 0.0
            dialog.add(JLabel("工作项描述:"), gbc)

            gbc.gridx = 1
            gbc.fill = GridBagConstraints.BOTH
            gbc.weightx = 1.0
            val descriptionField = JTextArea(5, 20) // 设定较大的输入框
            dialog.add(JScrollPane(descriptionField), gbc)

            // 添加确认和取消按钮
            val buttonPanel = JPanel()
            buttonPanel.layout = FlowLayout(FlowLayout.RIGHT)
            val okButton = JButton("确认")
            val cancelButton = JButton("取消")

            buttonPanel.add(okButton)
            buttonPanel.add(cancelButton)

            gbc.gridx = 0
            gbc.gridy = 3
            gbc.gridwidth = 2
            gbc.anchor = GridBagConstraints.EAST
            gbc.fill = GridBagConstraints.NONE
            dialog.add(buttonPanel, gbc)

            // 设置按钮点击事件
            okButton.addActionListener {
                // 处理确认操作
                val name = nameField.text
                val description = descriptionField.text
                val selectedItem = idComboBox.selectedItem as? SelectItem
                if (selectedItem == null) {
                    showNotification("提示","关联需求/缺陷Id不能为空", NotificationType.ERROR)
                }else{
                    val result = myService.createWork(WorkTask(description, name, selectedItem!!.id))
                    if (result) {
                        showNotification("提示","添加任务成功!")
                        dialog.dispose()
                        bindSubTreeNode(tree, demandNode, bugNode, workNode)
                    } else {
                        showNotification("提示","创建工作项任务失败", NotificationType.ERROR)
                    }
                }

            }

            cancelButton.addActionListener {
                dialog.dispose()
            }
            dialog.setSize(600, 400) // 设置宽度为 600，高度可以根据需要调整
            dialog.setLocationRelativeTo(null) // 确保对话框在屏幕中央显示
            dialog.isVisible = true
        }

        private fun bindRightMenu(tree: JTree) {
            val menu = JPopupMenu()
            val menuItem = JMenuItem("复制Id")
            menuItem.addActionListener {
                selectedNode?.let {
                    val customNode = it as? CustomNode
                    println("当前选中的节点: ${customNode?.relatedId}")
                    parentNode?.let {
                        copyToClipboard(customNode!!.relatedId)
                        val parentName = it.userObject.toString()
                        val tip = parentName + "Id:[${customNode.relatedId}] 已复制到粘贴板"
                        showNotification(customNode.userObject.toString(), tip)
                    }

                }
            }
            menu.add(menuItem)

            tree.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e);
                    var x = e!!.x;
                    var y = e.y;
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        var location = tree.getPathForLocation(e!!.x, e.y)
                        selectedNode = location?.lastPathComponent as? DefaultMutableTreeNode
                        parentNode = selectedNode?.parent as? DefaultMutableTreeNode
                        if (selectedNode?.isLeaf == true) {
                            tree.setSelectionPath(location);
                            menu.show(tree, x, y);
                        }
                    }
                }
            })
        }

        private fun copyToClipboard(text: String) {
            val stringSelection = StringSelection(text)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(stringSelection, null)
        }

        fun showNotification(title: String, tip: String) {
            showNotification(title, tip, NotificationType.INFORMATION)
        }

        fun showNotification(title: String, tip: String, notifyType: NotificationType) {
            var titleName = title
            if (title.length > 19) {
                titleName = title.substring(0, 19) + ".."
            }
            val notificationGroup =
                NotificationGroupManager.getInstance().getNotificationGroup("Windy Notification Group")
            val notification: Notification = notificationGroup.createNotification(titleName, tip, notifyType)
            notification.notify(project)
        }

        private fun bindSubTreeNode(
            tree: JTree,
            demandNode: DefaultMutableTreeNode,
            bugNode: DefaultMutableTreeNode,
            workNode: DefaultMutableTreeNode
        ) {
            val myService = project.getService(MyProjectService::class.java)
            myService.asyncDemandData {
                demandNode.removeAllChildren()
                demandNode.userObject = "需求 (${myService.demandPage!!.total})"
                for (item in myService.demandPage!!.getList()) {
                    demandNode.add(CustomNode(item.demandName, item.demandId))
                    dataMap.put(item.demandId, item)
                    val model = tree.model as DefaultTreeModel
                    model.reload()
                }
            }
            myService.asyncBugData {
                bugNode.removeAllChildren()
                bugNode.userObject = "缺陷 (${myService.bugPage!!.total})"
                for (item in myService.bugPage!!.getList()) {
                    bugNode.add(CustomNode(item.bugName, item.bugId))
                    dataMap.put(item.bugId, item)
                    val model = tree.model as DefaultTreeModel
                    model.reload()
                }
            }
            myService.asyncWorkData {
                workNode.removeAllChildren()
                workNode.userObject = "任务 (${myService.workPage!!.total})"
                for (item in myService.workPage!!.getList()) {
                    workNode.add(CustomNode(item.taskName, item.taskId))
                    dataMap.put(item.taskId, item)
                    val model = tree.model as DefaultTreeModel
                    model.reload()
                }
            }
        }
    }


}
