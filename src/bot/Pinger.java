package bot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import com.skype.Chat;
import com.skype.SkypeException;

public class Pinger implements Runnable {

	private String url;
	private Chat chat;

	public Pinger(String url, Chat chat) {
		this.url = url;
		this.chat = chat;
	}

	@Override
	public void run() {
		if (!url.contains("http")) {
			url = "http://" + url;
		}

		Date now = new Date();
		URL site;
		try {
			try {
				if (url.contains("ddos")) {
					chat.send("There seemed to be an issue with " + url + " but the connection was successful.");
					return;
				}
				site = new URL(url);
				HttpURLConnection con = (HttpURLConnection) site.openConnection();
				try {
					con.connect();
					con.setConnectTimeout(2000);
					if (con.getResponseMessage().equals("OK")) {
						InetAddress address = InetAddress.getByName(site.getHost());
						String[] pingInfo = address.toString().split("/");
						chat.send(pingInfo[0] + " (" + pingInfo[1] + ")" + " was reached in only " + (new Date().getTime() - now.getTime() + " milliseconds"));
					} else {
						chat.send("There seemed to be an issue with " + url + " but the connection was successful.");
					}
				} finally {
					con.disconnect();
				}
			} catch (MalformedURLException e1) {
				chat.send(url + " is a malformed URL");
			} catch (IOException e) {
				chat.send("Could not find a site called " + url);
			}
		} catch (SkypeException e) {
			e.printStackTrace();
		}
		TeslaInput.isPinging = false;
	}
}
