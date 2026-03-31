# Frontend: Cập nhật OAuth2RedirectHandler.vue

> **Ticket**: Cập nhật xử lý redirect sau OAuth2 login
> **Lý do**: Backend đã thêm `role` và `redirect` params vào redirect URL

---

## Thay đổi từ Backend

**Trước đây:**

```
/oauth2/redirect?token=eyJ...
```

**Bây giờ:**

```
/oauth2/redirect?token=eyJ...&role=STUDENT&redirect=/student/dashboard
```

| Param      | Mô tả                             |
| ---------- | --------------------------------- |
| `token`    | JWT token (như cũ)                |
| `role`     | `STUDENT` \| `TEACHER` \| `ADMIN` |
| `redirect` | Path dashboard tương ứng với role |

---

## Code cần sửa

### File: `src/views/auth/OAuth2RedirectHandler.vue`

```vue
<script setup lang="ts">
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const route = useRoute();
const router = useRouter();
const auth = useAuth();

onMounted(() => {
  // 1. Đọc params từ URL
  const token = route.query.token as string;
  const role = route.query.role as string; // MỚI
  const redirect = route.query.redirect as string; // MỚI
  const error = route.query.error as string;

  // 2. Xử lý lỗi
  if (error) {
    console.error("OAuth error:", error);
    router.push("/auth/login?error=" + error);
    return;
  }

  // 3. Xử lý thành công
  if (token && role && redirect) {
    // Lưu token và role
    auth.setToken(token);
    auth.setRole(role); // MỚI: cần thêm method này vào useAuth

    // Redirect theo path từ backend
    router.push(redirect);
  } else {
    // Fallback nếu thiếu params
    router.push("/auth/login?error=missing_params");
  }
});
</script>

<template>
  <div class="flex items-center justify-center h-screen">
    <p>Đang xử lý đăng nhập...</p>
  </div>
</template>
```

---

## Cập nhật useAuth (nếu chưa có)

### File: `src/composables/useAuth.ts`

Thêm `setRole` method nếu chưa có:

```typescript
// Thêm state
const role = ref<string | null>(localStorage.getItem("role"));

// Thêm computed
const userRole = computed(() => role.value);

// Thêm method
function setRole(newRole: string) {
  role.value = newRole;
  localStorage.setItem("role", newRole);
}

// Export thêm
return {
  // ... existing
  role,
  userRole,
  setRole,
};
```

---

## Checklist

- [ ] Đọc thêm `role` và `redirect` từ `route.query`
- [ ] Gọi `auth.setRole(role)` để lưu role
- [ ] Dùng `router.push(redirect)` thay vì hardcode path
- [ ] Thêm `setRole` method vào `useAuth` composable
- [ ] Test: Login → verify redirect đúng dashboard theo role

---

## Test cases

| Email đăng nhập   | Role trong DB | Expected redirect    |
| ----------------- | ------------- | -------------------- |
| student@gmail.com | STUDENT       | `/student/dashboard` |
| teacher@gmail.com | TEACHER       | `/teacher/dashboard` |
| admin@gmail.com   | ADMIN         | `/admin/dashboard`   |

**Lưu ý**: Role được xác định từ DB, không phải từ email. Admin cần dùng API `PUT /api/admin/users/{id}/role` để đổi role user.
