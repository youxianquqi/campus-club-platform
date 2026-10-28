<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="header">
        <h1>入社邀请处理</h1>
        <p class="muted">来自社团管理员的直达邀请：接受后即完成关联流程（具体入社规则以后端为准）。</p>
      </div>
      <p v-if="loading" class="state">加载中…</p>
      <template v-else>
        <div v-if="!invitations.length" class="empty-card">暂无待处理邀请</div>
        <div v-for="inv in invitations" :key="inv.id" class="invite-card">
          <div class="info">
            <h2>{{ inv.clubName || '未知社团' }}</h2>
            <p class="muted" v-if="inv.clubDescription">{{ inv.clubDescription }}</p>
            <p class="meta">收到时间 {{ formatDate(inv.createdAt) }} · {{ statusLabel(inv.status) }}</p>
            <p v-if="inv.message" class="msg">附言：{{ inv.message }}</p>
          </div>
          <div v-if="inv.status === 'pending'" class="actions">
            <button type="button" class="btn ok" :disabled="acting === inv.id" @click="accept(inv)">
              接受
            </button>
            <button type="button" class="btn nope" :disabled="acting === inv.id" @click="reject(inv)">
              拒绝
            </button>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { clubInviteApi } from '../utils/api.js';

export default {
  name: 'MyInvitations',
  components: { SiteHeader },
  data() {
    return {
      invitations: [],
      loading: true,
      acting: null,
    };
  },
  async mounted() {
    await this.load();
  },
  methods: {
    formatDate(iso) {
      if (!iso) return '—';
      return new Date(iso).toLocaleString();
    },
    statusLabel(s) {
      if (s === 'pending') return '待处理';
      if (s === 'accepted') return '已接受';
      if (s === 'rejected') return '已拒绝';
      return String(s ?? '—');
    },
    async load() {
      this.loading = true;
      try {
        const list = await clubInviteApi.myList();
        this.invitations = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn(e);
        alert(e.message || '加载邀请失败');
        this.invitations = [];
      } finally {
        this.loading = false;
      }
    },
    async accept(inv) {
      if (!confirm(`确认接受「${inv.clubName || '该社团'}」的邀请？`)) return;
      this.acting = inv.id;
      try {
        await clubInviteApi.accept(inv.id);
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      } finally {
        this.acting = null;
      }
    },
    async reject(inv) {
      if (!confirm('确认拒绝该邀请？')) return;
      this.acting = inv.id;
      try {
        await clubInviteApi.reject(inv.id);
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
.page {
  min-height: 100vh;
  background: #f4f4f4;
}
.main {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.header {
  margin-bottom: 20px;
}
.header h1 {
  font-size: 22px;
  margin-bottom: 8px;
}
.muted {
  color: #666;
  font-size: 14px;
}
.state {
  text-align: center;
  padding: 40px;
  color: #666;
}
.empty-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  color: #888;
}
.invite-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.invite-card h2 {
  font-size: 18px;
  margin-bottom: 8px;
}
.meta {
  font-size: 13px;
  color: #666;
  margin-top: 10px;
}
.msg {
  margin-top: 10px;
  color: #333;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.btn {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.ok {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.btn.nope {
  background: #eee;
  color: #333;
}
</style>
