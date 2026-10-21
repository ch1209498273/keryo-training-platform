<template>
  <ResultOnlyScore v-if="mode !== 'full'" :paperId="paperId" />
  <ResultWithDetail v-else :paperId="paperId" />
</template>

<script lang="ts" setup>
import ResultOnlyScore from '@/views/Exam/Exam/Client/components/ResultOnlyScore.vue'
import ResultWithDetail from '@/views/Exam/Exam/Client/components/ResultWithDetail.vue'

import { useRoute } from 'vue-router'
const route = useRoute()
const paperId = typeof route.query.id === 'string' ? route.query.id : undefined
const mode = typeof route.query.mode === 'string' ? route.query.mode : undefined
</script>

