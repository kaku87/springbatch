Sub DrawProjectClassDiagram()
    ' Spring Batch项目类图绘制宏
    ' 作者: AI Assistant
    ' 功能: 在Excel中自动绘制完整的项目类图
    
    Dim ws As Worksheet
    Dim shp As Shape
    Dim i As Integer, j As Integer
    Dim startX As Single, startY As Single
    Dim boxWidth As Single, boxHeight As Single
    Dim spacing As Single
    
    ' 设置工作表
    Set ws = ActiveSheet
    ws.Cells.Clear
    
    ' 设置绘图参数
    startX = 50
    startY = 50
    boxWidth = 200
    boxHeight = 150
    spacing = 50
    
    ' 清除现有图形
    For Each shp In ws.Shapes
        shp.Delete
    Next shp
    
    ' 定义类的位置和信息
    Dim classInfo As Variant
    classInfo = Array( _
        Array("SpringBatchDemoApplication", "应用启动类", "@SpringBootApplication" & vbLf & "@EnableBatchProcessing", "+ main(String[] args)", 1, 1), _
        Array("BatchConfig", "配置类", "@Configuration" & vbLf & "@EnableBatchProcessing", "- JobRepository jobRepository" & vbLf & "- PlatformTransactionManager transactionManager" & vbLf & "+ asyncBusinessJob(): Job" & vbLf & "+ asyncBusinessJobStep(): Step", 2, 1), _
        Array("AsyncConfig", "配置类", "@Configuration" & vbLf & "@EnableAsync", "+ taskExecutor(): Executor", 3, 1), _
        Array("BatchProperties", "属性配置类", "@ConfigurationProperties", "- int simulationDurationSeconds" & vbLf & "- Status status" & vbLf & "+ getSimulationDurationSeconds(): int" & vbLf & "+ getStatus(): Status", 4, 1), _
        Array("Status", "状态枚举类", "@Component", "- String processing" & vbLf & "- String completed" & vbLf & "- String failed" & vbLf & "+ getProcessing(): String" & vbLf & "+ getCompleted(): String" & vbLf & "+ getFailed(): String", 5, 1), _
        Array("Task", "实体类", "@Entity" & vbLf & "@Table", "- Long id" & vbLf & "- String name" & vbLf & "- String description" & vbLf & "- boolean processed" & vbLf & "- LocalDateTime createdAt" & vbLf & "- LocalDateTime processedAt" & vbLf & "+ getId(): Long" & vbLf & "+ getName(): String" & vbLf & "+ isProcessed(): boolean" & vbLf & "+ setProcessed(boolean)", 1, 2), _
        Array("BatchExecution", "实体类", "@Entity" & vbLf & "@Table", "- Long id" & vbLf & "- String batchId" & vbLf & "- Long jobId" & vbLf & "- String status" & vbLf & "- LocalDateTime startTime" & vbLf & "- LocalDateTime endTime" & vbLf & "+ getId(): Long" & vbLf & "+ getBatchId(): String" & vbLf & "+ setStatus(String)", 2, 2), _
        Array("TaskRepository", "数据访问接口", "@Repository" & vbLf & "<<interface>>", "+ findByProcessedFalse(): List<Task>", 3, 2), _
        Array("BatchExecutionRepository", "数据访问接口", "@Repository" & vbLf & "<<interface>>", "+ findByBatchId(String): Optional<BatchExecution>" & vbLf & "+ findByJobId(Long): Optional<BatchExecution>" & vbLf & "+ findByStatus(String): List<BatchExecution>", 4, 2), _
        Array("JobController", "控制器", "@RestController" & vbLf & "@RequestMapping", "- JobService jobService" & vbLf & "+ startJob(): ResponseEntity<String>" & vbLf & "+ stopJob(): ResponseEntity<String>" & vbLf & "+ getJobStatus(): ResponseEntity<String>", 5, 2), _
        Array("JobService", "服务类", "@Service", "- JobLauncher jobLauncher" & vbLf & "- Job asyncBusinessJob" & vbLf & "- JobExplorer jobExplorer" & vbLf & "- JobStopManager jobStopManager" & vbLf & "+ startAsyncBusinessJob(): String" & vbLf & "+ stopJob(): String" & vbLf & "+ getJobStatus(): String", 1, 3), _
        Array("JobStopManager", "停止管理器", "@Component", "- Map<Long, Thread> jobThreadMap" & vbLf & "- Map<Long, Boolean> stopFlags" & vbLf & "+ registerJobThread(Long, Thread)" & vbLf & "+ requestStop(Long)" & vbLf & "+ isStopRequested(Long): boolean" & vbLf & "+ clearStopFlag(Long)" & vbLf & "+ forceStopJob(Long)", 2, 3), _
        Array("AsyncBusinessJobTasklet", "任务执行器", "@Component", "- BatchExecutionRepository batchExecutionRepository" & vbLf & "- TaskRepository taskRepository" & vbLf & "- JobStopManager jobStopManager" & vbLf & "- BatchProperties batchProperties" & vbLf & "+ execute(): RepeatStatus" & vbLf & "+ executeBusinessLogicAsync(): CompletableFuture<Void>", 3, 3), _
        Array("JobCommandLineRunner", "命令行运行器", "@Component", "- JobService jobService" & vbLf & "+ run(String... args)", 4, 3), _
        Array("JpaRepository", "Spring接口", "<<interface>>", "Spring Data JPA基础接口", 5, 3), _
        Array("Tasklet", "Spring接口", "<<interface>>", "+ execute(): RepeatStatus", 1, 4), _
        Array("CommandLineRunner", "Spring接口", "<<interface>>", "+ run(String... args)", 2, 4) _
    )
    
    ' 绘制类框
    Dim classCount As Integer
    classCount = UBound(classInfo) + 1
    
    For i = 0 To classCount - 1
        Dim className As String, classType As String, annotations As String, methods As String
        Dim row As Integer, col As Integer
        
        className = classInfo(i)(0)
        classType = classInfo(i)(1)
        annotations = classInfo(i)(2)
        methods = classInfo(i)(3)
        col = classInfo(i)(4)
        row = classInfo(i)(5)
        
        ' 计算位置
        Dim x As Single, y As Single
        x = startX + (col - 1) * (boxWidth + spacing)
        y = startY + (row - 1) * (boxHeight + spacing)
        
        ' 创建类框
        Set shp = ws.Shapes.AddShape(msoShapeRectangle, x, y, boxWidth, boxHeight)
        
        ' 设置类框样式
        With shp
            .Fill.ForeColor.RGB = RGB(240, 248, 255) ' 淡蓝色背景
            .Line.ForeColor.RGB = RGB(0, 0, 0) ' 黑色边框
            .Line.Weight = 1.5
            
            ' 设置文本
            .TextFrame.Characters.Text = "《" & classType & "》" & vbLf & _
                                        className & vbLf & _
                                        "────────────────" & vbLf & _
                                        annotations & vbLf & _
                                        "────────────────" & vbLf & _
                                        methods
            
            ' 设置文本格式
            With .TextFrame.Characters.Font
                .Name = "Microsoft YaHei"
                .Size = 8
                .Bold = False
            End With
            
            ' 设置类名为粗体
            With .TextFrame.Characters(Len("《" & classType & "》" & vbLf) + 1, Len(className)).Font
                .Bold = True
                .Size = 10
                .ColorIndex = 1 ' 黑色
            End With
            
            .TextFrame.MarginLeft = 5
            .TextFrame.MarginRight = 5
            .TextFrame.MarginTop = 5
            .TextFrame.MarginBottom = 5
            .TextFrame.VerticalAlignment = xlVAlignTop
        End With
        
        ' 为接口类添加特殊样式
        If InStr(annotations, "<<interface>>") > 0 Then
            shp.Fill.ForeColor.RGB = RGB(255, 255, 224) ' 浅黄色背景
            shp.Line.DashStyle = msoLineDash ' 虚线边框
        End If
        
        ' 为实体类添加特殊样式
        If InStr(annotations, "@Entity") > 0 Then
            shp.Fill.ForeColor.RGB = RGB(240, 255, 240) ' 浅绿色背景
        End If
        
        ' 为配置类添加特殊样式
        If InStr(annotations, "@Configuration") > 0 Then
            shp.Fill.ForeColor.RGB = RGB(255, 240, 245) ' 浅粉色背景
        End If
    Next i
    
    ' 绘制关系线
    Call DrawRelationships(ws, startX, startY, boxWidth, boxHeight, spacing)
    
    ' 添加图例
    Call AddLegend(ws, startX + 6 * (boxWidth + spacing), startY)
    
    ' 添加标题
    Call AddTitle(ws, "Spring Batch项目类图")
    
    ' 调整工作表视图
    ws.Columns.AutoFit
    ws.Rows.AutoFit
    Application.ActiveWindow.Zoom = 75
    
    MsgBox "Spring Batch项目类图绘制完成！" & vbLf & _
           "图例说明：" & vbLf & _
           "• 淡蓝色：普通类" & vbLf & _
           "• 浅黄色：接口类" & vbLf & _
           "• 浅绿色：实体类" & vbLf & _
           "• 浅粉色：配置类" & vbLf & _
           "• 实线箭头：依赖关系" & vbLf & _
           "• 虚线箭头：实现关系", vbInformation, "类图绘制完成"
