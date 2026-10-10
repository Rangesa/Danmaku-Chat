# DanmakuChat

**マルチバージョン開発版:** Minecraft 1.21.8 / 1.21.11 / 26.2 / 26.3の専用JARを提供しています。4版で実際のMinecraftクライアント起動・ワールド生成・HUD描画の自動テストに成功。ただしMod Menu全機能やマルチプレイヤー実環境は引き続き検証中です。詳しくは[GitHub Releases](https://github.com/Rangesa/Danmaku-Chat/releases)と[検証内容](docs/MULTIVERSION.md)をご覧ください。

Minecraft 1.21.8 (Fabric) 用のニコニコ動画風弾幕チャットオーバーレイ Mod です。

## 概要

DanmakuChat は、Minecraft の標準的なチャットシステムを、ニコニコ動画にインスパイアされた流れるような弾幕スタイルのオーバーレイに置き換えます。チャットメッセージが画面の右から左へと流れ、ダイナミックで没入感のあるチャット体験を提供します。

## 特徴

- **標準チャットの非表示化**: バニラのチャット欄を隠し、画面をすっきりさせます。
- **流れる弾幕チャット**: メッセージが画面上を流れます。
- **衝突回避アルゴリズム**: メッセージ同士が重ならないように調整されます（ニコニコ動画の仕様を参考にしています）。
- **メッセージフィルタリング**: システムメッセージとユーザーチャットの表示/非表示を個別に切り替え可能です。
- **カスタマイズ性**: 流れる速度、透明度、レーン数、表示時間を自由に調整できます。
- **コマンド設定**: ゲーム内コマンドで簡単に設定を変更できます。
- **設定の保存**: 設定は JSON ファイルとして保存され、再起動後も維持されます。
- **外部連携**: Discord などの外部チャット連携をサポート（予定/一部対応）。
- **クライアントサイド**: クライアント側に導入するだけで動作します。

## 動作環境

- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.16.14 以上
- **Fabric API**: 0.129.0+1.21.8 以上
- **Java**: 21 以上

## インストール方法

1. Minecraft 1.21.8 用の Fabric Loader をインストールします。
2. Fabric API をダウンロードします。
3. ダウンロードした Mod ファイル（.jar）と Fabric API を `.minecraft/mods` フォルダに入れます。
4. ゲームを起動します。

※ 設定画面（ModMenu）を利用する場合は、別途 ModMenu をインストールしてください。

## 使い方

### コマンド

#### 現在の設定を表示
```
/danmaku
```

#### 有効化/無効化
```
/danmaku enable       # 弾幕を有効にする
/danmaku disable      # 弾幕を無効にする
```

#### チャットフィルター設定
```
/danmaku system <true|false>    # システムメッセージの表示切り替え
/danmaku user <true|false>      # ユーザーチャットの表示切り替え
```

使用例:
```
/danmaku system true     # システムメッセージを表示する
/danmaku system false    # システムメッセージを隠す（デフォルト）
/danmaku user true       # ユーザーチャットを表示する（デフォルト）
/danmaku user false      # ユーザーチャットを隠す
```

#### 詳細設定
```
/danmaku speed <0.1-5.0>        # スクロール速度（デフォルト: 1.0）
/danmaku lanes <1-20>           # 最大レーン数（デフォルト: 10）
/danmaku opacity <0.0-1.0>      # 背景の透明度（デフォルト: 0.8）
```

使用例:
```
/danmaku speed 2.0       # 速度を2倍にする
/danmaku lanes 15        # 同時に15行まで表示する
/danmaku opacity 0.5     # 半透明にする
```

# クライアントの起動（テスト用）
./gradlew runClient
```

## ライセンス

MIT License


## Multi-version development (draft)

The development branch includes experimental separate builds for Minecraft 1.21.8, 1.21.11, 26.2 and 26.3. **These are not released or verified compatibility claims.** See [docs/MULTIVERSION.md](docs/MULTIVERSION.md) for build instructions, porting notes and mandatory verification.
