import hardhatToolboxMochaEthersPlugin from "@nomicfoundation/hardhat-toolbox-mocha-ethers";
import { defineConfig } from "hardhat/config";
import * as dotenv from "dotenv";

dotenv.config();

export default defineConfig({
  plugins: [hardhatToolboxMochaEthersPlugin],
  solidity: {
    profiles: {
      default: {
        version: "0.8.20", // Downgraded matching your contract (pragma solidity ^0.8.20)
      },
      production: {
        version: "0.8.20",
        settings: {
          optimizer: {
            enabled: true,
            runs: 200,
          },
        },
      },
    },
  },
  networks: {
    hardhatMainnet: {
      type: "edr-simulated",
      chainType: "l1",
    },
    sepolia: {
      type: "http",
      chainType: "l1",
      url: process.env.INFURA_API_KEY
        ? `https://sepolia.infura.io/v3/${process.env.INFURA_API_KEY}`
        : "https://rpc.sepolia.org",
      accounts: process.env.TESTNET_PRIVATE_KEY
        ? [process.env.TESTNET_PRIVATE_KEY]
        : [],
    },
  },
});
