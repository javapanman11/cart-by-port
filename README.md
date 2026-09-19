# Cart By Port

Java / Spring Bootを用いて開発している、ECサイトを想定したバックエンドアプリケーションです。

商品管理APIを起点として、REST API、PostgreSQL、Spring Data JPA / Hibernate、DTO、Validation、例外ハンドリング、トランザクション、自動テストなど、バックエンド開発に必要な要素を段階的に実装しています。

現在は商品管理機能について、CRUD APIの実装だけでなく、JUnit / Mockitoによる単体テスト、MockMvcによるControllerテスト、PostgreSQLを使用した結合テスト、さらにHTTPリクエストからDBまでを通すAPI結合テストまで実装しています。

今後はCart / OrderなどECサイト固有のドメインを追加し、Docker、AWS、CI/CD、React / TypeScriptへ発展させる予定です。

> 現在開発中のポートフォリオです。  
> 実装済みの機能と今後実装予定の機能を分けて記載しています。

---

# 開発目的

単純にCRUD APIを作るだけではなく、

- なぜController / Service / Repositoryを分離するのか
- なぜEntityをそのままAPIへ公開しないのか
- トランザクションはどこで必要になるのか
- JPAのDirty Checkingはどのように動作するのか
- 単体テストと結合テストでは何を確認するのか
- HTTPからデータベースまでをどのようにテストするのか

といったバックエンド開発の仕組みを理解しながら実装することを目的としています。

最終的には、

```text
Frontend
   ↓
Backend API
   ↓
Database
   ↓
Docker
   ↓
Cloud
   ↓
Monitoring
   ↓
CI/CD
```

までを一つのサービスとして構築することを目標としています。

---

# 技術スタック

## Backend

- Java 21
- Spring Boot 4.1
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation

## Database

- PostgreSQL

## Test

- JUnit
- Mockito
- MockMvc
- Spring Boot Test

## JSON

- Jackson 3

## Build Tool

- Maven
- Maven Wrapper

## Version Control

- Git
- GitHub

## 今後追加予定

- Docker
- Docker Compose
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

商品に対する基本的なCRUD APIを実装しています。

| Method | Endpoint | 内容 |
|---|---|---|
| POST | `/products` | 商品登録 |
| GET | `/products` | 商品一覧取得 |
| GET | `/products/{id}` | 商品詳細取得 |
| PUT | `/products/{id}` | 商品更新 |
| DELETE | `/products/{id}` | 商品削除 |

現在の商品Entityでは、主に以下の情報を扱っています。

```text
id
name
price
stock
```

価格には `BigDecimal` を使用しています。

---

# 商品登録API

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

# 商品取得API

```http
GET /products/{id}
```

正常に取得できた場合、

```text
200 OK
```

を返します。

例:

```json
{
  "id": 1,
  "name": "キーボード",
  "price": 5000,
  "stock": 10,
  "message": "商品を取得しました"
}
```

---

# 商品更新API

```http
PUT /products/{id}
Content-Type: application/json
```

例:

```json
{
  "name": "メカニカルキーボード",
  "price": 7000,
  "stock": 8
}
```

商品更新では `@Transactional` とJPAのDirty Checkingを利用しています。

---

# 商品削除API

```http
DELETE /products/{id}
```

正常に削除された場合、

```text
204 No Content
```

を返します。

---

# DTO

APIの入出力とEntityの責務を分離するため、Request / Response DTOを使用しています。

```text
ProductRequest
ProductResponse
```

Request側は、

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

Response側は、

```text
PostgreSQL
      |
      v
Product Entity
      |
      v
ProductResponse
      |
      v
HTTP Response
```

という流れで処理しています。

Entityを直接HTTPレスポンスとして返さず、APIとして公開する情報をDTOで制御しています。

---

# Validation

商品登録・更新時にはBean Validationを使用しています。

## 商品名

未入力や空文字を許可しません。

```java
@NotBlank
```

## 価格

未入力および負数を許可しません。

```java
@NotNull
@PositiveOrZero
```

## 在庫

未入力および負数を許可しません。

```java
@NotNull
@PositiveOrZero
```

不正な値が送信された場合は、

```text
400 Bad Request
```

を返します。

例:

```json
{
  "message": "入力内容に誤りがあります",
  "errors": {
    "name": "商品名は必須です"
  }
}
```

複数項目に問題がある場合は、複数のValidationエラーをまとめて返します。

例:

```json
{
  "message": "入力内容に誤りがあります",
  "errors": {
    "stock": "在庫は必須です",
    "price": "金額は必須です"
  }
}
```

---

# 例外ハンドリング

`@RestControllerAdvice` を使用し、API全体の例外処理を共通化しています。

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

Validationエラーについても共通の例外ハンドラで処理し、API利用側がエラー内容を判断しやすい形式にしています。

---

# Repository

商品データへのアクセスにはSpring Data JPAを使用しています。

```java
public interface ProductRepository
        extends JpaRepository<Product, Long> {
}
```

`JpaRepository` から継承した、

