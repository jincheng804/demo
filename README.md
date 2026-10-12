# 蜂场养殖与蜂蜜溯源管理系统

## 本次审美优化与审查修复版

交付日期：2026-10-12。GitHub 仓库提供完整源码与构建说明，本地交付 ZIP 另含前端构建文件。改动和验证范围见 [审查报告](docs/审美优化与代码审查报告.md)，界面展示见 [界面预览](docs/界面预览.html)。

- Java 17、Spring Boot 3.5.16、MyBatis-Plus 3.5.6、MySQL 8、JWT。
- Vue 3、Vite 6.4.4、Element Plus、Pinia、ECharts 6.1.0。
- 建议 Node.js 22.12+ 或 24 LTS，最低 Node 20.19；Maven 3.9+。
- 数据库 38 张表，其中 1 张为新增的事务锁技术表。

## 一、已有系统升级

1. 备份现有数据库，保留当前部署文件。
2. 用有 ALTER/CREATE 权限的数据库账号执行 `sql/upgrade_review_20261011.sql`。脚本不清空业务数据，可重复执行。
3. 可运行只读的 `sql/check_inventory_consistency.sql`，核对历史批次与库存异常。
4. 按下述环境变量配置后端，重新构建部署；同时替换完整 `frontend/dist`。本版密码重置请求格式已改变，前后端需一起更新。
5. 重新登录。旧令牌不兼容本版的版本校验。

**已有库不要执行 `schema.sql`：该文件包含 DROP TABLE，仅供新建演示库。**

## 二、全新安装

在 MySQL 客户端中依次执行（路径换成解压后的实际绝对路径）：

```sql
SOURCE D:/your-project/bee-farm-system/sql/schema.sql;
SOURCE D:/your-project/bee-farm-system/sql/data.sql;
```

`schema.sql` 创建并选择 `bee_farm`；`data.sql` 是原项目演示数据。演示数据包含历史批次超额出库等异常，具体见审查报告，不应作为真实生产账目。已修改代码不会自动改写这些历史数据。

为程序配置可访问该库的 MySQL 账号；初始化脚本不自动创建数据库登录用户。

## 三、后端配置与启动

环境变量：

| 变量 | 用途 | 缺省值/说明 |
|---|---|---|
| `BEE_SERVER_ADDRESS` | 后端绑定地址 | 127.0.0.1，可通过环境变量修改 |
| `BEE_SERVER_PORT` | 后端端口 | 8090 |
| `BEE_DB_URL` | MySQL 连接地址 | 本机 3306 的 bee_farm，Asia/Shanghai |
| `BEE_DB_USERNAME` | 数据库用户名 | bee |
| `BEE_DB_PASSWORD` | 数据库密码 | 无缺省密码，须自行设置 |
| `BEE_JWT_SECRET` | 登录签名密钥 | 至少 64 字节；未设置会生成临时密钥，重启后须重新登录 |
| `BEE_TRACE_BASE_URL` | 溯源码指向的前端网址 | http://localhost:5173 |
| `BEE_CORS_ORIGINS` | 允许的前端来源，逗号分隔 | http://localhost:5173,http://127.0.0.1:5173 |

正式部署应设置数据库密码、固定的随机 JWT 密钥和实际前端网址。多个后端实例使用同一个 JWT 密钥。不要把实际密钥提交到源码。

PowerShell 启动示例：

```powershell
$env:BEE_DB_USERNAME='你的数据库用户名'
$env:BEE_DB_PASSWORD='你的数据库密码'
$env:BEE_JWT_SECRET='替换为你保存的至少64字节随机密钥'
cd backend
mvn clean package
java -jar target/bee-farm-backend.jar
```

服务端口 8090，接口前缀 `/api`。接口文档入口 `http://localhost:8090/api/doc.html`。

## 四、前端启动和部署

```powershell
cd frontend
npm ci
npm run dev
```

