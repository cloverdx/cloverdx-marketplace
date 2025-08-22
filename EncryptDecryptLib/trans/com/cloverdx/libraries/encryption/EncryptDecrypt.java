package com.cloverdx.libraries.encryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.JetelException;
import org.jetel.util.ExceptionUtils;
import org.jetel.util.file.FileUtils;

public class EncryptDecrypt extends AbstractGenericTransform {

	private static final String KEY_ALGORITHM_NAME_PROPERTY = "KeyAlgorithmName";
	private static final String CIPHER_NAME_PROPERTY = "CipherName";
	private static final String KEY_LENGTH_BITS_PROPERTY = "KeyLengthBits";
	private static final String ITERATIONS_PROPERTY = "Iterations";
	private static final String MODE_PROPERTY = "EncryptOrDecrypt";
	private static final String RW_BLOCK_SIZE = "ReadWriteBlockSize";
	
	private static final String STATUS_OK = "ok";
	private static final String STATUS_ERROR = "error";
	
	private static final String STACK_TRACE_FIELD = "stackTrace";
	private static final String ERROR_MESSAGE_FIELD = "errorMessage";
	private static final String STATUS_FIELD = "status";
	private static final String OUTPUT_FILE_URL_FIELD = "outputFileName";
	private static final String INPUT_FILE_URL_FIELD = "inputFileURL";
	private static final String SALT_FIELD = "salt";
	private static final String PASSWORD_FIELD = "password";
	private static final String OUTPUT_FILE_SIZE_FIELD = "outputFileSize";
	private static final String INPUT_FILE_SIZE_FIELD = "inputFileSize";

	private SecureRandom secureRandom = new SecureRandom();
	
	private String processingMode;
	private String cipherName = "AES/CTR/NOPADDING";
	private String keyAlgoName = "PBEWITHSHA256AND256BITAES-CBC-BC";
	private int blockSize = 1024;
	
	private int iterations = 1000;
	private int keyLength = 256;
	
	private Cipher cipher;
	private IvParameterSpec iv;
	
	@Override
	public void execute() {
		DataRecord input = inRecords[0];
		DataRecord output = outRecords[0];

		while ((input = readRecordFromPort(0)) != null) {
			boolean failed = false;
			try {
				transformFile(input, output);
			} catch (JetelException e) {
				output.getField(STATUS_FIELD).setValue(STATUS_ERROR);
				output.getField(ERROR_MESSAGE_FIELD).setValue(e.getMessage());
				output.getField(STACK_TRACE_FIELD).setValue(ExceptionUtils.stackTraceToString(e));
				failed = true;
			}
			
			if (!failed) {
				output.getField(STATUS_FIELD).setValue(STATUS_OK);
			}
			writeRecordToPort(0, output);
		}
	}

