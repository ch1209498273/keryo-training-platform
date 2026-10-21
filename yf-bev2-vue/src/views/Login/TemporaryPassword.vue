<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="w-[100%]"
    hide-required-asterisk
    label-position="top"
    size="large"
  >
    <el-form-item>
      <h2 class="text-2xl font-bold text-center w-[100%]">首次登录/密码过期</h2>
      <p class="text-13px text-center w-[100%] color-[var(--el-text-color-secondary)] mt-5px">
        请设置新的登录密码，完成后返回登录页重新登录
      </p>
    </el-form-item>

    <el-form-item label="新密码" prop="newPassword">
      <el-input
        v-model="form.newPassword"
        placeholder="至少8位，包含字母和数字"
        show-password
        clearable
        type="password"
      />
    </el-form-item>

    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input
        v-model="form.confirmPassword"
        placeholder="请再次输入新密码"
        show-password
        clearable
        type="password"
      />
    </el-form-item>

    <el-form-item>
      <el-button :loading="loading" class="w-[100%]" type="primary" @click="submit(formRef)">
        完成改密
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import request from '@/config/axios'

const route = useRoute()
const { replace } = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value || value.length < 8 || value.length > 30 || !/^[A-Za-z0-9]+$/.test(value) || !/[A-Za-z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('密码至少8位且必须同时包含字母和数字'))
    return
  }
  callback()
}

const validateConfirm = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  callback(value === form.newPassword ? undefined : new Error('两次输入的密码不一致'))
}

const rules = {
  newPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

const submit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await request.post({
        url: '/api/sys/user/temporary-password',
        data: {
          token: String(route.query.token || ''),
          newPassword: form.newPassword,
          confirmPassword: form.confirmPassword
        }
      })
      ElMessage.success('密码修改成功，请重新登录')
      await replace({ name: 'Login' })
    } finally {
      loading.value = false
    }
  })
}
</script>

