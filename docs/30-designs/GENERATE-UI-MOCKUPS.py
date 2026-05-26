from pathlib import Path
from xml.sax.saxutils import escape


OUT = Path(__file__).with_name("UI-MOCKUPS")
OUT.mkdir(exist_ok=True)

W, H = 1440, 960
SIDEBAR = 260
TOP = 64

PALETTE = {
    "paper": "#F7F4EF",
    "panel": "#FFFDFC",
    "ink": "#28231F",
    "muted": "#756E66",
    "line": "#DDD5CB",
    "side": "#ECE7DD",
    "side2": "#F7F2E8",
    "sideText": "#3F4A43",
    "sideMuted": "#7D877E",
    "sideLine": "#D5CBBE",
    "sideActive": "#E7D9C5",
    "accent": "#8B3F2F",
    "accent2": "#55746C",
    "gold": "#B58A4A",
    "blue": "#435C75",
    "green": "#5B7A62",
    "chip": "#EFE8DE",
    "warn": "#B7603B",
}

NAV = [
    ("三才图会", "SANCAI"),
    ("王圻文档", "WANGQI"),
    ("明代习俗", "MING"),
    ("AI 精修", "AI"),
    ("视觉资产", "VISUAL"),
    ("搜索问答", "QA"),
    ("知识图谱", "GRAPH"),
    ("标签体系", "TAG"),
    ("系统管理", "OPS"),
]

