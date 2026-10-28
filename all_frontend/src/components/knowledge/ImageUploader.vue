<template>
  <div class="image-uploader">
    <div
      class="upload-area"
      :class="{ 'drag-over': isDragOver }"
      @click="triggerUpload"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @drop.prevent="handleDrop"
    >
      <div class="upload-content">
        <span class="upload-icon">📷</span>
        <p class="upload-text">点击上传或拖拽图片到这里</p>
        <p class="upload-hint">支持 JPG、PNG、GIF、WebP 格式，最大 5MB</p>
      </div>
    </div>

    <div v-if="images.length > 0" class="preview-list">
      <div v-for="(img, index) in images" :key="index" class="preview-item">
        <img :src="img" alt="预览" />
        <button type="button" class="remove-btn" @click="removeImage(index)">×</button>
        <div v-if="uploading === index" class="upload-progress">
          <div class="progress-bar"></div>
        </div>
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleFileSelect"
    />
  </div>
</template>

<script>
export default {
  name: 'ImageUploader',
  props: {
    modelValue: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      images: [],
      isDragOver: false,
      uploading: null,
    };
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(newValue) {
        this.images = [...newValue];
      },
    },
  },
  methods: {
    triggerUpload() {
      this.$refs.fileInput.click();
    },
    handleFileSelect(e) {
      const files = Array.from(e.target.files);
      this.processFiles(files);
      e.target.value = '';
    },
    handleDrop(e) {
      this.isDragOver = false;
      const files = Array.from(e.dataTransfer.files).filter((file) =>
        file.type.startsWith('image/'),
      );
      this.processFiles(files);
    },
    async processFiles(files) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.size > 5 * 1024 * 1024) {
          alert(`图片 ${file.name} 超过 5MB 限制`);
          continue;
        }
        await this.uploadImage(file);
      }
    },
    async uploadImage(file) {
      const index = this.images.length;
      this.uploading = index;

      try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const data = await response.json();
          const imageUrl = data.url || data.data;
          this.images.push(imageUrl);
          this.emitInput();
        } else {
          const reader = new FileReader();
          reader.onload = (e) => {
            this.images.push(e.target.result);
            this.emitInput();
          };
          reader.readAsDataURL(file);
        }
      } catch (error) {
        console.error('上传失败:', error);
        const reader = new FileReader();
        reader.onload = (e) => {
          this.images.push(e.target.result);
          this.emitInput();
        };
        reader.readAsDataURL(file);
      } finally {
        this.uploading = null;
      }
    },
    removeImage(index) {
      this.images.splice(index, 1);
      this.emitInput();
    },
    emitInput() {
      this.$emit('update:modelValue', [...this.images]);
    },
  },
};
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #667eea;
  background: #f0f0ff;
}

.upload-area.drag-over {
  border-color: #667eea;
  background: #e8e8ff;
  transform: scale(1.02);
}

.upload-content {
  pointer-events: none;
}

.upload-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.upload-hint {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.preview-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e0e0e0;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  line-height: 1;
}

.remove-btn:hover {
  background: #f56c6c;
  transform: scale(1.1);
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(0, 0, 0, 0.3);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  animation: progress 1s ease-in-out infinite;
}

@keyframes progress {
  0% {
    width: 0%;
  }
  50% {
    width: 70%;
  }
  100% {
    width: 100%;
  }
}
</style>
