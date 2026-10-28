<template>
  <div class="club-list-skeleton">
    <div class="filters-skeleton">
      <div class="skeleton-filter" v-for="i in 4" :key="i"></div>
      <div class="skeleton-search"></div>
    </div>

    <div class="club-grid-skeleton">
      <div class="club-card-skeleton" v-for="i in 9" :key="i">
        <div class="skeleton-cover">
          <div class="skeleton-badge"></div>
        </div>
        <div class="skeleton-info">
          <div class="skeleton-name"></div>
          <div class="skeleton-tags">
            <div class="skeleton-tag" v-for="j in 3" :key="j"></div>
          </div>
          <div class="skeleton-meta"></div>
          <div class="skeleton-actions"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.club-list-skeleton {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px;
}

.filters-skeleton {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.skeleton-filter {
  width: 80px;
  height: 40px;
  border-radius: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-filter:nth-child(2) {
  animation-delay: 0.1s;
}
.skeleton-filter:nth-child(3) {
  animation-delay: 0.2s;
}
.skeleton-filter:nth-child(4) {
  animation-delay: 0.3s;
}

.skeleton-search {
  flex: 1;
  min-width: 200px;
  height: 40px;
  border-radius: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.club-grid-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.club-card-skeleton {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.skeleton-cover {
  position: relative;
  aspect-ratio: 16/10;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 50px;
  height: 24px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 16px;
}

.skeleton-name {
  width: 85%;
  height: 20px;
  border-radius: 4px;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.skeleton-tag {
  width: 60px;
  height: 24px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-tag:nth-child(2) {
  animation-delay: 0.1s;
}
.skeleton-tag:nth-child(3) {
  animation-delay: 0.2s;
}

.skeleton-meta {
  width: 70%;
  height: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-actions {
  display: flex;
  gap: 12px;
}

.skeleton-actions::before,
.skeleton-actions::after {
  content: '';
  flex: 1;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@media (max-width: 768px) {
  .club-list-skeleton {
    padding: 12px;
  }

  .club-grid-skeleton {
    grid-template-columns: 1fr;
  }

  .filters-skeleton {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }

  .skeleton-filter {
    flex-shrink: 0;
  }

  .skeleton-search {
    min-width: 150px;
  }
}
</style>
