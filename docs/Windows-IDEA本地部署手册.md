> 历史说明已清理原服务器地址；本文中的域名仅为占位示例，当前环境配置与数据库升级请以根目录 README 为准。

# 蜂场养殖与蜂蜜溯源管理系统 — Windows + IDEA 本地部署手册

> 适用对象：本科计算机专业学生，电脑从零开始配置
> 技术栈：JDK 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + Vue 3 + Element Plus

---

## 一、需要安装的软件（共 5 个）

| 软件 | 版本要求 | 作用 | 必装 |
|------|---------|------|------|
| JDK | 17（LTS） | 运行/编译 Java 后端 | ✅ |
| Maven | 3.6+ | 后端依赖管理和打包 | ✅ |
| Node.js | 20.19+（推荐 22.12+ 或 24 LTS） | 前端依赖管理和构建 | ✅ |
| MySQL | 8.0.x | 数据库 | ✅ |
| IntelliJ IDEA | 2023+（Community 免费版即可） | 代码编辑器 | ✅ |

> 说明：Maven 和 Node.js 安装后需要配置环境变量；IDEA 自带 Maven 插件，但建议独立安装 Maven 以便命令行使用。

---

## 二、各软件下载地址

### 1. JDK 17（推荐 Oracle 或 OpenJDK）

- **Oracle JDK 17**：https://www.oracle.com/java/technologies/downloads/#java17
  - 选择 Windows → x64 Installer（.exe）
- **OpenJDK 17（免费，推荐）**：https://adoptium.net/temurin/releases/?version=17
  - 选择 Operating System: Windows，Architecture: x64，Package Type: JDK，下载 .msi

### 2. Maven 3.9

- 官网：https://maven.apache.org/download.cgi
- 下载 `apache-maven-3.9.x-bin.zip`（Binary zip archive）

### 3. Node.js 22/24 LTS

- 官网：https://nodejs.org/zh-cn/download
- 下载 Windows Installer (.msi) 64-bit

### 4. MySQL 8.0

- 官网：https://dev.mysql.com/downloads/mysql/
- 下载 MySQL Installer for Windows（推荐 `mysql-installer-community-8.0.x.msi`）
- 安装时选择 "Server only" 或 "Developer Default"
- **设置 root 密码时请记住，后面要用**

### 5. IntelliJ IDEA

- 官网：https://www.jetbrains.com/idea/download/
- 下载 Community 版（免费）或 Ultimate 版（学生可免费申请）

---

## 三、环境变量配置

### 1. JDK 环境变量

1. 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
2. 在"系统变量"中点击"新建"：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot`（根据实际安装路径填写）
3. 找到系统变量中的 `Path`，点击"编辑"，新增两行：
   ```
   %JAVA_HOME%\bin
   ```
4. 验证：打开 cmd，输入 `java -version`，显示版本号即成功

### 2. Maven 环境变量

1. 把下载的 `apache-maven-3.9.x-bin.zip` 解压到 `C:\Program Files\apache-maven-3.9.x`
2. 系统变量新建：
   - 变量名：`MAVEN_HOME`
   - 变量值：`C:\Program Files\apache-maven-3.9.x`
3. Path 中新增：
   ```
   %MAVEN_HOME%\bin
   ```
4. 验证：cmd 输入 `mvn -v`，显示版本号即成功

### 3. Node.js 环境变量

Node.js 安装时默认会自动配置环境变量。验证：cmd 输入 `node -v` 和 `npm -v`。

---

## 四、镜像源配置（国内必做，否则下载极慢）

### 1. Maven 阿里云镜像

找到 Maven 安装目录下的 `conf/settings.xml`（如 `C:\Program Files\apache-maven-3.9.x\conf\settings.xml`），用记事本打开，在 `<mirrors>` 标签内添加：

```xml
<mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

同时建议设置本地仓库路径（避免放在 C 盘用户目录），在 `<settings>` 标签内（`<mirrors>` 之前）添加：

```xml
<localRepository>D:\maven-repo</localRepository>
```

### 2. npm 淘宝镜像

打开 cmd，执行：

