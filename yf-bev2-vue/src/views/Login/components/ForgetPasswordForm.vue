<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="dark:(border-1 border-[var(--el-border-color)] border-solid); w-[100%]"
    hide-required-asterisk
    label-position="left"
    label-width="70px"
    size="large"
  >
    <el-form-item>
      <h2 class="text-2xl font-bold text-center w-[100%]">忘记密码</h2>
      <p class="text-13px text-center w-[100%] color-[var(--el-text-color-secondary)] mt-5px">
        输入用户名确认身份后，请联系管理员为您重置密码
      </p>
    </el-form-item>

    <el-form-item label="用户名" prop="userName">
      <el-input v-model="form.userName" placeholder="请输入用户名" clearable type="text" />
    </el-form-item>

    <el-form-item label="真实姓名" prop="realName">
      <el-input v-model="form.realName" placeholder="请输入注册时的真实姓名" clearable type="text" />
    </el-form-item>

    <el-form-item label="验证码" prop="captchaValue">
      <input-captcha v-model="form" :placeholder="'验证码'" class="!w-full" />
    </el-form-item>

    <el-alert
      type="warning"
      :closable="false"
      show-icon
      title="密码由管理员重置"
      description="提交申请后，管理员可在「忘记密码申请」中看到您的用户名与姓名，核对身份后为您重置密码。"
      style="margin-bottom: 16px"
    />

    <el-form-item>
      <div class="w-[100%]">
        <el-button :loading="loading" class="w-[100%]" type="primary" @click="submit(formRef)">
          提交申请
        </el-button>
      </div>
      <div class="w-[100%] mt-15px">
        <el-button class="w-[100%]" @click="toLogin"> 返回登录 </el-button>
      </div>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import InputCaptcha from '@/components/InputCaptcha/src/InputCaptcha.vue'
import { apiForgetCheck } from '@/api/login'

const emit = defineEmits(['to-login'])

const form = reactive({
  userName: '',
  realName: '',
  captchaKey: '',
  captchaValue: ''
})
const formRef = ref<FormInstance>()
const loading = ref(false)

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  captchaValue: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const submit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl?.validate(async (isValid) => {
    if (isValid) {
      loading.value = true
      try {
        await apiForgetCheck({
          userName: form.userName.trim(),
          realName: form.realName.trim(),
          captchaKey: form.captchaKey,
          captchaValue: form.captchaValue
        })
        ElMessage.success('申请已提交，请等待管理员处理！')
        emit('to-login')
      } catch {
        // 错误提示由请求拦截器统一弹出
      } finally {
        loading.value = false
      }
    }
  })
}

const toLogin = () => {
  emit('to-login')
}
</script>

<style lang="less" scoped>
:deep(.el-form-item__label) {
  display: inline-block;
  width: 70px;
  margin-right: 10px;
  text-align-last: justify;
}
</style>

