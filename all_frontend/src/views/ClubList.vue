<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="toolbar">
        <h1>全部社团</h1>
        <div class="filters">
          <input
            v-model="keyword"
            type="search"
            class="search"
            placeholder="搜索社团名称..."
            @input="debounceSearch"
          />
          <select v-model="typeId" class="select" @change="loadPage(1)">
            <option value="">全部分类</option>
            <option v-for="t in types" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
          </select>
          <button type="button" class="match-btn" @click="openMatchPanel">社团匹配</button>
        </div>
      </div>

      <ClubListSkeleton v-if="loading && !hasLoaded" />

      <template v-else>
        <TransitionGroup name="card-grid" tag="div" class="grid">
          <div
            v-for="c in filteredClubs"
            :key="c.id"
            class="card"
            :class="{ 'selected-for-match': clubMatchSlot(c) > 0 }"
          >
            <div class="cover-wrap">
              <img
                :src="c.cover || ph"
                :alt="c.name"
                class="cover"
                loading="lazy"
                @error="onImgError"
              />
            </div>
            <div class="body">
              <h3>{{ c.name }}</h3>
              <p class="desc">{{ c.description || '暂无简介' }}</p>
              <div class="meta">
                <span>{{ c.memberCount || 0 }} 人</span>
                <span>评分 {{ c.rating ?? '—' }}</span>
              </div>
              <div class="card-actions">
                <router-link :to="'/club/' + c.id" class="view-link">查看详情</router-link>
                <button type="button" class="select-btn" @click.stop="selectClubForMatch(c)">
                  {{ matchSelectButtonLabel(c) }}
                </button>
              </div>
            </div>
          </div>
        </TransitionGroup>

        <EmptyState
          v-if="!loading && !filteredClubs.length"
          type="search"
          title="暂无社团"
          description="没有找到符合条件的社团，请尝试调整搜索条件或筛选条件"
          :showRetry="true"
          @retry="loadPage(1)"
        />

        <div v-if="totalPages > 1" class="pager">
          <button type="button" :disabled="page <= 1" @click="loadPage(page - 1)">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button type="button" :disabled="page >= totalPages" @click="loadPage(page + 1)">
            下一页
          </button>
        </div>
      </template>
    </main>

    <div v-if="showMatchPanel" class="match-panel-overlay" @click.self="closeMatchPanel">
      <Transition name="modal-fade">
        <div class="match-panel">
          <div class="match-panel-header">
            <h2>社团间成员兴趣匹配</h2>
            <div class="match-panel-actions">
              <button type="button" class="ghost-btn" @click="resetMatchSelection">清空已选</button>
              <button type="button" class="close-btn" @click="closeMatchPanel">×</button>
            </div>
          </div>
          <div class="match-panel-body">
            <div class="club-select-area">
              <div class="club-slot" :class="{ filled: selectedClub1 }">
                <div v-if="!selectedClub1" class="placeholder">
                  <span class="icon">1️⃣</span>
                  <span>请从列表中选择社团1</span>
                </div>
                <div v-else class="selected-club">
                  <img
                    :src="selectedClub1.cover || ph"
                    class="club-thumb"
                    loading="lazy"
                    @error="onImgError"
                  />
                  <span class="club-name">{{ selectedClub1.name }}</span>
                  <button type="button" class="remove-btn" @click.stop="clearMatchSlot(1)">
                    ×
                  </button>
                </div>
              </div>
              <div class="vs-icon">VS</div>
              <div class="club-slot" :class="{ filled: selectedClub2 }">
                <div v-if="!selectedClub2" class="placeholder">
                  <span class="icon">2️⃣</span>
                  <span>请从列表中选择社团2</span>
                </div>
                <div v-else class="selected-club">
                  <img
                    :src="selectedClub2.cover || ph"
                    class="club-thumb"
                    loading="lazy"
                    @error="onImgError"
                  />
                  <span class="club-name">{{ selectedClub2.name }}</span>
                  <button type="button" class="remove-btn" @click.stop="clearMatchSlot(2)">
                    ×
                  </button>
                </div>
              </div>
            </div>
            <p v-if="matchSelectionHint" class="match-hint-warn">{{ matchSelectionHint }}</p>
            <p class="match-desc">
              基于两社团<strong>在社成员</strong>在「兴趣标签」中的选择：对双方每个成员的标签集合做<strong
                >两两 Jaccard</strong
              >相似度，再取全体成员对的<strong>平均值</strong>作为匹配分；下方标签为成员兴趣的<strong>并集</strong>对比。
            </p>

            <button
              type="button"
              class="calculate-btn"
              :disabled="!selectedClub1 || !selectedClub2 || matching"
              @click="calculateMatch"
            >
              {{ matching ? '计算中...' : '开始匹配' }}
            </button>

            <div v-if="matchResult" class="match-result">
              <div class="score-display">
                <div
                  class="score-circle"
                  :style="{ background: getScoreColor(matchResult.matchScore) }"
                >
                  <span class="score-value">{{ matchResult.matchScore }}</span>
                  <span class="score-unit">%</span>
                </div>
                <p class="score-label">成员兴趣匹配度（平均 Jaccard）</p>
              </div>

              <div class="result-details">
                <div class="detail-item">
                  <span class="label">成员对数</span>
                  <span class="value">{{ matchResult.pairCount }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">相似度大于 0 的成员对</span>
                  <span class="value">{{ matchResult.pairsWithPositiveJaccard }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">在社成员 / 有标签</span>
                  <span class="value"
                    >社1：{{ matchResult.memberCount1 }} / {{ matchResult.membersWithTags1 }} ·
                    社2：{{ matchResult.memberCount2 }} / {{ matchResult.membersWithTags2 }}</span
                  >
                </div>
                <div class="detail-item wide">
                  <span class="label">成员兴趣并集</span>
                  <span class="value"
                    >交集 {{ matchResult.commonTagsCount }} 个；两社合计不重复标签
                    {{ matchResult.totalTagsCount }} 个（社1 并集
                    {{ matchResult.club1TagsCount }}，社2 并集
                    {{ matchResult.club2TagsCount }}）</span
                  >
                </div>
              </div>

              <div
                v-if="matchResult.commonTags && matchResult.commonTags.length"
                class="tags-section"
              >
                <h4>共同兴趣标签（成员并集交集）</h4>
                <div class="tags-list">
                  <span v-for="tag in matchResult.commonTags" :key="tag.tagName" class="tag common">
                    {{ tag.tagName }}
                  </span>
                </div>
              </div>

              <div class="tags-row">
                <div
                  v-if="matchResult.onlyInClub1 && matchResult.onlyInClub1.length"
                  class="tags-section half"
                >
                  <h4>仅社团1成员兴趣并集</h4>
                  <div class="tags-list">
                    <span
                      v-for="tag in matchResult.onlyInClub1"
                      :key="tag.tagName"
                      class="tag club1"
                    >
                      {{ tag.tagName }}
                    </span>
                  </div>
                </div>
                <div
                  v-if="matchResult.onlyInClub2 && matchResult.onlyInClub2.length"
                  class="tags-section half"
                >
                  <h4>仅社团2成员兴趣并集</h4>
                  <div class="tags-list">
                    <span
                      v-for="tag in matchResult.onlyInClub2"
                      :key="tag.tagName"
                      class="tag club2"
                    >
                      {{ tag.tagName }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import ClubListSkeleton from '../components/skeleton/ClubListSkeleton.vue';
import EmptyState from '../components/EmptyState.vue';
import { clubApi, clubTypeApi, IMG_PLACEHOLDER, onImgError } from '../utils/api.js';

export default {
  name: 'ClubList',
  components: { SiteHeader, ClubListSkeleton, EmptyState },
  data() {
    return {
      ph: IMG_PLACEHOLDER,
      types: [],
      clubs: [],
      keyword: '',
      typeId: '',
      page: 1,
      size: 12,
      total: 0,
      loading: false,
      hasLoaded: false,
      showMatchPanel: false,
      selectedClub1: null,
      selectedClub2: null,
      matchResult: null,
      matching: false,
      matchSelectionHint: '',
      _matchHintTimer: null,
      _searchTimer: null,
    };
  },
  beforeUnmount() {
    if (this._matchHintTimer) clearTimeout(this._matchHintTimer);
    if (this._searchTimer) clearTimeout(this._searchTimer);
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.total / this.size));
    },
    filteredClubs() {
      const k = this.keyword.trim().toLowerCase();
      if (!k) return this.clubs;
      return this.clubs.filter((c) => (c.name || '').toLowerCase().includes(k));
    },
  },
  methods: {
    onImgError,
    debounceSearch() {
      if (this._searchTimer) clearTimeout(this._searchTimer);
      this._searchTimer = setTimeout(() => {
        this.loadPage(1);
      }, 300);
    },
    async loadTypes() {
      try {
        const res = await clubTypeApi.list();
        this.types = res.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadPage(p) {
      this.page = p;
      this.loading = true;
      try {
        const tid = this.typeId ? Number(this.typeId) : undefined;
        const res = await clubApi.page(this.page, this.size, tid);
        const data = res.data;
        this.clubs = data?.records || [];
        this.total = data?.total ?? this.clubs.length;
        this.hasLoaded = true;
      } catch (e) {
        console.error(e);
        this.clubs = [];
      } finally {
        this.loading = false;
      }
    },
    openMatchPanel() {
      this.showMatchPanel = true;
      this.matchResult = null;
    },
    closeMatchPanel() {
      this.showMatchPanel = false;
    },
    pickClubRef(club) {
      const id = Number(club?.id);
      return {
        id: Number.isFinite(id) ? id : club?.id,
        name: club?.name || '',
        cover: club?.cover || null,
      };
    },
    clubMatchSlot(club) {
      const id = Number(club?.id);
      if (!Number.isFinite(id)) return 0;
      if (Number(this.selectedClub1?.id) === id) return 1;
      if (Number(this.selectedClub2?.id) === id) return 2;
      return 0;
    },
    matchSelectButtonLabel(club) {
      const s = this.clubMatchSlot(club);
      if (s === 1) return '已选为社团1 · 点击取消';
      if (s === 2) return '已选为社团2 · 点击取消';
      return '选入匹配';
    },
    showMatchHint(msg) {
      if (this._matchHintTimer) clearTimeout(this._matchHintTimer);
      this.matchSelectionHint = msg;
      this._matchHintTimer = setTimeout(() => {
        this.matchSelectionHint = '';
        this._matchHintTimer = null;
      }, 4500);
    },
    clearMatchSlot(slot) {
      if (slot === 1) this.selectedClub1 = null;
      else if (slot === 2) this.selectedClub2 = null;
      this.matchResult = null;
    },
    resetMatchSelection() {
      this.selectedClub1 = null;
      this.selectedClub2 = null;
      this.matchResult = null;
      this.matchSelectionHint = '';
      if (this._matchHintTimer) {
        clearTimeout(this._matchHintTimer);
        this._matchHintTimer = null;
      }
    },
    selectClubForMatch(club) {
      const id = Number(club?.id);
      if (!Number.isFinite(id)) return;
      const slot = this.clubMatchSlot(club);
      if (slot === 1) {
        this.selectedClub1 = null;
        this.matchResult = null;
        return;
      }
      if (slot === 2) {
        this.selectedClub2 = null;
        this.matchResult = null;
        return;
      }
      const ref = this.pickClubRef(club);
      if (!this.selectedClub1) {
        this.selectedClub1 = ref;
        this.matchResult = null;
        return;
      }
      if (!this.selectedClub2) {
        if (Number(this.selectedClub1.id) === id) return;
        this.selectedClub2 = ref;
        this.matchResult = null;
        return;
      }
      this.showMatchHint(
        '已选满两个社团：请先点击卡片上的「已选为社团1/2」取消一侧，或点弹窗右上角「清空已选」后重选。',
      );
    },
    async calculateMatch() {
      if (!this.selectedClub1 || !this.selectedClub2) return;
      this.matching = true;
      this.matchResult = null;
      try {
        const d = await clubApi.memberMatch(this.selectedClub1.id, this.selectedClub2.id);
        this.matchResult = {
          matchScore: d.matchScore ?? 0,
          commonTagsCount: d.aggregateIntersectionSize ?? 0,
          totalTagsCount: d.aggregateUnionAllUnique ?? 0,
          club1TagsCount: d.aggregateUnionSize1 ?? 0,
          club2TagsCount: d.aggregateUnionSize2 ?? 0,
          memberCount1: d.memberCount1 ?? 0,
          memberCount2: d.memberCount2 ?? 0,
          membersWithTags1: d.membersWithTags1 ?? 0,
          membersWithTags2: d.membersWithTags2 ?? 0,
          pairCount: d.pairCount ?? 0,
          pairsWithPositiveJaccard: d.pairsWithPositiveJaccard ?? 0,
          commonTags: d.commonTags || [],
          onlyInClub1: d.onlyInClub1 || [],
          onlyInClub2: d.onlyInClub2 || [],
        };
      } catch (e) {
        alert(e.message || '计算失败');
      } finally {
        this.matching = false;
      }
    },
    getScoreColor(score) {
      if (score >= 80) return 'linear-gradient(135deg, #52c41a, #73d13d)';
      if (score >= 60) return 'linear-gradient(135deg, #1890ff, #40a9ff)';
      if (score >= 40) return 'linear-gradient(135deg, #faad14, #ffc53d)';
      return 'linear-gradient(135deg, #ff4d4f, #ff7875)';
    },
  },
  mounted() {
    const q = this.$route.query.typeId;
    if (q) this.typeId = String(q);
    this.loadTypes();
    this.loadPage(1);
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg-muted);
}
.main {
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: 20px 16px 40px;
}
.toolbar {
  margin-bottom: 20px;
}
.toolbar h1 {
  font-size: 22px;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}
.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.search {
  flex: 1;
  min-width: 200px;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}
.search:focus {
  outline: none;
  border-color: var(--recruit-primary);
  box-shadow: 0 0 0 3px rgba(91, 127, 201, 0.15);
}
.select {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  min-width: 140px;
  cursor: pointer;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}
.match-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.match-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  box-shadow: var(--shadow-sm);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}
.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}
.card.selected-for-match {
  box-shadow: 0 0 0 3px var(--recruit-primary);
}
.cover-wrap {
  aspect-ratio: 16/10;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(0, 161, 214, 0.15) 0%, rgba(91, 127, 201, 0.2) 100%);
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.card:hover .cover {
  transform: scale(1.05);
}
.body {
  padding: 14px;
}
.body h3 {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--color-text-primary);
}
.desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}
.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 10px;
}
.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--color-border-light);
}
.view-link {
  font-size: 13px;
  color: var(--recruit-primary);
  text-decoration: none;
  transition: color 0.2s;
}
.view-link:hover {
  color: var(--recruit-primary-hover);
}
.select-btn {
  padding: 6px 12px;
  background: var(--color-border-light);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.select-btn:hover {
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
  border-color: var(--recruit-primary);
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}
.pager button {
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: all 0.2s;
}
.pager button:hover:not(:disabled) {
  border-color: var(--recruit-primary);
  color: var(--recruit-primary);
}
.pager button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.card-grid-enter-active {
  transition: all 0.4s ease-out;
}

.card-grid-leave-active {
  transition: all 0.3s ease-in;
}

.card-grid-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.card-grid-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.card-grid-move {
  transition: all 0.4s ease;
}

.match-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.match-panel {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}
.match-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-light);
}
.match-panel-header h2 {
  font-size: 18px;
  color: var(--color-text-primary);
  margin: 0;
  flex: 1;
}
.match-panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.ghost-btn {
  padding: 6px 12px;
  font-size: 13px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.ghost-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-text-muted);
  cursor: pointer;
  line-height: 1;
  padding: 0 4px;
  transition: color 0.2s;
}
.close-btn:hover {
  color: var(--color-error);
}
.match-hint-warn {
  font-size: 13px;
  color: var(--color-warning);
  background: rgba(250, 173, 20, 0.12);
  border: 1px solid rgba(250, 173, 20, 0.35);
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  line-height: 1.5;
}
.match-panel-body {
  padding: 24px;
}
.club-select-area {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}
.club-slot {
  flex: 1;
  min-height: 100px;
  border: 2px dashed var(--color-border);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  transition:
    border-color 0.2s,
    background-color 0.2s;
}
.club-slot.filled {
  border-style: solid;
  border-color: var(--recruit-primary);
  background: rgba(91, 127, 201, 0.08);
}
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--color-text-muted);
}
.placeholder .icon {
  font-size: 32px;
}
.selected-club {
  display: flex;
  align-items: center;
  gap: 12px;
}
.club-thumb {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}
.club-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}
.remove-btn {
  background: var(--color-error);
  color: var(--header-text-on-gradient);
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}
.remove-btn:hover {
  filter: brightness(1.08);
}
.vs-icon {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-primary);
}
.match-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}
.calculate-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 24px;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.calculate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}
.calculate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.match-result {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.score-display {
  text-align: center;
  margin-bottom: 24px;
}
.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
}
.score-value {
  font-size: 36px;
  font-weight: bold;
}
.score-unit {
  font-size: 14px;
}
.score-label {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.result-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  padding: 16px;
  background: var(--color-border-light);
  border-radius: 10px;
  margin-bottom: 20px;
}
.detail-item {
  text-align: center;
}
.detail-item.wide {
  grid-column: 1 / -1;
  text-align: left;
}
.detail-item .label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}
.detail-item .value {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.detail-item.wide .value {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}
.tags-section {
  margin-bottom: 16px;
}
.tags-section h4 {
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}
.tags-section.half {
  flex: 1;
}
.tags-row {
  display: flex;
  gap: 16px;
}
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
}
.tag.common {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
}
.tag.common small {
  opacity: 0.8;
  font-size: 10px;
  margin-left: 4px;
}
.tag.club1 {
  background: rgba(91, 127, 201, 0.15);
  color: var(--recruit-primary);
}
.tag.club2 {
  background: rgba(255, 107, 157, 0.15);
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .search,
  .select,
  .match-btn {
    width: 100%;
  }

  .club-select-area {
    flex-direction: column;
  }

  .vs-icon {
    transform: rotate(90deg);
  }

  .tags-row {
    flex-direction: column;
  }
}
</style>
