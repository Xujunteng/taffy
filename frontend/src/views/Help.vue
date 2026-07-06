<template>
  <div class="help-page">
    <h2 class="page-title">帮助中心</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-input
        v-model="keyword"
        placeholder="搜索帮助文章..."
        clearable
        size="large"
        @change="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </el-card>

    <!-- 分类和列表 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header><span>分类</span></template>
          <div class="category-list">
            <div
              v-for="cat in categories"
              :key="cat"
              class="category-item"
              :class="{ active: activeCategory === cat }"
              @click="activeCategory = cat"
            >
              {{ cat }}
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card>
          <template #header><span>帮助文章</span></template>
          <div class="article-list">
            <div
              v-for="article in filteredArticles"
              :key="article.id"
              class="article-item"
              @click="currentArticle = article"
            >
              <div class="article-title">{{ article.title }}</div>
              <div class="article-meta">
                <el-tag size="small">{{ article.category }}</el-tag>
                <span>{{ article.createdAt }}</span>
              </div>
            </div>
            <el-empty v-if="filteredArticles.length === 0" description="暂无相关文章" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文章详情对话框 -->
    <el-dialog v-model="showDialog" :title="currentArticle?.title" width="600px" destroy-on-close>
      <div class="article-content">
        {{ currentArticle?.content }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHelpArticles, searchHelpArticles } from '@/api/help'

const articles = ref([])
const keyword = ref('')
const activeCategory = ref('')
const currentArticle = ref(null)
const showDialog = ref(false)

const categories = computed(() => {
  const cats = new Set(articles.value.map(a => a.category))
  return Array.from(cats)
})

const filteredArticles = computed(() => {
  let list = articles.value
  if (activeCategory.value) {
    list = list.filter(a => a.category === activeCategory.value)
  }
  if (keyword.value) {
    list = list.filter(a => a.title.includes(keyword.value) || a.content.includes(keyword.value))
  }
  return list
})

const handleSearch = async () => {
  if (!keyword.value) {
    fetchArticles()
    return
  }
  try {
    const res = await searchHelpArticles(keyword.value)
    articles.value = res.data || []
  } catch {}
}

const fetchArticles = async () => {
  try {
    const res = await getHelpArticles()
    articles.value = res.data || []
  } catch {}
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
.category-list { max-height: 400px; overflow-y: auto; }
.category-item {
  padding: 10px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}
.category-item:hover, .category-item.active {
  background: #ecf5ff;
  color: #409EFF;
}
.article-list { max-height: 450px; overflow-y: auto; }
.article-item {
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}
.article-item:hover { background: #f5f7fa; }
.article-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 6px; }
.article-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #999; }
.article-content { white-space: pre-wrap; line-height: 1.8; color: #333; font-size: 14px; }
</style>