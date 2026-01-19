<template>
  <div class="dashboard">

    <!-- ✅ 统计卡片 -->
    <el-row :gutter="16" class="card-row">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div>
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon class="stat-icon">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ✅ 图表区：最近7天播放趋势 -->
    <el-card class="chart-card" shadow="never">
      <div class="chart-title">播放趋势（最近 7 天）</div>

      <div class="chart" v-loading="loading">
        <svg viewBox="0 0 600 200" class="line-chart">
          <polyline
              :points="linePoints"
              fill="none"
              stroke="#409EFF"
              stroke-width="3"
          />
          <circle
              v-for="(p, i) in pointList"
              :key="i"
              :cx="p.x"
              :cy="p.y"
              r="5"
              fill="#409EFF"
          />
        </svg>

        <div class="chart-labels">
          <span v-for="d in trendDays" :key="d">{{ d }}</span>
        </div>
      </div>
    </el-card>

    <!-- ✅ 最近新增歌曲 -->
    <el-card class="table-card" shadow="never">
      <div class="table-title">最近新增歌曲</div>
      <el-table :data="latestSongs" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="songName" label="歌曲名" />
        <el-table-column prop="singerName" label="歌手" width="140" />
        <el-table-column prop="playCount" label="播放量" width="120" />
        <el-table-column label="新增时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ✅ 最近操作日志 -->
    <el-card class="table-card" shadow="never">
      <div class="table-title">最近操作日志</div>
      <el-table :data="latestLogs" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="adminUserName" label="管理员" width="140" />
        <el-table-column prop="module" label="模块" width="140" />
        <el-table-column prop="action" label="动作" min-width="160" />
        <el-table-column prop="method" label="方法" width="90" />
        <el-table-column prop="path" label="路径" min-width="200" />
        <el-table-column label="IP" width="140">
          <template #default="{ row }">
            {{ normalizeIp(row.ip) }}
          </template>
        </el-table-column>
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.success) === 1 ? 'success' : 'danger'">
              {{ Number(row.success) === 1 ? "成功" : "失败" }}
            </el-tag>

          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { getDashboard } from "@/api/admin/dashboard"

import {
  UserFilled,
  Headset,
  Collection,
  Mic
} from "@element-plus/icons-vue"

const loading = ref(false)

// 后端返回：vo.stats
const statsData = ref({
  userCount: 0,
  songCount: 0,
  albumCount: 0,
  singerCount: 0,
  playCount7d: 0,
})

// 后端返回：vo.playTrend7d
// 期望格式：[{ day: "01-12", playCount: 123 }, ...] 或 {date, cnt} 都行，下面会兼容
const playTrend = ref([])

// 后端返回：vo.latestSongs
const latestSongs = ref([])

// 后端返回：vo.latestLogs
const latestLogs = ref([])

const stats = computed(() => ([
  { title: "用户数量", value: statsData.value.userCount, icon: UserFilled },
  { title: "歌曲总数", value: statsData.value.songCount, icon: Headset },
  { title: "专辑总数", value: statsData.value.albumCount, icon: Collection },
  { title: "歌手总数", value: statsData.value.singerCount, icon: Mic },
]))



// /** 把趋势行统一成 days + values */
// const trendDays = computed(() => {
//   return playTrend.value.map(x => x.day || x.date || x.d || "")
// })

const trendDays = computed(() => {
  return playTrend.value.map(x => formatDayLabel(x.day || x.date || x.d || ""))
})


const trendValues = computed(() => {
  return playTrend.value.map(x =>
      Number(x.playCount ?? x.cnt ?? x.count ?? 0)
  )
})

// ✅ SVG 折线点
const pointList = computed(() => {
  const values = trendValues.value.length ? trendValues.value : [0,0,0,0,0,0,0]
  const max = Math.max(...values, 1)

  return values.map((v, i) => {
    const x = (600 / (values.length - 1)) * i
    const y = 180 - (v / max) * 160
    return { x, y }
  })
})

const linePoints = computed(() =>
    pointList.value.map(p => `${p.x},${p.y}`).join(" ")
)

function formatDayLabel(s) {
  if (!s) return ""
  const str = String(s)
  if (str.length >= 10 && str.includes("-")) return str.slice(5, 10)
  return str
}


function normalizeIp(ip) {
  if (!ip) return ""
  if (ip === "0:0:0:0:0:0:0:1" || ip === "::1") return "127.0.0.1"
  if (String(ip).startsWith("::ffff:")) return String(ip).substring(7)
  return ip
}

function formatDateTime(v) {
  if (!v) return ""
  // 兼容 2026-01-12T12:12:12
  return String(v).replace("T", " ").slice(0, 19)
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getDashboard()
    const d = res.data || {}

    statsData.value = d.stats || statsData.value
    playTrend.value = d.playTrend7d || []
    latestSongs.value = d.latestSongs || []
    latestLogs.value = d.latestLogs || []
  } catch (e) {
    ElMessage.error("仪表盘数据加载失败")
    console.error(e)
  } finally {
    loading.value = false
  }
}




onMounted(loadDashboard)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-row {
  margin-bottom: 10px;
}

.stat-card {
  border-radius: 10px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #333;
}

.stat-icon {
  font-size: 36px;
  color: #409EFF;
}

.chart-card {
  border-radius: 10px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.chart {
  width: 100%;
  height: 240px;
}

.line-chart {
  width: 100%;
  height: 200px;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 8px;
  font-size: 12px;
  color: #666;
}

.table-card {
  border-radius: 10px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
