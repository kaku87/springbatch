# Spring Batch サンプルプロジェクト

これは完全なSpring Batchサンプルプロジェクトで、Spring Batchを使用してバッチ処理ジョブを実行する方法を示しており、Web界面管理、非同期実行、コマンドライン実行機能を含んでいます。

## 機能特性

- ✅ **非同期Job実行**: Web界面を通じて非同期でバッチ処理ジョブを開始
- ✅ **コマンドライン実行**: コマンドラインパラメータによるJob開始をサポート
- ✅ **Job管理界面**: 直感的なWeb界面でJobを管理
- ✅ **リアルタイム監視**: Job実行状態と履歴記録を確認
- ✅ **手動停止**: 実行中のJobを手動で停止可能
- ✅ **データ統計**: 処理進捗と統計情報を表示
- ✅ **テストデータ**: ワンクリックでテストデータを初期化

## 技術スタック

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Batch**
- **Spring Data JPA**
- **H2 Database** (インメモリデータベース)
- **Thymeleaf** (テンプレートエンジン)
- **Bootstrap 5** (フロントエンドUIフレームワーク)
- **Maven** (ビルドツール)

## プロジェクト構造

```
src/
├── main/
│   ├── java/com/example/springbatch/
│   │   ├── SpringBatchDemoApplication.java     # メインアプリケーションクラス
│   │   ├── config/
│   │   │   ├── AsyncConfig.java                # 非同期設定
│   │   │   └── BatchConfig.java                # Batch設定
│   │   ├── controller/
│   │   │   └── JobController.java              # Webコントローラー
│   │   ├── service/
│   │   │   └── JobService.java                 # Jobサービスクラス
│   │   ├── model/
│   │   │   └── Person.java                     # データモデル
│   │   ├── repository/
│   │   │   └── PersonRepository.java           # データアクセス層
│   │   └── command/
│   │       └── JobCommandLineRunner.java      # コマンドライン実行器
│   └── resources/
│       ├── application.yml                     # アプリケーション設定
│       └── templates/
│           └── index.html                      # 管理界面
└── pom.xml                                     # Maven設定
```

## クイックスタート

### 1. プロジェクトのコンパイル

```bash
mvn clean compile
```

### 2. アプリケーションの実行

```bash
mvn spring-boot:run
```

### 3. 管理界面へのアクセス

ブラウザで次のURLにアクセス: http://localhost:8080

### 4. テストデータの初期化

管理界面で「テストデータ初期化」ボタンをクリックすると、20件のテストレコードが作成されます。

### 5. Jobの開始

- **Web界面**: 「Job開始」ボタンをクリック
- **コマンドライン**: `java -jar target/spring-batch-demo-1.0.0.jar --job.run=true`

## 使用説明

### Web界面機能

1. **統計パネル**: 総レコード数、処理済み数、未処理数、実行中のJob数を表示
2. **操作パネル**: 
   - Job開始: 非同期でバッチ処理ジョブを開始
   - テストデータ初期化: 20件のテストレコードを作成
   - ページ更新: ページデータを更新
3. **実行中のJob**: 現在実行中のJobを表示、手動で停止可能
4. **Job実行履歴**: 全てのJobの実行記録と状態を表示

### コマンドライン実行

```bash
# コンパイルとパッケージング
mvn clean package

# コマンドラインでJobを開始
java -jar target/spring-batch-demo-1.0.0.jar --job.run=true

# 通常のWebアプリケーション起動（Jobは実行しない）
java -jar target/spring-batch-demo-1.0.0.jar
```

### Job処理ロジック

現在のサンプルJobの処理ロジック：
1. データベースから未処理のTaskレコードを読み取り
2. タスク名を大文字に変換し、ステータスを更新
3. 処理済みとしてマーク
4. データベースに保存
5. 各レコードの処理時間は約1秒（時間のかかる操作をシミュレート）

### APIインターフェース

- `POST /api/jobs/start` - Jobを開始
- `POST /api/jobs/{executionId}/stop` - Jobを停止
- `GET /api/jobs/{executionId}/status` - Job状態を取得
- `POST /api/data/init` - テストデータを初期化

### データベースアクセス

プロジェクトはH2インメモリデータベースを使用し、以下の方法でアクセスできます：

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:batchdb`
- ユーザー名: `sa`
- パスワード: (空)

## 拡張機能

### カスタムJob

1. `BatchConfig`クラスで新しいJobとStepを定義
2. カスタムのItemReader、ItemProcessor、ItemWriterを実装
3. `JobService`に対応する開始メソッドを追加

### 新しいデータソースの追加

1. `application.yml`のデータソース設定を修正
2. 対応するデータベースドライバの依存関係を追加
3. JPA設定を更新

### 外部システム統合

- ファイル処理: FlatFileItemReader/Writerを使用
- メッセージキュー: RabbitMQまたはKafkaを統合
- リモートサービス: RestTemplateまたはWebClientを使用

## トラブルシューティング

### よくある問題

1. **Jobが開始できない**
   - 他のJobが実行中でないか確認
   - ログのエラー情報を確認
   - データベース接続が正常か確認

2. **ページにアクセスできない**
   - ポート8080が使用されていないか確認
   - ファイアウォール設定を確認
   - アプリケーション起動ログを確認

3. **データベース接続失敗**
   - H2データベース設定を確認
   - JPA設定が正しいか確認
   - データベース初期化ログを確認

### ログ確認

アプリケーションログレベルはDEBUGに設定されており、詳細な実行情報を確認できます：

```bash
# リアルタイムログを確認
tail -f logs/spring-batch-demo.log

# またはコンソールで確認
mvn spring-boot:run
```

## ライセンス

本プロジェクトはMITライセンスを採用しています。詳細はLICENSEファイルをご覧ください。