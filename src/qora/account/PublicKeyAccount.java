package qora.account;

import qora.crypto.Crypto;
import qora.crypto.Ed25519;
import repository.Repository;

public class PublicKeyAccount extends Account {

	protected byte[] publicKey;

	public PublicKeyAccount(Repository repository, byte[] publicKey) {
		super(repository, Crypto.toAddress(publicKey));

		this.publicKey = publicKey;
	}

	protected PublicKeyAccount() {
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public boolean verify(byte[] signature, byte[] message) {
		return PublicKeyAccount.verify(this.publicKey, signature, message);
	}

	public static boolean verify(byte[] publicKey, byte[] signature, byte[] message) {
		try {
			return Ed25519.verify(signature, message, publicKey);
		} catch (Exception e) {
			return false;
		}
	}

	public static String getAddress(byte[] publicKey) {
		return Crypto.toAddress(publicKey);
	}

}
