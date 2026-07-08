<template>
  <section class="galaxy-hero">
    <div class="sea"></div>
    <div class="galaxy-line"></div>
    <div class="star s1"></div><div class="star s2"></div><div class="star s3"></div><div class="star s4"></div>
    <div class="hero-content">
      <div class="hero-kicker">星辰大海 · AI VOICE LIVE</div>
      <h1>让每一次直播，都像驶入一条会发光的星河。</h1>
      <p>统一管理声音训练、TTS 合成、直播话术、评价反馈与统计分析，面向联调和展示都能直接使用。</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="$router.push('/tts')">开始语音合成</el-button>
        <el-button size="large" @click="$router.push('/live')">进入直播面板</el-button>
      </div>
    </div>
    <div class="planet"><span></span></div>
  </section>
</template>
<style scoped>
.galaxy-hero { min-height: 360px; border-radius: 34px; position: relative; overflow: hidden; padding: 42px; border: 1px solid rgba(255,255,255,.22); box-shadow: 0 35px 120px rgba(0,0,0,.45); background: radial-gradient(circle at 72% 28%, rgba(125,211,252,.28), transparent 24%), linear-gradient(135deg, rgba(11,24,67,.92), rgba(38,20,76,.76)); }
.sea { position:absolute; inset:auto -10% -32% -10%; height: 54%; background: radial-gradient(ellipse at center, rgba(45,212,191,.22), transparent 64%), repeating-radial-gradient(ellipse at center, rgba(125,211,252,.28) 0 1px, transparent 2px 16px); filter: blur(.2px); transform: perspective(360px) rotateX(58deg); animation: seaMove 10s linear infinite; }
.galaxy-line { position:absolute; left:-8%; right:-8%; top:28%; height:120px; transform: rotate(-8deg); background: linear-gradient(90deg, transparent, rgba(255,255,255,.32), rgba(125,211,252,.36), rgba(167,139,250,.30), transparent); filter: blur(14px); }
.hero-content { position:relative; z-index:2; max-width: 700px; }
.hero-kicker { color:#7dd3fc; letter-spacing:.22em; font-size: 12px; font-weight: 900; margin-bottom: 12px; }
h1 { margin:0; font-size: clamp(34px, 5vw, 62px); line-height: 1.02; font-weight: 950; letter-spacing:-.06em; background: linear-gradient(90deg,#fff,#d7f1ff 45%,#e9ddff 72%,#ffe6b8); -webkit-background-clip:text; background-clip:text; color:transparent; }
p { max-width: 610px; color:#bdd0f3; font-size: 16px; line-height:1.85; margin: 20px 0 28px; }
.hero-actions { display:flex; gap:12px; flex-wrap:wrap; }
.planet { position:absolute; right:54px; bottom:38px; width:128px; height:128px; border-radius:50%; background: radial-gradient(circle at 34% 28%, #fff 0 7%, #7dd3fc 8% 28%, #5b21b6 50%, #0f172a 78%); box-shadow: 0 0 60px rgba(125,211,252,.42); }
.planet::before { content:""; position:absolute; left:-38px; right:-38px; top:50%; height:18px; border-radius:50%; transform: rotate(-18deg); background: linear-gradient(90deg, transparent, rgba(255,255,255,.7), transparent); }
.star { position:absolute; border-radius:50%; background:#fff; box-shadow:0 0 18px #fff; }
.s1 { width:5px; height:5px; top:52px; right:240px; animation: twinkle 1.8s infinite; }
.s2 { width:4px; height:4px; top:120px; right:160px; animation: twinkle 2.4s infinite; }
.s3 { width:6px; height:6px; top:70px; left:60px; animation: twinkle 2s infinite; }
.s4 { width:3px; height:3px; bottom:90px; left:45%; animation: twinkle 1.6s infinite; }
@keyframes twinkle { 50% { opacity:.25; transform: scale(.6); } }
@keyframes seaMove { to { background-position: 160px 0, 120px 120px; } }
@media (max-width: 820px) { .galaxy-hero { padding:28px; min-height:420px; } .planet { opacity:.45; right:-20px; bottom:18px; } }
</style>
