<!--
  @fileoverview IndexInfoTable component that displays index information in a table format.
  @component IndexInfoTable
  @author Eva Ray
  @description This component renders a table displaying index information such as database name, index name, table,
  index size, table size, and size ratio.
  The style has been generated by an LLM.
 -->

<script setup>
import DataTable from './DataTable.vue'

const props = defineProps({
  indexInfo: {
    type: Array,
    required: true
  }
})

const headers = [
  { label: "Database", key: "dbName", headerClass: "results-metric-header", cellClass: "results-metric-cell" },
  { label: "Index Name", key: "indexName", headerClass: "results-metric-header", cellClass: "results-metric-cell" },
  { label: "Table", key: "table", headerClass: "results-metric-header", cellClass: "results-metric-cell" },
  { label: "Index Size", key: "indexSize", headerClass: "results-metric-header", cellClass: "results-metric-cell" },
  { label: "Table Size", key: "tableSize", headerClass: "results-metric-header", cellClass: "results-metric-cell" },
  { label: "Size Ratio (%)", key: "sizeRatio", headerClass: "results-metric-header", cellClass: "results-metric-cell" }
]
</script>

<template>
  <div class="index-info-section">
    <h3>Index Information</h3>
    <DataTable
        :headers="headers"
        :items="indexInfo"
        keyField="dbName-indexName"
    />
  </div>
</template>

<style scoped>
.index-info-section {
  margin-top: 40px;
}

h3 {
  color: #23272f;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 24px;
  text-align: center;
  letter-spacing: 0.01em;
}
</style>