package transform.transaction;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.common.hash.HashCode;

import data.transaction.TransactionData;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import qora.transaction.Transaction.TransactionType;
import transform.TransformationException;
import transform.Transformer;
import utils.Base58;

public class TransactionTransformer extends Transformer {

	private static final Logger LOGGER = LogManager.getLogger(TransactionTransformer.class);

	protected static final int TYPE_LENGTH = INT_LENGTH;
	protected static final int REFERENCE_LENGTH = SIGNATURE_LENGTH;
	protected static final int FEE_LENGTH = BIG_DECIMAL_LENGTH;
	protected static final int BASE_TYPELESS_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	public static TransactionData fromBytes(byte[] bytes) throws TransformationException {
		if (bytes == null)
			return null;

		if (bytes.length < TYPE_LENGTH)
			throw new TransformationException("Byte data too short to determine transaction type");

		LOGGER.trace("tx hex: " + HashCode.fromBytes(bytes).toString());

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

		TransactionType type = TransactionType.valueOf(byteBuffer.getInt());
		if (type == null)
			return null;

		try {
			switch (type) {
				case GENESIS:
					return GenesisTransactionTransformer.fromByteBuffer(byteBuffer);

				case PAYMENT:
					return PaymentTransactionTransformer.fromByteBuffer(byteBuffer);

				case REGISTER_NAME:
					return RegisterNameTransactionTransformer.fromByteBuffer(byteBuffer);

				case UPDATE_NAME:
					return UpdateNameTransactionTransformer.fromByteBuffer(byteBuffer);

				case SELL_NAME:
					return SellNameTransactionTransformer.fromByteBuffer(byteBuffer);

				case CANCEL_SELL_NAME:
					return CancelSellNameTransactionTransformer.fromByteBuffer(byteBuffer);

				case BUY_NAME:
					return BuyNameTransactionTransformer.fromByteBuffer(byteBuffer);

				case CREATE_POLL:
					return CreatePollTransactionTransformer.fromByteBuffer(byteBuffer);

				case VOTE_ON_POLL:
					return VoteOnPollTransactionTransformer.fromByteBuffer(byteBuffer);

				case ARBITRARY:
					return ArbitraryTransactionTransformer.fromByteBuffer(byteBuffer);

				case ISSUE_ASSET:
					return IssueAssetTransactionTransformer.fromByteBuffer(byteBuffer);

				case TRANSFER_ASSET:
					return TransferAssetTransactionTransformer.fromByteBuffer(byteBuffer);

				case CREATE_ASSET_ORDER:
					return CreateOrderTransactionTransformer.fromByteBuffer(byteBuffer);

				case CANCEL_ASSET_ORDER:
					return CancelOrderTransactionTransformer.fromByteBuffer(byteBuffer);

				case MULTIPAYMENT:
					return MultiPaymentTransactionTransformer.fromByteBuffer(byteBuffer);

				case MESSAGE:
					return MessageTransactionTransformer.fromByteBuffer(byteBuffer);

				case DEPLOY_AT:
					return DeployATTransactionTransformer.fromByteBuffer(byteBuffer);

				default:
					throw new TransformationException("Unsupported transaction type [" + type.value + "] during conversion from bytes");
			}
		} catch (BufferUnderflowException e) {
			throw new TransformationException("Byte data too short for transaction type [" + type.value + "]");
		}
	}

	public static int getDataLength(TransactionData transactionData) throws TransformationException {
		switch (transactionData.getType()) {
			case GENESIS:
				return GenesisTransactionTransformer.getDataLength(transactionData);

			case PAYMENT:
				return PaymentTransactionTransformer.getDataLength(transactionData);

			case REGISTER_NAME:
				return RegisterNameTransactionTransformer.getDataLength(transactionData);

			case UPDATE_NAME:
				return UpdateNameTransactionTransformer.getDataLength(transactionData);

			case SELL_NAME:
				return SellNameTransactionTransformer.getDataLength(transactionData);

			case CANCEL_SELL_NAME:
				return CancelSellNameTransactionTransformer.getDataLength(transactionData);

			case BUY_NAME:
				return BuyNameTransactionTransformer.getDataLength(transactionData);

			case CREATE_POLL:
				return CreatePollTransactionTransformer.getDataLength(transactionData);

			case VOTE_ON_POLL:
				return VoteOnPollTransactionTransformer.getDataLength(transactionData);

			case ARBITRARY:
				return ArbitraryTransactionTransformer.getDataLength(transactionData);

			case ISSUE_ASSET:
				return IssueAssetTransactionTransformer.getDataLength(transactionData);

			case TRANSFER_ASSET:
				return TransferAssetTransactionTransformer.getDataLength(transactionData);

			case CREATE_ASSET_ORDER:
				return CreateOrderTransactionTransformer.getDataLength(transactionData);

			case CANCEL_ASSET_ORDER:
				return CancelOrderTransactionTransformer.getDataLength(transactionData);

			case MULTIPAYMENT:
				return MultiPaymentTransactionTransformer.getDataLength(transactionData);

			case MESSAGE:
				return MessageTransactionTransformer.getDataLength(transactionData);

			case DEPLOY_AT:
				return DeployATTransactionTransformer.getDataLength(transactionData);

			default:
				throw new TransformationException("Unsupported transaction type [" + transactionData.getType().value + "] when requesting byte length");
		}
	}