End Sub

Sub DrawRelationships(ws As Worksheet, startX As Single, startY As Single, boxWidth As Single, boxHeight As Single, spacing As Single)
    ' 绘制类之间的关系线
    Dim shp As Shape
    
    ' 定义关系数组 (起始类位置, 目标类位置, 关系类型)
    ' 关系类型: 1=依赖(实线箭头), 2=实现(虚线箭头), 3=组合(实线菱形)
    Dim relationships As Variant
    relationships = Array( _
        Array(1, 1, 4, 3, 1), _
        Array(2, 1, 3, 3, 1), _
        Array(4, 1, 5, 1, 3), _
        Array(3, 2, 5, 3, 2), _
        Array(4, 2, 5, 3, 2), _
        Array(5, 2, 1, 3, 1), _
        Array(1, 3, 2, 3, 1), _
        Array(3, 3, 1, 2, 1), _
        Array(3, 3, 3, 2, 1), _
        Array(3, 3, 2, 3, 1), _
        Array(3, 3, 4, 1, 1), _
        Array(4, 3, 1, 3, 1), _
        Array(4, 3, 2, 4, 2) _
    )
    
    ' 绘制关系线
    Dim i As Integer
    For i = 0 To UBound(relationships)
        Dim fromCol As Integer, fromRow As Integer, toCol As Integer, toRow As Integer, relType As Integer
        fromCol = relationships(i)(0)
        fromRow = relationships(i)(1)
        toCol = relationships(i)(2)
        toRow = relationships(i)(3)
        relType = relationships(i)(4)
        
        ' 计算起点和终点坐标
        Dim fromX As Single, fromY As Single, toX As Single, toY As Single
        fromX = startX + (fromCol - 1) * (boxWidth + spacing) + boxWidth / 2
        fromY = startY + (fromRow - 1) * (boxHeight + spacing) + boxHeight / 2
        toX = startX + (toCol - 1) * (boxWidth + spacing) + boxWidth / 2
        toY = startY + (toRow - 1) * (boxHeight + spacing) + boxHeight / 2
        
        ' 创建连接线
        Set shp = ws.Shapes.AddConnector(msoConnectorStraight, fromX, fromY, toX, toY)
        
        ' 设置线条样式
        With shp.Line
            .ForeColor.RGB = RGB(0, 0, 0)
            .Weight = 1.5
            
            Select Case relType
                Case 1 ' 依赖关系 - 实线箭头
                    .DashStyle = msoLineSolid
                    .EndArrowheadStyle = msoArrowheadTriangle
                Case 2 ' 实现关系 - 虚线箭头
                    .DashStyle = msoLineDash
                    .EndArrowheadStyle = msoArrowheadTriangle
                Case 3 ' 组合关系 - 实线菱形
                    .DashStyle = msoLineSolid
                    .BeginArrowheadStyle = msoArrowheadDiamond
            End Select
        End With
    Next i
