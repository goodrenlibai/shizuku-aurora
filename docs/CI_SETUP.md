# CI/CD 配置与运行指南

本指南说明如何把 `shizuku-aurora` 接入 GitHub Actions，实现推送自动构建、APK 产物下载，以及构建失败时的排查修复。

---

## 0. 安全提示（务必先读）

你提供的 Personal Access Token（PAT）曾在明文对话中出现。任何明文出现过的令牌都应视为已泄露：

1. **立即吊销**：进入 GitHub → Settings → Developer settings → Personal access tokens，删除该令牌；
2. **重新生成**：新建一个 scope 为 `repo` + `workflow` 的令牌；
3. 新令牌只通过 GitHub 仓库 **Secrets** 保存，绝不写入源码、日志或文档。

> 本工作流本身只使用仓库内置 `GITHUB_TOKEN`，不依赖任何 PAT，因此即便不配置 PAT 也能正常构建。
> PAT 仅用于「你在本地通过 `gh`/REST 检查状态或触发工作流」这类个人操作。

---

## 1. 配置仓库 Secrets

> 工作流不需要，但按你的要求，以下认证信息可配置为 Secrets 备用：

1. 打开 GitHub 仓库 → **Settings** → **Secrets and variables** → **Actions**；
2. 点击 **New repository secret**，分别添加：
   - 名称 `GH_USERNAME`，值 `goodrenlibai`
   - 名称 `GH_PAT`，值 = 你**重新生成**的令牌（不要使用已泄露的旧令牌）

---

## 2. 从零开始：建仓 → 推送 → 触发（无仓库时走这里）

前提：安装 [GitHub CLI](https://cli.github.com/)（`gh`），并 `gh auth login` 登录你的账号（`goodrenlibai`）。

```bash
cd shizuku-aurora

# 2.1 本地初始化并提交
git init
git add -A
git commit -m "feat: Shizuku Aurora (3A upgrade) + CI"
git branch -M main

# 2.2 创建远程仓库并推送（公开仓库；私有用 --private）
gh repo create shizuku-aurora --public --source . --remote origin --push

# 若未安装 gh，改用手工方式：
#   git remote add origin https://github.com/goodrenlibai/shizuku-aurora.git
#   git push -u origin main
```

推送任意分支（含 `main`）都会自动触发 `Android Build` 工作流。

### 已有仓库时（跳过建仓）

```bash
cd shizuku-aurora
git remote add origin https://github.com/<用户名>/<仓库名>.git
git push -u origin main
```

### 手动触发（workflow_dispatch）

```bash
gh workflow run android-build.yml --ref main          # 默认 debug
# 指定 release：
gh workflow run android-build.yml --ref main -f build_type=release
```

或网页：仓库 → **Actions** → `Android Build` → **Run workflow**。

---

## 3. 监控构建状态

### 方式 A：网页

仓库 → **Actions** → 点开最新 run，实时查看每个 step 的日志。

### 方式 B：gh CLI（使用配置好的 PAT）

```bash
export GH_TOKEN=<你的新PAT>
gh auth status
gh run list --repo <用户名>/<仓库名>            # 列出最近 runs
gh run watch --repo <用户名>/<仓库名>          # 跟踪最新 run 直到结束
gh workflow run android-build.yml --repo <用户名>/<仓库名>   # 手动触发
```

---

## 4. 下载 APK（构建成功后）

1. 进入对应的 workflow run 页面（Actions → 点击该 run）；
2. 页面底部 **Artifacts** 区域找到名为 **`android-apk`** 的产物；
3. 点击下载，解压得到 `app-debug.apk`（debug）或 `app-release-unsigned.apk`（release）。

> 注意：release 未配置签名，产物为 unsigned，安装前需自行签名或改用 debug。

---

## 5. 构建失败排查（迭代修复）

按出现概率从高到低，逐项核对：

| 症状 | 可能原因 | 修复 |
|------|----------|------|
| `Could not resolve com.github.topjohnwu.libsu:core` | 未配置 JitPack 仓库 | 已在 `settings.gradle.kts` 加入 `maven("https://jitpack.io")` |
| `Could not resolve dev.rikka.shizuku:provider` | 漏加 provider artifact | 已在 `libs.versions.toml` + `core/data` 加入 |
| `Manifest merger failed` / 找不到 `rikka.shizuku.ShizukuProvider` | provider 依赖缺失 | 同上，已修复 |
| `Unsupported class file major version 65`（Java 版本错） | 用了 Java 11/17 而非 21 | 工作流已固定 Temurin 21 |
| `compileSdk 36` 下载失败 | 缺少 SDK license | `android-actions/setup-android@v3` 已自动接受 license |
| 配置缓存报错 | `org.gradle.configuration-cache=true` 与某插件冲突 | 在 `gradle.properties` 临时改为 `false` |
| KSP / Hilt 版本冲突 | Kotlin 2.0.21 与 KSP 1.0.28 / Hilt 2.52 不匹配 | 按报错提示调整 `libs.versions.toml` 中 kotlin/ksp/hilt 三版本 |
| `gradle` 命令找不到 | 未装 Gradle | 工作流用 `gradle-version: '8.11.1'` 自动安装 |

每次修复后：

```bash
git add -A && git commit -m "fix: ..." && git push
```

再回到 Actions 页面确认新 run 是否变绿；失败则继续按日志定位，循环直到成功。

---

## 6. 工作流文件位置

```
shizuku-aurora/.github/workflows/android-build.yml
```