	public static byte[] toBytes(TransactionData transactionData) throws TransformationException {
		switch (transactionData.getType()) {
			case GENESIS:
				return GenesisTransactionTransformer.toBytes(transactionData);

			case PAYMENT:
				return PaymentTransactionTransformer.toBytes(transactionData);

			case REGISTER_NAME:
				return RegisterNameTransactionTransformer.toBytes(transactionData);

			case UPDATE_NAME:
				return UpdateNameTransactionTransformer.toBytes(transactionData);

			case SELL_NAME:
				return SellNameTransactionTransformer.toBytes(transactionData);

			case CANCEL_SELL_NAME:
				return CancelSellNameTransactionTransformer.toBytes(transactionData);

			case BUY_NAME:
				return BuyNameTransactionTransformer.toBytes(transactionData);

			case CREATE_POLL:
				return CreatePollTransactionTransformer.toBytes(transactionData);

			case VOTE_ON_POLL:
				return VoteOnPollTransactionTransformer.toBytes(transactionData);

			case ARBITRARY:
				return ArbitraryTransactionTransformer.toBytes(transactionData);

			case ISSUE_ASSET:
				return IssueAssetTransactionTransformer.toBytes(transactionData);

			case TRANSFER_ASSET:
				return TransferAssetTransactionTransformer.toBytes(transactionData);

			case CREATE_ASSET_ORDER:
				return CreateOrderTransactionTransformer.toBytes(transactionData);

			case CANCEL_ASSET_ORDER:
				return CancelOrderTransactionTransformer.toBytes(transactionData);

			case MULTIPAYMENT:
				return MultiPaymentTransactionTransformer.toBytes(transactionData);

			case MESSAGE:
				return MessageTransactionTransformer.toBytes(transactionData);

			case DEPLOY_AT:
				return DeployATTransactionTransformer.toBytes(transactionData);

			default:
				throw new TransformationException("Unsupported transaction type [" + transactionData.getType().value + "] during conversion to bytes");
		}
	}

	/**
	 * Serialize transaction as byte[], stripping off trailing signature ready for signing/verification.
	 * <p>
	 * Used by signature-related methods such as {@link Transaction#sign(PrivateKeyAccount)} and {@link Transaction#isSignatureValid()}
	 * 
	 * @param transactionData
	 * @return byte[] of transaction, without trailing signature
	 * @throws TransformationException
	 */
	public static byte[] toBytesForSigning(TransactionData transactionData) throws TransformationException {
		switch (transactionData.getType()) {
			case GENESIS:
				return GenesisTransactionTransformer.toBytesForSigningImpl(transactionData);

			case PAYMENT:
				return PaymentTransactionTransformer.toBytesForSigningImpl(transactionData);

			case REGISTER_NAME:
				return RegisterNameTransactionTransformer.toBytesForSigningImpl(transactionData);

			case UPDATE_NAME:
				return UpdateNameTransactionTransformer.toBytesForSigningImpl(transactionData);

			case SELL_NAME:
				return SellNameTransactionTransformer.toBytesForSigningImpl(transactionData);

			case CANCEL_SELL_NAME:
				return CancelSellNameTransactionTransformer.toBytesForSigningImpl(transactionData);

			case BUY_NAME:
				return BuyNameTransactionTransformer.toBytesForSigningImpl(transactionData);

			case CREATE_POLL:
				return CreatePollTransactionTransformer.toBytesForSigningImpl(transactionData);

			case VOTE_ON_POLL:
				return VoteOnPollTransactionTransformer.toBytesForSigningImpl(transactionData);

			case ARBITRARY:
				return ArbitraryTransactionTransformer.toBytesForSigningImpl(transactionData);

			case ISSUE_ASSET:
				return IssueAssetTransactionTransformer.toBytesForSigningImpl(transactionData);

			case TRANSFER_ASSET:
				return TransferAssetTransactionTransformer.toBytesForSigningImpl(transactionData);

			case CREATE_ASSET_ORDER:
				return CreateOrderTransactionTransformer.toBytesForSigningImpl(transactionData);

			case CANCEL_ASSET_ORDER:
				return CancelOrderTransactionTransformer.toBytesForSigningImpl(transactionData);

			case MULTIPAYMENT:
				return MultiPaymentTransactionTransformer.toBytesForSigningImpl(transactionData);

			case MESSAGE:
				return MessageTransactionTransformer.toBytesForSigningImpl(transactionData);

			case DEPLOY_AT:
				return DeployATTransactionTransformer.toBytesForSigningImpl(transactionData);

			default:
				throw new TransformationException(
						"Unsupported transaction type [" + transactionData.getType().value + "] during conversion to bytes for signing");
		}
	}

