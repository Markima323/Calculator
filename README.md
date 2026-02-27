# Calculator

离线安卓摆摊点单/计价 App（Kotlin + Compose + Room）。

## 已实现功能

- 点单主页：分类筛选、搜索、商品网格、实时总价、完成清空
- 购物车清单：加减、直接改数量、数量为 0 自动移除
- 商品管理：新增/编辑/删除、上下架、权重排序、导入导出 JSON
- 商品图片：相册选择、拍照、自动方形裁剪压缩并保存到私有目录
- 分类管理：新增/编辑/删除
- 设置页：货币符号、完成确认、0 元商品、重启恢复、显示已售完
- 完全离线：Room + DataStore + 本地图片

## 技术栈

- Kotlin
- Jetpack Compose
- Room (SQLite)
- DataStore (Preferences)
- Navigation Compose
- Coil

## 运行方式

1. 安装 Android Studio（含 Android SDK）。
2. 在项目根目录创建 `local.properties`：

```properties
sdk.dir=C:\\Users\\<你的用户名>\\AppData\\Local\\Android\\Sdk
```

3. 构建调试包：

```bash
./gradlew :app:assembleDebug
```

Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

## 文档

- [离线摆摊点单 App 需求与技术说明](docs/offline-stall-ordering-prd.md)
