package qora.transaction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.PaymentData;
import data.transaction.TransactionData;
import data.transaction.TransferAssetTransactionData;
import qora.account.Account;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.block.BlockChain;
import qora.payment.Payment;
import repository.DataException;
import repository.Repository;

public class TransferAssetTransaction extends Transaction {

	// Properties
	private TransferAssetTransactionData transferAssetTransactionData;

	// Constructors

	public TransferAssetTransaction(Repository repository, TransactionData transactionData) {
		super(repository, transactionData);

		this.transferAssetTransactionData = (TransferAssetTransactionData) this.transactionData;
	}

	// More information

	@Override
	public List<Account> getRecipientAccounts() throws DataException {
		return Collections.singletonList(new Account(this.repository, transferAssetTransactionData.getRecipient()));
	}

	@Override
	public boolean isInvolved(Account account) throws DataException {
		String address = account.getAddress();

		if (address.equals(this.getSender().getAddress()))
			return true;

		if (address.equals(transferAssetTransactionData.getRecipient()))
			return true;

		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) throws DataException {
		String address = account.getAddress();
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String senderAddress = this.getSender().getAddress();

		if (address.equals(senderAddress))
			amount = amount.subtract(this.transactionData.getFee());

		// We're only interested in QORA amounts
		if (transferAssetTransactionData.getAssetId() == Asset.QORA) {
			if (address.equals(transferAssetTransactionData.getRecipient()))
				amount = amount.add(transferAssetTransactionData.getAmount());
			else if (address.equals(senderAddress))
				amount = amount.subtract(transferAssetTransactionData.getAmount());
		}

		return amount;
	}

	// Navigation

	public Account getSender() throws DataException {
		return new PublicKeyAccount(this.repository, this.transferAssetTransactionData.getSenderPublicKey());
	}

	// Processing

	private PaymentData getPaymentData() {
		return new PaymentData(transferAssetTransactionData.getRecipient(), transferAssetTransactionData.getAssetId(),
				transferAssetTransactionData.getAmount());
	}

	@Override
	public ValidationResult isValid() throws DataException {
		// Are TransferAssetTransactions even allowed at this point?
		// XXX In gen1 this used NTP.getTime() but surely the transaction's timestamp should be used?
		if (this.transferAssetTransactionData.getTimestamp() < BlockChain.getAssetsReleaseTimestamp())
			return ValidationResult.NOT_YET_RELEASED;

		// Check reference is correct
		Account sender = getSender();

		if (!Arrays.equals(sender.getLastReference(), transferAssetTransactionData.getReference()))
			return ValidationResult.INVALID_REFERENCE;

		// Wrap asset transfer as a payment and delegate final payment checks to Payment class
		return new Payment(this.repository).isValid(transferAssetTransactionData.getSenderPublicKey(), getPaymentData(), transferAssetTransactionData.getFee());
	}

	@Override
	public void process() throws DataException {
		// Save this transaction itself
		this.repository.getTransactionRepository().save(this.transactionData);

		// Wrap asset transfer as a payment and delegate processing to Payment class. Only update recipient's last reference if transferring QORA.
		new Payment(this.repository).process(transferAssetTransactionData.getSenderPublicKey(), getPaymentData(), transferAssetTransactionData.getFee(),
				transferAssetTransactionData.getSignature(), false);
	}

	@Override
	public void orphan() throws DataException {
		// Delete this transaction itself
		this.repository.getTransactionRepository().delete(this.transactionData);

		// Wrap asset transfer as a payment and delegate processing to Payment class. Only revert recipient's last reference if transferring QORA.
		new Payment(this.repository).orphan(transferAssetTransactionData.getSenderPublicKey(), getPaymentData(), transferAssetTransactionData.getFee(),
				transferAssetTransactionData.getSignature(), transferAssetTransactionData.getReference(), false);
	}

}
