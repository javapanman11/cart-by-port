# Cart By Port

Java / Spring Boot を用いて開発している、ECサイトを想定したバックエンドAPIです。

商品管理APIを起点として、REST API、PostgreSQL、JPA / Hibernate、Validation、例外処理、自動テストなど、バックエンド開発で必要となる要素を段階的に実装しています。

今後はカート・注文機能、Docker、AWS、CI/CD、Reactフロントエンドなどを追加し、実際のWebサービスに近い構成へ発展させる予定です。

> 現在開発中のポートフォリオです。  
> 実装済みの機能と今後の予定を分けて記載しています。

---

## 開発目的

単純なCRUD APIを作るだけではなく、以下の技術を実際に組み合わせながら理解することを目的としています。

- Java / Spring Bootによるバックエンド開発
- REST API設計
- PostgreSQLを用いたデータ永続化
- Spring Data JPA / Hibernate
- DTOによる入出力設計
- Bean Validation
- 例外ハンドリング
- トランザクション
- Dirty Checking
- JUnit / Mockito / MockMvcによる自動テスト
- 結合テスト
- Docker
- AWS
- CI/CD

最終的には、バックエンド・データベース・クラウド・監視・CI/CDまで含めた構成を目指しています。

---

## 技術スタック

### Backend

- Java 21
- Spring Boot 4.1
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation

### Database

- PostgreSQL

### Test

- JUnit
- Mockito
- MockMvc
- Spring Boot Test

### Build Tool

- Maven
- Maven Wrapper

### 今後追加予定

- Docker
- AWS
  - Amazon ECR
  - Amazon ECS / Fargate
  - Amazon RDS for PostgreSQL
  - Amazon S3
  - Amazon CloudWatch
- GitHub Actions
- React
- TypeScript

---

# 現在のアーキテクチャ

```text
HTTP Request
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
Spring Data JPA
     |
     v
Hibernate
     |
     v
PostgreSQL
```

Controller・Service・Repositoryで責務を分離しています。

APIのRequest / ResponseにはDTOを使用し、Entityをそのまま外部へ公開しない構成にしています。

---

# 実装済み機能

## 商品管理API

現在、商品に対する基本的なCRUD APIを実装しています。

| Method | Endpoint | 内容 |
|---|---|---|
| POST | `/products` | 商品登録 |
| GET | `/products` | 商品一覧取得 |
| GET | `/products/{id}` | 商品詳細取得 |
| PUT | `/products/{id}` | 商品更新 |
| DELETE | `/products/{id}` | 商品削除 |

商品データとして現在扱っている主な項目は以下です。

```text
id
name
price
stock
```

価格には `BigDecimal` を使用しています。

---

# 商品登録例

## Request

```http
POST /products
Content-Type: application/json
```

```json
{
  "name": "キーボード",
  "price": 5000,
  "stock": 10
}
```

## Response

```json
{
  "id": 1,
  "name": "キーボード",
  "price": 5000,
  "stock": 10,
  "message": "商品を登録しました"
}
```

HTTP Status:

```text
201 Created
```

---

# Validation

商品登録・更新時にはBean Validationを使用しています。

現在、主に以下の入力値をチェックしています。

### 商品名

空文字や未入力を許可しません。

```java
@NotBlank
```

### 価格

未入力および負数を許可しません。

```java
@NotNull
@PositiveOrZero
```

### 在庫

未入力および負数を許可しません。

```java
@NotNull
@PositiveOrZero
```

不正な値が送信された場合は `400 Bad Request` を返します。

例:

```json
{
  "message": "入力内容に誤りがあります",
  "errors": {
    "name": "商品名は必須です"
  }
}
```

---

# 例外ハンドリング

`@RestControllerAdvice` を使用して、API全体の例外処理を共通化しています。

存在しない商品IDが指定された場合は、独自例外である

```text
ProductNotFoundException
```

を発生させ、

```text
404 Not Found
```

としてレスポンスを返します。

例:

```json
{
  "message": "商品が見つかりません。id =1"
}
```

Validationエラーについても、共通の例外ハンドラでレスポンス形式を整えています。

---

# DTO

APIとデータベースの責務を分離するため、Request / Response DTOを使用しています。

