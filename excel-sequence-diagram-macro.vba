' Spring Batch Job停止フロー序列図描画マクロ
' Excel VBAを使用してシーケンス図を自動描画

Sub DrawJobStopSequenceDiagram()
    Dim ws As Worksheet
    Dim shp As Shape
    Dim i As Integer
    Dim participantY As Integer
    Dim messageY As Integer
    Dim participantSpacing As Integer
    Dim messageSpacing As Integer
    
    ' 新しいワークシートを作成
    Set ws = ActiveWorkbook.Worksheets.Add
    ws.Name = "Job停止シーケンス図"
    
    ' 定数設定
    participantSpacing = 100
    messageSpacing = 30
    participantY = 50
    messageY = 100
    
    ' 参加者（アクター）の配置
    Dim participants As Variant
    participants = Array("Webクライアント", "JobController", "JobService", "JobExplorer", "JobOperator", "JobStopManager", "AsyncBusinessJobTasklet", "Job実行スレッド")
    
    ' 参加者ボックスを描画
    For i = 0 To UBound(participants)
        Set shp = ws.Shapes.AddShape(msoShapeRectangle, 50 + i * participantSpacing, participantY, 80, 30)
        With shp
            .TextFrame.Characters.Text = participants(i)
            .TextFrame.Characters.Font.Size = 8
            .TextFrame.Characters.Font.Bold = True
            .Fill.ForeColor.RGB = RGB(173, 216, 230) ' ライトブルー
            .Line.Weight = 1
            .TextFrame.HorizontalAlignment = xlHAlignCenter
            .TextFrame.VerticalAlignment = xlVAlignCenter
        End With
        
        ' 生存線を描画
        Set shp = ws.Shapes.AddLine(90 + i * participantSpacing, participantY + 30, 90 + i * participantSpacing, 800)
        With shp
            .Line.DashStyle = msoLineDash
            .Line.Weight = 1
            .Line.ForeColor.RGB = RGB(128, 128, 128)
        End With
    Next i
    
    ' タイトルを追加
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, 200, 10, 400, 30)
    With shp
        .TextFrame.Characters.Text = "Spring Batch Job停止フロー"
        .TextFrame.Characters.Font.Size = 14
        .TextFrame.Characters.Font.Bold = True
        .TextFrame.HorizontalAlignment = xlHAlignCenter
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
    
    ' メッセージ矢印とテキストを描画
    Call DrawMessage(ws, 0, 1, messageY, "POST /api/jobs/{executionId}/stop", "停止リクエスト送信")
    messageY = messageY + messageSpacing
    
    Call DrawMessage(ws, 1, 2, messageY, "stopJob(executionId)", "停止リクエスト転送")
    messageY = messageY + messageSpacing
    
    Call DrawMessage(ws, 2, 3, messageY, "getJobExecution(executionId)", "Job実行状態照会")
    messageY = messageY + messageSpacing
    
    Call DrawReturnMessage(ws, 3, 2, messageY, "JobExecutionオブジェクト", "実行情報返却")
    messageY = messageY + messageSpacing
    
    ' Alt条件分岐ボックス
    Call DrawAltBox(ws, messageY, "Job実行中")
    messageY = messageY + 40
    
    Call DrawMessage(ws, 2, 4, messageY, "stop(executionId)", "Spring Batch標準停止")
    messageY = messageY + messageSpacing
    
    Call DrawMessage(ws, 2, 5, messageY, "setStopFlag(executionId)", "カスタム停止フラグ設定")
    messageY = messageY + messageSpacing
    
    Call DrawMessage(ws, 5, 7, messageY, "interrupt()", "Job実行スレッド中断")
    messageY = messageY + messageSpacing
    
    Call DrawReturnMessage(ws, 7, 6, messageY, "InterruptedException", "スレッド中断シグナル")
    messageY = messageY + messageSpacing
    
    ' ループボックス
    Call DrawLoopBox(ws, messageY, "ビジネス処理ループ")
    messageY = messageY + 40
    
    Call DrawMessage(ws, 6, 5, messageY, "shouldStop(executionId)", "停止フラグチェック")
    messageY = messageY + messageSpacing
    
    ' 内部Alt条件分岐
    Call DrawAltBox(ws, messageY, "停止フラグがtrue")
    messageY = messageY + 40
    
    Call DrawReturnMessage(ws, 5, 6, messageY, "true", "停止シグナル返却")
    messageY = messageY + messageSpacing
    
    Call DrawSelfMessage(ws, 6, messageY, "処理ループ終了", "優雅な停止処理")
    messageY = messageY + messageSpacing
    
    ' Else分岐
    Call DrawAltBox(ws, messageY, "処理継続")
    messageY = messageY + 40
    
    Call DrawReturnMessage(ws, 5, 6, messageY, "false", "実行継続")
    messageY = messageY + messageSpacing
    
    Call DrawSelfMessage(ws, 6, messageY, "ビジネスロジック実行", "ビジネスデータ処理")
    messageY = messageY + messageSpacing
    
    ' 後処理
    messageY = messageY + 40
    Call DrawMessage(ws, 6, 5, messageY, "clearStopFlag(executionId)", "停止フラグとスレッド参照クリア")
    messageY = messageY + messageSpacing
    
    Call DrawReturnMessage(ws, 2, 1, messageY, "true", "停止成功")
    messageY = messageY + messageSpacing
    
    Call DrawReturnMessage(ws, 1, 0, messageY, "{\"success\": true, \"message\": \"Job停止成功\"}", "成功レスポンス返却")
    messageY = messageY + messageSpacing
    
    ' Else分岐（Job未実行）
    Call DrawAltBox(ws, messageY, "Job未実行")
    messageY = messageY + 40
    
    Call DrawReturnMessage(ws, 2, 1, messageY, "false", "停止失敗")
    messageY = messageY + messageSpacing
    
    Call DrawReturnMessage(ws, 1, 0, messageY, "{\"success\": false, \"message\": \"Job未実行\"}", "失敗レスポンス返却")
    messageY = messageY + messageSpacing
    
    ' 完了メッセージ
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, 200, messageY + 20, 400, 30)
    With shp
        .TextFrame.Characters.Text = "停止フロー完了"
        .TextFrame.Characters.Font.Size = 12
        .TextFrame.Characters.Font.Bold = True
        .TextFrame.HorizontalAlignment = xlHAlignCenter
        .Fill.ForeColor.RGB = RGB(255, 255, 224) ' ライトイエロー
        .Line.Weight = 1
    End With
    
    ' ワークシートの表示を調整
    ws.Activate
    ActiveWindow.Zoom = 75
    
    MsgBox "Spring Batch Job停止フローの序列図が作成されました！", vbInformation