	private void transformFile(DataRecord input, DataRecord output) throws JetelException {

		String password = input.getField(PASSWORD_FIELD).getValue().toString();
		String salt = input.getField(SALT_FIELD).getValue().toString();
		String inputFileURL = input.getField(INPUT_FILE_URL_FIELD).getValue().toString();
		String outputFileURL = input.getField(OUTPUT_FILE_URL_FIELD).getValue().toString();

		output.reset();
		output.getField(INPUT_FILE_URL_FIELD).setValue(inputFileURL);
		output.getField(OUTPUT_FILE_URL_FIELD).setValue(outputFileURL);

		DataInputStream inputStream;
		DataOutputStream outputStream;

		try {
			inputStream =  new DataInputStream(FileUtils.getInputStream(getGraph().getRuntimeContext().getContextURL(), inputFileURL));
		} catch (Exception e) {
			throw new JetelException("Unable to open input stream.", e);
		}

		try {
			outputStream = new DataOutputStream(FileUtils.getOutputStream(getGraph().getRuntimeContext().getContextURL(), outputFileURL, false, 0));
		} catch (Exception e) {
			try {
				inputStream.close();
			} catch (Exception f) {
			}
			throw new JetelException("Unable to open output stream.", e);
		}

		ProcessStatus status;
		try {
			switch (processingMode) {
				case "encrypt":
					status = encrypt(password, salt, inputStream, outputStream);
					break;
					
				case "decrypt":
					status = decrypt(password, salt, inputStream, outputStream);
					break;
					
				default:
					throw new JetelException("Unknown processing mode.");
			}
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
			}
	
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
			}
		}
		
		status.updateOutputRecord(output);
	}
	
	private ProcessStatus encrypt(String password, String salt, DataInputStream inputStream, DataOutputStream outputStream) throws JetelException {
		initEncryptionCipher(password, salt);

		// When encrypting, we save IV at the beginning of the data.
		byte[] ivBytes = iv.getIV();
		logInfo("Encryption IV length: " + ivBytes.length);
		try {
			outputStream.writeInt(ivBytes.length);
			outputStream.write(ivBytes, 0, ivBytes.length);
		} catch (IOException e) {
			throw new JetelException(e);
		}
		
		ProcessStatus status = new ProcessStatus(0, 4 + ivBytes.length);
		ProcessStatus tStatus = transformStream(cipher, inputStream, outputStream);
		
		status.add(tStatus);
		
		return status;
	}
	
	private ProcessStatus decrypt(String password, String salt, DataInputStream inputStream, DataOutputStream outputStream) throws JetelException {

		// When decrypting, we need to read IV from the file. First read length (one integer), then read the IV data.
		byte[] ivBytes;
		int ivLength;
		try {
			ivLength = inputStream.readInt();
			logInfo("Decryption IV length: " + ivLength);
			
			ivBytes = new byte[ivLength];
			inputStream.read(ivBytes);
		} catch (IOException e) {
			throw new JetelException(e);
		}

		iv = new IvParameterSpec(ivBytes);
		initDecryptionCipher(password, salt);
		
		ProcessStatus status = new ProcessStatus(4 + ivLength, 0);
		ProcessStatus tStatus = transformStream(cipher, inputStream, outputStream);

		status.add(tStatus);
		
		return status;
	}
	
	private ProcessStatus transformStream(Cipher cipher, InputStream inputStream, OutputStream outputStream) throws JetelException {
		byte[] buffer = new byte[blockSize];
		byte[] cipherBlock = new byte[cipher.getOutputSize(blockSize)];
		int bytesRead = 0;
		int cipherBytes = 0;
		long cipherBytesTotal = 0;
		long bytesReadTotal = 0;
		
		try {
			// Read whole input file in chunks and encrypt each.
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				cipherBytes = cipher.update(buffer, 0, bytesRead, cipherBlock);
				
				bytesReadTotal += bytesRead;
				cipherBytesTotal += cipherBytes;
				
				outputStream.write(cipherBlock, 0, cipherBytes);
			}
			
			// Finalize encryption.
			cipherBytes = cipher.doFinal(cipherBlock, 0);
			outputStream.write(cipherBlock, 0, cipherBytes);
			cipherBytesTotal += cipherBytes;
		} catch (IOException | ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			throw new JetelException("Encryption error.", e);
		}
		
		return new ProcessStatus(bytesReadTotal, cipherBytesTotal);
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);
		return status;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
		
		Security.setProperty("crypto.policy", "unlimited");
		
		registerProvider();

		iterations = getProperties().getIntProperty(ITERATIONS_PROPERTY);
		if (iterations < 1) {
			throw new ComponentNotReadyException("Iterations count has to be at least 1.");
		}
		
		keyLength = getProperties().getIntProperty(KEY_LENGTH_BITS_PROPERTY);
		if (keyLength < 1) {
			throw new ComponentNotReadyException("Length of the key in bits has to be at least 1.");
		}
		
		cipherName = getProperties().getProperty(CIPHER_NAME_PROPERTY);
		if (cipherName == null || "".equals(cipherName)) {
			throw new ComponentNotReadyException("Cipher name cannot be empty.");
		}
		
		keyAlgoName = getProperties().getProperty(KEY_ALGORITHM_NAME_PROPERTY);
		if (keyAlgoName == null || "".equals(keyAlgoName)) {
			throw new ComponentNotReadyException("Key generator algorithm name cannot be empty.");
		}
		
		processingMode = getProperties().getProperty(MODE_PROPERTY);
		blockSize = getProperties().getIntProperty(RW_BLOCK_SIZE);
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
	
	public static synchronized void registerProvider() {
	    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
	    Security.insertProviderAt(new BouncyCastleProvider(), 0);
	  }
	
	private void initEncryptionCipher(String password, String salt) throws JetelException {
		logInfo("Creating ENCRYPT cipher.");

		cipher = createCipher();
		iv = generateIV(cipher);
		SecretKey key = generateKey(password, salt);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, iv, secureRandom);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new JetelException("Unable to initialize encryption cipher.", e);
		}
	}
	
	private void initDecryptionCipher(String password, String salt) throws JetelException {
		logInfo("Creating DECRYPT cipher.");
		
		cipher = createCipher();
		SecretKey key = generateKey(password, salt);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, iv, secureRandom);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new JetelException("Unable to initialize decryption cipher.", e);
		}
	}
	
	private Cipher createCipher() throws JetelException {
		try {
			return Cipher.getInstance(cipherName, "BC");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
			throw new JetelException("Unable to get cipher instance.", e);
		}
	}
	
	private IvParameterSpec generateIV(Cipher cipher) {
		byte[] ivBytes = new byte[cipher.getBlockSize()];
		secureRandom.nextBytes(ivBytes);
		return new IvParameterSpec(ivBytes);
	}
	
	private SecretKey generateKey(String password, String salt) throws JetelException {
		SecretKeyFactory keyFactory;
		try {
			keyFactory = SecretKeyFactory.getInstance(keyAlgoName);
		} catch (NoSuchAlgorithmException e) {
			throw new JetelException("Unable to initialize key specification factory", e);
		}
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterations, keyLength);
		
		try {
			return keyFactory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			throw new JetelException("Unable to generate key secret.", e);
		}
	}
	
	private void logInfo(Object message) {
		getLogger().info(message);
	}
	
	private static class ProcessStatus {
		private long inputBytes;
		private long outputBytes;
		
		public ProcessStatus(long inputBytes, long outputBytes) {
			this.inputBytes = inputBytes;
			this.outputBytes = outputBytes;
		}
		
		public void updateOutputRecord(DataRecord output) {
			output.getField(INPUT_FILE_SIZE_FIELD).setValue(inputBytes);
			output.getField(OUTPUT_FILE_SIZE_FIELD).setValue(outputBytes);
		}
		
		public void addInputBytes(long b) {
			inputBytes += b;
		}
		
		public void addOutputBytes(long b) {
			outputBytes += b;
		}
		
		public void add(ProcessStatus other) {
			addInputBytes(other.inputBytes);
			addOutputBytes(other.outputBytes);
		}
	}
}
