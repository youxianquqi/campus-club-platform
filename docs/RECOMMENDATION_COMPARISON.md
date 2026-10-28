# 推荐算法功能对比：Python Demo vs Java 后端实现

> 对比基于冷启动的学生社团推荐 Demo 与 `backend-platform` 中 `RecommendationService` 的实现差异

---

## 一、算法核心公式对比

### Python Demo

```
总分 = 公开标签分 + 隐藏标签分 + QQ加分
     = Jaccard(学生兴趣, 社团公开标签) × 60
     + min(命中隐藏标签数 × 5, 20)
     + (学生QQ == 社团QQ 或 学生QQ in 推荐QQ列表 ? 20 : 0)

相似度 = 总分 / 100
```

### Java 后端

```
总分 = 公开标签分 + 隐藏标签分 + QQ加分
     = Jaccard(用户标签, 社团可见标签) × maxTagScore
     + min(命中隐藏标签数 × hiddenMatchUnit, maxHiddenScore)
     + (用户boostClubId == 社团ID 或 QQ关键词匹配 ? qqBoostScore : 0)

相似度 = min(1.0, min(总分, maxTotal) / maxTotal)
```

---

## 二、权重配置对比

| 参数 | Python Demo (硬编码) | Java 后端 (数据库) | 差异 |
|------|---------------------|-------------------|------|
| 公开标签最高分 | `MAX_TAG_SCORE = 60.0` | `maxTagScore` (默认 60.0) | ✅ 一致 |
| QQ 加分 | `QQ_BOOST_SCORE = 20.0` | `qqBoostScore` (默认 20.0) | ✅ 一致 |
| 隐藏标签单位分 | `HIDDEN_MATCH_UNIT = 5.0` | `hiddenMatchUnit` (默认 5.0) | ✅ 一致 |
| 隐藏标签最高分 | `MAX_HIDDEN_SCORE = 20.0` | `maxHiddenScore` (默认 20.0) | ✅ 一致 |
| 总分最大值 | `100.0` | `maxTagScore + qqBoostScore + maxHiddenScore = 100.0` | ✅ 一致 |
| 权重来源 | 代码硬编码 | `recommendation_rule` 表 | ⚠️ 更灵活 |

---

## 三、功能模块详细对比

### 3.1 标签匹配

| 维度 | Python Demo | Java 后端 | 差异 |
|------|-------------|-----------|------|
| **公开标签** | `public_tags` 逗号分隔字符串 | `club_tag` 关联表 (可见标签 `isHidden=false`) | ⚠️ 存储方式不同 |
| **隐藏标签** | `hidden_tags` 逗号分隔字符串 | `club_tag` 关联表 (`isHidden=true`) | ⚠️ 存储方式不同 |
| **学生标签** | `interest_tags` 逗号分隔字符串 | `user_tag` 关联表 | ⚠️ 存储方式不同 |
| **相似度算法** | Jaccard = |A∩B| / |A∪B| | Jaccard = |A∩B| / |A∪B| | ✅ 完全一致 |
| **空集处理** | 两空集 → 0.0 | 两空集 → 0.0 | ✅ 一致 |
| **标签解析** | `split(",")` | `parseTagsString()` 支持 `,，;；` 多分隔符 | 🔺 Java 更灵活 |

### 3.2 QQ 加分机制

| 维度 | Python Demo | Java 后端 | 差异 |
|------|-------------|-----------|------|
| **匹配方式** | 学生QQ == 社团QQ **或** 学生QQ in 推荐QQ列表 | 用户 `recommendBoostClubId` == 社团ID **或** QQ 关键词子串匹配 | 🔺 机制不同 |
| **数据来源** | Excel `qq_group_id` + `recommend_qq_groups` | 数据库 `user.recommend_boost_club_id` + 社团 `qq_group` 字段 | ⚠️ 数据模型不同 |
| **加分逻辑** | 精确匹配 → 满分 | 精确匹配或子串匹配 → 满分 | 🔺 Java 支持模糊匹配 |

**Python Demo 逻辑：**
```python
def compute_qq_score(student_qq, club_qq, recommend_qq_set):
    if student_qq == club_qq or student_qq in recommend_qq_set:
        return QQ_BOOST_SCORE
    return 0.0
```

