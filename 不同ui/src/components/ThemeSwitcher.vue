<template>
  <el-popover placement="bottom-end" :width="328" trigger="click" popper-class="theme-popper">
    <template #reference>
      <el-button :circle="compact" :text="compact" class="theme-trigger" aria-label="切换界面风格">
        <el-icon><Brush /></el-icon>
        <span v-if="!compact">{{ currentThemeInfo.name }}</span>
      </el-button>
    </template>

    <div class="theme-panel">
      <div class="theme-panel__head">
        <strong>界面风格</strong>
        <span>自动记住当前选择</span>
      </div>
      <div class="theme-grid">
        <button
          v-for="item in themes"
          :key="item.id"
          type="button"
          class="theme-option"
          :class="{ active: currentTheme === item.id }"
          @click="setTheme(item.id)"
        >
          <span class="theme-preview" :class="`preview-${item.id}`">
            <i class="preview-side"></i>
            <i class="preview-top"></i>
            <i class="preview-card one"></i>
            <i class="preview-card two"></i>
          </span>
          <span class="theme-copy">
            <strong>{{ item.name }}</strong>
            <small>{{ item.description }}</small>
          </span>
          <el-icon v-if="currentTheme === item.id" class="theme-check"><Select /></el-icon>
        </button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { useTheme } from '../theme'

defineProps({ compact: { type: Boolean, default: false } })
const { themes, currentTheme, currentThemeInfo, setTheme } = useTheme()
</script>

<style scoped>
.theme-trigger { gap: 7px; }
.theme-panel__head { display:flex; justify-content:space-between; align-items:flex-end; margin-bottom:12px; }
.theme-panel__head strong { font-size:15px; }
.theme-panel__head span { color:var(--muted); font-size:12px; }
.theme-grid { display:grid; gap:8px; }
.theme-option {
  position:relative; width:100%; display:grid; grid-template-columns:76px 1fr 20px; align-items:center; gap:10px;
  padding:9px; border:1px solid var(--border); border-radius:12px; background:var(--card); color:var(--text);
  text-align:left; cursor:pointer; transition:.18s ease;
}
.theme-option:hover { transform:translateY(-1px); border-color:var(--primary); }
.theme-option.active { border-color:var(--primary); box-shadow:0 0 0 2px var(--primary-soft); }
.theme-copy { min-width:0; display:flex; flex-direction:column; gap:3px; }
.theme-copy strong { font-size:13px; }
.theme-copy small { overflow:hidden; color:var(--muted); font-size:11px; white-space:nowrap; text-overflow:ellipsis; }
.theme-check { color:var(--primary); }
.theme-preview { position:relative; display:block; width:76px; height:46px; overflow:hidden; border:1px solid rgba(127,127,127,.2); border-radius:8px; background:#f5f7fb; }
.theme-preview i { position:absolute; display:block; }
.preview-side { inset:0 auto 0 0; width:18px; background:#fff; }
.preview-top { top:0; right:0; width:58px; height:10px; background:#fff; }
.preview-card { top:16px; height:12px; border-radius:3px; background:#fff; box-shadow:0 2px 6px rgba(0,0,0,.08); }
.preview-card.one { left:24px; width:20px; }
.preview-card.two { left:48px; width:22px; }
.preview-ocean { background:#eef3ff; }
.preview-ocean .preview-side,.preview-ocean .preview-top,.preview-ocean .preview-card { background:#fff; }
.preview-ocean .preview-side { border-right:3px solid #3366ff; }
.preview-midnight { background:#0b1020; }
.preview-midnight .preview-side,.preview-midnight .preview-top,.preview-midnight .preview-card { background:#151b2d; }
.preview-midnight .preview-side { border-right:2px solid #7c8cff; }
.preview-sakura { background:linear-gradient(135deg,#fce7f1,#efe4ff); }
.preview-sakura .preview-side,.preview-sakura .preview-top,.preview-sakura .preview-card { background:rgba(255,255,255,.72); }
.preview-matcha { background:#f7f3e8; }
.preview-matcha .preview-side,.preview-matcha .preview-top,.preview-matcha .preview-card { background:#fdfbf5; }
.preview-matcha .preview-side { border-right:2px solid #4f7a63; }
.preview-cyber { background:#07090f; border-radius:2px; }
.preview-cyber .preview-side,.preview-cyber .preview-top,.preview-cyber .preview-card { background:#101521; border:1px solid #00e5ff; border-radius:1px; box-shadow:0 0 5px rgba(0,229,255,.55); }
.preview-cyber .preview-card.two { border-color:#ff3dbf; }
</style>