```bash
npm config set registry https://registry.npmmirror.com
```

验证：`npm config get registry`，显示淘宝地址即成功。

---

## 五、数据库初始化

### 1. 登录 MySQL

打开 cmd，输入：

```bash
mysql -u root -p
```

输入安装时设置的 root 密码。

### 2. 创建数据库和用户

```sql
-- 创建数据库
CREATE DATABASE bee_farm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建用户（密码自行设置，建议 CHANGE_ME_DB_PASSWORD）
CREATE USER 'bee'@'localhost' IDENTIFIED BY 'CHANGE_ME_DB_PASSWORD';

-- 授权
GRANT ALL PRIVILEGES ON bee_farm.* TO 'bee'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 导入表结构和初始数据

退出 MySQL（输入 `exit`），在 cmd 中执行：

```bash
# 导入表结构
mysql -u bee -p bee_farm < 项目路径\sql\schema.sql

# 导入初始数据
mysql -u bee -p bee_farm < 项目路径\sql\data.sql
```

> 注意：`项目路径` 替换为你解压代码后的实际路径，如 `D:\bee-farm-system`。

### 4. 验证

```bash
mysql -u bee -p bee_farm -e "SHOW TABLES;"
```

应显示 37 张表。

---

## 六、IDEA 导入项目并启动后端

### 1. 导入项目

1. 打开 IDEA → File → Open
2. 选择项目根目录 `bee-farm-system`（包含 backend 和 frontend 两个子目录）
3. 等待 IDEA 识别 Maven 项目，右下角会提示 "Maven projects need to be imported"，点击 "Import Changes" 或 "Enable Auto-Import"

### 2. 配置 JDK

1. File → Project Structure → Project
2. SDK 选择 17，Language level 选择 17
3. Modules → backend → Sources → Language level 选择 17

### 3. 修改数据库配置

打开 `backend/src/main/resources/application.yml`，确认数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bee_farm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: bee
    password: CHANGE_ME_DB_PASSWORD
```

> 如果你创建数据库用户时设置了不同密码，这里要改成你自己的密码。

### 4. 启动后端

1. 在 IDEA 左侧项目树找到 `backend/src/main/java/com/beefarm/BeeFarmApplication.java`
2. 右键 → Run 'BeeFarmApplication'
3. 控制台出现 `Started BeeFarmApplication` 和 `Tomcat started on port 8090` 即启动成功
4. 浏览器访问 `http://localhost:8090/api/auth/login` 测试（POST 请求）

---

## 七、启动前端

### 方式一：IDEA 终端启动（推荐）

1. 在 IDEA 底部打开 Terminal（Alt+F12）
2. 进入前端目录：
   ```bash
   cd frontend
   ```
3. 安装依赖（第一次需要，约 3-5 分钟）：
   ```bash
   npm install
   ```
4. 启动开发服务器：
   ```bash
   npm run dev
   ```
5. 控制台显示 `Local: http://localhost:5173/` 即成功
6. 浏览器访问 `http://localhost:5173`

### 方式二：VS Code 或 cmd 启动

在 frontend 目录下打开 cmd，执行同样的 `npm install` 和 `npm run dev`。

---

## 八、登录账号

系统预置 6 个角色账号，密码均为 `123456`（admin 密码为 `xxxr0708`）：

| 用户名 | 角色 | 真实姓名 | 主要权限 |
|--------|------|---------|---------|
| admin | 系统管理员 | 系统管理员 | 全部权限 |
| manager | 场长 | 张场长 | 养殖管理、采收、质检、审核 |
| farmer | 养殖员 | 李养殖 | 蜂箱、蜂王、巡检、喂养、病害 |
| producer | 生产员 | 王生产 | 摇蜜采收、装瓶、质检录入 |
| warehouse | 仓管员 | 赵仓储 | 库存、出入库、采购 |
| sales | 销售员 | 孙销售 | 客户管理、销售出库 |

---

## 九、前端构建生产版本（可选）

如果需要打包前端静态文件部署到 Nginx：

```bash
cd frontend
npm run build
```

产物在 `frontend/dist/` 目录，将其复制到 Nginx 的 html 目录即可。