```text
save()
findById()
findAll()
delete()
existsById()
```

などを利用しています。

Spring Data JPAによってRepositoryの実装が自動生成されるため、基本的なCRUD処理について独自のSQLやRepository実装を作成せずにデータアクセスを行っています。

---

# Transaction / Dirty Checking

商品更新処理では `@Transactional` を使用しています。

```java
@Transactional
public ProductResponse updateProduct(...) {
    ...
}
```

Repositoryから取得したEntityはPersistence ContextによってManaged Entityとして管理されます。

そのEntityに対して、

```java
product.setName(...);
product.setPrice(...);
product.setStock(...);
```

のように値を変更することで、明示的に `save()` を呼ばなくてもHibernateのDirty CheckingによってUPDATE SQLが発行されます。

結合テストでは実際に、

```text
INSERT
↓
UPDATE
↓
SELECT
```

を行い、更新内容がPostgreSQLへ反映されていることまで確認しています。

---

# 自動テスト

JUnit / Mockito / MockMvc / Spring Boot Testを使用して、自動テストを実装しています。

テストを、

```text
単体テスト
Controllerテスト
DB結合テスト
API結合テスト
```

に分け、それぞれ異なる責務を確認しています。

---

# Service単体テスト

```text
ProductService
      |
      v
Mock ProductRepository
```

RepositoryをMockitoでMock化し、Serviceのロジック単体を確認しています。

現在、主に以下をテストしています。

- 商品登録
- 商品取得
- 存在しない商品取得時の例外
- 商品更新
- 商品削除

Mockを使用することでPostgreSQLへ接続せず、Serviceのロジックだけを独立してテストしています。

---

# Controllerテスト

```text
MockMvc
   |
   v
ProductController
   |
   v
Mock ProductService
```

ProductServiceをMock化し、Web / Controller層をテストしています。

主に、

- HTTP Status
- JSON Response
- Validation

を確認しています。

例えば商品登録APIでは、

```text
POST /products
↓
201 Created
```

になることや、レスポンスJSONの内容を確認しています。

不正なRequestの場合は、

```text
400 Bad Request
```

になることもテストしています。

---

# PostgreSQL結合テスト

Mockを使用せず、

```text
ProductService
      |
      v
ProductRepository
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

まで実際に接続してテストしています。

## UPDATE

商品更新テストでは、

```text
INSERT
↓
Serviceで更新
↓
Dirty Checking
↓
UPDATE
↓
EntityManager.flush()
↓
EntityManager.clear()
↓
SELECT
```

という流れで、実際にPostgreSQLへ更新内容が保存されていることを確認しています。

## DELETE

削除テストでは、

```text
INSERT
↓
DELETE
↓
SELECT / existsById
```

を実行し、商品が本当にPostgreSQLから削除されたことを確認しています。

---

# API結合テスト

MockMvcを利用し、APIの入口からPostgreSQLまでを一気通貫で確認する結合テストを実装しています。

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
Spring Data JPA
   |
   v
Hibernate
   |
   v
PostgreSQL
```

ここではProductServiceやProductRepositoryをMock化せず、実際のSpring BootアプリケーションとPostgreSQLを接続しています。

---

## POST API結合テスト

```text
POST /products
↓
Controller
↓
Service
↓
Repository
↓
PostgreSQLへINSERT
↓
201 Created
↓
PostgreSQLから再取得
```

HTTPレスポンスが正しいだけでなく、本当にDBへ商品が保存されたことまで確認しています。

---

## GET API結合テスト

POSTで商品を登録した後、レスポンスJSONから登録された商品のIDを取得します。

```text
POST /products
↓
商品登録
↓
レスポンスからid取得
↓
GET /products/{id}
↓
200 OK
↓
登録した商品と同じ内容か確認
```

POSTで作成されたリソースを、実際に別のAPIから取得できることを確認しています。

---

## PUT API結合テスト

```text
POST /products
↓
商品登録
↓
id取得
↓
PUT /products/{id}
↓
200 OK
↓
更新後レスポンスを確認
↓
EntityManager.flush()
↓
EntityManager.clear()
↓
PostgreSQLから再取得
↓
DB上の値も更新されていることを確認
```

APIレスポンスだけではなく、Dirty Checkingによる変更がPostgreSQLまで反映されていることを確認しています。

---

## DELETE API結合テスト

```text
POST /products
↓
商品登録
↓
id取得
↓
DELETE /products/{id}
↓
204 No Content
↓
EntityManager.flush()
↓
EntityManager.clear()
↓
existsById()
↓
DBから削除されたことを確認
```

HTTP上で削除成功になっているだけでなく、PostgreSQL上から実際に商品が消えていることまで確認しています。

---

# APIレスポンスを利用したテスト

API結合テストでは、POSTレスポンスを次のAPI呼び出しへ利用しています。

```text
POST Response
{
  "id": 10,
  ...
}

↓
MvcResult

↓
Response Body

↓
JsonMapper / JsonNode

↓
id取得

↓
GET /products/10
PUT /products/10
DELETE /products/10
```

