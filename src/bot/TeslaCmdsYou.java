package bot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skype.Chat;
import com.skype.ContactList;
import com.skype.Friend;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.User;

public class TeslaCmdsYou {
	
	static String speaker;
	static Date time;
	static User[] members;
	static int targetID;
	static int lineCount;
	
	public TeslaCmdsYou(Date Time, String Speaker, User[] Members){
		time = Time;
		speaker = Speaker;
		members = Members;
	}

	public String rollDice(String number){
		String msg;
		try{
			if(number.length()==0){
				return speaker + " rolls around pointlessly.";
			}else{
				if(number.length()>=10){
					return speaker + " throws a sphere and it rolls away.";
				}
			}
			long nbr = Long.parseLong(number);
			String rand = Long.toString(Math.round(Math.random()*(nbr-1))+1);
			if(nbr < 0){
				msg = speaker + " throws dice all over the place";
			}else if(nbr == 0){
				msg = speaker + " rolls i on an imaginary die";
			}else if(nbr==1){
				msg = speaker + " collapses time and space by rolling a 1-sided dice.";
			}else if(nbr ==2){
				if(rand.equals("1")){
					msg = speaker + " flips a coin and gets heads.";
				}else{
					msg = speaker + " flips a coin and gets tails.";
				}
			}else{
				msg = speaker + " rolls a " + rand + " on a " + Long.toString(nbr) + " sided die.";
			}
		} catch(NumberFormatException  e){
			msg = "Nice try, " + speaker;
		}
		return msg;
	}
	public String lastSeen(String username) throws SkypeException{
		getUserByName(username);
		String exception = Exception(username, false);
		if(!exception.equals("")){
			return exception;
		}
		String onlineStatus="";
		User user = members[targetID];
		String name = username;
		if(user.getStatus().toString().toLowerCase().equals("offline")){
			String status = user.getBuddyStatus().toString();
			if(status.equals("ADDED")){
				getUserByName("Tesla Bot");
				int teslaID = targetID;
				getUserByName(speaker);
				return name + " was last seen on " + user.getLastOnlineTime(members[targetID], members[teslaID]).toString();
			}else{
				onlineStatus="offline";
			}
		}else{
			onlineStatus="online";
		}
		try{
			getUserByName("Tesla Bot");
			int teslaID = targetID;
			getUserByName(speaker);
			return name + " is currently " + onlineStatus + ". His last message was " + user.getLastMessage().getTime(members[targetID], members[teslaID]).toString();
		} catch (Exception e){
			return name + " is currently " + onlineStatus + " but has never posted anything as far as I can see.";
		}
	}
	public String users() throws SkypeException{
		String names="";
		for(int i=0;i<members.length;i++){
			if(i != 0){
				if(i == members.length-1){
					names = names.concat(" and ");
				}else{
					names = names.concat(", ");
				}
			}
			if(members[i].getFullName().equals("")){
				names = names.concat(members[i].toString());
			}else{
				names = names.concat(members[i].getFullName());
			}
		}
		return "I spy with his little eye " + names;
	}
	public String poke(String username) throws SkypeException{
		getUserByName(username);
		String exception = Exception(username, false);
		User user = null;
		if(!exception.equals("")){
			if(exception.equals(username + " could not be found in this chat.")){
				ContactList contacts = new ContactList();
				Friend[] friendArray = contacts.getAllFriends();
				for(int i=0;i<friendArray.length;i++){
					if(friendArray[i].getFullName().equalsIgnoreCase(username) || friendArray[i].toString().equalsIgnoreCase(username)){
						user = friendArray[i];
						break;
					}
					if(i==friendArray.length-1){
						return exception;
					}
				}
			}else{
				return exception;
			}
		}else{
			user = members[targetID];
		}
		user.chat().send("You have been poked by " + speaker);
		return speaker + " just poked " + username + " right where it hurts.";
	}
	public String slap(String username) throws SkypeException{
		if(username.length()==0 || username.toLowerCase().equals("self")){
			return speaker + " slaps himself in confusion. Super effective!";
		}
		getUserByName(username);
		String exception = Exception(username, false);
		if(!exception.equals("")){
			if(exception.equals(username + " could not be found in this chat.")){
				username = "Somebody that goes by the name of " + username;
			}else{
				return exception;
			}
		}
		String msg="";
		Long rand = Math.round(Math.random()*2);
		if(rand == 0){
			msg = "slapped to another dimension";
		}else if(rand==1){
			msg = "slapped by a large trout";
		}else if(rand==2){
			msg = "the biggest slap of his life";
		}
		return username + " just got " + msg + " courtesy of " + speaker;
	}
	
	public String youtubeLink(String url) throws IOException{
		GetPage page = new GetPage(url);
		int errorID = page.getError();
		if(errorID==1){
			return "Youtube link detected but could not be read.";
		}
		String title = page.getTitle();
		String author = page.getAuthor();
		String duration = page.getDuration().toLowerCase();
		if(duration.substring(duration.length()-2).contains("m")){
			duration = duration.substring(0,duration.length()-1) + "0" + duration.substring(duration.length()-1);
		}
		title = title.replace(" - YouTube", "");
		return title + " [" + duration +"] by " + author;		
	}
	public String wikipediaLink(String url) throws IOException{
		try{
			url = url.replace(" ", "_");
			GetPage page = new GetPage(url);
			return page.getWikiError();
		}catch (Exception e){
			return "I could not find a Wikipedia article with that exact name.";
		}
	}

