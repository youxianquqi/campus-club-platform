import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import CreateClub from '../views/CreateClub.vue';
import ClubManage from '../views/ClubManage.vue';
import ClubApply from '../views/ClubApply.vue';
import Chat from '../views/Chat.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import ClubList from '../views/ClubList.vue';
import ClubDetail from '../views/ClubDetail.vue';
import Profile from '../views/Profile.vue';
import TagSelection from '../views/TagSelection.vue';
import Activities from '../views/Activities.vue';
import ActivityDetail from '../views/ActivityDetail.vue';
import Recruitment from '../views/Recruitment.vue';
import RecruitmentDetail from '../views/RecruitmentDetail.vue';
import Footprint from '../views/Footprint.vue';
import KnowledgeBase from '../views/KnowledgeBase.vue';
import KnowledgeDetail from '../views/KnowledgeDetail.vue';
import KnowledgeCreate from '../views/KnowledgeCreate.vue';
import KnowledgeList from '../views/KnowledgeList.vue';
import QuestionnaireList from '../views/QuestionnaireList.vue';
import QuestionnaireDetail from '../views/QuestionnaireDetail.vue';
import CreateQuestionnaire from '../views/CreateQuestionnaire.vue';
import VoteList from '../views/VoteList.vue';
import VoteDetail from '../views/VoteDetail.vue';
import CreateVote from '../views/CreateVote.vue';
import BrainstormHub from '../views/BrainstormHub.vue';
import MyInvitations from '../views/MyInvitations.vue';
import AdminLayout from '../views/admin/AdminLayout.vue';
import AdminDashboard from '../views/admin/AdminDashboard.vue';
import AdminClubAudit from '../views/admin/AdminClubAudit.vue';
import AdminActivityAudit from '../views/admin/AdminActivityAudit.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/create-club',
    name: 'CreateClub',
    component: CreateClub,
    meta: { requiresAuth: true },
  },
  {
    path: '/club-manage',
    name: 'ClubManage',
    component: ClubManage,
    meta: { requiresAuth: true },
  },
  {
    path: '/clubs',
    name: 'ClubList',
    component: ClubList,
  },
  {
    path: '/club/:id',
    name: 'ClubDetail',
    component: ClubDetail,
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
  },
  {
    path: '/tag-match',
    redirect: '/profile',
  },
  {
    path: '/tag-selection',
    name: 'TagSelection',
    component: TagSelection,
    meta: { requiresAuth: true },
  },
  {
    path: '/my-invitations',
    name: 'MyInvitations',
    component: MyInvitations,
    meta: { requiresAuth: true },
  },
  {
    path: '/activities',
    name: 'Activities',
    component: Activities,
  },
  {
    path: '/activity/:id',
    name: 'ActivityDetail',
    component: ActivityDetail,
  },
  {
    path: '/recruitment',
    name: 'Recruitment',
    component: Recruitment,
  },
  {
    path: '/recruitment/:id',
    name: 'RecruitmentDetail',
    component: RecruitmentDetail,
  },
  {
    path: '/footprint',
    name: 'Footprint',
    component: Footprint,
    meta: { requiresAuth: true },
  },
  {
    path: '/knowledge',
    name: 'KnowledgeBase',
    component: KnowledgeBase,
  },
  {
    path: '/knowledge/create',
    name: 'KnowledgeCreate',
    component: KnowledgeCreate,
    meta: { requiresAuth: true },
  },
  {
    path: '/knowledge/edit/:id',
    name: 'KnowledgeEdit',
    component: KnowledgeCreate,
    meta: { requiresAuth: true },
  },
  {
    path: '/knowledge/my',
    name: 'KnowledgeList',
    component: KnowledgeList,
    meta: { requiresAuth: true },
  },
  {
    path: '/knowledge/:id',
    name: 'KnowledgeDetail',
    component: KnowledgeDetail,
  },
  {
    path: '/brainstorm',
    name: 'BrainstormHub',
    component: BrainstormHub,
  },
  {
    path: '/questionnaires',
    name: 'QuestionnaireList',
    component: QuestionnaireList,
  },
  {
    path: '/questionnaires/club/:clubId',
    name: 'QuestionnaireListByClub',
    component: QuestionnaireList,
  },
  {
    path: '/votes',
    name: 'VoteList',
    component: VoteList,
  },
  {
    path: '/votes/club/:clubId',
    name: 'VoteListByClub',
    component: VoteList,
  },
  {
    path: '/questionnaires/create',
    name: 'CreateQuestionnaire',
    component: CreateQuestionnaire,
    meta: { requiresAuth: true },
  },
  {
    path: '/questionnaires/:id',
    name: 'QuestionnaireDetail',
    component: QuestionnaireDetail,
  },
  {
    path: '/votes/create',
    name: 'CreateVote',
    component: CreateVote,
    meta: { requiresAuth: true },
  },
  {
    path: '/votes/:id',
    name: 'VoteDetail',
    component: VoteDetail,
  },
  {
    path: '/club-apply/:id?',
    name: 'ClubApply',
    component: ClubApply,
    meta: { requiresAuth: true },
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat,
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: AdminDashboard,
      },
      {
        path: 'clubs/pending',
        name: 'AdminClubAudit',
        component: AdminClubAudit,
      },
      {
        path: 'activities/pending',
        name: 'AdminActivityAudit',
        component: AdminActivityAudit,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const loggedIn = localStorage.getItem('isLoggedIn') === 'true';
  if (to.meta.requiresAuth && !loggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }
  if (to.meta.requiresAdmin) {
    const role = Number(localStorage.getItem('userRole') || '0');
    if (role !== 3) {
      next({ path: '/', replace: true });
      return;
    }
  }
  next();
});

export default router;
