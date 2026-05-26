# Kuzhambu Workers

Python 能力支撑工程，用于承载 OCR、图像分析、AI 评估、数据清洗、知识图谱离线处理和批量导入导出辅助。

本目录不承载核心业务规则，不直接写入正式业务数据。

## Environment

- Python 版本固定为 3.10。
- 本地虚拟环境使用 `kuzhambu-workers/.venv/`。

## Invocation

- Java 主系统原则上通过 HTTP request 调用 Python worker。
- Worker 只提供能力计算接口。
- 权限、任务状态、审计、候选结果确认和最终数据写入由 Java 主系统负责。
