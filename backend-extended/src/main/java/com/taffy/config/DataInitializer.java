package com.taffy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM help_articles", Integer.class);
            // 首次启动或旧版本只有 6 篇种子文章时，重新写入完整帮助文档
            if (count != null && count <= 6) {
                jdbcTemplate.update("DELETE FROM help_articles");
                seed(jdbcTemplate);
                log.info("已写入 {} 篇帮助文章(种子数据)", count);
            } else {
                log.info("帮助文章表已存在 {} 条数据, 跳过种子插入", count);
            }
        } catch (Exception e) {
            log.warn("数据库未就绪,跳过种子数据插入: {}", e.getMessage());
        }
    }

    private void seed(JdbcTemplate db) {
        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "平台快速入门",
            "欢迎使用 Taffy 直播助手！本平台帮助你用 AI 声音进行直播。\n\n"
            + "完整使用流程：\n"
            + "① 注册账号 → 登录平台\n"
            + "② 进入「声音管理」→ 点击「上传声音」→ 选择音频文件（支持 WAV/MP3/M4A）→ 填写模型名称\n"
            + "③ 上传后自动提交训练 → 等待状态变为「就绪」（约 5-30 秒）\n"
            + "④ 进入「TTS 转换」→ 下拉选择已训练好的声音模型 → 输入直播话术文本\n"
            + "⑤ 调整语速和音调参数 → 点击「合成语音」→ 试听或下载音频\n"
            + "⑥ 在「直播控制」页面配置 OBS 连接 → 开始 AI 语音直播\n\n"
            + "提示：首次使用建议先录制 5-15 分钟的清晰人声样本，训练效果更好。",
            "入门指南", 1);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "如何录制优质音频样本",
            "音频质量直接影响声音模型的训练效果和 TTS 合成音质。以下是录制建议：\n\n"
            + "【环境要求】\n"
            + "• 选择安静的房间，关闭门窗，避免空调、风扇等背景噪音\n"
            + "• 避免空旷房间产生回声，可在房间内放置布艺物品吸音\n\n"
            + "【设备建议】\n"
            + "• 推荐使用外接麦克风（USB 麦克风或领夹麦），效果远好于笔记本内置麦克风\n"
            + "• 手机录音也是不错的选择（iPhone 语音备忘录 / Android 录音机）\n"
            + "• 麦克风距离嘴巴 15-30 厘米，避免喷麦\n\n"
            + "【录制要点】\n"
            + "• 时长：建议 5-15 分钟，最短不低于 1 分钟\n"
            + "• 语速：用你平时直播的正常语速，不要太快或太慢\n"
            + "• 内容：可以说一段你喜欢的话、读新闻、或者模拟直播场景\n"
            + "• 音量：保持音量稳定，不要忽大忽小\n"
            + "• 情绪：用自然的语气，就像在跟朋友聊天\n\n"
            + "【格式支持】\n"
            + "• WAV（推荐，无损质量）\n"
            + "• MP3 / M4A / AAC（会自动转换后分析）\n"
            + "• 文件大小上限：50MB\n\n"
            + "注意：男声和女声会被自动识别，匹配对应的 EdgeTTS 神经网络音色。",
            "入门指南", 2);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "声音训练与音色匹配",
            "上传音频后，系统会自动进行声音分析和训练。以下是训练过程说明：\n\n"
            + "【训练流程】\n"
            + "1. 音频预处理：非 WAV 格式会自动用 ffmpeg 转为标准 16kHz 单声道 WAV\n"
            + "2. 音高分析：逐帧分析音频的基频（pitch），提取 25% 低音分位数作为参考\n"
            + "3. 性别判定：综合 maleRatio（男声帧占比）+ p25 低音底线 双重判定\n"
            + "4. 音色匹配：根据音高从 EdgeTTS 中文神经网络音色库中匹配最接近的声音\n"
            + "5. 保存模型：将音色特征（pitchHz、brightness、RMS）保存到数据库\n\n"
            + "【可用音色列表】\n"
            + "女声：晓伊（活泼高音）、晓晓（温暖低音）\n"
            + "男声：云希（活力阳光）、云夏（可爱）、云扬（专业可靠）、云健（激情）\n\n"
            + "【训练状态】\n"
            + "• 训练中：正在分析音频特征\n"
            + "• 就绪：训练完成，可用于 TTS 合成\n"
            + "• 失败：训练出错（通常是因为音频文件丢失或格式异常）\n\n"
            + "提示：训练完成后，可在 TTS 页面选择模型试听效果。不满意可重新上传音频再次训练。",
            "声音管理", 3);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "TTS 语音合成指南",
            "TTS（Text-to-Speech）将文字转换为自然语音，支持多种音色和参数调节。\n\n"
            + "【使用步骤】\n"
            + "1. 进入「TTS 转换」页面\n"
            + "2. 在「声音模型」下拉框中选择已训练好的模型（不选则使用默认女声）\n"
            + "3. 在文本框中输入或粘贴直播话术（最长 2000 字）\n"
            + "4. 调节语速和音调参数\n"
            + "5. 点击「合成语音」→ 等待 3-10 秒 → 试听结果\n"
            + "6. 满意后可下载 WAV 音频文件\n\n"
            + "【参数说明】\n"
            + "语速（Speed）：0.5 ~ 2.0，默认 1.0\n"
            + "  • 0.5-0.8：慢速，适合产品详细介绍\n"
            + "  • 1.0-1.3：正常，适合日常直播聊天\n"
            + "  • 1.5-2.0：快速，适合促销抢购场景\n\n"
            + "音调（Pitch）：0.5 ~ 2.0，默认 1.0\n"
            + "  • 0.5-0.8：低沉，声音更成熟稳重\n"
            + "  • 1.0-1.3：正常，保持原始音色\n"
            + "  • 1.5-2.0：尖锐，声音更活泼年轻\n\n"
            + "【合成历史】\n"
            + "所有合成记录自动保存在右侧「合成历史」中，可随时下载或重新试听。",
            "功能说明", 4);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "直播脚本管理",
            "脚本管理帮助你提前准备好直播话术，开播时快速调用。\n\n"
            + "【创建脚本】\n"
            + "1. 进入「脚本管理」页面 → 点击「新建脚本」\n"
            + "2. 填写标题（如：618 大促开场话术）\n"
            + "3. 选择分类（开场/产品介绍/互动/结束等）\n"
            + "4. 编写脚本内容 → 保存\n\n"
            + "【使用技巧】\n"
            + "• 按直播流程分段编写（开场白 → 产品介绍 → 互动 → 结束语）\n"
            + "• 使用 TTS 预览功能试听效果，调整措辞\n"
            + "• 标注语气提示（如「语速放慢」「此处停顿」）方便直播时把握节奏\n"
            + "• 定期更新脚本，保持内容新鲜\n\n"
            + "【脚本分类建议】\n"
            + "• 开场白：欢迎语、今日主题介绍\n"
            + "• 产品介绍：商品卖点、优惠信息\n"
            + "• 互动话术：引导点赞、关注、评论\n"
            + "• 结束语：感谢观看、下次预告",
            "功能说明", 5);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "OBS 直播配置完整教程",
            "通过 OBS WebSocket 连接，平台可以直接将 TTS 合成的语音推送到直播间。\n\n"
            + "【安装 OBS】\n"
            + "1. 下载 OBS Studio：https://obsproject.com/\n"
            + "2. 安装并启动 OBS\n\n"
            + "【安装 WebSocket 插件】（OBS 28+ 已内置，可跳过）\n"
            + "1. 下载 obs-websocket：https://github.com/obsproject/obs-websocket/releases\n"
            + "2. 安装后重启 OBS\n\n"
            + "【配置步骤】\n"
            + "1. OBS 菜单栏 → 工具 → WebSocket 服务器设置\n"
            + "2. 勾选「启用 WebSocket 服务器」\n"
            + "3. 设置服务器端口（默认 4455）\n"
            + "4. 设置密码（可选但建议设置）\n"
            + "5. 点击「确定」保存\n\n"
            + "【连接平台】\n"
            + "1. 在本平台「直播控制」页面输入 OBS WebSocket 信息\n"
            + "2. 主机地址：默认 localhost（本机）或 OBS 所在 IP\n"
            + "3. 端口：默认 4455\n"
            + "4. 密码：你在 OBS 中设置的密码\n"
            + "5. 点击「连接」→ 状态显示已连接即可使用\n\n"
            + "【使用流程】\n"
            + "1. 选择直播平台和脚本\n"
            + "2. 点击「开始直播」\n"
            + "3. 平台自动将 TTS 合成的语音推送到 OBS\n"
            + "4. OBS 将音频混入直播流，观众即可听到 AI 语音",
            "直播集成", 6);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "如何进行声音评价",
            "声音评价功能让你对平台上的声音模型进行评分和评论，帮助其他用户发现好声音。\n\n"
            + "【提交评价】\n"
            + "1. 进入「声音评价」页面\n"
            + "2. 在「声音模型」下拉框中选择要评价的模型\n"
            + "3. 点击星星打分（1-5 星）\n"
            + "4. 填写评价内容（可选，最长 500 字）\n"
            + "5. 选择是否公开你的姓名：\n"
            + "   • 打开「显示姓名」→ 评价会显示你的用户名\n"
            + "   • 关闭 → 显示为「匿名用户」\n"
            + "6. 点击「提交评价」\n\n"
            + "【浏览评价】\n"
            + "• 所有评价对全部用户公开可见\n"
            + "• 可按声音模型筛选查看\n"
            + "• 每条评价显示评分、内容、评价人和声音模型名\n\n"
            + "【模型分享】\n"
            + "• 看到感兴趣的声音模型？点击评价卡片上的模型名标签\n"
            + "• 在弹出的对话框中点击「添加到我的声音管理」\n"
            + "• 模型会自动复制到你的账号，可在 TTS 中直接使用",
            "入门指南", 7);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "数据统计面板说明",
            "数据统计帮助你了解平台使用情况和直播效果。\n\n"
            + "【统计指标说明】\n"
            + "• 声音模型数量：你已创建的声音模型总数\n"
            + "• TTS 合成次数：累计文本转语音的合成次数\n"
            + "• 评价数量：收到和发出的评价总数\n"
            + "• 脚本数量：你创建的直播脚本数量\n"
            + "• 直播场次：已完成的直播会话数量\n\n"
            + "【使用建议】\n"
            + "• 定期查看统计，了解哪些模型使用频率最高\n"
            + "• 根据 TTS 合成次数判断话术的热度\n"
            + "• 评价分数低的模型可考虑重新训练优化\n\n"
            + "提示：数据统计页面实时更新，刷新即可查看最新数据。",
            "功能说明", 8);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "EdgeTTS 神经网络语音说明",
            "本平台使用微软 Edge TTS 神经网络语音引擎，提供免费高质量的中文语音合成。\n\n"
            + "【技术特点】\n"
            + "• 神经网络语音：基于深度学习，发音自然流畅\n"
            + "• 完全免费：无需付费 API Key\n"
            + "• 低延迟：合成速度快，适合直播场景\n"
            + "• 离线可用：需要首次联网下载语音模型\n\n"
            + "【中文女声】\n"
            + "• 晓伊（zh-CN-XiaoyiNeural）：活泼开朗，适合年轻化直播\n"
            + "• 晓晓（zh-CN-XiaoxiaoNeural）：温暖亲切，适合生活类直播\n\n"
            + "【中文男声】\n"
            + "• 云希（zh-CN-YunxiNeural）：活力阳光，适合游戏/体育直播\n"
            + "• 云夏（zh-CN-YunxiaNeural）：可爱活泼，适合二次元/娱乐\n"
            + "• 云扬（zh-CN-YunyangNeural）：专业可靠，适合新闻/科技直播\n"
            + "• 云健（zh-CN-YunjianNeural）：激情澎湃，适合促销/带货直播\n\n"
            + "【音色自动匹配】\n"
            + "上传你的声音样本后，系统会自动分析音高特征，从上述音色中选择最接近的一个。\n"
            + "男声样本 → 匹配男声音色库；女声样本 → 匹配女声音色库。",
            "技术参考", 9);

        db.update("INSERT INTO help_articles (title, content, category, sort_order) VALUES (?, ?, ?, ?)",
            "常见问题 FAQ",
            "Q：训练需要多长时间？\n"
            + "A：通常 5-30 秒即可完成。系统分析的是音色特征而非深度学习训练，速度很快。\n\n"
            + "Q：支持哪些音频格式？\n"
            + "A：WAV（推荐）、MP3、M4A、AAC、OGG。非 WAV 格式会自动用 ffmpeg 转换后分析。\n\n"
            + "Q：为什么我的男声被识别为女声？\n"
            + "A：系统使用多特征联合判定（男性帧占比 + 低音底线分析）。如果误判，请检查录音是否包含高声片段。建议录制时保持自然低沉语调，避免尖声或背景女声。\n\n"
            + "Q：音频文件大小限制是多少？\n"
            + "A：上传限制 50MB。建议录制 5-15 分钟即可，过长的音频对效果提升有限。\n\n"
            + "Q：TTS 合成的音频可以商用吗？\n"
            + "A：EdgeTTS 合成的音频可用于个人直播。商业用途请参考微软服务条款。\n\n"
            + "Q：可以同时运行多个直播吗？\n"
            + "A：可以。每个直播会话独立管理，互不影响。\n\n"
            + "Q：忘记了密码怎么办？\n"
            + "A：目前请联系管理员重置密码。后续版本会加入自助找回密码功能。\n\n"
            + "Q：如何提高合成音质？\n"
            + "A：① 使用高质量录音样本 ② 调节语速和音调到合适值 ③ 保持话术文本通顺流畅。",
            "常见问题", 10);
    }
}
