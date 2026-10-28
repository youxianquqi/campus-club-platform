<template>
  <div class="audit">
    <div class="head">
      <h1 class="title">待审核社团</h1>
      <p class="sub">
        审核通过后社团将对全校可见；驳回后创建者可修改后重新提交（需产品流程配合）。
      </p>
    </div>

    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="err" class="err">{{ err }}</div>
    <div v-else-if="!rows.length" class="empty">暂无待审核申请</div>
    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>类型ID</th>
            <th>负责人ID</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in rows" :key="c.id">
            <td>{{ c.id }}</td>
            <td class="name">{{ c.name }}</td>
            <td>{{ c.typeId }}</td>
            <td>{{ c.leaderId }}</td>
            <td>{{ formatTime(c.createdAt) }}</td>
            <td class="actions">
              <button type="button" class="btn ok" :disabled="acting === c.id" @click="approve(c)">
                通过
              </button>
              <button type="button" class="btn no" :disabled="acting === c.id" @click="reject(c)">
                驳回
              </button>
              <router-link :to="'/club/' + c.id" class="link" target="_blank">预览</router-link>
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
  name: 'AdminClubAudit',
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
        const res = await adminApi.pendingClubs(this.page, this.size);
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
    async approve(c) {
      if (!confirm(`确定通过「${c.name}」？`)) return;
      this.acting = c.id;
      try {
        await adminApi.approveClub(c.id);
        alert('已通过');
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      } finally {
        this.acting = null;
      }
    },
    async reject(c) {
      const reason = prompt(`驳回「${c.name}」\n可选填写原因：`, '') ?? '';
      if (reason === null) return;
      this.acting = c.id;
      try {
        await adminApi.rejectClub(c.id, reason);
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