MODULES = [
    {
        "file": "AI-CONFIG-PROMPT-REQUIREMENTS.svg",
        "active": "OPS",
        "title": "AI 配置与提示词",
        "crumb": "系统管理 / AI 配置",
        "primary": "模型映射与提示词状态",
        "stats": [("启用模型", "12"), ("提示词模板", "11"), ("可用动作", "28"), ("降级状态", "正常")],
        "tabs": ["模型列表", "能力映射", "提示词", "检测历史"],
        "list_title": "AI 功能动作",
        "rows": ["翻译 translate", "标签 tags", "图片理解 image_analysis", "知识图谱 knowledge_graph", "问答 qa"],
        "detail_title": "提示词版本对比",
        "detail": ["变量校验通过", "当前版本 v18", "影响范围：三才图会 / 翻译", "AI 优化建议待确认"],
        "right": ["主服务：可用", "备用服务：待命", "平均延迟 1.8s", "今日成本 32.6"],
    },
    {
        "file": "AI-REFINEMENT-REQUIREMENTS.svg",
        "active": "AI",
        "title": "AI 内容精修",
        "crumb": "工具 / AI 精修",
        "primary": "候选结果区",
        "stats": [("待确认", "24"), ("批量任务", "3"), ("失败重试", "2"), ("已应用", "186")],
        "tabs": ["翻译", "标签", "摘要", "问答对", "图片理解"],
        "list_title": "批量处理队列",
        "rows": ["天文卷 01 翻译", "王圻文档 摘要", "明代习俗 标签", "三才图会 图片理解", "长条目拆分"],
        "detail_title": "候选内容预览",
        "detail": ["用户确认前不写入正式内容", "可编辑后接受", "可拒绝并保留原文", "取消批量后保留已完成结果"],
        "right": ["loading 状态明确", "失败原因可读", "支持重试", "版本可追溯"],
    },
    {
        "file": "AUTH-REQUIREMENTS.svg",
        "active": "OPS",
        "title": "认证与权限",
        "crumb": "系统管理 / 权限",
        "primary": "账号派发与角色控制",
        "stats": [("admin", "2"), ("editor", "9"), ("viewer", "41"), ("禁用", "3")],
        "tabs": ["用户", "角色", "权限判断", "审计日志"],
        "list_title": "用户列表",
        "rows": ["陈校勘 editor", "李研究 viewer", "赵管理员 admin", "王编辑 editor", "禁用账号 003"],
        "detail_title": "权限覆盖",
        "detail": ["查看 / 编辑 / 删除", "导出 / 分享 / 批量操作", "搜索 / 问答上下文", "删除用户前二次确认"],
        "right": ["不提供自助注册", "HTTPS", "密码不明文保存", "关键操作审计"],
    },
    {
        "file": "DATA-REFINEMENT-REQUIREMENTS.svg",
        "active": "GRAPH",
        "title": "数据精修工作台",
        "crumb": "知识图谱 / 数据精修",
        "primary": "实体与关系人工确认",
        "stats": [("待精修", "76"), ("实体", "1,284"), ("关系", "2,931"), ("已确认", "68%")],
        "tabs": ["实体标注", "关系抽取", "待精修", "质量入口"],
        "list_title": "待精修门类",
        "rows": ["天文象纬", "地理山川", "人物品类", "器用制度", "草木鸟兽"],
        "detail_title": "关系修正",
        "detail": ["人工确认优先于 AI 初始结果", "删除实体前二次确认", "保存后更新图谱质量", "摘要与问答对不在此维护"],
        "right": ["按门类筛选", "可追溯保存", "质量报告入口", "服务三才图会"],
    },
    {
        "file": "KNOWLEDGE-GRAPH-REQUIREMENTS.svg",
        "active": "GRAPH",
        "title": "知识图谱",
        "crumb": "知识图谱 / 三才图会",
        "primary": "鸟瞰层 / 门类层 / 详情层",
        "stats": [("实体", "1,284"), ("关系", "2,931"), ("门类", "14"), ("世系关系", "275")],
        "tabs": ["鸟瞰", "门类", "详情", "质量报告", "世系图"],
        "list_title": "图谱质量",
        "rows": ["天文 92%", "地理 78%", "人物 85%", "器用 73%", "动物 69%"],
        "detail_title": "实体详情",
        "detail": ["来源条目可追溯", "已人工确认不被覆盖", "重生成保留人工结果", "搜索问答不依赖图谱"],
        "right": ["异步进度", "失败原因", "8 国 109 君", "低质量门类重提取"],
    },
    {
        "file": "MING-CUSTOMS-REQUIREMENTS.svg",
        "active": "MING",
        "title": "明代习俗",
        "crumb": "知识库 / 明代习俗",
        "primary": "习俗条目与标签云",
        "stats": [("习俗条目", "312"), ("标签", "86"), ("私有", "18"), ("导出", "7")],
        "tabs": ["列表", "标签云", "详情弹窗", "导出"],
        "list_title": "习俗列表",
        "rows": ["元旦朝贺", "乡饮酒礼", "市井饮食", "婚嫁礼俗", "岁时祭祀"],
        "detail_title": "详情弹窗",
        "detail": ["Markdown 安全渲染", "摘要与问答对内联维护", "公开/私有可见性", "删除前二次确认"],
        "right": ["标签云按频率", "关键词搜索", "HTML 设定集", "过期导出不可下载"],
    },
    {
        "file": "OPERATIONS-REQUIREMENTS.svg",
        "active": "OPS",
        "title": "运维管理",
        "crumb": "系统管理 / 运维",
        "primary": "运行状态与清理任务",
        "stats": [("访问量", "8,241"), ("AI 调用", "1,936"), ("备份", "30d"), ("异常", "4")],
        "tabs": ["仪表盘", "备份", "日志", "健康检查", "清理"],
        "list_title": "长任务状态",
        "rows": ["批量标签提取", "视觉资产导出", "手动备份", "知识图谱提取", "过期分享清理"],
        "detail_title": "运维指标",
        "detail": ["周报 / 月报导出", "恢复前创建快照", "日志保留 30 天", "admin 可用"],
        "right": ["健康检查", "运行指标", "热门内容", "搜索点击分析"],
    },
    {
        "file": "QA-REQUIREMENTS.svg",
        "active": "QA",
        "title": "智能问答",
        "crumb": "工具 / 智能问答",
        "primary": "多库问答与来源追溯",
        "stats": [("会话", "128"), ("来源命中", "94%"), ("上下文", "3 轮"), ("私有过滤", "开启")],
        "tabs": ["多库问答", "王圻追问", "会话历史", "调试"],
        "list_title": "会话列表",
        "rows": ["浑仪与天文观测", "王圻文档追问", "明代婚嫁习俗", "器用制度比较", "地理山川条目"],
        "detail_title": "回答来源",
        "detail": ["来源可跳转", "显示知识库 / 标题 / 位置", "删除或无权来源显示不可用", "知识图谱非前置"],
        "right": ["同义词扩展", "实体识别增强", "失败可重试", "上下文调试"],
    },
    {
        "file": "SANCAI-KNOWLEDGE-REQUIREMENTS.svg",
        "active": "SANCAI",
        "title": "三才图会知识库",
        "crumb": "知识库 / 三才图会",
        "primary": "门类 / 卷 / 条目三级浏览",
        "stats": [("门类", "14"), ("卷", "106"), ("条目", "3,462"), ("归档", "42")],
        "tabs": ["门类", "卷列表", "条目", "版本", "导出", "静态展示"],
        "list_title": "门类导航",
        "rows": ["天文", "地理", "人物", "宫室", "器用"],
        "detail_title": "条目详情",
        "detail": ["原文 / 译文 / 标签 / 配图", "摘要与问答对内联维护", "草稿 / 发布 / 归档", "自动保存用于防丢"],
        "right": ["50 / 100 / 200 分页", "当前卷内搜索", "展示页风险确认", "导出过期状态"],
    },
    {
        "file": "SEARCH-REQUIREMENTS.svg",
        "active": "QA",
        "title": "跨库搜索",
        "crumb": "工具 / 搜索",
        "primary": "三库统一检索",
        "stats": [("结果", "286"), ("三才", "178"), ("王圻", "63"), ("习俗", "45")],
        "tabs": ["全部", "三才图会", "王圻文档", "明代习俗"],
        "list_title": "分组结果",
        "rows": ["三才图会 / 浑仪", "王圻文档 / 天文考", "明代习俗 / 祭祀", "三才图会 / 地理", "明代习俗 / 饮食"],
        "detail_title": "查询增强",
        "detail": ["同义词扩展", "查询清洗与改写", "实体识别与链接", "返回搜索页保留查询状态"],
        "right": ["权限先过滤", "关键词高亮", "日志记录", "无结果提示"],
    },
    {
        "file": "SHARING-REQUIREMENTS.svg",
        "active": "QA",
        "title": "内容分享",
        "crumb": "工具 / 分享",
        "primary": "跨库只读分享链接",
        "stats": [("有效链接", "49"), ("公开", "31"), ("私有", "18"), ("过期", "7")],
        "tabs": ["创建", "我的分享", "访问统计", "风险确认"],
        "list_title": "分享目标",
        "rows": ["三才条目：浑仪", "王圻文档：天文考", "明代习俗：岁时祭祀", "三才条目：山川", "明代习俗：婚嫁"],
        "detail_title": "风险提示",
        "detail": ["公开分享私有内容需二次确认", "分享状态不改变内容私有状态", "撤销或过期后不可访问", "目标删除显示占位"],
        "right": ["单链接多内容", "批量创建", "访问统计", "CDN 不覆盖"],
    },
    {
        "file": "TAXONOMY-REQUIREMENTS.svg",
        "active": "TAG",
        "title": "统一标签与同义词",
        "crumb": "系统管理 / 标签体系",
        "primary": "跨库标签治理",
        "stats": [("待审核", "17"), ("标签", "523"), ("同义词", "383"), ("废弃", "21")],
        "tabs": ["待审核", "全部标签", "合并", "同义词", "统计"],
        "list_title": "待审核标签",
        "rows": ["浑天说", "六分仪", "岁时", "舆地图", "乡饮酒"],
        "detail_title": "治理操作",
        "detail": ["通过时选择分类", "拒绝退出可用集合", "合并保留别名", "废弃保留历史统计"],
        "right": ["Top 20", "知识库分布", "AI/人工占比", "月度新增趋势"],
    },
    {
        "file": "VISUAL-ASSET-REQUIREMENTS.svg",
        "active": "VISUAL",
        "title": "视觉资产",
        "crumb": "工具 / 视觉资产",
        "primary": "原图到视觉设定集",
        "stats": [("原图", "842"), ("AI 图", "319"), ("描述", "406"), ("导出", "11")],
        "tabs": ["原图", "图片理解", "信息融合", "视觉描述", "生图", "导出"],
        "list_title": "条目资产",
        "rows": ["浑仪图", "山川图", "宫室图", "器用图", "草木图"],
        "detail_title": "五步流程",
        "detail": ["原图管理", "图片理解结果使用", "文本与图片权重融合", "视觉描述与 AI 生图"],
        "right": ["区分原图/AI 图", "当前使用版本", "删除前二次确认", "过期导出不可下载"],
    },
    {
        "file": "WANGQI-DOCUMENT-REQUIREMENTS.svg",
        "active": "WANGQI",
        "title": "王圻文档",
        "crumb": "知识库 / 王圻文档",
        "primary": "文档阅读与时间线",
        "stats": [("文档", "214"), ("摘要", "168"), ("问答对", "522"), ("私有", "12")],
        "tabs": ["列表", "时间线", "详情", "追加问答", "导出"],
        "list_title": "文档列表",
        "rows": ["天文考", "舆地考", "礼仪考", "食货考", "职官考"],
        "detail_title": "文档详情",
        "detail": ["正文安全展示", "摘要/标签/问答对内联维护", "原始文件关联与替换", "单文档追加式问答"],
        "right": ["无草稿生命周期", "公开/私有", "文件缺失仍可读", "导出过期状态"],
    },
]


