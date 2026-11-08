<template>
  <header class="bg-white shadow-sm border-b border-slate-200">
    <div class="max-w-5xl mx-auto px-4 py-4 flex items-center justify-between">
      <h1 class="text-xl font-semibold text-venly-dark">Venly · Release 1</h1>
      <nav class="flex gap-2">
        <RouterLink
          to="/crear-perfil"
          class="px-3 py-2 text-sm rounded-md hover:bg-slate-100"
          active-class="bg-venly-primary text-white hover:bg-venly-primary"
        >
          Crear perfil
        </RouterLink>
        <RouterLink
          to="/consultar-perfil"
          class="px-3 py-2 text-sm rounded-md hover:bg-slate-100"
          active-class="bg-venly-primary text-white hover:bg-venly-primary"
        >
          Consultar perfil
        </RouterLink>
      </nav>
    </div>
  </header>
</template>