---

## 十、常见问题排查

### Q1：`mvn -v` 提示不是内部命令
A：Maven 环境变量未配置或 Path 未生效。检查 `MAVEN_HOME` 和 `Path` 配置，配置后**重启 cmd**。

### Q2：`npm install` 报错或极慢
A：检查 npm 镜像是否配置为淘宝源。执行 `npm config get registry` 确认。

### Q3：后端启动报数据库连接失败
A：检查 MySQL 是否启动（服务中查看 MySQL80 服务状态），检查 `application.yml` 中的用户名密码是否正确，检查数据库 `bee_farm` 是否已创建。

### Q4：后端启动报 `Table 'bee_farm.xxx' doesn't exist`
A：未导入 schema.sql。按第五节步骤导入表结构。

### Q5：前端页面空白或接口 404
A：检查后端是否启动（端口 8090），检查前端 `vite.config.js` 中的代理配置是否指向 `http://localhost:8090`。

### Q6：IDEA 导入后后端代码报红
A：Maven 依赖未下载。右键 `backend/pom.xml` → Maven → Reload Project，等待依赖下载完成。

### Q7：端口被占用
A：后端默认 8090，前端默认 5173。如果被占用，修改 `application.yml` 的 `server.port` 和 `vite.config.js` 的 `server.port`。

---

## 十一、项目目录结构说明

```
bee-farm-system/
├── backend/                    # 后端 Spring Boot 项目
│   ├── pom.xml                # Maven 依赖配置
│   └── src/main/
│       ├── java/com/beefarm/  # Java 源码
│       │   ├── common/        # 公共模块（异常、工具、配置）
│       │   ├── system/        # 系统管理（用户、角色、菜单）
│       │   ├── breed/         # 养殖管理（蜂箱、蜂王、巡检、喂养）
│       │   ├── disease/       # 病害防治（病害记录、用药记录）
│       │   ├── produce/       # 采收生产（摇蜜、装瓶、质检）
│       │   ├── stock/         # 库存管理（台账、流水、仓库）
│       │   ├── sale/          # 销售出库（客户、出库单）
│       │   ├── purchase/      # 采购管理（供应商、采购单）
│       │   ├── trace/         # 溯源管理（溯源码、扫码日志）
│       │   └── report/        # 统计报表
│       └── resources/
│           ├── application.yml # 应用配置
│           └── mapper/         # MyBatis XML 映射文件
├── frontend/                   # 前端 Vue 3 项目
│   ├── package.json           # 前端依赖配置
│   ├── vite.config.js         # Vite 构建配置
│   ├── index.html
│   ├── public/                # 静态资源（蜜蜂图标等）
│   └── src/
│       ├── api/               # 接口请求封装
│       ├── views/             # 页面组件（按模块分目录）
│       ├── components/        # 公共组件（专业词汇提示等）
│       ├── router/            # 路由配置
│       ├── store/             # Pinia 状态管理
│       ├── styles/            # 全局样式
│       └── utils/             # 工具函数
├── sql/                        # 数据库脚本
│   ├── schema.sql             # 表结构（37张表）
│   └── data.sql               # 初始数据
├── docs/                       # 项目文档
└── README.md
```

---

## 十二、快速启动清单（按顺序执行）

```
□ 1. 安装 JDK 17，配置 JAVA_HOME 和 Path
□ 2. 安装 Maven，配置 MAVEN_HOME、Path、阿里云镜像
□ 3. 安装 Node.js 18，配置 npm 淘宝镜像
□ 4. 安装 MySQL 8，设置 root 密码
□ 5. 安装 IDEA
□ 6. 创建数据库 bee_farm 和用户 bee
□ 7. 导入 schema.sql 和 data.sql
□ 8. IDEA 打开项目，等待 Maven 依赖下载
□ 9. 修改 application.yml 数据库密码
□ 10. 启动后端 BeeFarmApplication
□ 11. 前端 npm install
□ 12. 前端 npm run dev
□ 13. 浏览器访问 http://localhost:5173，用 admin 登录
```

全部完成后系统即可正常使用。
