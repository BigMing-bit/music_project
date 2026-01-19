<template>
  <div class="page">
    <div class="panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="评论/用户/歌曲" clearable />
        </el-form-item>
        <el-form-item label="回复状态">
          <el-select v-model="query.replied" placeholder="全部" clearable style="width: 140px;">
            <el-option label="未回复" :value="0" />
            <el-option label="已回复" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="query.targetType" placeholder="选择目标类型" clearable style="width: 140px;">
            <el-option label="歌曲" :value="1" />
            <el-option label="歌单" :value="2" />
            <el-option label="专辑" :value="3" />
            <!-- 可以根据实际的 targetType 进行填充 -->
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="danger" :disabled="!selectedIds.length" @click="onBatchDelete">
          批量删除
        </el-button>
      </div>
      <div class="toolbar-right">共 <span class="total">{{ total }}</span> 条</div>
    </div>

    <div class="panel">
      <el-table
          :data="list"
          border
          stripe
          v-loading="loading"
          :header-cell-style="headerStyle"
          @selection-change="onSelect"
          :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" align="center" sortable />
        <el-table-column prop="userNickName" label="用户" align="center" width="140" />
        <el-table-column label="目标类型" width="100" align="center">
          <template #default="{ row }">
            <span>{{ getTargetTypeName(row.targetType) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="targetName" label="目标名称" align="center" min-width="160">
          <template #default="{ row }">
            <span>{{ row.targetName }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="评论内容" align="center" min-width="260" show-overflow-tooltip />
        <el-table-column label="回复" min-width="220" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <div>
              <span v-if="row.replyContent">{{ row.replyContent }}</span>
              <el-tag v-else type="warning">未回复</el-tag>
            </div>
          </template>
        </el-table-column>


        <el-table-column label="时间" align="center" width="180">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
            <el-button size="small" type="primary" @click="openReply(row)">
              回复
            </el-button>
            <el-button size="small" type="danger" @click="onDelete(row.id)">
              删除
            </el-button>
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

    <!-- 查看弹窗 -->
    <el-dialog v-model="detailVisible" title="评论详情" width="620px">
      <div class="drow"><b>用户：</b>{{ detail.userNickName || detail.userId }}</div>
      <div class="drow"><b>评论：</b>{{ detail.content }}</div>
      <div class="drow"><b>回复：</b>{{ detail.replyContent || "未回复" }}</div>
      <div class="drow"><b>管理员：</b>{{ detail.replyAdminName || detail.replyAdminId }}</div>
      <div class="drow"><b>评论时间：</b>{{ fmt(detail.createTime) }}</div>
      <div class="drow" v-if="detail.replyTime"><b>回复时间：</b>{{ fmt(detail.replyTime) }}</div>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复评论" width="620px">
      <div style="margin-bottom: 10px;">
        <div class="drow"><b>评论内容：</b>{{ currentRow?.content }}</div>
        <div class="drow" v-if="currentRow?.replyContent"><b>已有回复：</b>{{ currentRow?.replyContent }}</div>
      </div>

      <el-input
          v-model="replyForm.replyContent"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="请输入回复内容（最多500字）"
      />

      <template #footer>
        <el-button @click="replyVisible=false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  getCommentPage,
  replyComment,
  deleteComment,
  deleteComments
} from "@/api/admin/comment"

const detail = ref({})
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])
const DEFAULT_PAGE_SIZE = Number(localStorage.getItem("COMMENT_PAGE_SIZE")) || 10

const headerStyle = {
  backgroundColor: "#f7f8fa",
  color: "#333",
  fontWeight: 600
}

const query = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: "",
  replied: null, // 1已回复 0未回复 null全部
  targetType: null, // 新增目标类型查询
  targetId: null // 新增目标ID查询
})

function fmt(v) {
  if (!v) return ""
  return String(v).replace("T", " ").slice(0, 19)
}

function getTargetTypeName(targetType) {
  switch (targetType) {
    case 1:
      return "歌曲"
    case 2:
      return "歌单"
    case 3:
      return "专辑"
    default:
      return ""
  }
}
async function reset() {
  query.keyword = ""
  query.replied = null
  query.pageNum = 1
  query.targetType = null // 重置目标类型
  query.targetId = null // 重置目标ID
  await loadData()
}

async function loadData() {
  loading.value = true
  try {
   const  res = await getCommentPage(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function pageChange(page) {
  query.pageNum = page
  loadData()
}

function sizeChange(size) {
  query.pageSize = size
  query.pageNum = 1
  localStorage.setItem("COMMENT_PAGE_SIZE", String(size))
  loadData()
}


function onSelect(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function onDelete(id) {
  await ElMessageBox.confirm("确定删除该评论吗？删除后不可恢复", "提示", { type: "warning" })
  await deleteComment(id)
  ElMessage.success("删除成功")
  await loadData()
}

async function onBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条评论吗？`, "提示", { type: "warning" })
  await deleteComments(selectedIds.value)
  ElMessage.success("删除成功")
  await loadData()
}

const detailVisible = ref(false)

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

const replyVisible = ref(false)
const replyLoading = ref(false)
const currentRow = ref(null)
const replyForm = ref({ replyContent: "" })

function openReply(row) {
  currentRow.value = row
  replyForm.value.replyContent = row.replyContent || ""
  replyVisible.value = true
}

async function submitReply() {
  if (!currentRow.value) return
  if (!replyForm.value.replyContent || !replyForm.value.replyContent.trim()) {
    ElMessage.warning("回复内容不能为空")
    return
  }
  replyLoading.value = true
  try {
    await replyComment(currentRow.value.id, replyForm.value.replyContent.trim())
    ElMessage.success("回复成功")
    replyVisible.value = false
    await loadData()
  } finally {
    replyLoading.value = false
  }
}


onMounted(loadData)
</script>

<style scoped>
.drow { margin: 8px 0; color: #333; }

.page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel
{
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.query-form
{
  margin-bottom: -12px;
}

.toolbar {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.toolbar-right
{
  font-size: 14px;
  color: #666;
}
/* ✅ 分页 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