	/**
	 * Generic serialization of transaction as byte[], stripping off trailing signature ready for signing/verification.
	 * 
	 * @param transactionData
	 * @return byte[] of transaction, without trailing signature
	 * @throws TransformationException
	 */
	protected static byte[] toBytesForSigningImpl(TransactionData transactionData) throws TransformationException {
		try {
			byte[] bytes = TransactionTransformer.toBytes(transactionData);

			if (transactionData.getSignature() == null)
				return bytes;

			return Arrays.copyOf(bytes, bytes.length - Transformer.SIGNATURE_LENGTH);
		} catch (TransformationException e) {
			throw new RuntimeException("Unable to transform transaction to byte array for signing", e);
		}
	}

	public static JSONObject toJSON(TransactionData transactionData) throws TransformationException {
		switch (transactionData.getType()) {
			case GENESIS:
				return GenesisTransactionTransformer.toJSON(transactionData);

			case PAYMENT:
				return PaymentTransactionTransformer.toJSON(transactionData);

			case REGISTER_NAME:
				return RegisterNameTransactionTransformer.toJSON(transactionData);

			case UPDATE_NAME:
				return UpdateNameTransactionTransformer.toJSON(transactionData);

			case SELL_NAME:
				return SellNameTransactionTransformer.toJSON(transactionData);

			case CANCEL_SELL_NAME:
				return CancelSellNameTransactionTransformer.toJSON(transactionData);

			case BUY_NAME:
				return BuyNameTransactionTransformer.toJSON(transactionData);

			case CREATE_POLL:
				return CreatePollTransactionTransformer.toJSON(transactionData);

			case VOTE_ON_POLL:
				return VoteOnPollTransactionTransformer.toJSON(transactionData);

			case ARBITRARY:
				return ArbitraryTransactionTransformer.toJSON(transactionData);

			case ISSUE_ASSET:
				return IssueAssetTransactionTransformer.toJSON(transactionData);

			case TRANSFER_ASSET:
				return TransferAssetTransactionTransformer.toJSON(transactionData);

			case CREATE_ASSET_ORDER:
				return CreateOrderTransactionTransformer.toJSON(transactionData);

			case CANCEL_ASSET_ORDER:
				return CancelOrderTransactionTransformer.toJSON(transactionData);

			case MULTIPAYMENT:
				return MultiPaymentTransactionTransformer.toJSON(transactionData);

			case MESSAGE:
				return MessageTransactionTransformer.toJSON(transactionData);

			case DEPLOY_AT:
				return DeployATTransactionTransformer.toJSON(transactionData);

			default:
				throw new TransformationException("Unsupported transaction type [" + transactionData.getType().value + "] during conversion to JSON");
		}
	}

	@SuppressWarnings("unchecked")
	static JSONObject getBaseJSON(TransactionData transaction) {
		JSONObject json = new JSONObject();

		json.put("type", transaction.getType().value);
		json.put("fee", transaction.getFee().toPlainString());
		json.put("timestamp", transaction.getTimestamp());
		json.put("signature", Base58.encode(transaction.getSignature()));

		byte[] reference = transaction.getReference();
		if (reference != null)
			json.put("reference", Base58.encode(reference));

		// XXX Can't do this as it requires database access:
		// json.put("confirmations", RepositoryManager.getTransactionRepository.getConfirmations(transaction));

		return json;
	}

}