End Sub

' メッセージ矢印を描画するサブルーチン
Sub DrawMessage(ws As Worksheet, fromIndex As Integer, toIndex As Integer, y As Integer, message As String, note As String)
    Dim shp As Shape
    Dim fromX As Integer
    Dim toX As Integer
    
    fromX = 90 + fromIndex * 100
    toX = 90 + toIndex * 100
    
    ' 矢印を描画
    Set shp = ws.Shapes.AddLine(fromX, y, toX, y)
    With shp
        .Line.Weight = 2
        .Line.ForeColor.RGB = RGB(0, 0, 255) ' 青色
        .Line.EndArrowheadStyle = msoArrowheadTriangle
    End With
    
    ' メッセージテキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, (fromX + toX) / 2 - 50, y - 15, 100, 12)
    With shp
        .TextFrame.Characters.Text = message
        .TextFrame.Characters.Font.Size = 8
        .TextFrame.HorizontalAlignment = xlHAlignCenter
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
    
    ' 注釈テキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, toX + 10, y - 5, 80, 10)
    With shp
        .TextFrame.Characters.Text = note
        .TextFrame.Characters.Font.Size = 7
        .TextFrame.Characters.Font.Color = RGB(128, 128, 128)
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
End Sub

' 戻りメッセージ矢印を描画するサブルーチン
Sub DrawReturnMessage(ws As Worksheet, fromIndex As Integer, toIndex As Integer, y As Integer, message As String, note As String)
    Dim shp As Shape
    Dim fromX As Integer
    Dim toX As Integer
    
    fromX = 90 + fromIndex * 100
    toX = 90 + toIndex * 100
    
    ' 破線矢印を描画
    Set shp = ws.Shapes.AddLine(fromX, y, toX, y)
    With shp
        .Line.Weight = 2
        .Line.ForeColor.RGB = RGB(0, 128, 0) ' 緑色
        .Line.DashStyle = msoLineDash
        .Line.EndArrowheadStyle = msoArrowheadTriangle
    End With
    
    ' メッセージテキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, (fromX + toX) / 2 - 50, y - 15, 100, 12)
    With shp
        .TextFrame.Characters.Text = message
        .TextFrame.Characters.Font.Size = 8
        .TextFrame.HorizontalAlignment = xlHAlignCenter
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
    
    ' 注釈テキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, toX - 90, y - 5, 80, 10)
    With shp
        .TextFrame.Characters.Text = note
        .TextFrame.Characters.Font.Size = 7
        .TextFrame.Characters.Font.Color = RGB(128, 128, 128)
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
End Sub

