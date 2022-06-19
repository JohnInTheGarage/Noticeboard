package driverway.nb.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import jcifs.CIFSContext;
import jcifs.Configuration;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFileInputStream;

/**
 * 
 *
 * @author john
 */
public class SambaCaller {

	public List<String> fetch(String urlSMB) throws Exception {
		BaseContext baseCxt = null;
		Properties jcifsProperties = new Properties();
		CIFSContext auth = null;
		jcifsProperties.setProperty("jcifs.smb.client.enableSMB2", "true");
		jcifsProperties.setProperty("jcifs.smb.client.dfs.disabled", "true");
				
		Configuration config;
		config = new PropertyConfiguration(jcifsProperties);
		baseCxt = new BaseContext(config);
		auth = baseCxt.withCredentials(new NtlmPasswordAuthenticator("", "pi", "/baila"));
		SmbFileInputStream smbis = new SmbFileInputStream(urlSMB, auth); 
		List<String> doc = new BufferedReader(new InputStreamReader(smbis,
				StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
		smbis.close();
		return doc;
	}


}
