<template>
  <ContentWrap>
    <!-- 引导提示：引导完善所属中心与职位信息 -->
    <el-alert
      v-if="!form.deptCode"
      type="warning"
      :closable="false"
      show-icon
      title="您尚未设置所属中心"
      description="请在下方的「所属中心」中选择您所在的血透中心（选项由管理员统一维护）；职位如需调整请联系管理员。"
      style="margin-bottom: 20px"
    />
    <el-alert
      v-if="roleTip"
      type="info"
      :closable="false"
      show-icon
      :title="roleTip"
      style="margin-bottom: 20px"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户编号">
            <el-input v-model="form.id" autocomplete="off" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职位/角色">
            <el-tag :type="roleTags.type" effect="light">{{ roleTags.name }}</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属中心" prop="deptCode">
            <el-select
              v-model="form.deptCode"
              filterable
              clearable
              placeholder="请选择所属中心"
              style="width: 100%"
            >
              <el-option
                v-for="d in deptOptions"
                :key="d.value"
                :label="d.label"
                :value="d.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="登录账号" prop="userName">
            <el-input v-model="form.userName" autocomplete="off" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" autocomplete="off" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="手机号码" prop="mobile">
            <el-input v-model="form.mobile" autocomplete="off" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" autocomplete="off" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户头像" prop="avatar">
            <file-uploader v-model="form.avatar" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div>
      <el-button type="primary" @click="handleSave(formRef)">保存</el-button>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ContentWrap } from '@/components/ContentWrap'
import { computed, onMounted, reactive, ref, unref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { infoApi, updateApi } from '@/api/sys/user'
import { treeApi } from '@/api/sys/depart'
import type { UserDataType } from '../types'
import { useUserStoreWithOut } from '@/store/modules/user'
import { FileUploader } from '@/plugins/uploader'

const userInfo = useUserStoreWithOut().getUserInfo

const form = ref<UserDataType>({})
const formRef = ref<FormInstance>()
const rules = reactive<FormRules>({
  realName: [
    {
      required: true,
      message: '姓名必须输入',
      trigger: 'blur'
    }
  ]
})

// 部门（中心）树 → 扁平选项（管理员在部门管理中维护）
const deptOptions = ref<{ value: string; label: string }[]>([])
const flatDepts = (nodes: any[], prefix = '') => {
  nodes.forEach((n) => {
    const label = prefix + (n.text || n.deptName || n.value || '')
    deptOptions.value.push({ value: n.value, label })
    if (n.children?.length) {
      flatDepts(n.children, label + ' / ')
    }
  })
}
const loadDepts = () => {
  treeApi({}).then((res: any) => {
    deptOptions.value = []
    const data = res.data || []
    flatDepts(data)
  })
}

// 角色 id → 中文名 映射
const ROLE_NAMES: Record<string, string> = {
  admin: '超级管理员',
  keryo_director: '总监',
  keryo_supervisor: '区域督导',
  keryo_assistant: '助理管理员',
  keryo_head_nurse: '医护长',
  keryo_team_leader: '小组长',
  keryo_nurse: '一线医护（普通学员）',
  user: '学员（已停用）'
}

const roleTags = computed(() => {
  const roles: string[] = form.value.roles || []
  if (!roles.length) {
    return { name: '未分配', type: 'danger' }
  }
  // 取第一个有效角色展示
  const valid = roles.filter((r) => r && r !== 'user')
  const name = ROLE_NAMES[valid[0] || roles[0]] || valid[0] || roles[0]
  const type = valid.length === 0 ? 'danger' : 'success'
  return { name, type }
})

const roleTip = computed(() => {
  const roles: string[] = form.value.roles || []
  if (!roles.length) {
    return '您还没有分配职位（角色），请联系管理员为您分配职位。'
  }
  if (roles.length === 1 && roles[0] === 'user') {
    return '当前职位为已停用的「学员」角色，请联系管理员为您调整为普通学员（一线医护）或其他职位。'
  }
  return ''
})

const handleSave = (formEl: FormInstance | undefined) => {
  if (!formEl) return

  formEl.validate((valid) => {
    if (valid) {
      const formData = unref(form)
      updateApi(formData).then(() => {
        ElMessage({
          showClose: true,
          message: '操作成功！',
          type: 'success'
        })

        loadInfo()
      })
    }
  })
}

// 获取用户信息
const loadInfo = () => {
  infoApi({ token: userInfo?.token }).then((res) => {
    form.value = res.data
  })
}

onMounted(() => {
  loadInfo()
  loadDepts()
})
</script>

