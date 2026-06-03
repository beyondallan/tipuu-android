# CLAUDE.md

Android 客户端项目说明。

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

## 构建命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行测试
./gradlew test

# 运行 Lint 检查
./gradlew lint

# 安装到设备
./gradlew installDebug

# 清理构建产物
./gradlew clean
```

## 架构

```
┌─────────────────────────────────────────────┐
│  UI Layer (Compose Screens, ViewModels)      │
│  → 使用 StateFlow 管理 UI 状态               │
└─────────────────────┬───────────────────────┘
                      │ 调用 UseCase
                      ▼
┌─────────────────────────────────────────────┐
│  Domain Layer (Entities, UseCases,           │
│               Repository Interfaces)         │
│  → 纯 Kotlin，无 Android 依赖                │
└─────────────────────┬───────────────────────┘
                      │ 实现 Repository
                      ▼
┌─────────────────────────────────────────────┐
│  Data Layer (API, Database, Repository Impl) │
│  → Ktor (网络) + Room (数据库)               │
└─────────────────────────────────────────────┘
```

## 目录结构

```
app/src/main/kotlin/tech/ti/social/
├── core/           # 核心基础设施
│   ├── network/    # Ktor 网络封装
│   ├── storage/    # 存储封装 (待实现)
│   ├── hardware/   # 硬件抽象层 (待实现)
│   └── error/      # 错误处理 (待实现)
│
├── di/             # Hilt 依赖注入模块
│   ├── AppModule.kt
│   └── NetworkModule.kt
│
├── feature/        # 功能模块
│   ├── main/       # 主入口/导航
│   ├── auth/       # 认证 (待实现)
│   ├── bluetooth/  # 蓝牙 (待实现)
│   ├── audio/      # 音频 (待实现)
│   └── ...
│
└── ui/             # 共享 UI 组件
    └── theme/      # Material 3 主题
```

## 模块依赖规则

Feature 模块之间**不直接依赖**，通过 `core/` 层的 Provider/Repository 接口通信。

## 添加新功能模块

1. 创建 `feature/<name>/` 目录
2. 按三层结构组织：`data/`, `domain/`, `ui/`
3. 在 `di/` 中添加对应的 Hilt Module
4. 在 `feature/main/navigation/NavGraph.kt` 中注册路由

## 状态管理

```kotlin
// ViewModel 使用 StateFlow
class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // ...
        }
    }
}

// Compose 中使用 collectAsStateWithLifecycle()
@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // ...
}
```

## 网络请求

```kotlin
// 使用 Ktor Client
class AuthRepositoryImpl(
    private val client: HttpClient
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<User> {
        return try {
            val response = client.post("auth/login") {
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e.toAppException())
        }
    }
}
```

## 待实现功能

- [ ] DataStore + Room 存储层封装
- [ ] BLE 硬件抽象层
- [ ] Audio 硬件抽象层
- [ ] Auth 认证模块完整实现
- [ ] 导航系统 (NavGraph)
- [ ] 错误处理统一封装