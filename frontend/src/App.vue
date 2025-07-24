<!--
  @fileoverview Main application component that handles routing and state management for benchmark results.
  @component App
  @author Eva Ray
  @description This component serves as the entry point for the application, managing the routing between different pages
-->

<script setup>
import { ref, computed, provide } from 'vue'
import HomePage from './components/pages/HomePage.vue'
import ResultsPage from './components/pages/ResultsPage.vue'
import NotFound from './components/pages/NotFound.vue'

const routes = {
  '/': HomePage,
  '/results': ResultsPage
}

const currentPath = ref(window.location.hash)

window.addEventListener('hashchange', () => {
  currentPath.value = window.location.hash
})

const currentView = computed(() => {
  return routes[currentPath.value.slice(1) || '/'] || NotFound
})

// State management for benchmark results
const benchmarkResults = ref({})

const setBenchmarkResults = (results) => {
  benchmarkResults.value = results
}

// Provide the results and setter to child components
provide('benchmarkResults', benchmarkResults)
provide('setBenchmarkResults', setBenchmarkResults)
</script>

<template>
  <main>
    <component :is="currentView" />
  </main>
</template>

<style>
@media print {
  .workload-plot-content {
    display: grid !important;
    grid-template-columns: repeat(3, 1fr) !important;
    gap: 24px !important;
  }
  * {
    page-break-inside: avoid !important;
  }
}
</style>