**Java 后端逻辑：**
```java
// 1. 优先使用用户设置的推荐加成社团
Integer effectiveBoostClubId = user.getRecommendBoostClubId();

// 2. 如果用户设置了 boost club，则对该 club 加分
if (qqBoostClubId != null && qqBoostClubId.equals(club.getId())) {
    qqPart = w.qqBoostScore;
}
// 3. 或者通过 QQ 群号子串匹配加分
else if (qqBoostMatches(club, qqNormKeyword)) {
    qqPart = w.qqBoostScore;
}
```

### 3.3 数据源对比

| 维度 | Python Demo | Java 后端 |
|------|-------------|-----------|
| **数据格式** | Excel (`.xlsx`) | MySQL 数据库 |
| **学生表** | `学生表` Sheet | `user` + `user_tag` 表 |
| **社团表** | `社团表` Sheet | `club` + `club_tag` 表 |
| **标签字典** | 无（直接使用字符串） | `tag` 表（ID ↔ 名称映射） |

---

## 四、架构设计对比

### 4.1 整体架构

**Python Demo：**
```
Excel 文件
    ↓
load_data() → preprocess() → compute_total_score() → recommend_for_student()
    ↓
排序取 Top-N → 打印结果
```

**Java 后端：**
```
MySQL 数据库
    ↓
RecommendationService.recommendForStudent(userId, qqBoostKeyword, qqBoostClubId)
    ├── RecommendationRuleMapper → 获取权重配置
    ├── UserTagMapper → 获取用户标签
    ├── ClubMapper → 获取所有社团
    ├── ClubTagProvider → 获取可见/隐藏标签
    ├── TagMatchingService → 计算 Jaccard 相似度
    └── 排序返回 ClubRecommendation 列表
```

### 4.2 扩展性对比

| 维度 | Python Demo | Java 后端 |
|------|-------------|-----------|
| **权重配置** | 硬编码，修改需改代码 | 数据库 `recommendation_rule` 表，可动态调整 |
| **标签体系** | 固定公开/隐藏标签 | 可见标签 + 隐藏标签 + 权重 + 描述 |
| **实体抽象** | 无（直接操作 DataFrame） | `EntityTagProvider` 接口 + 5个实现 |
| **推荐类型** | 仅社团推荐 | 社团/活动/投票/招募/问卷 + 综合推荐 |
| **跨社团匹配** | 无 | `ClubInterMemberMatchService` 社团间成员匹配 |
| **并发处理** | 单线程串行 | Spring 事务管理 + 连接池 |
| **API 接口** | 无（命令行工具） | RESTful API + Controller |

---

## 五、核心算法实现对比

### 5.1 Jaccard 相似度

**Python：**
```python
def jaccard_similarity(set1, set2):
    if not set1 and not set2:
        return 0.0
    inter = len(set1 & set2)
    union = len(set1 | set2)
    return inter / union if union > 0 else 0.0
```

**Java：**
```java
public double calculateJaccardSimilarity(Set<Integer> set1, Set<Integer> set2) {
    if (set1 == null || set2 == null) return 0.0;
    if (set1.isEmpty() && set2.isEmpty()) return 0.0;
    
    Set<Integer> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);
    Set<Integer> union = new HashSet<>(set1);
    union.addAll(set2);
    if (union.isEmpty()) return 0.0;
    
    return (double) intersection.size() / union.size();
}
```

**结论：** ✅ 算法逻辑完全一致

### 5.2 公开标签分

**Python：**
```python
def compute_tag_score(student_tags, club_public_tags):
    sim = jaccard_similarity(student_tags, club_public_tags)
    return sim * MAX_TAG_SCORE  # sim * 60.0
```

**Java：**
```java
double tagPart = jaccardVisibleTags(userTagIds, club) * w.maxTagScore;
// jaccardVisibleTags → TagMatchingService.calculateJaccardSimilarity
// maxTagScore 默认 60.0
```

**结论：** ✅ 算法逻辑完全一致

### 5.3 隐藏标签分

**Python：**
```python
def compute_hidden_score(student_tags, club_hidden_tags):
    hits = len(student_tags & club_hidden_tags)
    return min(hits * HIDDEN_MATCH_UNIT, MAX_HIDDEN_SCORE)
    # min(hits * 5.0, 20.0)
```

**Java：**
```java
int matches = tagMatchingService.countMatches(userTagIds, hiddenTagIds);
return Math.min(w.maxHiddenScore, matches * w.hiddenMatchUnit);
// Math.min(20.0, matches * 5.0)
```

**结论：** ✅ 算法逻辑完全一致

---

## 六、输出格式对比

### Python Demo 输出

