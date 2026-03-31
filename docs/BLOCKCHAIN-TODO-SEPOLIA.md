# Blockchain TODO Placeholder (ETH Sepolia)

Mục tiêu: chuẩn bị khung tích hợp blockchain cho module chứng chỉ, hiện tại chưa bật thực thi on-chain.

## Trạng thái hiện tại

- Certificate đang lưu nội bộ DB.
- Các field blockchain (`transactionHash`, `blockNumber`, `contractAddress`) đã có trong schema.
- API verify hiện tại kiểm tra hash nội bộ, chưa gọi RPC blockchain.

## TODO triển khai (theo thứ tự)

1. **Dependency + Config**
   - [ ] Thêm dependency `web3j` vào `pom.xml`.
   - [ ] Dùng block config placeholder trong `application.properties`.
   - [ ] Đọc secret qua env (`BLOCKCHAIN_WALLET_PRIVATE_KEY`), không hardcode.

2. **Abstraction layer**
   - [ ] Tạo interface `BlockchainCertificateClient`.
   - [ ] Method đề xuất:
     - `issueCertificate(certificateId, certificateHash)` -> trả `txHash`, `blockNumber`, `contractAddress`
     - `verifyCertificate(certificateHash)` -> trả `isValid`, metadata tx
     - `revokeCertificate(certificateId, reason)` -> trả `txHash` revoke

3. **Service integration**
   - [ ] Tách logic verify hiện tại trong `AdminService` sang gọi `BlockchainCertificateClient` khi `app.blockchain.enabled=true`.
   - [ ] `revokeCertificate` cập nhật DB sau khi tx thành công (hoặc đánh dấu pending/failed nếu tx lỗi).
   - [ ] Chuẩn hóa `networkName` thành `Ethereum Sepolia`.

4. **Data model / migration**
   - [ ] Bổ sung cột nếu cần:
     - `network` (vd: `SEPOLIA`)
     - `last_verified_at`
     - `revoke_tx_hash`
     - `onchain_status` (`PENDING`, `CONFIRMED`, `FAILED`)

5. **Reliability**
   - [ ] Timeout + retry khi gọi RPC.
   - [ ] Idempotency key cho issue/revoke để tránh ghi trùng.
   - [ ] Log tx lifecycle (`submitted`, `mined`, `failed`).

6. **API contract (không đổi FE nhiều)**
   - [ ] Giữ response field hiện tại, chỉ làm dữ liệu thật hơn:
     - `blockchainInfo.transactionHash`
     - `blockchainInfo.blockNumber`
     - `blockchainInfo.contractAddress`
     - `blockchainInfo.status`

7. **Testing**
   - [ ] Unit test mock `BlockchainCertificateClient`.
   - [ ] Integration test luồng issue/verify/revoke khi bật `BLOCKCHAIN_ENABLED=true` (test env).

## Env gợi ý

- `BLOCKCHAIN_ENABLED=false`
- `BLOCKCHAIN_NETWORK=sepolia`
- `BLOCKCHAIN_CHAIN_ID=11155111`
- `BLOCKCHAIN_RPC_URL=https://sepolia.infura.io/v3/<project-id>`
- `BLOCKCHAIN_CONTRACT_ADDRESS=0x...`
- `BLOCKCHAIN_WALLET_PRIVATE_KEY=<secret>`
