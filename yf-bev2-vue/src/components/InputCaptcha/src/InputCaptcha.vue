<template>
  <div :class="[prefixCls, `${prefixCls}--${configGlobal?.size}`]">
    <el-input v-bind="$attrs" v-model="valueRef.captchaValue" type="text" />
    <el-image :src="imageUrl" @click="changeCode" />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useConfigGlobal } from '@/hooks/web/useConfigGlobal'
import { useDesign } from '@/hooks/web/useDesign'
import { v4 as uuidv4 } from 'uuid'
import { CAPTCHA_EXPIRE_MS } from './captchaExpiry'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('input-captcha')

const imageUrl = ref<String>('')

const props = defineProps({
  modelValue: propTypes.object.def({
    captchaKey: '',
    captchaValue: ''
  })
})

const { configGlobal } = useConfigGlobal()

const emit = defineEmits(['update:modelValue'])

// 输入框的值
const valueRef = ref(props.modelValue)
let expiryTimer: ReturnType<typeof setTimeout> | undefined

const scheduleRefresh = () => {
  if (expiryTimer) {
    clearTimeout(expiryTimer)
  }
  expiryTimer = setTimeout(() => {
    changeCode()
  }, CAPTCHA_EXPIRE_MS)
}

const changeCode = () => {
  // 生成新的key
  const key = uuidv4()
  valueRef.value.captchaKey = key
  valueRef.value.captchaValue = ''
  imageUrl.value = `${import.meta.env.VITE_API_HOST}/api/common/captcha/gen?key=${key}`
  scheduleRefresh()
}

defineExpose({ refresh: changeCode })

// 监听
watch(
  () => valueRef.value,
  (val: object) => {
    emit('update:modelValue', val)
  }
)

// 初始化验证码
onMounted(() => {
  changeCode()
})

onBeforeUnmount(() => {
  if (expiryTimer) {
    clearTimeout(expiryTimer)
  }
})
</script>

<style lang="less" scoped>
@prefix-cls: ~'@{namespace}-input-captcha';

.@{prefix-cls} {
  display: flex;
  align-items: center;

  .el-image {
    cursor: pointer;
    margin-left: 5px;
  }

  :deep(.@{elNamespace}-input__clear) {
    margin-left: 5px;
  }

  &--mini > &__bar {
    border-radius: var(--el-border-radius-small);
  }
}
</style>

