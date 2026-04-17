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
