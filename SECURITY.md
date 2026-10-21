# Security Policy

## 公共仓库边界

本仓库是 Portfolio 演示项目，不是生产环境。请不要提交以下内容：

- API Key、数据库密码、JWT Secret、OAuth Token、私钥或证书；
- 真实培训资料、患者/员工信息、生产数据库导出和运行日志；
- 含个人绝对路径的配置或诊断文件。

本地配置通过环境变量提供。提交前应检查 staged diff，而不是只检查工作区文件名。

## 发现疑似泄露

如果发现疑似凭据，请不要在公开 Issue 中粘贴原文：

1. 立即停止继续提交或传播；
2. 在凭据所属平台撤销并轮换；
3. 清理公开分支、提交和构建产物；
4. 使用私下渠道向项目负责人报告文件路径、提交号和影响范围。

公开仓库不提供生产级安全响应或 SLA。本项目的安全边界、已知限制和未来生产工作见 [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md) 与 [`docs/PORTFOLIO_RELEASE.md`](docs/PORTFOLIO_RELEASE.md)。

