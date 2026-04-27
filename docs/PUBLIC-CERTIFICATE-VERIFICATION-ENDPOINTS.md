# PUBLIC CERTIFICATE VERIFICATION ENDPOINTS

## 1) Muc tieu

Tai lieu nay mo ta 3 endpoint public cho trang tuyen dung xac minh chung chi:

- Search hoc sinh theo ten hoac ma hoc sinh
- Lay chi tiet hoc sinh va full danh sach certificate
- Xac minh certificate tren blockchain

Tat ca endpoint trong tai lieu nay la GET va khong can dang nhap.

## 2) Base URL va quyen truy cap

- Base URL: /api/public
- Auth: Khong can JWT, khong can OAuth2 login
- Pham vi truy cap: Public (read-only)

## 3) Envelope response chung

Tat ca response su dung ApiResponse:

```json
{
  "success": true,
  "message": null,
  "data": {},
  "error": null
}
```

Loi thong thuong:

```json
{
  "success": false,
  "error": "..."
}
```

## 4) GET /api/public/students/search

### Muc dich

Tim danh sach hoc sinh theo student code hoac ten hoc sinh.

### Query params

- q: string, optional, mac dinh ""
- page: int, optional, mac dinh 1
- limit: int, optional, mac dinh 20

Gioi han backend:

- page < 1 => tu dong ve 1
- limit < 1 => tu dong ve 1
- limit > 100 => tu dong ve 100

### Tim theo gi

- userCode contains q
- fullName contains q
- Chi lay user co role STUDENT va isActive = true

### Output

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "studentId": "USR-STU-001",
        "studentCode": "HS00001",
        "studentName": "Le Minh Tuan",
        "avatarUrl": null
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

## 5) GET /api/public/students/{studentCode}

### Muc dich

Lay chi tiet 1 hoc sinh theo student code va full danh sach certificate cua hoc sinh do.

### Path params

- studentCode: string, required. Vi du: HS00001

### Tim theo gi

- Tim User theo userCode = studentCode
- Bat buoc role STUDENT
- Bat buoc isActive = true
- Lay tat ca certificate theo studentId

### Sap xep certificates

- issueDate giam dan (moi nhat truoc)
- Neu issueDate bang nhau thi createdAt giam dan

### Output

```json
{
  "success": true,
  "data": {
    "student": {
      "studentId": "USR-STU-001",
      "studentCode": "HS00001",
      "studentName": "Le Minh Tuan",
      "avatarUrl": null
    },
    "certificates": [
      {
        "certificateId": "CERT_1712345678900_ab12cd34",
        "classId": "CLS-001",
        "classCode": "SD18301",
        "courseCode": "JAVA6",
        "courseName": "Lap trinh Java 6",
        "issuedAt": "2026-04-27T00:00:00Z",
        "status": "issued",
        "verificationHash": "0x...",
        "transactionHash": "0x...",
        "contractAddress": "0x..."
      }
    ]
  }
}
```

## 6) GET /api/public/certificates/{certificateId}/verify

### Muc dich

Xac minh 1 certificate bang certificateId, doi chieu DB hash va on-chain hash.

### Path params

- certificateId: string, required

### Backend verify logic

- Tim certificate trong DB theo certificateId
- Recompute expected hash tu du lieu DB: studentId:classId:certificateId:issueDateEpochDay
- Doc certificate tren chain theo certificateId
- So sanh:
  - dbHash == expectedHash
  - dbHash == onChainHash
  - onChain certId dung
  - onChain isValid = true

### verificationStatus

- confirmed: du dieu kien hop le toan bo
- revoked: hash khop nhung on-chain isValid = false
- mismatch: con lai (sai hash, sai certId, khong dong bo)

### Output

```json
{
  "success": true,
  "data": {
    "certificateId": "CERT_1712345678900_ab12cd34",
    "isValid": true,
    "verificationHash": "0x...",
    "verificationStatus": "confirmed",
    "verifiedAt": "2026-04-27T09:08:07Z",
    "student": {
      "studentId": "USR-STU-001",
      "studentCode": "HS00001",
      "studentName": "Le Minh Tuan",
      "avatarUrl": null
    },
    "certificate": {
      "certificateId": "CERT_1712345678900_ab12cd34",
      "classId": "CLS-001",
      "classCode": "SD18301",
      "courseCode": "JAVA6",
      "courseName": "Lap trinh Java 6",
      "issuedAt": "2026-04-27T00:00:00Z",
      "status": "issued",
      "verificationHash": "0x...",
      "transactionHash": "0x...",
      "contractAddress": "0x..."
    },
    "blockchainInfo": {
      "transactionHash": "0x...",
      "contractAddress": "0x...",
      "networkName": "Ethereum Sepolia",
      "explorerUrl": "https://sepolia.etherscan.io/tx/0x...",
      "onChainStatus": "confirmed"
    }
  }
}
```

## 7) HTTP status va loi

- 200: Thanh cong
- 404: studentCode hoac certificateId khong ton tai
- 500: Loi he thong hoac loi blockchain read khong mong muon

## 8) Quick test bang REST Client

```http
GET /api/public/students/search?q=HS00001&page=1&limit=20
GET /api/public/students/HS00001
GET /api/public/certificates/CERT_1712345678900_ab12cd34/verify
```
