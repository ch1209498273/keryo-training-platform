# Contributing

感谢参与医护培训平台的工程改进。

## 开发约定

- 开始前阅读 [`AGENTS.md`](AGENTS.md)、[`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) 和相关 Roadmap 条目；
- 一个任务保持一个清晰范围，不因技术债顺手扩大架构改造；
- 业务规则不明确时标记 `NEEDS BUSINESS CONFIRMATION`，不要由实现者猜测；
- 先补测试或复现证据，再进行最小修改；
- 不提交 Key、密码、Token、真实业务数据、日志、构建产物或本机路径。

## 本地验证

根据修改范围运行相关验证，至少包括对应的 Java、前端或 Python 测试。Baseline、API Smoke 和人工 UAT 的门禁不能用单元测试替代。命令和环境变量见 [`scripts/baseline/README.md`](scripts/baseline/README.md)。

## 提交与评审

提交信息应说明任务目的和影响范围。Pull Request 应包含：

- 背景与变更范围；
- 测试命令及结果；
- 数据库或配置影响；
- 已知限制和未完成事项；
- 是否需要人工 UAT 或业务确认。

不要把真实凭据粘贴到 Issue、Pull Request、截图或测试报告中。