' 自己メッセージを描画するサブルーチン
Sub DrawSelfMessage(ws As Worksheet, index As Integer, y As Integer, message As String, note As String)
    Dim shp As Shape
    Dim x As Integer
    
    x = 90 + index * 100
    
    ' 自己矢印を描画（四角形の矢印）
    Set shp = ws.Shapes.AddLine(x, y, x + 20, y)
    With shp
        .Line.Weight = 2
        .Line.ForeColor.RGB = RGB(255, 0, 0) ' 赤色
    End With
    
    Set shp = ws.Shapes.AddLine(x + 20, y, x + 20, y + 10)
    With shp
        .Line.Weight = 2
        .Line.ForeColor.RGB = RGB(255, 0, 0)
    End With
    
    Set shp = ws.Shapes.AddLine(x + 20, y + 10, x, y + 10)
    With shp
        .Line.Weight = 2
        .Line.ForeColor.RGB = RGB(255, 0, 0)
        .Line.EndArrowheadStyle = msoArrowheadTriangle
    End With
    
    ' メッセージテキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, x + 25, y, 100, 12)
    With shp
        .TextFrame.Characters.Text = message
        .TextFrame.Characters.Font.Size = 8
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
    
    ' 注釈テキスト
    Set shp = ws.Shapes.AddTextbox(msoTextOrientationHorizontal, x + 25, y + 12, 100, 10)
    With shp
        .TextFrame.Characters.Text = note
        .TextFrame.Characters.Font.Size = 7
        .TextFrame.Characters.Font.Color = RGB(128, 128, 128)
        .Fill.Visible = msoFalse
        .Line.Visible = msoFalse
    End With
End Sub

' Alt条件分岐ボックスを描画するサブルーチン
Sub DrawAltBox(ws As Worksheet, y As Integer, condition As String)
    Dim shp As Shape
    
    ' Alt条件ボックス
    Set shp = ws.Shapes.AddShape(msoShapeRectangle, 50, y, 700, 25)
    With shp
        .TextFrame.Characters.Text = "alt [" & condition & "]"
        .TextFrame.Characters.Font.Size = 9
        .TextFrame.Characters.Font.Bold = True
        .Fill.ForeColor.RGB = RGB(255, 255, 224) ' ライトイエロー
        .Line.Weight = 1
        .Line.ForeColor.RGB = RGB(128, 128, 128)
        .TextFrame.HorizontalAlignment = xlHAlignLeft
        .TextFrame.VerticalAlignment = xlVAlignCenter
    End With
End Sub

' ループボックスを描画するサブルーチン
Sub DrawLoopBox(ws As Worksheet, y As Integer, condition As String)
    Dim shp As Shape
    
    ' ループ条件ボックス
    Set shp = ws.Shapes.AddShape(msoShapeRectangle, 50, y, 700, 25)
    With shp
        .TextFrame.Characters.Text = "loop [" & condition & "]"
        .TextFrame.Characters.Font.Size = 9
        .TextFrame.Characters.Font.Bold = True
        .Fill.ForeColor.RGB = RGB(224, 255, 224) ' ライトグリーン
        .Line.Weight = 1
        .Line.ForeColor.RGB = RGB(128, 128, 128)
        .TextFrame.HorizontalAlignment = xlHAlignLeft
        .TextFrame.VerticalAlignment = xlVAlignCenter
    End With
End Sub