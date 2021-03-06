package transform.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.json.simple.JSONObject;

import com.google.common.base.Utf8;
import com.google.common.hash.HashCode;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import data.transaction.TransactionData;
import qora.account.PublicKeyAccount;
import qora.block.BlockChain;
import qora.transaction.IssueAssetTransaction;
import data.transaction.IssueAssetTransactionData;
import transform.TransformationException;
import utils.Serialization;

public class IssueAssetTransactionTransformer extends TransactionTransformer {

	// Property lengths
	private static final int ISSUER_LENGTH = PUBLIC_KEY_LENGTH;
	private static final int OWNER_LENGTH = ADDRESS_LENGTH;
	private static final int NAME_SIZE_LENGTH = INT_LENGTH;
	private static final int DESCRIPTION_SIZE_LENGTH = INT_LENGTH;
	private static final int QUANTITY_LENGTH = LONG_LENGTH;
	private static final int IS_DIVISIBLE_LENGTH = BOOLEAN_LENGTH;
	private static final int ASSET_REFERENCE_LENGTH = REFERENCE_LENGTH;

	private static final int TYPELESS_LENGTH = BASE_TYPELESS_LENGTH + ISSUER_LENGTH + OWNER_LENGTH + NAME_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH
			+ QUANTITY_LENGTH + IS_DIVISIBLE_LENGTH;

	static TransactionData fromByteBuffer(ByteBuffer byteBuffer) throws TransformationException {
		long timestamp = byteBuffer.getLong();

		byte[] reference = new byte[REFERENCE_LENGTH];
		byteBuffer.get(reference);

		byte[] issuerPublicKey = Serialization.deserializePublicKey(byteBuffer);

		String owner = Serialization.deserializeAddress(byteBuffer);

		String assetName = Serialization.deserializeSizedString(byteBuffer, IssueAssetTransaction.MAX_NAME_SIZE);

		String description = Serialization.deserializeSizedString(byteBuffer, IssueAssetTransaction.MAX_DESCRIPTION_SIZE);

		long quantity = byteBuffer.getLong();

		boolean isDivisible = byteBuffer.get() != 0;

		byte[] assetReference = new byte[ASSET_REFERENCE_LENGTH];
		// In v1, IssueAssetTransaction uses Asset.parse which also deserializes reference.
		if (timestamp < BlockChain.getIssueAssetV2Timestamp())
			byteBuffer.get(assetReference);

		BigDecimal fee = Serialization.deserializeBigDecimal(byteBuffer);

		byte[] signature = new byte[SIGNATURE_LENGTH];
		byteBuffer.get(signature);

		return new IssueAssetTransactionData(issuerPublicKey, owner, assetName, description, quantity, isDivisible, fee, timestamp, reference, signature);
	}

	public static int getDataLength(TransactionData transactionData) throws TransformationException {
		IssueAssetTransactionData issueAssetTransactionData = (IssueAssetTransactionData) transactionData;

		int dataLength = TYPE_LENGTH + TYPELESS_LENGTH + Utf8.encodedLength(issueAssetTransactionData.getAssetName())
				+ Utf8.encodedLength(issueAssetTransactionData.getDescription());

		// In v1, IssueAssetTransaction uses Asset.toBytes which also serializes reference.
		if (transactionData.getTimestamp() < BlockChain.getIssueAssetV2Timestamp())
			dataLength += ASSET_REFERENCE_LENGTH;

		return dataLength;
	}

	public static byte[] toBytes(TransactionData transactionData) throws TransformationException {
		try {
			IssueAssetTransactionData issueAssetTransactionData = (IssueAssetTransactionData) transactionData;

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			bytes.write(Ints.toByteArray(issueAssetTransactionData.getType().value));
			bytes.write(Longs.toByteArray(issueAssetTransactionData.getTimestamp()));
			bytes.write(issueAssetTransactionData.getReference());

			bytes.write(issueAssetTransactionData.getIssuerPublicKey());

			Serialization.serializeAddress(bytes, issueAssetTransactionData.getOwner());

			Serialization.serializeSizedString(bytes, issueAssetTransactionData.getAssetName());
			Serialization.serializeSizedString(bytes, issueAssetTransactionData.getDescription());

			bytes.write(Longs.toByteArray(issueAssetTransactionData.getQuantity()));
			bytes.write((byte) (issueAssetTransactionData.getIsDivisible() ? 1 : 0));

			// In v1, IssueAssetTransaction uses Asset.toBytes which also serializes Asset's reference which is the IssueAssetTransaction's signature
			if (transactionData.getTimestamp() < BlockChain.getIssueAssetV2Timestamp()) {
				byte[] assetReference = issueAssetTransactionData.getSignature();
				if (assetReference != null)
					bytes.write(assetReference);
				else
					bytes.write(new byte[ASSET_REFERENCE_LENGTH]);
			}

			Serialization.serializeBigDecimal(bytes, issueAssetTransactionData.getFee());

			if (issueAssetTransactionData.getSignature() != null)
				bytes.write(issueAssetTransactionData.getSignature());

			return bytes.toByteArray();
		} catch (IOException | ClassCastException e) {
			throw new TransformationException(e);
		}
	}

	/**
	 * In Qora v1, the bytes used for verification have transaction type set to REGISTER_NAME_TRANSACTION so we need to test for v1-ness and adjust the bytes
	 * accordingly.
	 * 
	 * @param transactionData
	 * @return byte[]
	 * @throws TransformationException
	 */
	public static byte[] toBytesForSigningImpl(TransactionData transactionData) throws TransformationException {
		byte[] bytes = TransactionTransformer.toBytesForSigningImpl(transactionData);

		if (transactionData.getTimestamp() >= BlockChain.getIssueAssetV2Timestamp())
			return bytes;

		// Special v1 version

		// Zero duplicate signature/reference
		int start = bytes.length - SIGNATURE_LENGTH - BIG_DECIMAL_LENGTH;
		int end = start + SIGNATURE_LENGTH;
		Arrays.fill(bytes, start, end, (byte) 0);

		return bytes;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject toJSON(TransactionData transactionData) throws TransformationException {
		JSONObject json = TransactionTransformer.getBaseJSON(transactionData);

		try {
			IssueAssetTransactionData issueAssetTransactionData = (IssueAssetTransactionData) transactionData;

			byte[] issuerPublicKey = issueAssetTransactionData.getIssuerPublicKey();

			json.put("issuer", PublicKeyAccount.getAddress(issuerPublicKey));
			json.put("issuerPublicKey", HashCode.fromBytes(issuerPublicKey).toString());
			json.put("owner", issueAssetTransactionData.getOwner());
			json.put("assetName", issueAssetTransactionData.getAssetName());
			json.put("description", issueAssetTransactionData.getDescription());
			json.put("quantity", issueAssetTransactionData.getQuantity());
			json.put("isDivisible", issueAssetTransactionData.getIsDivisible());
		} catch (ClassCastException e) {
			throw new TransformationException(e);
		}

		return json;
	}

}