def text(x, y, value, size=22, fill=None, weight=400, anchor="start"):
    fill = fill or PALETTE["ink"]
    return (
        f'<text x="{x}" y="{y}" font-family="Noto Sans SC, Source Han Sans SC, '
        f'PingFang SC, sans-serif" font-size="{size}" font-weight="{weight}" '
        f'fill="{fill}" text-anchor="{anchor}">{escape(str(value))}</text>'
    )


def rect(x, y, w, h, fill, stroke="none", rx=8, sw=1):
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>'


def line(x1, y1, x2, y2, stroke=None, sw=1):
    stroke = stroke or PALETTE["line"]
    return f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{stroke}" stroke-width="{sw}"/>'


def chip(x, y, label, fill=None, color=None):
    fill = fill or PALETTE["chip"]
    color = color or PALETTE["ink"]
    width = max(64, len(label) * 13 + 24)
    return rect(x, y, width, 30, fill, rx=15) + text(x + 14, y + 21, label, 14, color, 500)


def wrap_label(label, max_chars=14):
    if len(label) <= max_chars:
        return [label]
    return [label[:max_chars], label[max_chars : max_chars * 2]]


def sidebar(active):
    parts = [rect(0, 0, SIDEBAR, H, PALETTE["side"], rx=0)]
    parts.append(rect(0, 0, SIDEBAR, 64, PALETTE["side2"], rx=0))
    parts.append(line(SIDEBAR, 0, SIDEBAR, H, PALETTE["sideLine"]))
    parts.append(text(28, 41, "三才翰典", 24, PALETTE["sideText"], 700))
    parts.append(text(28, 88, "知识库", 13, PALETTE["sideMuted"], 500))
    y = 110
    for label, key in NAV:
        is_active = key == active
        if is_active:
            parts.append(rect(16, y - 24, 228, 42, PALETTE["sideActive"], stroke="#D4BFA1", rx=6))
            parts.append(rect(16, y - 24, 4, 42, PALETTE["accent"], rx=2))
        parts.append(text(34, y + 2, label, 16, PALETTE["ink"] if is_active else PALETTE["sideText"], 600 if is_active else 400))
        y += 48
        if y == 254:
            parts.append(text(28, y + 8, "工具与管理", 13, PALETTE["sideMuted"], 500))
            y += 42
    parts.append(line(28, 870, 232, 870, PALETTE["sideLine"]))
    parts.append(rect(28, 890, 204, 46, "#F7F2E8", stroke=PALETTE["sideLine"], rx=8))
    parts.append(text(44, 915, "内容优先 · 学术极简", 14, PALETTE["sideText"], 600))
    parts.append(text(44, 932, "古朴亮色 left bar", 11, PALETTE["sideMuted"], 400))
    return "".join(parts)


