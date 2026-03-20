import { useAuth } from '@/composables/useAuth';
import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        // Auth routes (outside AppLayout)
        {
            path: '/auth/login',
            name: 'login',
            component: () => import('@/views/auth/LoginGoogle.vue'),
            meta: { requiresAuth: false }
        },
        // OAuth2 redirect handler
        {
            path: '/oauth2/redirect',
            name: 'oauth2Redirect',
            component: () => import('@/views/auth/OAuth2RedirectHandler.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/templates/pages/auth/Access.vue')
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/templates/pages/auth/Error.vue')
        },

        // Main app layout (with sidebar, topbar)
        {
            path: '/',
            component: AppLayout,
            children: [
                {
                    path: '/',
                    name: 'dashboard',
                    component: () => import('@/templates/Dashboard.vue')
                },

                // Student routes
                {
                    path: '/student/dashboard',
                    name: 'studentDashboard',
                    component: () => import('@/views/student/StudentDashboard.vue'),
                    meta: { requiresAuth: true, roles: ['STUDENT'] }
                },
                {
                    path: '/student/course/:id',
                    name: 'courseDetail',
                    component: () => import('@/views/student/CourseDetail.vue'),
                    meta: { requiresAuth: true, roles: ['STUDENT'] }
                },
                {
                    path: '/student/quiz/:quizId',
                    name: 'quizPage',
                    component: () => import('@/views/student/QuizPage.vue'),
                    meta: { requiresAuth: true, roles: ['STUDENT'] }
                },
                {
                    path: '/student/certificates',
                    name: 'studentCertificates',
                    component: () => import('@/views/student/CertificatesPage.vue'),
                    meta: { requiresAuth: true, roles: ['STUDENT'] }
                },

                // Teacher routes
                {
                    path: '/teacher/dashboard',
                    name: 'teacherDashboard',
                    component: () => import('@/views/teacher/TeacherDashboard.vue'),
                    meta: { requiresAuth: true, roles: ['TEACHER'] }
                },
                {
                    path: '/teacher/class/:id',
                    name: 'classDetail',
                    component: () => import('@/views/teacher/ClassDetail.vue'),
                    meta: { requiresAuth: true, roles: ['TEACHER'] }
                },
                {
                    path: '/teacher/quiz/:classId',
                    name: 'quizManagement',
                    component: () => import('@/views/teacher/QuizManagement.vue'),
                    meta: { requiresAuth: true, roles: ['TEACHER'] }
                },
                {
                    path: '/teacher/quiz/:classId/submissions',
                    name: 'quizSubmissions',
                    component: () => import('@/views/teacher/QuizSubmissionsPage.vue'),
                    meta: { requiresAuth: true, roles: ['TEACHER'] }
                },

                // Admin routes
                {
                    path: '/admin/dashboard',
                    name: 'adminDashboard',
                    component: () => import('@/views/admin/AdminDashboard.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },
                {
                    path: '/admin/certificate/:id',
                    name: 'certificateDetail',
                    component: () => import('@/views/admin/CertificateManagement.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },
                {
                    path: '/admin/users',
                    name: 'userManagement',
                    component: () => import('@/views/admin/UserManagementPage.vue'),
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },

                // Template routes (kept for reference/development)
                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/templates/uikit/FormLayout.vue')
                },
                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/templates/uikit/InputDoc.vue')
                },
                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/templates/uikit/ButtonDoc.vue')
                },
                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/templates/uikit/TableDoc.vue')
                },
                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/templates/uikit/ListDoc.vue')
                },
                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/templates/uikit/TreeDoc.vue')
                },
                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/templates/uikit/PanelsDoc.vue')
                },
                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/templates/uikit/OverlayDoc.vue')
                },
                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/templates/uikit/MediaDoc.vue')
                },
                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/templates/uikit/MessagesDoc.vue')
                },
                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/templates/uikit/FileDoc.vue')
                },
                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/templates/uikit/MenuDoc.vue')
                },
                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/templates/uikit/ChartDoc.vue')
                },
                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/templates/uikit/MiscDoc.vue')
                },
                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/templates/uikit/TimelineDoc.vue')
                },
                {
                    path: '/blocks/free',
                    name: 'blocks',
                    meta: {
                        breadcrumb: ['Prime Blocks', 'Free Blocks']
                    },
                    component: () => import('@/templates/utilities/Blocks.vue')
                },
                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/templates/pages/Empty.vue')
                },
                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/templates/pages/Crud.vue')
                },
                {
                    path: '/start/documentation',
                    name: 'documentation',
                    component: () => import('@/templates/pages/Documentation.vue')
                }
            ]
        },

        // Other routes
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/templates/pages/Landing.vue')
        },

        // 404 route
        {
            path: '/:pathMatch(.*)*',
            name: 'notfound',
            component: () => import('@/templates/pages/NotFound.vue')
        }
    ]
});

// Route guards for authentication and role-based access
router.beforeEach((to, from, next) => {
    const auth = useAuth();
    const requiresAuth = to.meta.requiresAuth ?? false;
    const requiredRoles = to.meta.roles || [];

    if (requiresAuth) {
        if (!auth.isAuthenticated.value) {
            next({ name: 'login' });
            return;
        }
        if (requiredRoles.length && !requiredRoles.includes(auth.userRole.value)) {
            next({ name: 'accessDenied' });
            return;
        }
    }
    next();
});

export default router;
