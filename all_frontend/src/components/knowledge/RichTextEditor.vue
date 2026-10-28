<template>
  <div class="rich-text-editor">
    <div class="toolbar">
      <button type="button" class="toolbar-btn" @click="execCommand('bold')" title="加粗 (Ctrl+B)">
        <strong>B</strong>
      </button>
      <button
        type="button"
        class="toolbar-btn"
        @click="execCommand('italic')"
        title="斜体 (Ctrl+I)"
      >
        <em>I</em>
      </button>
      <button
        type="button"
        class="toolbar-btn"
        @click="execCommand('underline')"
        title="下划线 (Ctrl+U)"
      >
        <u>U</u>
      </button>
      <div class="toolbar-divider"></div>
      <button
        type="button"
        class="toolbar-btn"
        @click="execCommand('insertUnorderedList')"
        title="无序列表"
      >
        ☰
      </button>
      <button
        type="button"
        class="toolbar-btn"
        @click="execCommand('insertOrderedList')"
        title="有序列表"
      >
        ☱
      </button>
      <div class="toolbar-divider"></div>
      <button type="button" class="toolbar-btn" @click="formatBlock('h2')" title="标题2">H2</button>
      <button type="button" class="toolbar-btn" @click="formatBlock('h3')" title="标题3">H3</button>
      <button type="button" class="toolbar-btn" @click="formatBlock('p')" title="段落">P</button>
      <div class="toolbar-divider"></div>
      <button type="button" class="toolbar-btn" @click="insertImage" title="插入图片">🖼️</button>
    </div>
    <div
      ref="editor"
      class="editor-content"
      contenteditable="true"
      @input="handleInput"
      @paste="handlePaste"
      @keydown="handleKeydown"
    ></div>
    <input
      type="file"
      ref="imageInput"
      accept="image/*"
      style="display: none"
      @change="handleImageSelect"
    />
  </div>
</template>

<script>
export default {
  name: 'RichTextEditor',
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: '在这里输入内容...',
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      imageInput: null,
    };
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(newValue) {
        if (this.$refs.editor && this.$refs.editor.innerHTML !== newValue) {
          this.$refs.editor.innerHTML = newValue;
        }
      },
    },
  },
  mounted() {
    if (this.modelValue) {
      this.$refs.editor.innerHTML = this.modelValue;
    } else {
      this.$refs.editor.innerHTML = '';
    }
    this.imageInput = this.$refs.imageInput;
  },
  methods: {
    execCommand(command, value = null) {
      document.execCommand(command, false, value);
      this.$refs.editor.focus();
      this.emitInput();
    },
    formatBlock(tag) {
      document.execCommand('formatBlock', false, tag);
      this.$refs.editor.focus();
      this.emitInput();
    },
    handleInput() {
      this.emitInput();
    },
    handlePaste(e) {
      e.preventDefault();
      const text = e.clipboardData.getData('text/plain');
      document.execCommand('insertText', false, text);
    },
    handleKeydown(e) {
      if (e.ctrlKey || e.metaKey) {
        switch (e.key.toLowerCase()) {
          case 'b':
            e.preventDefault();
            this.execCommand('bold');
            break;
          case 'i':
            e.preventDefault();
            this.execCommand('italic');
            break;
          case 'u':
            e.preventDefault();
            this.execCommand('underline');
            break;
        }
      }
    },
    emitInput() {
      const html = this.$refs.editor.innerHTML;
      this.$emit('update:modelValue', html);
    },
    insertImage() {
      this.$refs.imageInput.click();
    },
    handleImageSelect(e) {
      const file = e.target.files[0];
      if (file) {
        this.uploadImage(file);
      }
      e.target.value = '';
    },
    async uploadImage(file) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData,
        });
        if (response.ok) {
          const data = await response.json();
          const imgUrl = data.url || data.data;
          document.execCommand('insertImage', false, imgUrl);
          this.emitInput();
        } else {
          const reader = new FileReader();
          reader.onload = (e) => {
            document.execCommand('insertImage', false, e.target.result);
            this.emitInput();
          };
          reader.readAsDataURL(file);
        }
      } catch (error) {
        console.error('图片上传失败:', error);
        const reader = new FileReader();
        reader.onload = (e) => {
          document.execCommand('insertImage', false, e.target.result);
          this.emitInput();
        };
        reader.readAsDataURL(file);
      }
    },
    clear() {
      this.$refs.editor.innerHTML = '';
      this.emitInput();
    },
    getContent() {
      return this.$refs.editor.innerHTML;
    },
    setContent(html) {
      if (this.$refs.editor) {
        this.$refs.editor.innerHTML = html;
      }
    },
  },
};
</script>

<style scoped>
.rich-text-editor {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
  flex-wrap: wrap;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #e0e0e0;
  margin: 0 4px;
}

.editor-content {
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  outline: none;
}

.editor-content:empty:before {
  content: attr(data-placeholder);
  color: #999;
  pointer-events: none;
}

.editor-content :deep(h2) {
  font-size: 20px;
  font-weight: 600;
  margin: 16px 0 8px 0;
  color: #333;
}

.editor-content :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 12px 0 6px 0;
  color: #333;
}

.editor-content :deep(p) {
  margin: 8px 0;
}

.editor-content :deep(ul),
.editor-content :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}

.editor-content :deep(li) {
  margin: 4px 0;
}

.editor-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
}

.editor-content :deep(pre) {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.editor-content :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}
</style>