def topbar(module):
    parts = [rect(SIDEBAR, 0, W - SIDEBAR, TOP, PALETTE["panel"], rx=0), line(SIDEBAR, TOP, W, TOP)]
    parts.append(text(292, 40, module["crumb"], 16, PALETTE["muted"], 500))
    parts.append(rect(760, 17, 300, 30, "#F2EEE8", stroke=PALETTE["line"], rx=15))
    parts.append(text(782, 38, "搜索条目、文档、标签、来源", 14, "#91887F"))
    parts.append(rect(1114, 16, 92, 32, "#F3E7DA", rx=16))
    parts.append(text(1135, 38, "编辑中", 14, PALETTE["accent"], 600))
    parts.append(rect(1230, 12, 40, 40, "#E9DFD2", stroke=PALETTE["line"], rx=20))
    parts.append(text(1250, 38, "陈", 18, PALETTE["ink"], 700, "middle"))
    parts.append(text(1284, 29, "avatar-head", 12, PALETTE["muted"], 500))
    parts.append(text(1284, 46, "admin", 11, "#9A9288", 400))
    return "".join(parts)


def module_svg(module):
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">',
        rect(0, 0, W, H, PALETTE["paper"], rx=0),
        sidebar(module["active"]),
        topbar(module),
    ]
    x0 = SIDEBAR + 32
    parts.append(text(x0, 118, module["title"], 34, PALETTE["ink"], 700))
    parts.append(text(x0, 150, module["primary"], 17, PALETTE["muted"], 400))

    stat_w = 196
    for i, (label, value) in enumerate(module["stats"]):
        x = x0 + i * (stat_w + 18)
        parts.append(rect(x, 178, stat_w, 86, PALETTE["panel"], stroke=PALETTE["line"], rx=8))
        parts.append(text(x + 20, 210, label, 14, PALETTE["muted"], 500))
        parts.append(text(x + 20, 247, value, 28, PALETTE["ink"], 700))

    tab_x = x0
    for i, tab in enumerate(module["tabs"]):
        fill = "#E9DED0" if i == 0 else "#F4F0EA"
        color = PALETTE["accent"] if i == 0 else PALETTE["muted"]
        parts.append(chip(tab_x, 292, tab, fill, color))
        tab_x += max(78, len(tab) * 14 + 36)

    parts.append(rect(x0, 344, 360, 462, PALETTE["panel"], stroke=PALETTE["line"], rx=8))
    parts.append(text(x0 + 24, 382, module["list_title"], 20, PALETTE["ink"], 700))
    y = 422
    for idx, row in enumerate(module["rows"]):
        parts.append(rect(x0 + 20, y - 24, 320, 52, "#F8F5F0" if idx != 0 else "#F1E6DA", stroke="#ECE3D8", rx=6))
        parts.append(text(x0 + 38, y + 7, row, 16, PALETTE["ink"] if idx == 0 else PALETTE["muted"], 600 if idx == 0 else 400))
        y += 62

    mid_x = x0 + 386
    parts.append(rect(mid_x, 344, 484, 462, PALETTE["panel"], stroke=PALETTE["line"], rx=8))
    parts.append(text(mid_x + 24, 382, module["detail_title"], 22, PALETTE["ink"], 700))
    parts.append(rect(mid_x + 24, 410, 436, 160, "#F4EFE8", stroke="#E7DCCF", rx=8))
    parts.append(text(mid_x + 48, 456, "主内容区", 18, PALETTE["accent"], 700))
    parts.append(text(mid_x + 48, 488, "统一页面骨架，模块内容独立成文", 15, PALETTE["muted"], 400))
    parts.append(text(mid_x + 48, 524, "left-side / head-bar / avatar-head 保持一致", 15, PALETTE["muted"], 400))
    y = 628
    for item in module["detail"]:
        parts.append(rect(mid_x + 24, y - 24, 436, 42, "#FAF8F4", stroke="#EEE7DE", rx=6))
        parts.append(rect(mid_x + 38, y - 10, 8, 8, PALETTE["accent2"], rx=4))
        parts.append(text(mid_x + 58, y + 4, item, 15, PALETTE["ink"], 500))
        y += 52

    right_x = mid_x + 510
    parts.append(rect(right_x, 344, 274, 462, PALETTE["panel"], stroke=PALETTE["line"], rx=8))
    parts.append(text(right_x + 24, 382, "状态与约束", 20, PALETTE["ink"], 700))
    y = 424
    colors = [PALETTE["green"], PALETTE["blue"], PALETTE["gold"], PALETTE["accent"]]
    for idx, item in enumerate(module["right"]):
        parts.append(rect(right_x + 24, y - 24, 226, 50, "#F8F5F0", stroke="#EEE7DE", rx=6))
        parts.append(rect(right_x + 40, y - 6, 10, 10, colors[idx % len(colors)], rx=5))
        label_lines = wrap_label(item, 13)
        parts.append(text(right_x + 62, y + (0 if len(label_lines) == 1 else -6), label_lines[0], 14, PALETTE["ink"], 600))
        if len(label_lines) > 1:
            parts.append(text(right_x + 62, y + 14, label_lines[1], 12, PALETTE["muted"], 400))
        y += 64
    parts.append(line(right_x + 24, 700, right_x + 250, 700))
    parts.append(text(right_x + 24, 738, "文件小于 800K", 14, PALETTE["muted"], 500))
    parts.append(text(right_x + 24, 766, "SVG 效果图", 14, PALETTE["muted"], 500))

    parts.append(rect(x0, 836, 1152, 66, "#EFE7DC", stroke=PALETTE["line"], rx=8))
    parts.append(text(x0 + 24, 876, "统一视觉规则：内容优先、学术极简、工具型体验；页面结构统一，模块能力独立。", 17, PALETTE["ink"], 600))
    parts.append("</svg>")
    return "".join(parts)


def main():
    for module in MODULES:
        (OUT / module["file"]).write_text(module_svg(module), encoding="utf-8")


if __name__ == "__main__":
    main()