```text
ProductRequest
ProductResponse
```

EntityをそのままAPIレスポンスとして返すのではなく、

```text
HTTP Request
      |
      v
ProductRequest
      |
      v
Service
      |
      v
Product Entity
      |
      v
PostgreSQL
```

という形で処理しています。

レスポンスも、

```text
Product Entity
      |
      v
ProductResponse
      |
      v
HTTP Response
```

へ変換しています。

---

# JPA / Hibernate

データアクセスにはSpring Data JPAを使用しています。

```java
public interface ProductRepository
        extends JpaRepository<Product, Long> {
}
```

`JpaRepository` が提供する、

```text
save()
findById()
findAll()
delete()
existsById()
```

などを利用しています。

Spring Data JPAの仕組みにより、Repositoryの具体的な実装を自分で作成せずにデータアクセスを行っています。

---

# Transaction / Dirty Checking

商品更新処理では `@Transactional` を使用しています。

```java
@Transactional
public ProductResponse updateProduct(...) {
    ...
}
```

JPAのManaged Entityに対して値を変更することで、明示的に `save()` を呼び出さなくてもHibernateのDirty CheckingによってUPDATE SQLが発行される構成にしています。

結合テストでは、実際にPostgreSQLに対してUPDATE SQLが発行され、更新内容が保存されていることも確認しています。

---

# 自動テスト

JUnit / Mockito / MockMvc / Spring Boot Testを使用して、自動テストを実装しています。

テストを以下の役割に分けています。

## Service単体テスト

```text
ProductService
      |
      v
Mock ProductRepository
```

RepositoryをMockitoでMock化し、Serviceのロジック単体を確認しています。

主に以下をテストしています。

- 商品登録
- 商品取得
- 存在しない商品の取得
- 商品更新
- 商品削除

---

## Controllerテスト

```text
MockMvc
   |
   v
ProductController
   |
   v
Mock ProductService
```

ProductServiceをMock化し、HTTPレイヤーの動作を確認しています。

主に、

- HTTP Status
- JSON Response
- Validation

を確認しています。

---

## PostgreSQL結合テスト

```text
ProductService
      |
      v
ProductRepository
      |
      v
JPA / Hibernate
      |
      v
PostgreSQL
```

Mockを使用せず、本物のPostgreSQLへ接続して確認しています。

商品更新テストでは、

```text
INSERT
  |
  v
UPDATE
  |
  v
SELECT
```

を実際に実行し、Dirty Checkingによる更新がDBへ反映されることを確認しています。

商品削除テストでは、

```text
INSERT
  |
  v
DELETE
  |
  v
SELECT / existsById
```

によって、本当にDBから削除されていることを確認しています。

---

## API結合テスト

さらに、

```text
MockMvc
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
Hibernate
   |
   v
PostgreSQL
```

までを一気通貫で確認するテストも実装しています。

商品登録APIにHTTPリクエストを送り、

```text
POST /products
```

から、

```text
Controller
Service
Repository
PostgreSQL
```

まで実際に処理させたうえで、

PostgreSQLからデータを再取得し、本当に商品が保存されていることを確認しています。

---

# テスト実行

以下のコマンドでテストを実行できます。

```bash
./mvnw test
```

---

# ローカル実行環境

現在はローカルのPostgreSQLを使用しています。

## Database

```text
Database: cartbyport_db
User: cartbyport_user
```

DBパスワードはGitHubへ保存せず、環境変数から取得するようにしています。

