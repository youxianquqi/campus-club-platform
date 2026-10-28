<template>
  <div class="page-container">
    <SiteHeader />

    <main class="main-content">
      <div v-if="!userId" class="login-hint">
        <p>请先登录后使用社团间联系。</p>
        <router-link to="/login" class="link-btn">去登录</router-link>
      </div>

      <div v-else-if="contextLoading" class="login-hint">
        <p>加载中…</p>
      </div>

      <div v-else-if="!ctx.leader" class="login-hint">
        <p><strong>社团间联系</strong>仅对<strong>社团负责人</strong>开放。</p>
        <p class="hint-detail">
          请使用已绑定「社团管理」角色的账号登录（创建社团审核通过后，系统会写入对应权限）。普通成员可浏览社团详情页申请入社。
        </p>
        <router-link to="/clubs" class="link-btn">浏览社团</router-link>
      </div>

      <div v-else class="chat-container">
        <div class="chat-sidebar">
          <div class="sidebar-header">
            <h3>社团间联系</h3>
          </div>
          <div class="my-club-bar">
            我方：<strong>{{ ctx.clubName || '我的社团' }}</strong>
            <span class="cid">ID {{ ctx.clubId }}</span>
          </div>
          <div class="new-peer-row">
            <input
              v-model.number="peerClubInput"
              type="number"
              min="1"
              class="target-input"
              placeholder="对方社团 ID"
              @keydown.enter.prevent="openPeerByInput"
            />
            <button type="button" class="btn-open" @click="openPeerByInput">联系</button>
          </div>
          <p class="room-hint">与对方社团负责人一对一频道；仅双方可见本会话内容。</p>
          <div v-if="loadingList" class="side-loading">加载会话…</div>
          <div v-else class="chat-list">
            <div
              v-for="chat in chatList"
              :key="chat.peerClubId"
              class="chat-item"
              :class="{ active: activePeerClubId === chat.peerClubId }"
              @click="selectChat(chat)"
            >
              <img :src="chat.avatar" class="chat-avatar" alt="" @error="onImgError" />
              <div class="chat-info">
                <div class="chat-name-row">
                  <span class="chat-name">{{ chat.name }}</span>
                  <span class="chat-time">{{ chat.time }}</span>
                </div>
                <p class="chat-preview">{{ chat.lastMessage }}</p>
              </div>
            </div>
            <p v-if="!chatList.length" class="empty-side">暂无会话，输入对方社团 ID 发起联系</p>
          </div>
        </div>

        <div class="chat-main">
          <div v-if="currentChatInfo" class="chat-header">
            <div class="chat-user-info">
              <img
                :src="currentChatInfo.avatar"
                class="chat-user-avatar"
                alt=""
                @error="onImgError"
              />
              <div class="chat-user-detail">
                <h4>{{ currentChatInfo.name }}</h4>
                <span class="sub-id">{{ currentChatInfo.subtitle }}</span>
              </div>
            </div>
          </div>

          <div v-else class="chat-placeholder">
            <p>选择左侧会话或输入对方社团 ID</p>
          </div>

          <div v-show="hasActivePeer" class="message-list" ref="messageList">
            <div v-if="loadingHistory" class="hist-loading">加载消息…</div>
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ self: message.isSelf }"
            >
              <img
                v-if="!message.isSelf"
                :src="message.avatar"
                class="message-avatar"
                alt=""
                @error="onImgError"
              />
              <div class="message-content">
                <div v-if="message.senderLabel" class="sender-line">{{ message.senderLabel }}</div>
                <div class="message-bubble">
                  <p>{{ message.content }}</p>
                </div>
                <span class="message-time">{{ message.time }}</span>
              </div>
              <img
                v-if="message.isSelf"
                :src="message.avatar"
                class="message-avatar"
                alt=""
                @error="onImgError"
              />
            </div>
          </div>

          <div v-if="hasActivePeer" class="input-area">
            <div class="input-wrapper">
              <textarea
                v-model="inputMessage"
                placeholder="输入消息…"
                class="message-input"
                rows="1"
                @keydown.enter.prevent="sendMessage"
              ></textarea>
              <button
                class="send-btn"
                type="button"
                :disabled="!inputMessage.trim() || sending"
                @click="sendMessage"
              >
                <svg viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  getCurrentUserId,
  chatApi,
  userApi,
  resolveBackendUrl,
  IMG_PLACEHOLDER,
  onImgError,
} from '../utils/api.js';

