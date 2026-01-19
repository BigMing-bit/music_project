<template>
  <el-popover placement="bottom" width="520" trigger="click">
    <template #reference>
      <el-input
          :model-value="modelValue"
          placeholder="请选择图标"
          readonly
      >
        <template #prefix>
          <el-icon v-if="CurrentIcon"><component :is="CurrentIcon" /></el-icon>
        </template>
      </el-input>
    </template>

    <el-input v-model="keyword" placeholder="搜索图标" clearable />

    <div style="margin-top:10px; max-height:260px; overflow:auto;">
      <el-row :gutter="10">
        <el-col
            v-for="name in filtered"
            :key="name"
            :span="4"
            style="margin-bottom:10px;"
        >
          <el-tooltip :content="name" placement="top">
            <el-button
                style="width:100%;"
                @click="select(name)"
            >
              <el-icon><component :is="icons[name]" /></el-icon>
            </el-button>
          </el-tooltip>
        </el-col>
      </el-row>
    </div>
  </el-popover>
</template>

<script setup>
import { computed, ref } from "vue"
import * as icons from "@element-plus/icons-vue"

const props = defineProps({
  modelValue: { type: String, default: "" }
})
const emit = defineEmits(["update:modelValue"])

const keyword = ref("")

const names = Object.keys(icons)

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return names
  return names.filter(n => n.toLowerCase().includes(k))
})

const CurrentIcon = computed(() => (props.modelValue ? icons[props.modelValue] : null))

function select(name) {
  emit("update:modelValue", name)
}
</script>
