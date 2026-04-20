# 🚀 Kế Hoạch Tích Hợp Smart Contract (Sepolia) & API Cấp Bằng

Tài liệu này vạch ra lộ trình từ A-Z: từ việc viết mã nguồn Smart Contract (Solidity), dùng Hardhat deploy lên mạng Sepolia, cho tới việc tích hợp Web3j vào Backend Spring Boot để gọi hàm cấp bằng (issue) cho từng giao dịch.

---

## 🏗️ GIAI ĐOẠN 1: SMART CONTRACT (SOLIDITY & HARDHAT)

Do chúng ta cấp mỗi giao dịch 1 bằng (one-by-one), Smart Contract cần có các hàm nhận vào ID chứng chỉ và Hash của chứng chỉ đó.

### 1. Khởi tạo & Cấu hình Hardhat

- Tạo thư mục mới (ví dụ: `blockchain/`) nằm cùng cấp hoặc bên trong dự án.
- Chạy `npm init -y` và `npm install --save-dev hardhat @nomicfoundation/hardhat-toolbox dotenv`.
- Khởi tạo project: `npx hardhat init`.
- Cấu hình `hardhat.config.js` với mạng `sepolia`:
  - Cần `SEPOLIA_RPC_URL` (từ Alchemy/Infura).
  - Cần `PRIVATE_KEY` (Ví metamask chứa Sepolia ETH để làm phí gas).

### 2. Viết Smart Contract (`CertificateRegistry.sol`)

Viết contract quản lý chứng chỉ với cấu trúc cơ bản:

- **Struct / Mapping**: Lưu trữ trạng thái chứng chỉ theo `certificateId` hoặc `certificateHash`.
  - `mapping(string => Certificate) public certificates;`
- **Hàm `issueCertificate`**:
  - Input: `string memory certId`, `string memory certHash`, `uint256 issueDate`
  - Logic: Kiểm tra xem chứng chỉ đã tồn tại chưa, gán thông tin, emit event `CertificateIssued`.
- **Hàm `revokeCertificate`**:
  - Input: `string memory certId`
  - Logic: Chuyển trạng thái chứng chỉ sang invalid, emit event `CertificateRevoked`.
- **Owner (Phân quyền)**: Contract kế thừa `Ownable` để đảm bảo chỉ Admin Backend (ví deploy) mới được phép gọi hàm `issue` và `revoke`.

### 3. Compile & Lấy ABI, Bytecode

- Chạy lệnh: `npx hardhat compile`
- Hardhat sẽ sinh ra thư mục `artifacts/`. Lấy hai phần cực kỳ quan trọng để tích hợp Java:
  - File JSON chứa **ABI** (Application Binary Interface).
  - Chuỗi **Bytecode** (.bin).

### 4. Deploy lên Sepolia testnet

- Viết file `scripts/deploy.js`.
- Chạy lệnh deploy: `npx hardhat run scripts/deploy.js --network sepolia`
- **Output:** Nhận được `CONTRACT_ADDRESS` trên mạng Sepolia.

---

## ⚙️ GIAI ĐOẠN 2: TÍCH HỢP SPRING BOOT (WEB3J)

### 1. Thêm Thư viện Web3j

Bổ sung thư viện vào `pom.xml`:

```xml
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>5.0.0</version>
</dependency>
```

### 2. Sinh Java Wrapper Class từ ABI

Sử dụng công cụ `web3j-cli` để dịch file `.abi` và `.bin` (từ bước Hardhat) sang class Java.

- _Câu lệnh mẫu:_ `web3j generate solidity -a CertificateRegistry.abi -b CertificateRegistry.bin -o src/main/java -p main.backend.blockchain`
- Class sinh ra sẽ hỗ trợ gọi hàm Smart Contract trong Java như gọi class thông thường.

### 3. Cấu hình biến môi trường (`application.properties`)

Bổ sung các tham số cần thiết:

```properties
blockchain.rpc-url=https://eth-sepolia.g.alchemy.com/v2/YOUR_API_KEY
blockchain.private-key=YOUR_METAMASK_PRIVATE_KEY
blockchain.contract-address=0x_YOUR_DEPLOYED_CONTRACT_ADDRESS
```

### 4. Tạo `BlockchainClientService`

Tạo class xử lý Service để trừu tượng hóa web3j:

- Load `Credentials` từ private key.
- Kết nối tới `Web3j.build(new HttpService(rpcUrl))`.
- Khởi tạo Wrapper Class: `CertificateRegistry.load(contractAddress, web3j, credentials, gasProvider)`.
- Cung cấp hàm `issueCertificateOnChain(certId, certHash)`:
  - Trả về `TransactionReceipt` (chứa `transactionHash` và `blockNumber`).

---

## 🔌 GIAI ĐOẠN 3: TRIỂN KHAI API BACKEND

### 1. API: Cấp Bằng Mới (`POST /api/admin/certificates/issue`)

- Nhận input: `studentId`, `classId`.
- **Validation**: Đảm bảo sinh viên nằm trong danh sách thi đạt của lớp.
- **Tạo Hash**: Generate `certificateHash` từ (studentId + classId + timestamp) bằng bộ băm SHA-256.
- **Gọi On-chain**:
  - Gọi tới `BlockchainClientService.issueCertificateOnChain(certId, certHash)`.
  - Luồng sẽ bị block (Synchronous) khoảng 12s - 15s để chờ Sepolia confirm giao dịch.
- **Lưu DB**:
  - Ghi bản ghi mới vào bảng `certificates` với trạng thái `ISSUED`.
  - Lưu lại `transaction_hash`, `block_number`, `contract_address`, `certificate_hash`.
- Return JSON cho FE kèm trạng thái giao dịch.

### 2. API: Thu hồi Bằng (`POST /api/admin/certificates/{certId}/revoke`)

- Tương tự như Issue: Cần gọi lên Smart Contract hàm `revokeCertificateOnChain(...)` để ghi nhận sự kiện thu hồi trên blockchain.
- Đợi Transaction Receipt, sau đó cập nhật status `REVOKED` vào DB.

---

## 🎯 NHỮNG LƯU Ý DÀNH CHO TEAM FRONTEND

1. **API Loading Time**: Thời gian sinh một block trên Ethereum ~12s. Do chúng ta gọi API dạng đồng bộ (đại diện Backend ký và phát tx trực tiếp), FE khi gọi nút `Cấp bằng` bắt buộc phải **hiển thị Loading/Spinner** và tăng `timeout` của Axios lên tối thiểu 60s để tránh lỗi Network Timeout.
2. Dữ liệu Blockchain bao gồm `transactionHash`, `blockNumber` sau khi có thành công mới được Backend lưu lại và trả về cho FE (đồng bộ lên trang chi tiết bằng Blockchain).
