# Modrinth 自動公開（Danmaku Chat）

投稿先は既存の Danmaku Chat プロジェクトです。新規プロジェクトの重複作成はしません。

- Modrinth: https://modrinth.com/mod/danmaku-chat
- 固定プロジェクトID: TIlGeskT
- GitHub: https://github.com/Rangesa/Danmaku-Chat

## 初期状態（重要）

安全のため **自動公開はOFF** です。公開ワークフローはリポジトリの main ブランチに合流してから有効になります。PRや開発ブランチでは認証トークンを扱いません。

本プロジェクトは1.21.8 / 1.21.11 / 26.2 / 26.3 のGradleビルドとFabric Client GameTest（実クライアントの起動・ワールド生成・合成チャット・HUD描画）に成功しています。一方、実マルチプレイヤーの署名付きチャット、Mod Menuの全操作、他Modとの互換性などは未検証で、完全動作保証とは区別してください。

## 初回に一度だけ行う設定

1. 既存 Danmaku Chat プロジェクトの管理権限があるModrinthアカウントでログインし、個人アクセストークン(PAT)を作成してください。必要な権限は「Create versions / Version Create」です。権限は必要最小限にします。
2. GitHubの Rangesa/Danmaku-Chat → Settings → Secrets and variables → Actions → New repository secret で、MODRINTH_TOKEN という名前でPATを登録します。PATをコード、コミット、Issue、チャットに貼らないでください。
3. Modrinthの既存プロジェクト → Settings → Disclosures で、AI-generated code とAI-generated text等の開示を正確に設定します。今回の移植はAIが複数クラスの生成・修正と公開自動化コードに関与しています。主にAI生成のプロジェクトはModrinthの規則上、公開できない場合があります。開示だけでは公開が許可されるわけではありません。
4. 実機テストとプロジェクトの公開適格性を確認してください。過去に作成したModrinthプロジェクトの現状をAIが自動的に証明することはできません。
5. GitHub Settings → Secrets and variables → Actions → Variables で、MODRINTH_AI_DISCLOSURE_CONFIRMED=true と MODRINTH_PUBLISH_ENABLED=true を登録します。どちらかが未設定なら公開ワークフローは安全にスキップします。

ここまでを一度設定すれば、その後は main に変更をpushするたびにCI・パッケージ整合性チェックを実施し、すべて通ったときにModrinthの既存プロジェクトへ自動でアップロードと公開を行います。毎回手作業でアップロードする必要はありません。

## 技術的な仕様

- GitHub Actionsで4種類の独立したMinecraftバージョンごとにGradleビルドします。マトリクスのどれか一つが失敗すれば全体の公開ジョブは開始しません。加えて全4バージョンの実Minecraft Client GameTestが成功しない限り公開ジョブは開始しません。
- 配布用JARを1つに絞り、fabric.mod.json のMod ID、Modバージョン、Minecraft依存のバージョン一致などを検査します。1.21.11固有のMixinリストと、26.xのMod Menuクラスも検査します。
- Modrinth v2 APIでプロジェクトID TIlGeskT の存在・ソースURL・承認状態を確認し、POST /version にJARをアップロードします。
- Modrinth依存関係には Fabric API（required）と Mod Menu（optional）を設定します。
- mod_version（gradle.properties）に対象MCの名前を付けて、例: 1.1.0-dev-mc26.3 として公開します。
- dev/beta/alpha 表記のある版は beta として、それ以外は release として version status listed で公開します。
- 同じバージョン番号かつSHA-512の同一ファイルはスキップします。同名バージョンに別のJARが存在する場合は上書きせず、エラーで停止します（mod_versionを上げる必要があります）。
- シークレットは main での公開ジョブだけに渡されます。fork PR、ビルド、単体テストには渡しません。

## オフラインでの検証

    python3 -m unittest discover -s scripts -p "test_modrinth_publisher.py" -v
    gradle clean build -Pmc_target=26.3
    python3 scripts/modrinth_publisher.py --target 26.3

最後の行は dry-run です。アップロードはしません。

## 公開制限と管理

Modrinthは2026年8月以降、AI生成コンテンツの規定と開示義務を導入しています。 https://modrinth.com/legal/rules と https://support.modrinth.com/en/articles/16567675-content-disclosures を参照してください。

この自動化はModrinthの審査、規約、投稿権限を回避しません。AI生成コードの比率や制作実態により公開が認められない場合は、公開フラグを有効にしないでください。既存のアイコンやギャラリーは変更しません。

稼働後もコンパイル成功は実機動作の保証ではありません。mainへの変更にはCIを必須とし、公開後の重大な回帰に備えていつでもMODRINTH_PUBLISH_ENABLED=falseで停止できます。
