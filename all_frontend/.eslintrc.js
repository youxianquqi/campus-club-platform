/**
 * ESLint 配置文件
 * 
 * 规则说明：
 * - error: 代码有问题，会导致构建失败
 * - warn: 代码可能有问题，但不阻塞构建
 * - off: 关闭该规则
 */

module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended'
  ],
  parserOptions: {
    ecmaVersion: 2021,
    sourceType: 'module'
  },
  plugins: [
    'vue'
  ],
  rules: {
    // ========================================
    // Vue 3 Composition API 规则
    // ========================================
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'warn',
    'vue/require-default-prop': 'off',
    'vue/require-explicit-emits': 'warn',
    'vue/component-tags-order': ['error', {
      order: ['script', 'template', 'style']
    }],
    'vue/block-order': ['error', {
      order: ['script', 'template', 'style']
    }],
    
    // ========================================
    // JavaScript 规则
    // ========================================
    // 缩进：2空格
    'indent': ['error', 2, {
      SwitchCase: 1
    }],
    
    // 行尾分号
    'semi': ['error', 'always'],
    
    // 单引号
    'quotes': ['error', 'single', {
      avoidEscape: true
    }],
    
    // 尾随逗号
    'comma-dangle': ['error', 'always-multiline'],
    
    // 关键字空格
    'keyword-spacing': ['error', {
      before: true,
      after: true
    }],
    
    // 箭头函数空格
    'arrow-spacing': ['error', {
      before: true,
      after: true
    }],
    
    // 对象键空格
    'object-curly-spacing': ['error', 'always'],
    
    // 数组括号空格
    'array-bracket-spacing': ['error', 'never'],
    
    // ========================================
    // 最佳实践规则
    // ========================================
    'no-console': ['warn', {
      allow: ['warn', 'error']
    }],
    'no-debugger': 'error',
    'no-unused-vars': ['warn', {
      argsIgnorePattern: '^_',
      varsIgnorePattern: '^_'
    }],
    'no-var': 'error',
    'prefer-const': 'warn',
    'prefer-arrow-callback': 'warn',
    'prefer-template': 'warn',
    'eqeqeq': ['error', 'always'],
    'curly': ['error', 'all'],
    
    // ========================================
    // 常见错误规则
    // ========================================
    'no-extra-semi': 'error',
    'no-unexpected-multiline': 'error',
    'no-dupe-keys': 'error',
    'no-duplicate-case': 'error',
    'no-empty': ['error', {
      allowEmptyCatch: true
    }],
    
    // ========================================
    // 禁止使用的语句
    // ========================================
    'no-with': 'error',
    'noeval': 'error',
    'no-implied-eval': 'error',
    'no-new-func': 'error',
    'no-script-url': 'error'
  },
  
  // 忽略某些文件的检查
  ignorePatterns: [
    'node_modules',
    'dist',
    'build',
    '*.min.js',
    'public'
  ]
}
