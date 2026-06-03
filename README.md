# Ti Social Android Client

Android 原生客户端应用，用于与 Ti Social 后端服务交互。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.0.x (K2 编译器) |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM |
| 依赖注入 | Hilt |
| 网络 | Ktor Client |
| 序列化 | Kotlinx Serialization |
| 本地存储 | Room + DataStore |
| 异步 | Coroutines + Flow |
| 导航 | Compose Navigation |

## 项目状态

🚧 **骨架阶段** - 项目结构已搭建，核心功能待实现

## 快速开始

```bash
# 1. 同步 Gradle
./gradlew

# 2. 构建 Debug APK
./gradlew assembleDebug

# 3. 安装到设备
./gradlew installDebug
```

## 目录结构

```
app/src/main/kotlin/tech/ti/social/
├── core/           # 核心基础设施
├── di/             # Hilt 依赖注入
├── feature/        # 功能模块
│   └── main/       # 主入口
└── ui/             # 共享 UI 组件
    └── theme/      # Material 3 主题
```

## 开发指引

详见 [CLAUDE.md](CLAUDE.md)