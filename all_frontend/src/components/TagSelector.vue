<template>
  <div class="tag-selector">
    <div class="selector-header">
      <h3 v-if="title">{{ title }}</h3>
      <span class="selected-count" v-if="multiple && selectedTags.length > 0">
        已选择 {{ selectedTags.length }} 个标签
      </span>
    </div>

    <div class="tags-container">
      <button
        v-for="tag in tags"
        :key="tag.id || tag.name"
        class="tag-button"
        :class="{
          'is-selected': isTagSelected(tag),
          'is-disabled': disabled || (multiple && selectedTags.length >= maxSelections && !isTagSelected(tag)),
        }"
        @click="handleTagClick(tag)"
        :disabled="disabled"
      >
        <span v-if="tag.icon" class="tag-icon">{{ tag.icon }}</span>
        <span class="tag-name">{{ tag.name }}</span>
        <span v-if="tag.count !== undefined" class="tag-count">({{ tag.count }})</span>
        <span v-if="isTagSelected(tag)" class="check-mark">✓</span>
      </button>
    </div>

    <div v-if="showClearButton && selectedTags.length > 0" class="clear-section">
      <button class="clear-btn" @click="clearSelection">
        清除选择
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TagSelector',
  props: {
    tags: {
      type: Array,
      required: true,
      default: () => [],
    },
    modelValue: {
      type: [String, Array],
      default: () => [],
    },
    multiple: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    maxSelections: {
      type: Number,
      default: 10,
    },
    showClearButton: {
      type: Boolean,
      default: true,
    },
  },
  emits: ['update:modelValue', 'select'],
  computed: {
    selectedTags() {
      if (this.multiple) {
        return Array.isArray(this.modelValue) ? this.modelValue : [];
      }
      return this.modelValue ? [this.modelValue] : [];
    },
  },
  methods: {
    isTagSelected(tag) {
      if (this.multiple) {
        return this.selectedTags.some(
          (selected) => (selected.id || selected) === (tag.id || tag.name)
        );
      }
      return (this.modelValue?.id || this.modelValue) === (tag.id || tag.name);
    },
    handleTagClick(tag) {
      if (this.disabled) return;
      if (this.multiple) {
        this.toggleMultipleSelection(tag);
      } else {
        this.handleSingleSelection(tag);
      }
      this.$emit('select', tag);
    },
    toggleMultipleSelection(tag) {
      const tagId = tag.id || tag.name;
      const currentSelection = [...this.selectedTags];
      const index = currentSelection.findIndex(
        (selected) => (selected.id || selected) === tagId
      );

      if (index > -1) {
        currentSelection.splice(index, 1);
      } else {
        if (currentSelection.length < this.maxSelections) {
          currentSelection.push(tag);
        }
      }

      this.$emit('update:modelValue', currentSelection);
    },
    handleSingleSelection(tag) {
      const tagId = tag.id || tag.name;
      if (this.modelValue && (this.modelValue.id || this.modelValue) === tagId) {
        this.$emit('update:modelValue', '');
      } else {
        this.$emit('update:modelValue', tag);
      }
    },
    clearSelection() {
      if (this.multiple) {
        this.$emit('update:modelValue', []);
      } else {
        this.$emit('update:modelValue', '');
      }
    },
  },
};
</script>

<style scoped>
.tag-selector {
  padding: 16px;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.selector-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.selected-count {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-button:hover:not(:disabled):not(.is-selected) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.tag-button.is-selected {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.tag-button.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.tag-icon {
  font-size: 16px;
}

.tag-name {
  font-weight: 500;
}

.tag-count {
  font-size: 12px;
  opacity: 0.7;
}

.check-mark {
  font-size: 12px;
  font-weight: bold;
}

.clear-section {
  margin-top: 16px;
  text-align: center;
}

.clear-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clear-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