export default {
  name: 'Chat',
  components: { SiteHeader },
  data() {
    return {
      userId: null,
      contextLoading: true,
      ctx: {
        leader: false,
        clubId: null,
        clubName: null,
      },
      peerClubInput: null,
      activePeerClubId: null,
      chatList: [],
      messages: [],
      selfAvatar: IMG_PLACEHOLDER,
      inputMessage: '',
      loadingList: false,
      loadingHistory: false,
      sending: false,
    };
  },
  computed: {
    hasActivePeer() {
      return this.activePeerClubId != null && this.activePeerClubId > 0;
    },
    currentChatInfo() {
      if (!this.hasActivePeer) return null;
      const row = this.chatList.find((c) => c.peerClubId === this.activePeerClubId);
      return {
        name: row?.name || `社团 #${this.activePeerClubId}`,
        avatar: row?.avatar || IMG_PLACEHOLDER,
        subtitle: `对方社团 · ID ${this.activePeerClubId}`,
      };
    },
  },
  watch: {
    '$route.query.peerClub'(v) {
      if (v && this.ctx.leader) {
        const id = Number(v);
        if (Number.isFinite(id) && id > 0) this.openPeerClub(id, false);
      }
    },
  },
  methods: {
    onImgError,
    resolveAvatar(url) {
      if (!url) return IMG_PLACEHOLDER;
      const s = String(url);
      if (s.startsWith('http')) return s;
      return resolveBackendUrl(s);
    },
    formatMsgTime(iso) {
      if (!iso) return '';
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return '';
      return d.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    formatSideTime(iso) {
      if (!iso) return '';
      return this.formatRelative(iso);
    },
    formatRelative(iso) {
      if (!iso) return '';
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return '';
      const sec = (Date.now() - d.getTime()) / 1000;
      if (sec < 60) return '刚刚';
      if (sec < 3600) return `${Math.floor(sec / 60)}分钟前`;
      if (sec < 86400) return `${Math.floor(sec / 3600)}小时前`;
      if (sec < 86400 * 7) return `${Math.floor(sec / 86400)}天前`;
      return d.toLocaleDateString();
    },
    async loadSelfProfile() {
      if (!this.userId) return;
      try {
        const res = await userApi.getUserById(this.userId);
        const u = res.data;
        if (u?.avatar) this.selfAvatar = this.resolveAvatar(u.avatar);
      } catch (e) {
        console.error(e);
      }
    },
    async loadContext() {
      this.contextLoading = true;
      try {
        const d = await chatApi.clubContext();
        this.ctx = {
          leader: !!d?.leader,
          clubId: d?.clubId != null ? Number(d.clubId) : null,
          clubName: d?.clubName || null,
        };
      } catch (e) {
        console.error(e);
        this.ctx = { leader: false, clubId: null, clubName: null };
      } finally {
        this.contextLoading = false;
      }
    },
    async loadThreads() {
      if (!this.ctx.leader) return;
      this.loadingList = true;
      try {
        const rows = await chatApi.clubThreads();
        const list = Array.isArray(rows) ? rows : [];
        this.chatList = list.map((t) => ({
          peerClubId: Number(t.peerClubId),
          name: t.peerClubName || `社团 #${t.peerClubId}`,
          avatar: IMG_PLACEHOLDER,
          lastMessage: t.lastMessage || '',
          time: this.formatSideTime(t.lastAt),
        }));
      } catch (e) {
        console.error(e);
        this.chatList = [];
      } finally {
        this.loadingList = false;
      }
    },
    upsertSidebar(peerClubId, name, lastMessage, lastAtIso) {
      const id = Number(peerClubId);
      const idx = this.chatList.findIndex((c) => c.peerClubId === id);
      const row = {
        peerClubId: id,
        name: name || `社团 #${id}`,
        avatar: IMG_PLACEHOLDER,
        lastMessage: lastMessage || '',
        time: lastAtIso ? this.formatSideTime(lastAtIso) : '刚刚',
      };
      if (idx >= 0) {
        this.chatList.splice(idx, 1);
      }
      this.chatList.unshift(row);
    },
    openPeerByInput() {
      const id = Number(this.peerClubInput);
      if (!Number.isFinite(id) || id <= 0) {
        alert('请输入有效的对方社团 ID');
        return;
      }
      this.openPeerClub(id, true);
    },
    async openPeerClub(peerClubId, syncRoute) {
      const id = Number(peerClubId);
      if (!Number.isFinite(id) || id <= 0) return;
      if (this.ctx.clubId != null && id === this.ctx.clubId) {
        alert('不能填写本社团 ID');
        return;
      }
      const exists = this.chatList.some((c) => c.peerClubId === id);
      if (!exists) {
        this.upsertSidebar(id, `社团 #${id}`, '', null);
      }
      this.activePeerClubId = id;
      if (syncRoute) {
        await this.$router.replace({ path: '/chat', query: { peerClub: String(id) } });
      }
      await this.loadHistory(id);
    },
    async selectChat(chat) {
      this.activePeerClubId = chat.peerClubId;
      await this.$router.replace({ path: '/chat', query: { peerClub: String(chat.peerClubId) } });
      await this.loadHistory(chat.peerClubId);
    },
    async loadHistory(peerClubId) {
      if (!this.userId || !peerClubId) return;
      this.loadingHistory = true;
      try {
        const res = await chatApi.getMessages({ peerClubId, page: 0, size: 100 });
        const list = res?.messages || [];
        this.messages = list.map((m) => {
          const isSelf = m.isMine === true || Number(m.senderId) === this.userId;
          const clubLabel = m.senderClubName || '';
          const senderLabel = !isSelf
            ? [clubLabel, m.senderName].filter(Boolean).join(' · ')
            : [this.ctx.clubName || '我方', m.senderName].filter(Boolean).join(' · ');
          return {
            id: m.id,
            content: m.content || '',
            time: this.formatMsgTime(m.createdAt),
            isSelf,
            senderLabel: senderLabel || null,
            avatar: isSelf ? this.selfAvatar : this.resolveAvatar(m.senderAvatar),
          };
        });
        this.$nextTick(() => this.scrollToBottom());
      } catch (e) {
        console.error(e);
        this.messages = [];
        alert(e.message || '加载消息失败');
      } finally {
        this.loadingHistory = false;
      }
    },
    async sendMessage() {
      const text = this.inputMessage.trim();
      if (!text || !this.userId || this.sending || !this.hasActivePeer) return;
      this.sending = true;
      try {
        const res = await chatApi.sendMessage({
          peerClubId: this.activePeerClubId,
          content: text,
        });
        if (res && res.success) {
          this.inputMessage = '';
          this.messages.push({
            id: res.messageId || Date.now(),
            content: text,
            time: this.formatMsgTime(res.createdAt),
            isSelf: true,
            senderLabel: [this.ctx.clubName || '我方', '我'].filter(Boolean).join(' · '),
            avatar: this.selfAvatar,
          });
          this.upsertSidebar(
            this.activePeerClubId,
            this.currentChatInfo?.name || `社团 #${this.activePeerClubId}`,
            text,
            res.createdAt,
          );
          this.$nextTick(() => this.scrollToBottom());
        } else {
          alert((res && res.message) || '发送失败');
        }
      } catch (e) {
        alert(e.message || '发送失败');
      } finally {
        this.sending = false;
      }
    },
    scrollToBottom() {
      const list = this.$refs.messageList;
      if (list) list.scrollTop = list.scrollHeight;
    },
  },
  async mounted() {
    this.userId = getCurrentUserId();
    if (!this.userId) return;
    await this.loadSelfProfile();
    await this.loadContext();
    if (this.ctx.leader) {
      await this.loadThreads();
      const pc = this.$route.query.peerClub;
      if (pc) {
        const id = Number(pc);
        if (Number.isFinite(id) && id > 0) {
          await this.openPeerClub(id, false);
        }
      }
    }
  },
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f4f4f4;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.login-hint {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.login-hint p {
  margin-bottom: 16px;
  color: #666;
}

.hint-detail {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
}

.link-btn {
  display: inline-block;
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
  font-size: 14px;
}

.chat-container {
  display: flex;
  height: calc(100vh - 100px);
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  font-size: 18px;
  color: #333;
}

.my-club-bar {
  padding: 10px 16px;
  font-size: 13px;
  color: #555;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.my-club-bar .cid {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.new-peer-row {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.btn-open {
  padding: 8px 14px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  flex-shrink: 0;
}

.room-hint {
  padding: 10px 16px;
  font-size: 12px;
  color: #888;
  border-bottom: 1px solid #f0f0f0;
  line-height: 1.5;
}

.target-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.side-loading,
.hist-loading,
.empty-side {
  padding: 16px;
  font-size: 13px;
  color: #999;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.chat-item:hover {
  background: #f9f9f9;
}

.chat-item.active {
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(0, 161, 214, 0.1) 100%);
}

.chat-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  object-fit: cover;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.chat-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.chat-time {
  font-size: 12px;
  color: #999;
}

.chat-preview {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  object-fit: cover;
}

.chat-user-detail h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 2px;
}

.sub-id {
  font-size: 12px;
  color: #999;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.message-list {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
  background: #fafafa;
  min-height: 200px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  flex-shrink: 0;
  object-fit: cover;
}

.message-content {
  max-width: 60%;
}

.sender-line {
  font-size: 11px;
  color: #888;
  margin-bottom: 4px;
}

.message-bubble {
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-item.self .message-bubble {
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
}

.message-bubble p {
  font-size: 14px;
  line-height: 1.6;
}

.message-time {
  display: block;
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.message-item.self .message-time {
  text-align: left;
}

.input-area {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  max-height: 100px;
  font-family: inherit;
}

.message-input:focus {
  border-color: #ff6b9d;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: transform 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-sidebar {
    width: 100%;
    max-height: 40vh;
  }

  .chat-container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 100px);
  }
}
</style>