```
【20240001】计算机学院 软件工程
兴趣标签：{'编程', '科技创新', '人工智能'}
  Top1: AI研究社 (总分=85.00, 相似度=85.00%)
  Top2: 机器人社 (总分=72.50, 相似度=72.50%)
  Top3: ...
```

### Java 后端 输出 (API 响应)

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "AI研究社",
      "description": "研究人工智能的社团",
      "tags": [1, 2, 3],
      "similarity": 0.85
    },
    {
      "id": 5,
      "name": "机器人社",
      "description": "机器人爱好者聚集地",
      "tags": [1, 5],
      "similarity": 0.725
    }
  ]
}
```

---

## 七、功能完备性矩阵

| 功能 | Python Demo | Java 后端 | 备注 |
|------|:-----------:|:---------:|------|
| 公开标签匹配 | ✅ | ✅ | 完全一致 |
| 隐藏标签匹配 | ✅ | ✅ | 完全一致 |
| QQ 加分 | ✅ | ✅ | 机制不同但效果类似 |
| Jaccard 相似度 | ✅ | ✅ | 完全一致 |
| 权重可配置 | ❌ | ✅ | Java 通过数据库配置 |
| 多实体推荐 | ❌ | ✅ | 活动/投票/招募/问卷 |
| 综合推荐 | ❌ | ✅ | UniversalRecommendation |
| 跨社团匹配 | ❌ | ✅ | ClubInterMemberMatch |
| 推荐状态管理 | ❌ | ✅ | 接受/拒绝推荐 |
| 社团AI总结 | ❌ | ✅ | AIController |
| 相似社团查找 | ❌ | ✅ | 基于成员标签匹配 |
| API 接口 | ❌ | ✅ | RESTful |
| 事务管理 | ❌ | ✅ | Spring Transaction |
| 权限控制 | ❌ | ✅ | JWT + 角色 |
| 分页查询 | ❌ | ✅ | 支持分页 |

---

## 八、差异总结

### 8.1 完全一致的部分 ✅

| 项目 | 说明 |
|------|------|
| 核心评分公式 | `总分 = 公开标签分 + 隐藏标签分 + QQ加分` |
| Jaccard 相似度计算 | 数学定义完全一致 |
| 默认权重配置 | `60 + 20 + 20 = 100` |
| 隐藏标签计算 | `min(命中数 × 5, 20)` |

### 8.2 存在差异的部分 ⚠️

| 项目 | Python Demo | Java 后端 | 影响 |
|------|-------------|-----------|------|
| QQ 加分逻辑 | 精确匹配学生QQ与社团QQ | 支持用户设置 boost club + QQ 子串匹配 | 匹配范围不同 |
| 标签存储 | 逗号分隔字符串 | 关联表 + isHidden 字段 | 查询方式不同 |
| 权重来源 | 硬编码 | 数据库可配置 | 灵活性不同 |
| 推荐范围 | 仅社团 | 社团+活动+投票+招募+问卷 | 功能覆盖不同 |

### 8.3 Java 后端独有功能 🔺

1. **`UniversalRecommendationService`** - 统一推荐服务，支持活动/投票/招募/问卷推荐
2. **`ClubInterMemberMatchService`** - 跨社团成员兴趣匹配，计算两个社团成员的 Jaccard 相似度
3. **`EntityTagProvider` 体系** - 统一标签抽象，支持关联表和字符串两种存储方式
4. **`RecommendationRule` 表** - 动态权重配置，无需修改代码
5. **AI 集成** - `AIController` 提供 AI 社团推荐和运营总结
6. **推荐记录管理** - 支持创建邀请、接受/拒绝推荐

---

## 九、结论

Python Demo 是 Java 后端推荐算法的 **核心逻辑原型**。两者在 **核心评分公式** 和 **Jaccard 相似度算法** 上完全一致，验证了算法实现的正确性。

Java 后端在 Python Demo 的基础上进行了以下扩展：

1. **数据持久化**：从 Excel 迁移到 MySQL
2. **权重可配置**：从硬编码到数据库动态配置
3. **多实体支持**：从单一社团推荐扩展到多种实体推荐
4. **工程化**：RESTful API、事务管理、权限控制、分页等
5. **高级功能**：跨社团匹配、AI 推荐、推荐状态管理

**一句话总结：** Python Demo 是算法原型，Java 后端是生产级实现，核心算法一致，后端在此基础上做了全面的工程化和功能扩展。