End Sub

Sub AddLegend(ws As Worksheet, x As Single, y As Single)
    ' 添加图例
    Dim shp As Shape
    Dim legendHeight As Single
    legendHeight = 200
    
    ' 创建图例框
    Set shp = ws.Shapes.AddShape(msoShapeRectangle, x, y, 180, legendHeight)
    With shp
        .Fill.ForeColor.RGB = RGB(248, 248, 248)
        .Line.ForeColor.RGB = RGB(0, 0, 0)
        .Line.Weight = 1
        
        .TextFrame.Characters.Text = "图例说明" & vbLf & _
                                    "────────────" & vbLf & _
                                    "■ 普通类" & vbLf & _
                                    "■ 接口类" & vbLf & _
                                    "■ 实体类" & vbLf & _
                                    "■ 配置类" & vbLf & _
                                    "────────────" & vbLf & _
                                    "→ 依赖关系" & vbLf & _
                                    "⇢ 实现关系" & vbLf & _
                                    "◆ 组合关系"
        
        With .TextFrame.Characters.Font
            .Name = "Microsoft YaHei"
            .Size = 9
        End With
        
        .TextFrame.MarginLeft = 10
        .TextFrame.MarginTop = 10
        .TextFrame.VerticalAlignment = xlVAlignTop
    End With
End Sub

Sub AddTitle(ws As Worksheet, title As String)
    ' 添加标题
    Dim shp As Shape
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, 50, 10, 600, 30)
    
    With shp
        .TextFrame.Characters.Text = title
        With .TextFrame.Characters.Font
            .Name = "Microsoft YaHei"
            .Size = 16
            .Bold = True
            .ColorIndex = 1
        End With
        .TextFrame.HorizontalAlignment = xlHAlignCenter
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
End Sub

' 辅助宏：清除所有图形
Sub ClearAllShapes()
    Dim ws As Worksheet
    Dim shp As Shape
    
    Set ws = ActiveSheet
    
    For Each shp In ws.Shapes
        shp.Delete
    Next shp
    
    ws.Cells.Clear
    MsgBox "所有图形已清除！", vbInformation
End Sub

' 辅助宏：调整视图
Sub AdjustView()
    With Application.ActiveWindow
        .Zoom = 60
        .ScrollRow = 1
        .ScrollColumn = 1
    End With
End Sub