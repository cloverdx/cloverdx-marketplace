package com.cloverdx.libraries.encryption;

import java.security.Provider;
import java.security.Security;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;

public class GetAlgorithms extends AbstractGenericTransform {

	@Override
	public void execute() {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			//Security.insertProviderAt(new BouncyCastleProvider(), 1);
			Security.addProvider(new BouncyCastleProvider());
		}

		DataRecord algoPort = outRecords[0];
		DataRecord providerPort = outRecords[1];
		
		for (Provider provider: Security.getProviders()) {
			Set<Provider.Service> services = provider.getServices();
			
			providerPort.getField("name").setValue(provider.getName());
			providerPort.getField("version").setValue(provider.toString());
			providerPort.getField("algorithmCount").setValue(services.size());
			writeRecordToPort(1, providerPort);
			providerPort.reset();
			
			for (Provider.Service service: services) {
				algoPort.getField("providerName").setValue(provider.getName());
				algoPort.getField("providerVersion").setValue(provider.toString());
				algoPort.getField("algorithmName").setValue(service.getAlgorithm());
				writeRecordToPort(0, algoPort);
				algoPort.reset();
			}
		}
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
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
}
