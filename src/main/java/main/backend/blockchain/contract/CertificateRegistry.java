package main.backend.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.5.2.
 */
@SuppressWarnings("rawtypes")
public class CertificateRegistry extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b50338061003557604051631e4fbdf760e01b81525f600482015260240160405180910390fd5b61003e81610044565b50610093565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b610900806100a05f395ff3fe608060405234801561000f575f80fd5b5060043610610060575f3560e01c8063142e154214610064578063291fd18f14610079578063715018a61461008c5780638da5cb5b146100945780638f2b91ea146100b3578063f2fde38b146100d6575b5f80fd5b6100776100723660046105d5565b6100e9565b005b61007761008736600461060f565b6101e6565b61007761032a565b5f546040516001600160a01b0390911681526020015b60405180910390f35b6100c66100c13660046105d5565b61033d565b6040516100aa94939291906106c4565b6100776100e4366004610701565b610480565b6100f16104bd565b600181604051610101919061072e565b9081526040519081900360200190206003015460ff166101795760405162461bcd60e51b815260206004820152602860248201527f4365727469666963617465206e6f7420666f756e64206f7220616c7265616479604482015267081c995d9bdad95960c21b60648201526084015b60405180910390fd5b5f60018260405161018a919061072e565b908152604051908190036020018120600301805492151560ff19909316929092179091557f266095b5af7e2323760fef151f1d78896740cd2cc1b73a6357ff9e4c3fdae740906101db908390610749565b60405180910390a150565b6101ee6104bd565b6001836040516101fe919061072e565b9081526040519081900360200190206003015460ff16156102615760405162461bcd60e51b815260206004820152601a60248201527f436572746966696361746520616c7265616479206578697374730000000000006044820152606401610170565b604051806080016040528084815260200183815260200182815260200160011515815250600184604051610295919061072e565b908152604051908190036020019020815181906102b290826107e1565b50602082015160018201906102c790826107e1565b5060408281015160028301556060909201516003909101805460ff1916911515919091179055517f3ca9b880c8645562f6f6dae2ec8c6da0e58ddcc82681ed677eb65aaaaf5877429061031d908590859061089d565b60405180910390a1505050565b6103326104bd565b61033b5f6104e9565b565b80516020818301810180516001825292820191909301209152805481906103639061075b565b80601f016020809104026020016040519081016040528092919081815260200182805461038f9061075b565b80156103da5780601f106103b1576101008083540402835291602001916103da565b820191905f5260205f20905b8154815290600101906020018083116103bd57829003601f168201915b5050505050908060010180546103ef9061075b565b80601f016020809104026020016040519081016040528092919081815260200182805461041b9061075b565b80156104665780601f1061043d57610100808354040283529160200191610466565b820191905f5260205f20905b81548152906001019060200180831161044957829003601f168201915b50505050600283015460039093015491929160ff16905084565b6104886104bd565b6001600160a01b0381166104b157604051631e4fbdf760e01b81525f6004820152602401610170565b6104ba816104e9565b50565b5f546001600160a01b0316331461033b5760405163118cdaa760e01b8152336004820152602401610170565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b634e487b7160e01b5f52604160045260245ffd5b5f82601f83011261055b575f80fd5b813567ffffffffffffffff8082111561057657610576610538565b604051601f8301601f19908116603f0116810190828211818310171561059e5761059e610538565b816040528381528660208588010111156105b6575f80fd5b836020870160208301375f602085830101528094505050505092915050565b5f602082840312156105e5575f80fd5b813567ffffffffffffffff8111156105fb575f80fd5b6106078482850161054c565b949350505050565b5f805f60608486031215610621575f80fd5b833567ffffffffffffffff80821115610638575f80fd5b6106448783880161054c565b94506020860135915080821115610659575f80fd5b506106668682870161054c565b925050604084013590509250925092565b5f5b83811015610691578181015183820152602001610679565b50505f910152565b5f81518084526106b0816020860160208601610677565b601f01601f19169290920160200192915050565b608081525f6106d66080830187610699565b82810360208401526106e88187610699565b6040840195909552505090151560609091015292915050565b5f60208284031215610711575f80fd5b81356001600160a01b0381168114610727575f80fd5b9392505050565b5f825161073f818460208701610677565b9190910192915050565b602081525f6107276020830184610699565b600181811c9082168061076f57607f821691505b60208210810361078d57634e487b7160e01b5f52602260045260245ffd5b50919050565b601f8211156107dc575f81815260208120601f850160051c810160208610156107b95750805b601f850160051c820191505b818110156107d8578281556001016107c5565b5050505b505050565b815167ffffffffffffffff8111156107fb576107fb610538565b61080f81610809845461075b565b84610793565b602080601f831160018114610842575f841561082b5750858301515b5f19600386901b1c1916600185901b1785556107d8565b5f85815260208120601f198616915b8281101561087057888601518255948401946001909101908401610851565b508582101561088d57878501515f19600388901b60f8161c191681555b5050505050600190811b01905550565b604081525f6108af6040830185610699565b82810360208401526108c18185610699565b9594505050505056fea264697066735822122076504f9a7e564120daeabaa1fb66765be4841f2815f4d079973e391b78dabcfb64736f6c63430008140033";

    public static final String FUNC_CERTIFICATES = "certificates";

    public static final String FUNC_ISSUECERTIFICATE = "issueCertificate";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_REVOKECERTIFICATE = "revokeCertificate";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event CERTIFICATEISSUED_EVENT = new Event("CertificateIssued", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event CERTIFICATEREVOKED_EVENT = new Event("CertificateRevoked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected CertificateRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CertificateRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CertificateRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CertificateRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<CertificateIssuedEventResponse> getCertificateIssuedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, transactionReceipt);
        ArrayList<CertificateIssuedEventResponse> responses = new ArrayList<CertificateIssuedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.certId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.certHash = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CertificateIssuedEventResponse getCertificateIssuedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, log);
        CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
        typedResponse.log = log;
        typedResponse.certId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.certHash = (String) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateIssuedEventFromLog(log));
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEISSUED_EVENT));
        return certificateIssuedEventFlowable(filter);
    }

    public static List<CertificateRevokedEventResponse> getCertificateRevokedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, transactionReceipt);
        ArrayList<CertificateRevokedEventResponse> responses = new ArrayList<CertificateRevokedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.certId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CertificateRevokedEventResponse getCertificateRevokedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, log);
        CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
        typedResponse.log = log;
        typedResponse.certId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateRevokedEventFromLog(log));
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEREVOKED_EVENT));
        return certificateRevokedEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple4<String, String, BigInteger, Boolean>> certificates(String param0) {
        final Function function = new Function(FUNC_CERTIFICATES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple4<String, String, BigInteger, Boolean>>(function,
                new Callable<Tuple4<String, String, BigInteger, Boolean>>() {
                    @Override
                    public Tuple4<String, String, BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, String, BigInteger, Boolean>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (Boolean) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> issueCertificate(String _certId, String _certHash, BigInteger _issueDate) {
        final Function function = new Function(
                FUNC_ISSUECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_certId), 
                new org.web3j.abi.datatypes.Utf8String(_certHash), 
                new org.web3j.abi.datatypes.generated.Uint256(_issueDate)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revokeCertificate(String _certId) {
        final Function function = new Function(
                FUNC_REVOKECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_certId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CertificateRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CertificateRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CertificateRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CertificateRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CertificateRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CertificateRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CertificateRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<CertificateRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<CertificateRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<CertificateRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class CertificateIssuedEventResponse extends BaseEventResponse {
        public String certId;

        public String certHash;
    }

    public static class CertificateRevokedEventResponse extends BaseEventResponse {
        public String certId;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
