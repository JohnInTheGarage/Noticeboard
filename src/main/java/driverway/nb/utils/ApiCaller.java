/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package driverway.nb.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 *
 * @author john
 */
public class ApiCaller {
    
    private final HttpClient client = HttpClient.newHttpClient();
    
    public ApiCaller(){
        
    }
    
	public String callUKAPI(String URL, String UkMetOfficeApiKey) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.headers("apikey", UkMetOfficeApiKey,
						"accept", "application/json")
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

  	public String callSpainAPI(String URL) throws IOException, InterruptedException {
		HttpRequest request = locateSpainURL(URL);
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();

	}

	public String callOWAPI(String URL) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();

	}

    /*
    * call this to find the URL for spanish advisories of weather warnings 
    * (separate to normal API call)
    */
   	private HttpRequest locateSpainURL(String URL) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200 || response.body().contains("exito") == false) {
			throw new IOException("Status not 200 or 'exito' missing");
		}
		int pos1 = response.body().indexOf("datos");
		int pos2 = response.body().indexOf(":", pos1);
		pos1 = response.body().indexOf("\"", pos2);
		pos1++;
		pos2 = response.body().indexOf("\"", pos1);
		URL = response.body().substring(pos1, pos2);
		request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.build();
		return request;
	}

    /*
	* Gets the Set of XML files for weather warnings
    */
	public ArrayList<String> callSpainAvisosAPI(String URL) throws IOException, InterruptedException {
		HttpRequest request = locateSpainURL(URL);
		//HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		HttpResponse<byte[]> responseBytes = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		ByteArrayInputStream bais = new ByteArrayInputStream(responseBytes.body());
		ArrayList<String> xmls = unTarFile(bais);
		return xmls;

	}

    	/**
	 *
	 * @param tarFile
	 * @param xmls
	 * @throws IOException
	 */
	private ArrayList<String> unTarFile(ByteArrayInputStream tarFile) throws IOException {
		TarArchiveInputStream tis = new TarArchiveInputStream((InputStream) tarFile);
		TarArchiveEntry tarEntry = null;
		ArrayList<String> xmls = new ArrayList<>();

		while ((tarEntry = tis.getNextTarEntry()) != null) {
			byte[] btis = tis.readAllBytes();
			xmls.add(new String(btis));
		}
		tis.close();
		return xmls;
	}


}