	public String googleLink(String url) {
		try{
			url = url.replace(" ", "+");
			return url;
		}catch(Exception e){
			return "I couldn't Google that for you.";
		}
	}
	public String forumLink(String url){
		try {
			GetPage page = new GetPage(url);
			return page.getTitle();
		} catch (IOException e) {
			return ""; //could not read forum
		}
	}
	
	public String ping(){
		Date now = new Date();
		Long ping = now.getTime() - time.getTime();
		return "The ball was pong'd back to " + speaker + " in only " + ping + " milliseconds";
	}
	public String ping(String url) throws IOException{
		if(!url.contains("http")){
			url = "http://" + url;
		}
		Date now = new Date();
		URL site = new URL(url);
		HttpURLConnection con = (HttpURLConnection)site.openConnection();
		try{  
			con.connect();
			if(con.getResponseMessage().equals("OK")){
				InetAddress address = InetAddress.getByName(site.getHost());
				String[] pingInfo = address.toString().split("/");
				return pingInfo[0] + " (" + pingInfo[1] + ")" + " was reached in only " + (new Date().getTime() - now.getTime() + " milliseconds");
			}else{
				return "There seemed to be an issue with " + url + " but the connection was successful.";
			}
		}catch(Exception e){  
			return "No response from " + url;
		}finally{  
			con.disconnect();    
		}  
	}
	public void getHelp() throws SkypeException{
		getUserByName(speaker);
		String exception = Exception(speaker, true);
		if(!exception.equals("")){
			return;
		}
		User user = members[targetID];
		try{
			user.chat().send(getFileInfo("txtCmd.txt"));
		}catch (Exception e){
			user.chat().send("I could not find the help file.");
		}
	}
	public static void addAllFriends(Chat chat, String adder, String members) throws IOException, SkypeException{
		ContactList contacts = new ContactList();
		String contactMsg = getFileInfo("addMessage.txt");
		contactMsg = contactMsg.replace("<friend>",adder);
		ArrayList<User> users = new ArrayList<User>();
		if(members.isEmpty()){
			User[] chatMembers = chat.getAllMembers();
			for(int i=0;i<chatMembers.length;i++){
				users.add(chatMembers[i]);
			}
		}else{
			String[] memberList = members.split(" ");
			for(int i=0;i<memberList.length;i++){
				users.add(new User(memberList[i]));
			}
		}
		String temp="";
		for(int i=0;i<users.size();i++){
			temp=contactMsg;
			if(users.get(i).getBuddyStatus().toString().equals("NEVER_BEEN")){
				if(users.get(i).toString().equals("teslarobot")){
					return;
				}
				temp = temp.replace("<user>",users.get(i).toString());
				contacts.addFriend(users.get(i), temp);
			}
		}
	}
	public String addFriends(String name) throws SkypeException, IOException{
		ContactList contacts = new ContactList();
		String contactMsg = getFileInfo("addMessage.txt");
		contactMsg = contactMsg.replace("<friend>",speaker);
		try{
			getUserByName(name);
			String exception = Exception(name, true);
			if(!exception.equals("")){
				if(exception.contains("could not be found")){
					if(name.length()>30){
						return "This user most definitely does not exist.";
					}
					Pattern nameFormat = Pattern.compile("[^\\w\\-\\.]");
					Matcher nameMatch = nameFormat.matcher(name);
					if(nameMatch.find()){
						return name + " is not a valid username. Usernames can only contain A-z 0-9 -_.";
					}
					if(contacts.getFriend(name)!=null){
						return name + " is already a friend of Tesla Bot.";
					}
					contactMsg = contactMsg.replace("<user>", name);
					try{
						contacts.addFriend(name, contactMsg);
						return "Tesla Bot could not find a user with the name " + name + " in this chat so I have added him from Skype's global list of contacts. Please make sure you have added the person using his username and not his display name.";
					}catch (Exception e){
						return "Tesla Bot tried to add " + name + " as a friend but failed. Are you sure you typed it in correctly? You also need to type in the username and not the dispaly name of the contact.";
					}
				}
				return exception;
			}
			String status = members[targetID].getBuddyStatus().toString();
			if(status.equals("NEVER_BEEN")){
				contactMsg = contactMsg.replace("<user>", name);
				contacts.addFriend(name, contactMsg);
				return "I have sent a friend request to " + name;
			}else if(status.equals("PENING")){
				return "I have already sent " + name + " a friend.";
			}else if(status.equals("ADDED")){
				return "I am already good friends with " + name;
			}else if(status.equals("DELETED")){
				return "I used to be friends with " + name + "but he broke my heart.";
			}
			return "I ran into some sort of issue while trying to add " + name + "as a friend.";
		}catch (Exception e){
			return "I could not find a user with that username.";
		}
	}
	public String notifyChats(String formName) throws SkypeException, IOException {
		getUserByName(speaker);
		String exception = Exception(speaker, true);
		if(!exception.equals("")){
			return exception;
		}
		if(isAdmin(members[targetID].toString().toLowerCase())){
			Chat[] chats = Skype.getAllChats();
			for(int i=0;i<chats.length;i++){
				if(chats[i].getStatus().toString().equals("MULTI_SUBSCRIBED")){
					if(formName.toLowerCase().equals("online")){
						chats[i].send("Tesla Bot is now online and fully charged.");
					}else if(formName.toLowerCase().equals("offline")){
						chats[i].send("Tesla Bot shutting down...");
					}else{
						chats[i].send(getFileInfo("notify.txt"));
					}
				}
			}
			return "Notifications sent.";
		}
		return "You found a secret feature that you aren't allowed to use!";
	}
	public String doneAction(String type) throws SkypeException{
		getUserByName(speaker);
		String exception = Exception(speaker, true);
		if(!exception.equals("")){
			return exception;
		}
		String temp = type.toLowerCase();
		if(type.equals("")){
			return speaker + " is simply done.";
		}else if(temp.equals("task")){
			return speaker + " has completed yet another task!";
		}else if(temp.equals("bug")){
			return speaker + " has fixed yet another bug!";
		}else if(temp.contains("whatever you specify")){
			return speaker + " clearly misinterpreted the help file.";
		}else{
			return speaker + " is now done " + type + " and will spare you his cool stories.";
		}
	}
	public String imgurLink(String url) throws IOException, SkypeException{
		if(url.contains("/a/")){ //if the link is to an album
			return "";
		}
		if(url.contains("i.imgur.com")){
			url = url.replace("http://", "");
			url = url.replace("i.imgur.com/", "");
			url = url.substring(0,url.length()-4);
			url = "http://imgur.com/gallery/" + url;
		}
		if(!url.contains("/gallery/")){
			Pattern IMGUR_LINK = Pattern.compile("imgur.com/(.....)\\z");
			Matcher imgurMatch = IMGUR_LINK.matcher(url);
			if(imgurMatch.find()){
				url = "http://imgur.com/gallery/" + imgurMatch.group(1);
			}
		}
		if(url.contains("/gallery/")){
			GetPage page = new GetPage(url);
			int errorID = page.getError();
			if(errorID==1){
				return "Imgur link detected but could not be read.";
			}
			String title = page.getTitle();
			title = title.replace(" - Imgur", "");
			if(title.contains("imgur: the simple image sharer")){
				return "";
			}
			return speaker + " linked to imgur picture titled " + title;
		}
		return "";
	}
	public String twitterLink(String url) throws IOException, SkypeException{
		if(url.contains("/status/")){
			GetPage page = new GetPage(url);
			int errorID = page.getError();
			if(errorID==1){
				return "Twitter link detected but could not be read.";
			}
			String tweet = page.getTweet();
			String author = page.getAuthor();
			String timestamp = page.getDuration();
			return "@" + author + "\n" + tweet + "\n" + timestamp;
		}
		return "";
	}
	public String redditLink(String url) throws IOException, SkypeException{	
		if(url.contains("/comments/")){
			GetPage page = new GetPage(url);
			int errorID = page.getError();
			if(errorID==1){
				return "Redding link detected but could not be read.";
			}

			String title = page.getTitle();
			String votes = page.getVotes();
			return title + "    " + votes;
		}
		return "";
	}
	private boolean isAdmin(String name){
		if(name.equals("blackduck606") || name.equals("teslarobot")){
			return true;
		}else{
			return false;
		}
	}
	public void getUserByName(String username) throws SkypeException{
		for(int i=0;i<members.length;i++){
			if(username.equalsIgnoreCase(members[i].getFullName()) || username.equalsIgnoreCase(members[i].toString())){
				targetID = i;
				return;
			}
		}
		targetID = -1;
	}
	private String Exception(String username, boolean selfAllowed) throws SkypeException{
		if(targetID == -1){
			if(username.length()==0){
				return speaker + " stalks his pray as he awaits to strike.";
			}
			return username + " could not be found in this chat.";
		}else if(username.length() == 0 || (members[targetID].getFullName().equals(speaker) && !selfAllowed)){
			return speaker + " hits itself in confusion. Super effective!";
		}
		return "";
	}
	public String getRndmLine(String file) throws FileNotFoundException{
		try{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
						
			int lineNumber=0;
			getFileInfo(file);
			int randMsg = (int) Math.round(Math.random()*(lineCount-1));
			String strLine;
			while((strLine = br.readLine()) != null){
				if(lineNumber==randMsg){
					return strLine + " ["+ (lineNumber+1) + "/" + (lineCount) + "] ";
				}
				lineNumber++;
			}
			in.close();
			return "I tried to speak but simply stuttered.";
		}catch (Exception e){
			return "I could not find the file.";
		}
	}
	public static String getFileInfo(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 1; //start at first line
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        lineCount = count;
	        String fileResult = new String(c);
	        return fileResult;
	    } finally {
	        is.close();
	    }
	}
}
