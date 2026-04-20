# 🏗️ Hướng dẫn Chi Tiết: Giai Đoạn 1 - Phát triển Smart Contract (Hardhat TypeScript)

Tài liệu này cung cấp hướng dẫn chi tiết từng bước để xây dựng, biên dịch và triển khai Smart Contract lên mạng Ethereum Sepolia testnet sử dụng **Hardhat (Minimum Setup - TypeScript)**. Việc tích hợp đã được tối giản hóa, lược bỏ các plugin frontend và testing framework (Mocha/Chai) không cần thiết cho backend.

---

## 🛠️ Chuẩn bị (Môi trường VS Code)

1. **Node.js**: Phiên bản 18.x hoặc mới hơn.
2. **VS Code Extensions cần thiết:**
   - **Solidity** (bởi _Nomic Foundation_).
   - **DotENV**.

---

## 🚀 CÁC BƯỚC THỰC HIỆN

### Bước 1: Cấu hình Hardhat (TypeScript & Infura)

Mục tiêu: Hoàn thiện cấu trúc dự án Hardhat TypeScript tối giản. Hệ thống sử dụng Infura làm RPC node.

1. Đảm bảo cấu trúc thư mục (tại `blockchain/`):
   ```bash
   mkdir -p contracts scripts
   ```
2. Cài đặt các package cốt lõi:
   ```bash
   npm install --save-dev hardhat dotenv typescript ts-node @types/node @nomicfoundation/hardhat-ethers ethers
   npm install @openzeppelin/contracts
   ```
3. Thiết lập file `.env` (dựa trên cấu hình hiện tại của bạn):
   ```env
   TESTNET_PRIVATE_KEY=your_private_key
   ETHERSCAN_API_KEY=your_etherscan_key
   INFURA_API_KEY=your_infura_key
   REPORT_GAS=true
   ```
4. Cấu hình file `hardhat.config.ts`:
   Thay thế toàn bộ nội dung file `hardhat.config.ts` bằng cấu hình tối giản sau:

   ```typescript
   import { HardhatUserConfig } from "hardhat/config";
   import "@nomicfoundation/hardhat-ethers";
   import * as dotenv from "dotenv";

   dotenv.config();

   const config: HardhatUserConfig = {
     solidity: "0.8.20",
     networks: {
       sepolia: {
         url: process.env.INFURA_API_KEY
           ? `https://sepolia.infura.io/v3/${process.env.INFURA_API_KEY}`
           : "",
         accounts: process.env.TESTNET_PRIVATE_KEY
           ? [process.env.TESTNET_PRIVATE_KEY]
           : [],
       },
     },
   };

   export default config;
   ```

### Bước 2: Viết Smart Contract (`CertificateRegistry.sol`)

1. Tạo file `contracts/CertificateRegistry.sol`.
2. Mã nguồn:

   ```solidity
   // SPDX-License-Identifier: MIT
   pragma solidity ^0.8.20;

   import "@openzeppelin/contracts/access/Ownable.sol";

   contract CertificateRegistry is Ownable {
       struct Certificate {
           string certId;
           string certHash;
           uint256 issueDate;
           bool isValid;
       }

       mapping(string => Certificate) public certificates;

       event CertificateIssued(string certId, string certHash);
       event CertificateRevoked(string certId);

       // Pass msg.sender to Ownable constructor for OpenZeppelin ^5.0.0
       constructor() Ownable(msg.sender) {}

       function issueCertificate(string memory _certId, string memory _certHash, uint256 _issueDate) public onlyOwner {
           require(!certificates[_certId].isValid, "Certificate already exists");
           certificates[_certId] = Certificate(_certId, _certHash, _issueDate, true);
           emit CertificateIssued(_certId, _certHash);
       }

       function revokeCertificate(string memory _certId) public onlyOwner {
           require(certificates[_certId].isValid, "Certificate not found or already revoked");
           certificates[_certId].isValid = false;
           emit CertificateRevoked(_certId);
       }
   }
   ```

### Bước 3: Compile & Lấy ABI, Bytecode

1. Dịch Contract:
   ```bash
   npx hardhat compile
   ```
2. Lấy Artifacts:
   - File JSON chứa ABI và Bytecode sẽ nằm tại: `artifacts/contracts/CertificateRegistry.sol/CertificateRegistry.json`.

### Bước 4: Deploy lên Sepolia Testnet

1. Tạo file script `scripts/deploy.ts`:

   ```typescript
   import { ethers } from "hardhat";

   async function main() {
     const factory = await ethers.getContractFactory("CertificateRegistry");
     const registry = await factory.deploy();

     await registry.waitForDeployment();
     const address = await registry.getAddress();

     console.log("CertificateRegistry deployed to:", address);
   }

   main().catch((error) => {
     console.error(error);
     process.exitCode = 1;
   });
   ```

2. Chạy lệnh Deploy:
   ```bash
   npx hardhat run scripts/deploy.ts --network sepolia
   ```
3. **Lưu trữ Contract Address:**
   Copy địa chỉ xuất ra từ Terminal và thêm vào `application.properties` của Backend Java.
