<template>
  <div class="audit">
    <div class="head">
      <h1 class="title">待审核活动</h1>
      <p class="sub">
        社团负责人提交的新活动默认为草稿；通过后状态为「报名中」，前台列表与报名方可使用；驳回后标记为已取消。
      </p>
    </div>

    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="err" class="err">{{ err }}</div>
    <div v-else-if="!rows.length" class="empty">暂无待审核活动</div>
    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>社团ID</th>
            <th>地点</th>
            <th>开始时间</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in rows" :key="a.id">
            <td>{{ a.id }}</td>
            <td class="name">{{ a.title }}</td>
            <td>{{ a.clubId }}</td>
            <td>{{ a.location || '—' }}</td>
            <td>{{ formatTime(a.startTime) }}</td>
            <td>{{ formatTime(a.createdAt) }}</td>
            <td class="actions">
              <button type="button" class="btn ok" :disabled="acting === a.id" @click="approve(a)">
                通过
              </button>
              <button type="button" class="btn no" :disabled="acting === a.id" @click="reject(a)">
                驳回
              </button>
              <router-link :to="'/club/' + a.clubId" class="link" target="_blank"
                >所属社团</router-link
              >
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager" v-if="totalPages > 1">
        <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ totalPages }}</span>
        <button type="button" :disabled="page >= totalPages" @click="goPage(page + 1)">
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { adminApi } from '../../utils/api.js';

export default {
  name: 'AdminActivityAudit',
  data() {
    return {
      rows: [],
      total: 0,
      page: 1,
      size: 10,
      loading: false,
      err: '',
      acting: null,
    };
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.total / this.size));
    },
  },
  mounted() {
    this.load();
  },
  methods: {
    formatTime(iso) {
      if (!iso) return '—';
      return new Date(iso).toLocaleString('zh-CN');
    },
    async load() {
      this.loading = true;
      this.err = '';
      try {
        const res = await adminApi.pendingActivities(this.page, this.size);
        const p = res.data;
        this.rows = p?.records || [];
        this.total = p?.total ?? this.rows.length;
      } catch (e) {
        this.err = e.message || '加载失败';
        this.rows = [];
      } finally {
        this.loading = false;
      }
    },
    goPage(p) {
      this.page = p;
      this.load();
    },
    async approve(a) {
      if (!confirm(`确定通过活动「${a.title}」？`)) return;
      this.acting = a.id;
      try {
        await adminApi.approveActivity(a.id);
        alert('已通过');
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      } finally {
        this.acting = null;
      }
    },
    async reject(a) {
      const reason = prompt(`驳回活动「${a.title}」\n可选填写原因：`, '') ?? '';
      if (reason === null) return;
      this.acting = a.id;
      try {
        await adminApi.rejectActivity(a.id, reason);
        alert('已驳回');
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      } finally {
        this.acting = null;
      }
    },
  },
};
</script>

<style scoped>
.head {
  margin-bottom: 24px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.sub {
  margin-top: 8px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  max-width: 640px;
  line-height: 1.5;
}

.muted,
.empty {
  color: var(--color-text-muted);
  padding: var(--spacing-xl) 0;
}

.err {
  color: var(--color-error);
}

.table-wrap {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  border-bottom: 1px solid var(--color-border-light);
}

.table th {
  background: var(--color-border-light);
  color: var(--color-text-secondary);
  font-weight: 500;
}

.name {
  font-weight: 500;
  color: var(--color-text-primary);
}

.actions {
  white-space: nowrap;
}

.btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  cursor: pointer;
  margin-right: 8px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.ok {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: var(--header-text-on-gradient);
}

.btn.no {
  background: var(--color-border-light);
  color: var(--color-text-primary);
}

.link {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.pager {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.pager button {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  cursor: pointer;
}

.pager button:disabled {
  opacity: 0.5;
}
</style>
