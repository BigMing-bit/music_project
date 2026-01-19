<template>
  <div class="page">
    <!-- 查询栏 -->
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="菜单名称">
          <el-input v-model="query.keyword" placeholder="请输入菜单名称" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="openDialog()">新增菜单</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>
      <div class="toolbar-right">共 <span class="total">{{ total }}</span> 条</div>
    </div>

    <!-- 表格 -->
    <div class="panel">
      <el-table
          :data="list"
          stripe
          border
          v-loading="loading"
          element-loading-text="加载中..."
          @selection-change="handleSelectionChange"
          :header-cell-style="headerStyle"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="menuName" label="菜单名称" min-width="160" align="center" />
        <el-table-column prop="menuType" label="菜单类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 1">目录</el-tag>
            <el-tag v-else-if="row.menuType === 2">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 3">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图标" width="120" align="center">
          <template #default="{ row }">
            <div style="display:flex; align-items:center; justify-content:center; gap:6px;">
              <el-icon v-if="row.icon && icons[row.icon]">
                <component :is="icons[row.icon]" />
              </el-icon>
              <span v-if="row.icon">{{ row.icon }}</span>
              <span v-else style="color:#999;">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="权限标识" width="200" align="center" />
        <el-table-column prop="path" label="路径" min-width="180" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="val => changeStatus(row.id, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
            layout="total, prev, pager, next, sizes, jumper"
            :total="total"
            :page-size="query.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :current-page="query.pageNum"
            @current-change="pageChange"
            @size-change="sizeChange"
        />
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="菜单名称" required>
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="权限标识" required>
          <el-input v-model="form.permissionCode" placeholder="请输入权限标识" />
        </el-form-item>

        <el-form-item label="菜单链接" required>
          <el-input v-model="form.path" placeholder="请输入菜单链接" />
        </el-form-item>

        <el-form-item label="菜单图标">
          <IconSelect v-model="form.icon" />
        </el-form-item>

        <el-form-item label="菜单类型">
          <el-select v-model="form.menuType" style="width: 180px">
            <el-option label="目录" :value="1" />
            <el-option label="菜单" :value="2" />
            <el-option label="按钮" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 如果你想在弹窗展示创建时间（不可编辑），就打开这一段 -->
        <!--
        <el-form-item label="创建时间">
          <el-input :model-value="formatTime(form.createTime)" disabled />
        </el-form-item>
        -->
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import {getMenuList, addMenu, updateMenu, deleteMenus, updateMenuStatus, deleteMenu} from "@/api/admin/menu"
import * as icons from "@element-plus/icons-vue"


import IconSelect from "@/components/IconSelect.vue"
import { useMenuStore } from "@/store/menu"
const menuStore = useMenuStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("MENU_PAGE_SIZE")) || 10
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: "", status: null })

const headerStyle = { backgroundColor: "#f7f8fa", color: "#333", fontWeight: 600 }


// ✅ 正确：defineProps 只调用一次
const props = defineProps({
  modelValue: {
    type: String,
    default: ""
  }
})
const emit = defineEmits(["update:modelValue"])

const keyword = ref("")
const names = Object.keys(icons)


const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return names
  return names.filter(n => n.toLowerCase().includes(k))
})


// ✅ 正确使用 props.modelValue
const CurrentIcon = computed(() => {
  return props.modelValue && icons[props.modelValue]
      ? icons[props.modelValue]
      : null
})
function select(name) {
  emit("update:modelValue", name)
}

function formatTime(t) {
  if (!t) return ""
  return String(t).replace("T", " ")
}

function loadData() {
  loading.value = true
  getMenuList(query)
      .then(res => {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
      })
      .finally(() => (loading.value = false))
}

function resetQuery() {
  query.keyword = ""
  query.status = null
  query.pageNum = 1
  loadData()
}

function pageChange(page) {
  query.pageNum = page
  loadData()
}

function sizeChange(size) {
  query.pageSize = size
  query.pageNum = 1
  localStorage.setItem("MENU_PAGE_SIZE", String(size))
  loadData()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function changeStatus(id, status) {
  updateMenuStatus(id, status).then(() => ElMessage.success("状态更新成功"))
}

function handleDelete(id) {
  ElMessageBox.confirm("确定删除该菜单吗？", "提示", { type: "warning" })
      .then(() => deleteMenu([id]))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

function handleBatchDelete() {
  ElMessageBox.confirm("确定批量删除选中的菜单吗？", "提示", { type: "warning" })
      .then(() => deleteMenus(selectedIds.value))
      .then(() => {
        ElMessage.success("删除成功")
        loadData()
      })
}

const dialogVisible = ref(false)
const dialogTitle = ref("新增菜单")

// ✅ form 字段统一为后端驼峰
const form = reactive({
  id: null,
  menuName: "",
  path: "",
  icon: "",
  permissionCode: "",
  menuType: 1,
  status: 1,
  createTime: "" // 仅展示用，可不传给后端
})

function resetForm() {
  Object.assign(form, {
    id: null,
    menuName: "",
    path: "",
    icon: "",
    permissionCode: "",
    menuType: 1,
    status: 1,
    createTime: ""
  })
}

function openDialog(row) {
  dialogVisible.value = true

  if (row?.id) {
    dialogTitle.value = "编辑菜单"

    // ✅ 直接用 row.menuName / row.menuType 回填
    Object.assign(form, {
      id: row.id,
      menuName: row.menuName ?? "",
      path: row.path ?? "",
      icon: row.icon ?? "",
      permissionCode: row.permissionCode ?? "",
      menuType: row.menuType ?? 1,
      status: row.status ?? 1,
      createTime: row.createTime ?? ""
    })
  } else {
    dialogTitle.value = "新增菜单"
    resetForm()
  }
}

async function submitForm() {
  const payload = {
    id: form.id,
    menuName: form.menuName,
    path: form.path,
    icon: form.icon,
    permissionCode: form.permissionCode,
    menuType: form.menuType,
    status: form.status,
    parentId: form.parentId, // 如果你有父级
    sort: form.sort          // 如果你有排序
  }

  const api = form.id ? updateMenu : addMenu
  await api(payload)

  ElMessage.success("保存成功")
  dialogVisible.value = false
  loadData()

  // ✅ 刷新左侧菜单
  await menuStore.refreshMenus()
}
onMounted(loadData)
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.panel { background:#fff; border-radius:8px; padding:16px; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.query-form { margin-bottom:-12px; }
.toolbar { background:#fff; border-radius:8px; padding:14px 16px; display:flex; justify-content:space-between; align-items:center; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.toolbar-left { display:flex; gap:10px; }
.toolbar-right { font-size:14px; color:#666; }
.total { font-weight:bold; color:#409eff; }
.pager { margin-top:16px; display:flex; justify-content:flex-end; }
</style>