`application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cartbyport_db
spring.datasource.username=cartbyport_user
spring.datasource.password=${CARTBYPORT_DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

実行前に環境変数を設定します。

macOS:

```bash
export CARTBYPORT_DB_PASSWORD='YOUR_PASSWORD'
```

その後、Spring Bootを起動します。

```bash
./mvnw spring-boot:run
```

起動後は、

```text
http://localhost:8080
```

でAPIへアクセスできます。

---

# curl実行例

商品登録:

```bash
curl -i -X POST http://localhost:8080/products \
-H "Content-Type: application/json" \
-d '{
  "name":"キーボード",
  "price":5000,
  "stock":10
}'
```

商品一覧取得:

```bash
curl -i http://localhost:8080/products
```

商品詳細取得:

```bash
curl -i http://localhost:8080/products/1
```

商品更新:

```bash
curl -i -X PUT http://localhost:8080/products/1 \
-H "Content-Type: application/json" \
-d '{
  "name":"メカニカルキーボード",
  "price":7000,
  "stock":8
}'
```

商品削除:

```bash
curl -i -X DELETE http://localhost:8080/products/1
```

---

# セキュリティ

データベースパスワードなどの秘密情報はGitリポジトリに含めず、環境変数から取得するようにしています。

例:

```properties
spring.datasource.password=${CARTBYPORT_DB_PASSWORD}
```

今後AWSへデプロイする際も、認証情報やシークレットをソースコードへ直接記述しない構成にする予定です。

---

# 今後の開発予定

現在の商品CRUDを土台として、ECサイトとして必要な機能を段階的に追加していきます。

## EC機能

- Cart（カート）
- CartItem
- Order（注文）
- OrderItem
- ユーザーと注文の関連付け
- 商品在庫管理
- Entity間リレーション
- 検索
- ページング

## API設計

- REST APIの改善
- Response設計の改善
- エラーレスポンスの共通化
- 検索条件追加
- Pagination
- API仕様のドキュメント化

## Test

- API結合テストの拡充
- 異常系テストの追加
- Repositoryテスト
- Testcontainersの導入検討
- PostgreSQLテスト環境の分離

## Docker

Spring BootとPostgreSQLをDocker化する予定です。

```text
Spring Boot Container
        |
        v
PostgreSQL Container
```

Docker Composeによるローカル開発環境も構築予定です。

## AWS

Docker対応後、AWSへのデプロイを予定しています。

想定構成:

```text
Client
  |
  v
Spring Boot API
  |
  v
Amazon ECS / Fargate
  |
  +------ Amazon RDS for PostgreSQL
  |
  +------ Amazon S3
  |
  +------ Amazon CloudWatch

Docker Image
  |
  v
Amazon ECR
```

使用予定サービス:

- Amazon ECR
- Amazon ECS / Fargate
- Amazon RDS for PostgreSQL
- Amazon S3
- Amazon CloudWatch

AWSについては、資格学習だけでなく、このアプリケーションを実際にデプロイしながら理解を深める予定です。

## CI/CD

GitHub Actionsを利用し、

```text
GitHub Push
     |
     v
Automated Test
     |
     v
Build
     |
     v
Docker Build
     |
     v
Amazon ECR
     |
     v
Amazon ECS / Fargate
```

というCI/CDパイプラインを構築する予定です。

## Frontend

バックエンドAPI完成後は、

- React
- TypeScript

を使用してECサイトのフロントエンドを実装予定です。

## Data / Recommendation

将来的には購入履歴を利用し、

```text
「この商品を購入した人は、
 こんな商品も購入しています」
```

のようなレコメンド機能を追加する予定です。

初期段階ではSQLによる商品の共起分析から実装し、その後データ分析基盤や機械学習への発展も検討しています。

---

# 最終的に目指す構成

```text
React / TypeScript
        |
        v
Spring Boot REST API
        |
        v
Amazon ECS / Fargate
        |
        +------ Amazon RDS PostgreSQL
        |
        +------ Amazon S3
        |
        +------ Amazon CloudWatch

Docker Image
        |
        v
Amazon ECR

GitHub
   |
   v
GitHub Actions
   |
   v
Test / Build / Deploy
```

商品・カート・注文といったECドメインの実装だけではなく、

- Backend
- Database
- Test
- Docker
- Cloud
- Monitoring
- CI/CD

まで一連の開発を経験できるポートフォリオへ発展させていく予定です。

---

# 現在の開発ステータス

現在は、

```text
商品CRUD API
     ↓
PostgreSQL永続化
     ↓
Validation
     ↓
例外ハンドリング
     ↓
Service単体テスト
     ↓
Controllerテスト
     ↓
PostgreSQL結合テスト
     ↓
API → PostgreSQL 結合テスト
```

まで実装しています。

次のフェーズでは、商品APIのテストを拡充した後、Cart / OrderなどECサイトの主要ドメイン実装へ進む予定です。