これにより、固定IDではなく実際にPostgreSQLによって採番されたIDを使って一連のAPI操作をテストしています。

---

# テスト実行

以下のコマンドですべてのテストを実行できます。

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

Spring Bootを起動します。

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

## 商品登録

```bash
curl -i -X POST http://localhost:8080/products \
-H "Content-Type: application/json" \
-d '{
  "name":"キーボード",
  "price":5000,
  "stock":10
}'
```

## 商品一覧取得

```bash
curl -i http://localhost:8080/products
```

## 商品詳細取得

```bash
curl -i http://localhost:8080/products/1
```

## 商品更新

```bash
curl -i -X PUT http://localhost:8080/products/1 \
-H "Content-Type: application/json" \
-d '{
  "name":"メカニカルキーボード",
  "price":7000,
  "stock":8
}'
```

## 商品削除

```bash
curl -i -X DELETE http://localhost:8080/products/1
```

---

# セキュリティ

データベースパスワードなどの秘密情報はGitリポジトリに含めず、環境変数から取得しています。

```properties
spring.datasource.password=${CARTBYPORT_DB_PASSWORD}
```

今後AWSへデプロイする際も、AWS認証情報やDBパスワードなどの秘密情報をソースコードへ直接記述しない構成にする予定です。

---

# 今後の開発予定

現在の商品管理APIを土台として、ECサイトとして必要な機能を段階的に追加します。

---

## Cart

次のフェーズでは、ショッピングカート機能を実装します。

想定Entity:

```text
Cart
CartItem
Product
```

想定する機能:

- 商品をカートへ追加
- 商品数量変更
- カートから商品削除
- カート内容取得
- 合計金額計算

ここでは、

- `@OneToMany`
- `@ManyToOne`
- Entity間リレーション
- 外部キー
- ドメインロジック

などを扱う予定です。

---

## Order

Cart実装後は注文機能を追加する予定です。

想定Entity:

```text
Order
OrderItem
Product
```

想定機能:

- 注文作成
- 注文詳細
- 注文明細
- 商品価格の保持
- 在庫減算
- トランザクション制御

---

## API

今後、

- 商品検索
- Pagination
- Sort
- APIレスポンス設計改善
- エラーレスポンス共通化
- API仕様のドキュメント化

なども追加予定です。

---

# Docker

Spring BootとPostgreSQLをDocker化する予定です。

想定構成:

```text
Spring Boot Container
        |
        v
PostgreSQL Container
```

Docker Composeによるローカル開発環境も構築する予定です。

---

# AWS

Docker対応後はAWSへのデプロイを予定しています。

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

利用予定サービス:

- Amazon ECR
- Amazon ECS / Fargate
- Amazon RDS for PostgreSQL
- Amazon S3
- Amazon CloudWatch

AWSについては資格学習だけではなく、このアプリケーションを実際にデプロイしながら理解を深める予定です。

---

# CI/CD

GitHub Actionsを利用したCI/CDも実装予定です。

想定フロー:

```text
GitHub Push
     |
     v
JUnit / Integration Test
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

コード変更後のテスト・ビルド・デプロイを自動化することを目標としています。

---

# Frontend

バックエンドAPIの実装後は、

- React
- TypeScript

を使用してECサイトのフロントエンドを実装する予定です。

最終的には、

```text
React
   |
   v
Spring Boot REST API
   |
   v
PostgreSQL
```

という形で、フロントエンドとバックエンドを連携させます。

---

# Data / Recommendation

将来的には購入履歴を利用して、

```text
「この商品を購入した人は、
 こんな商品も購入しています」
```

のようなレコメンド機能を追加する予定です。

初期段階ではSQLによる商品の共起分析などから実装し、その後データ分析基盤へ発展させることも検討しています。

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

商品・カート・注文などのECドメインだけでなく、

```text
Backend
Database
Testing
Docker
Cloud
Monitoring
CI/CD
```

まで一連の開発を経験できるポートフォリオへ発展させていきます。

---

# 現在の開発ステータス

現在は商品管理APIについて、

```text
商品CRUD API
     ↓
PostgreSQL永続化
     ↓
Request / Response DTO
     ↓
Bean Validation
     ↓
例外ハンドリング
     ↓
@Transactional
     ↓
Dirty Checking
     ↓
Service単体テスト
     ↓
Controllerテスト
     ↓
PostgreSQL結合テスト
     ↓
POST API結合テスト
     ↓
GET API結合テスト
     ↓
PUT API結合テスト
     ↓
DELETE API結合テスト
```

まで実装しています。

商品管理機能については、

**API実装 → DB永続化 → Validation → 例外処理 → 単体テスト → DB結合テスト → APIからPostgreSQLまでの一気通貫テスト**

まで一通り実装しました。

次のフェーズでは `Cart / CartItem` を実装し、ProductとのEntityリレーション、数量変更、合計金額計算など、複数のEntityが関係するECドメインの実装へ進みます。

---

# Repository

GitHub:

https://github.com/javapanman11/cart-by-port