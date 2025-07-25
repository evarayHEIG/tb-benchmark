<!--
  @fileoverview UniqueQueryResults component that displays the results of a unique query benchmark.
  @component UniqueQueryResults
  @author Eva Ray
  @description This component displays the results of a unique query benchmark, including execution times, execution
  plans, and index information. It uses child components to render the results in a structured format.
  The style has been generated by an LLM.
-->
<script setup>
import { computed } from 'vue'
import ExecutionTimePlot from './ExecutionTimePlot.vue'
import TextCard from './TextCard.vue'
import BenchmarkResultsTable from './BenchmarkResultsTable.vue'
import IndexInfoTable from './IndexInfoTable.vue'

const props = defineProps({
  results: {
    type: Object,
    default: () => ({})
  }
})


/**
 * Transform the nested results into a flat array for table display.
 */
const flattenedResults = computed(() => {
  return Object.entries(props.results).map(([dbName, result]) => ({
    dbName,
    queryName: 'Query', // Pour une requête unique
    avgExecutionTime: result?.avgExecutionTime,
    queryPerSecond: result?.queryPerSecond,
    initialConnectionTime: result?.initialConnectionTime,
    standardDeviation: result?.standardDeviation,
    variance: result?.variance,
    percentile95: result?.percentile95,
    cacheHitsRatio: result?.cacheInfo?.hitsRatio || 0,
    cacheHits: result?.cacheInfo?.hits || 0,
    cacheMisses: result?.cacheInfo?.misses || 0,
    explainPlan: result?.explainPlan
  }))
})

const chartData = computed(() => {
    return Object.entries(props.results).map(([dbName, result]) => ({
        x: dbName,
        y: result?.avgExecutionTime || 0
    }))
})

/**
 * Extracts index information from the results.
 * @returns {Array} An array of index information objects.
 */
const indexInfo = computed(() => {
  const indexData = []

  Object.entries(props.results).forEach(([dbName, result]) => {
    if (result?.indexInfo && Array.isArray(result.indexInfo)) {
      result.indexInfo.forEach(index => {
        indexData.push({
          dbName,
          ...index
        })
      })
    }
  })

  return indexData
})

/**
 * Extracts execution plans and queries from the results.
 * @returns {Array} An array of objects containing database names and their execution plans.
 */
const executionPlansAndQueries = computed(() => {
  return Object.entries(props.results)
      .filter(([_, result]) => result?.explainPlan)
      .map(([dbName, result]) => ({
        dbName,
        explainPlan: result.explainPlan,
        query: result.query || ''
      }))
})
</script>

<template>
  <div v-if="results && Object.keys(results).length > 0" class="results-container">
    <h2 class="results-title">Benchmark Results</h2>

    <!-- Utilisation du composant BenchmarkResultsTable -->
    <BenchmarkResultsTable :results="flattenedResults" />

    <!-- Graphique -->
    <div>
      <h3>Avg Execution Time Comparison</h3>
      <ExecutionTimePlot :data="chartData" />
    </div>

    <!-- Plans d'exécution -->
    <div class="execution-plans-section">
      <div class="execution-plans-grid">
        <div v-for="plan in executionPlansAndQueries" :key="plan.dbName">
          <h3>{{ plan.dbName }}</h3>
          <TextCard :title="'Query'" :content="plan.query"/>
          <TextCard :title="'Execution Plan'" :content="plan.explainPlan"/>
        </div>
      </div>
    </div>

    <!-- Table des informations d'index si disponible -->
    <IndexInfoTable v-if="indexInfo.length > 0" :indexInfo="indexInfo" />
  </div>
</template>

<style scoped>
.results-container {
  background: #f6f7fb;
  max-width: 100%;
  margin: 40px auto;
  padding: 32px 24px;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05), 0 1.5px 4px rgba(0, 0, 0, 0.03);
  font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
}

.results-title {
  color: #23272f;
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 32px;
  text-align: center;
  letter-spacing: 0.01em;
}

h3 {
  color: #23272f;
  font-size: 1.5rem;
  font-weight: 600;
  margin: 40px 0 24px;
  text-align: center;
  letter-spacing: 0.01em;
}

.execution-plans-grid {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 24px;
  width: 100%;
}

@media (max-width: 768px) {
  .results-container {
    padding: 16px 4px;
  }
}
</style>