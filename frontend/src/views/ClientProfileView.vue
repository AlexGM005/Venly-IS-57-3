<script setup lang="ts">
import ProfileCard from '@/components/ProfileCard.vue';
import DisabledActionButton from '@/components/DisabledActionButton.vue';
import type { Profile } from '@/types/profile';

defineProps<{ profile: Profile }>();
</script>

<template>
  <div>
    <header class="mb-8 text-center">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Mi cuenta</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">Mi perfil</h1>
      <p class="mt-2 text-venly-muted">Estos son los datos registrados en tu cuenta.</p>
    </header>
    <ProfileCard :profile="profile">
      <template #actions>
        <DisabledActionButton label="Modificar perfil" />
      </template>
    </ProfileCard>
  </div>
</template>
