import hre from "hardhat";

async function main() {
  const factory = await hre.ethers.getContractFactory("CertificateRegistry");
  const registry = await factory.deploy();

  await registry.waitForDeployment();
  const address = await registry.getAddress();

  console.log("CertificateRegistry deployed to:", address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
