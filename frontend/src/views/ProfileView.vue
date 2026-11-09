<script setup lang="ts">
import ClientProfileView from '@/views/ClientProfileView.vue';
import StaffProfileView from '@/views/StaffProfileView.vue';
import { useSession } from '@/composables/useSession';

const { currentUser, isStaff } = useSession();
</script>

<template>
  <section v-if="currentUser" class="mx-auto max-w-3xl animate-fade-up">
    <RouterLink to="/menu" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-accent">
      ← Volver al menú
    </RouterLink>
    <StaffProfileView v-if="isStaff" :current-user="currentUser" />
    <ClientProfileView v-else :profile="currentUser" />
  </section>
</template>