访问 `http://localhost:5173`；开发代理将 `/api` 转发到后端 8090。开发服务缺省绑定本机。

```powershell
npm run build
```

生产环境部署 `frontend/dist`，网站根路径使用 history 路由回退到 `index.html`，并将 `/api` 请求反向代理到后端。溯源二维码应配置外部设备可访问的真实域名。

## 五、演示账号

| 账号 | 初始密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| manager | 123456 | 场长 |
| farmer | 123456 | 养殖员 |
| producer | 123456 | 生产员 |
| warehouse | 123456 | 库管员 |
| sales | 123456 | 销售员 |

来自原项目初始化数据；实际部署应修改初始密码。出库单创建人不能审核本人单据，需要另一个具有审核权限的账号。

## 六、重跑检查

后端：`cd backend` 后运行 `mvn test`，使用隔离 H2 数据库，不访问配置中的真实 MySQL。当前 30 项通过。

前端：先 `npm ci`、`npm run build`，再在第一个终端执行：

```powershell
npm run preview -- --host 127.0.0.1 --port 5178 --strictPort
```

第二个终端执行 `npm run test:ui`。缺省使用本机 Chrome；没有 Chrome 时，可执行 `npx playwright install chromium`，设置 `$env:PLAYWRIGHT_BROWSER='chromium'` 后再运行。端口不同时用 `QA_URL` 指定地址。结果和截图默认生成在 `frontend/tests/review-artifacts/`。检查通过拦截 API 使用模拟数据，不向真实后端写入。

当前浏览器检查 47 项通过，涵盖 32 个业务菜单及桌面/手机交互。真实 MySQL 导入、真实设备扫码和部署后的联调需在实际环境确认。

## 七、结构与业务规则

- `backend/`：后端实现及事务/权限检查。
- `frontend/`：前端实现、检查脚本和已构建的 dist。
- `sql/`：全新建库、演示数据、已有库升级、只读数据核对。
- `docs/`：本次报告、截图、检查摘要和原项目历史设计说明。

采收/产品/出库/采购编号分别为 HS/CP/CK/CG + 日期 + 4 位流水；溯源码采用 TR + UUID。采收校验休药期，装瓶校验重量和状态，出库审核校验质检、批次余额及库存，并由数据库事务维护流水。退出登录及改密会令该用户已有令牌失效。

旧版 UI 报告与 96 项历史检查结果保留供参考，本次最终结果以 `docs/review-evidence` 为准。

## GitHub 发布与地址配置

本项目发布到 https://github.com/jincheng804/demo 。旧服务器地址已从演示数据和文档中清理，演示扫码 IP 改用 RFC 5737 文档示例网段 192.0.2.0/24。

- 后端绑定地址、端口、数据库连接、数据库密码、JWT 密钥、溯源基础地址和允许的跨域来源均通过环境变量设置。示例见 `deploy/bee.env.example`；复制后填写本机或服务器配置，实际环境文件不提交到 GitHub。Spring Boot 不会自动读取 .env 文件，应在 IDEA 中配置环境变量，或由启动脚本/systemd 加载。
- 前端本地代理可复制 `frontend/.env.example` 为 `.env.local`，设置 `VITE_API_PROXY_TARGET` 和 `VITE_DEV_HOST`。前端环境变量不能存放数据库密码或 JWT 密钥。
- 演示数据的二维码保存为相对路径；输出二维码图片和查询链接时统一按当前 `BEE_TRACE_BASE_URL` 生成，因此已有数据库中的旧二维码网址不会把新下载的二维码导向原服务器。已印刷的旧二维码内容仍需由部署方通过旧域名跳转或重新印刷处理。
- GitHub 提交完整源码、锁文件、SQL、测试、说明和界面预览。前端 dist、依赖目录、target、本机环境文件及论文 Word 文档不上传；部署方克隆仓库后运行 `npm ci && npm run build` 和 `mvn clean package` 生成产物。此前提供的源码 ZIP 中包含的 dist 属于本地交付产物。
