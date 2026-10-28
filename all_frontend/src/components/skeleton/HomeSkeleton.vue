<template>
  <div class="home-skeleton">
    <div class="banner-skeleton">
      <div class="skeleton-banner"></div>
      <div class="ranking-skeleton">
        <div class="skeleton-ranking-item" v-for="i in 5" :key="i"></div>
      </div>
    </div>

    <div class="dots-skeleton">
      <div class="skeleton-dot" v-for="i in 3" :key="i"></div>
    </div>

    <div class="section-skeleton">
      <div class="section-header-skeleton">
        <div class="skeleton-title"></div>
        <div class="skeleton-more"></div>
      </div>
      <div class="club-grid-skeleton">
        <div class="club-card-skeleton" v-for="i in 8" :key="i">
          <div class="skeleton-cover"></div>
          <div class="skeleton-info">
            <div class="skeleton-name"></div>
            <div class="skeleton-meta"></div>
            <div class="skeleton-actions"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="section-skeleton">
      <div class="section-header-skeleton">
        <div class="skeleton-title"></div>
        <div class="skeleton-more"></div>
      </div>
      <div class="activity-grid-skeleton">
        <div class="activity-card-skeleton" v-for="i in 6" :key="i">
          <div class="skeleton-cover"></div>
          <div class="skeleton-info">
            <div class="skeleton-name"></div>
            <div class="skeleton-desc"></div>
            <div class="skeleton-meta"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-skeleton {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px;
}

.banner-skeleton {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 300px);
  gap: 16px;
  margin-bottom: 16px;
}

.skeleton-banner {
  height: 220px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.ranking-skeleton {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.skeleton-ranking-item {
  height: 80px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-ranking-item:nth-child(2) {
  animation-delay: 0.1s;
}
.skeleton-ranking-item:nth-child(3) {
  animation-delay: 0.2s;
}
.skeleton-ranking-item:nth-child(4) {
  animation-delay: 0.3s;
}

.dots-skeleton {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin: 16px 0;
}

.skeleton-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f0f0f0;
}

.section-skeleton {
  margin-bottom: 40px;
}

.section-header-skeleton {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.skeleton-title {
  width: 120px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-more {
  width: 80px;
  height: 20px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.club-grid-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.club-card-skeleton {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.skeleton-cover {
  aspect-ratio: 16/10;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 12px;
}

.skeleton-name {
  width: 80%;
  height: 18px;
  border-radius: 4px;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-meta {
  width: 60%;
  height: 14px;
  border-radius: 4px;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-actions {
  display: flex;
  gap: 8px;
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

.activity-grid-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.activity-card-skeleton {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.activity-card-skeleton .skeleton-cover {
  aspect-ratio: 16/9;
}

.skeleton-desc {
  width: 90%;
  height: 14px;
  border-radius: 4px;
  margin: 8px 0;
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

@media (max-width: 992px) {
  .banner-skeleton {
    grid-template-columns: 1fr;
  }

  .ranking-skeleton {
    display: none;
  }
}

@media (max-width: 768px) {
  .home-skeleton {
    padding: 12px;
  }

  .club-grid-skeleton,
  .activity-grid-skeleton {
    grid-template-columns: 1fr;
  }
}
</style>
