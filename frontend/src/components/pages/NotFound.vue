<!--
  @fileoverview NotFound component that displays a 404 error message when a page is not found.
  @component NotFound
  @author Eva Ray
  @description This component is displayed when the user navigates to a route that does not exist in the application.
  The style has been generated by an LLM.
-->

<template>
  <div class="not-found-page">
    <div class="container">
      <h1>404 - Page Not Found</h1>
      <p>The page you're looking for doesn't exist.</p>
      <button @click="goHome" class="btn btn-large">
        Go to Home
      </button>
    </div>
  </div>
</template>

<script setup>

/**
 * @description Function to navigate the user back to the home page.
 */
const goHome = () => {
  window.location.hash = '#/'
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.container {
  text-align: center;
}

.container h1 {
  color: #dc3545;
  margin-bottom: 20px;
}

.container p {
  color: #767b8a;
  font-size: 1.1rem;
  margin-bottom: 30px;
}
</style>
