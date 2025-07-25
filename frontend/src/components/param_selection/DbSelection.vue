<!--
  @fileoverview DbSelection component that allows users to select databases for benchmarking.
  @component DbSelection
  @author Eva Ray
  @description This component provides a checkbox selection interface for different databases. It fetches the list of
  databases from a server endpoint and allows users to select multiple databases. It emits the selected database values
  to the parent component. The style has been generated by an LLM.
-->

<script setup>
import { ref, onMounted } from 'vue'

const databases = ref([])
const selectedDatabaseValues = ref([])

// Fetch available databases from the server
onMounted(async () => {
    try {
        const response = await fetch('http://localhost:7070/meta/databases')
        if (!response.ok) throw new Error('Network')
        databases.value = await response.json()
    } catch (error) {
        console.error('Error loading db:', error)
    }
})

// Expose data to parent component
defineExpose({
    getSelections: () => {
        // Retourne un tableau d'objets {label, value} pour chaque base sélectionnée
        const selected = databases.value.filter(db => selectedDatabaseValues.value.includes(db.value))
        return {
            selectedDatabases: selected.map(db => db.value)
        }
    }
})
</script>

<template>
    <div class="container">
        <h2>Database</h2>
        <div class="selection-row">
            <label v-for="db in databases" :key="db.value" class="checkbox-label">
                <input 
                    type="checkbox" 
                    :value="db.value" 
                    v-model="selectedDatabaseValues" 
                    class="checkbox-input" 
                />
                <span class="checkbox-text">{{ db.label }}</span>
            </label>
        </div>
    </div>
</template>

