# Taffy 多风格 UI

本版本在不修改业务 API 和页面功能的前提下，加入了 5 套可切换界面风格：

1. 云海蓝：清爽专业的默认后台风格
2. 深夜直播：低眩光深色控制台风格
3. 樱花玻璃：粉紫渐变与毛玻璃风格
4. 抹茶米色：低饱和自然暖色风格
5. 赛博霓虹：深黑、高对比、霓虹科技风格

## 使用方式

- 登录/注册页：右上角点击“界面风格”按钮。
- 登录后后台：顶部工具栏点击当前主题名称。
- 选择结果保存在浏览器 localStorage 中，刷新或重新登录后仍会保留。

## 主要文件

- `src/theme.js`：主题列表、当前主题状态和本地保存逻辑
- `src/components/ThemeSwitcher.vue`：主题选择器与预览卡片
- `src/style.css`：5 套主题变量及 Element Plus 全局适配
- `src/App.vue`：登录页和后台顶部切换入口

## 新增主题

在 `src/theme.js` 添加主题信息，并在 `src/style.css` 添加：

```css
html[data-theme="your-theme"] {
  --bg: ...;
  --card: ...;
  --primary: ...;
  /* 其余变量参考现有主题 */
}
```

然后为 `ThemeSwitcher.vue` 中的预览添加 `.preview-your-theme` 样式即可。
