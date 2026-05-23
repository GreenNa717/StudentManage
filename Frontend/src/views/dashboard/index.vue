<script setup lang="ts">
import { computed, nextTick, onActivated, onMounted, ref, watch } from 'vue'
import { getDashboardOverview, getDepartmentStudentCount, getScoreDistribution } from '../../api/dashboard'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'

use([CanvasRenderer, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const appStore = useAppStore()
const userStore = useUserStore()
const overview = ref({ studentCount: 0, teacherCount: 0, courseCount: 0, departmentCount: 0 })
const deptChartOption = ref({})
const scoreChartOption = ref({})
const deptData = ref<any[]>([])
const scoreData = ref<any[]>([])

function getCSSVar(name: string) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}

function buildDeptOption() {
  if (!deptData.value.length) return
  deptChartOption.value = {
    tooltip: {},
    grid: { left: 60, right: 20, bottom: 40, top: 20 },
    xAxis: { type: 'category', data: deptData.value.map((d: any) => d.departmentName), axisLabel: { color: getCSSVar('--text-secondary') } },
    yAxis: { type: 'value', axisLabel: { color: getCSSVar('--text-secondary') } },
    series: [{
      type: 'bar',
      data: deptData.value.map((d: any) => d.studentCount),
      itemStyle: { color: getCSSVar('--primary'), borderRadius: [4, 4, 0, 0] },
      barWidth: '40%',
    }],
  }
}

function buildScoreOption() {
  if (!scoreData.value.length) return
  scoreChartOption.value = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: getCSSVar('--text-secondary') } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: scoreData.value.map((d: any) => ({ name: d.range, value: d.count })),
      label: { color: getCSSVar('--text-regular') },
      itemStyle: { borderRadius: 6 },
    }],
  }
}

watch(() => appStore.theme, () => {
  nextTick(() => {
    buildDeptOption()
    buildScoreOption()
  })
})

watch(() => appStore.darkMode, () => {
  nextTick(() => {
    buildDeptOption()
    buildScoreOption()
  })
})

const statCards = computed(() => {
  if (userStore.role === 'TEACHER') {
    return [
      { key: 'student', label: '关联学生数', desc: '本人课程覆盖学生数', value: overview.value.studentCount, icon: 'Avatar', color: '#409eff' },
      { key: 'teacher', label: '我的教师档案', desc: '当前登录教师账号', value: overview.value.teacherCount, icon: 'User', color: '#67c23a' },
      { key: 'course', label: '我的课程数', desc: '本人授课课程数量', value: overview.value.courseCount, icon: 'Notebook', color: '#e6a23c' },
      { key: 'department', label: '所属院系数', desc: '当前账号关联院系', value: overview.value.departmentCount, icon: 'OfficeBuilding', color: '#6366f1' },
    ]
  }
  if (userStore.role === 'STUDENT') {
    return [
      { key: 'student', label: '我的学生档案', desc: '当前登录学生账号', value: overview.value.studentCount, icon: 'Avatar', color: '#409eff' },
      { key: 'teacher', label: '授课教师数', desc: '与本人课程相关的教师数', value: overview.value.teacherCount, icon: 'User', color: '#67c23a' },
      { key: 'course', label: '我的课程数', desc: '本人已有成绩记录的课程数', value: overview.value.courseCount, icon: 'Notebook', color: '#e6a23c' },
      { key: 'department', label: '所属院系数', desc: '当前账号关联院系', value: overview.value.departmentCount, icon: 'OfficeBuilding', color: '#6366f1' },
    ]
  }
  return [
    { key: 'student', label: '学生总数', desc: '已注册在籍学生', value: overview.value.studentCount, icon: 'Avatar', color: '#409eff' },
    { key: 'teacher', label: '教师总数', desc: '在职教职工人数', value: overview.value.teacherCount, icon: 'User', color: '#67c23a' },
    { key: 'course', label: '课程总数', desc: '本学期开课数量', value: overview.value.courseCount, icon: 'Notebook', color: '#e6a23c' },
    { key: 'department', label: '院系总数', desc: '下设院系单位', value: overview.value.departmentCount, icon: 'OfficeBuilding', color: '#6366f1' },
  ]
})
const departmentTitle = computed(() => (userStore.role === 'ADMIN' ? '各院系学生人数' : '关联院系学生人数'))
const departmentDesc = computed(() => {
  if (userStore.role === 'TEACHER') {
    return '展示当前教师所属院系的学生规模'
  }
  if (userStore.role === 'STUDENT') {
    return '展示当前学生所属院系的学生规模'
  }
  return '展示各院系在籍学生分布情况'
})
const scoreTitle = computed(() => (userStore.role === 'STUDENT' ? '我的成绩分布' : '成绩分布'))
const scoreDesc = computed(() => {
  if (userStore.role === 'TEACHER') {
    return '本人授课课程成绩区间占比'
  }
  if (userStore.role === 'STUDENT') {
    return '本人各课程成绩区间占比'
  }
  return '全部课程成绩区间占比'
})

let dataLoaded = false

async function loadData() {
  try {
    const res1: any = await getDashboardOverview()
    overview.value = res1.data

    const res2: any = await getDepartmentStudentCount()
    deptData.value = res2.data
    buildDeptOption()

    const res3: any = await getScoreDistribution()
    scoreData.value = res3.data
    buildScoreOption()
    dataLoaded = true
  } catch (e) {
    console.error('Dashboard load failed:', e)
  }
}

onMounted(() => {
  if (!dataLoaded) loadData()
})

onActivated(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card" :style="{ '--card-color': card.color }">
          <div class="stat-icon">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-desc">{{ card.desc }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ departmentTitle }}</span>
              <span class="chart-desc">{{ departmentDesc }}</span>
            </div>
          </template>
          <v-chart :option="deptChartOption" style="height: 320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ scoreTitle }}</span>
              <span class="chart-desc">{{ scoreDesc }}</span>
            </div>
          </template>
          <v-chart :option="scoreChartOption" style="height: 320px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: var(--card-bg);
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  border: 1px solid var(--card-border);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: color-mix(in srgb, var(--card-color) 12%, transparent);
  color: var(--card-color);
  flex-shrink: 0;
}

[data-theme="classic"] .stat-icon {
  border-radius: 50%;
}

[data-theme="premium"] .stat-icon {
  border-radius: 16px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-desc {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

.chart-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
