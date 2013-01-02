package bot;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Date;

import com.skype.Call;
import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ContactList;
import com.skype.SkypeException;
import com.skype.Call.Status;
import com.skype.User;
import com.skype.User.BuddyStatus;
import com.skype.connector.ConnectorException;

public class TeslaInput {

	private String chatMessageID;
	private String status;
	private String[] cmd;
	private boolean justAdded;
	private boolean isBusy;
	
	private boolean shadow = false;
	private boolean isShadowing = false;
	
	public static boolean isPinging = false;

	Radio radio = new Radio();

	public void getInput(String input) throws ConnectorException, SkypeException, IOException {
		if (input.equals("PONG")) {
			return;
		}
		cmd = input.split(" ");
		if (cmd[0].equals("CHATMESSAGE")) {
			if (cmd.length == 3) {
				System.out.println("Chat event occured");
			} else {
				try {
					status = cmd[2] + " " + cmd[3];
					if (status.equals("STATUS RECEIVED") || status.equals("STATUS SENDING")) {
						chatMessageID = cmd[1];
						readMessage(chatMessageID);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error in reading message.");
					return;
				}
			}
		} else if (cmd[0].equals("CALL")) {
			if (cmd.length == 4) {
				if (cmd[3].equals("FINISHED")) {
					radio.resetRadio();
				}
				if (cmd[3].equals("RINGING")) {
					try {
						Call call = new Call(cmd[1]);
						if (radio.isListening()) {
							radio.play(call);
						} else if (!call.equals(radio.getCall()) && call.getStatus() != Status.REFUSED) {
							if(!shadow){
								call.finish();
							}else{
								shadow = false;
								isShadowing = true;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				} else if (cmd[2].startsWith("VAA")) {
					if (cmd[3].equals("FALSE")) {
						radio.songOver();
					}
				}
			}
		} else if (cmd[0].equals("CHAT")) {
			try {
				if (cmd[2].equals("ADDER") || justAdded) { //when somebody adds you to a chat
					Chat newChat = new Chat(cmd[1]);
					TeslaCmdsYou.addAllFriends(newChat, cmd[3], "");
					if (newChat.getAllMembers().length > 2 || justAdded) {
						newChat.send(TeslaCmdsYou.getFileInfo("txt\\greetings.txt"));
					}
					justAdded = false;
				} else if (cmd[2].equals("MEMBERS")) {
					Chat newChat = new Chat(cmd[1]);
					TeslaCmdsYou.addAllFriends(newChat, "who has added you to a group chat I'm in", formName(cmd, 3));
				}
			} catch (IOException e) {
				return;
			} catch (SkypeException e) {
				return;
			}
		} else if (cmd[0].equals("USER")) {
			try {
				if (cmd[2].equals("RECEIVEDAUTHREQUEST")) {
					ContactList contacts = new ContactList();
					justAdded = true;
					contacts.addFriend(cmd[1], "");
				}
			} catch (Exception e) {
				System.err.println(e.getCause());
				return;
			}
		}
	}

	private void readMessage(String msgID) throws ConnectorException, SkypeException, IOException {
		ChatMessage msg = new ChatMessage(msgID);
		String body = msg.getContent();
		String speaker = msg.getSenderDisplayName();
		if (!speaker.equals("Tesla Bot")) {
			msg.setSeen();
		}
		Date time = msg.getTime();
		if(new Date().getTime() - time.getTime() > 10000){
			return;
		}
		System.out.println(time + " : " + speaker + ": " + body);

		if (body.startsWith("!") || body.startsWith("http") || body.startsWith("www")) {
			if (body.startsWith("www")) {
				body = body.replace("www", "http://www");
			}
			String[] args = body.split("\\s+");
			TeslaCmdsYou action = new TeslaCmdsYou(time, speaker, msg.getChat().getAllMembers());
			String rtrn = "";
			if (body.startsWith("!")) {
				args[0] = args[0].toLowerCase();
				if (args[0].equals("!roll")) {
					rtrn = action.rollDice(formName(args, 1));
				} else if (args[0].equals("!whereis")) {
					rtrn = action.lastSeen(formName(args, 1));
				} else if (args[0].equals("!users")) {
					rtrn = action.users();
				} else if (args[0].equals("!poke")) {
					rtrn = action.poke(formName(args, 1));
				} else if (args[0].equals("!slap")) {
					rtrn = action.slap(formName(args, 1));
				} else if (args[0].equals("!search") || args[0].equals("!google")) {
					rtrn = action.googleLink("https://www.google.com/#hl=en&q=" + formName(args, 1));
				} else if (args[0].equals("!wiki")) {
					rtrn = action.wikipediaLink("http://en.wikipedia.org/wiki/" + formName(args, 1));
				} else if (args[0].equals("!ping")) {
					if (args.length == 1) {
						rtrn = action.ping();
					} else {
						if(isPinging){
							rtrn = "A website is already being pinged at the moment and so your request has been ignored.";
						}else{
							isPinging = true;
							action.ping(args[1], msg.getChat());
						}
					}
				} else if (args[0].equals("!pong")) {
					rtrn = "Apparently " + speaker + " thinks the name of the game is Pong-Ping. Everybody point and laugh.";
				} else if (args[0].equals("!quote")) {
					rtrn = action.getRndmLine("txt\\TeslaQuotes.txt");
				} else if (args[0].equals("!help")) {
					User user = action.getUserID(speaker);
					if (user.getBuddyStatus() == BuddyStatus.ADDED) {
						rtrn = "Help has been delivered to you personally.";
					} else {
						rtrn = "Help was sent but may not be received as you are not friends with Tesla Bot.";
					}
					action.getFullFile("txt\\txtCmd.txt");
				} else if (args[0].equals("!add")) {
					rtrn = action.addFriends(speaker, formName(args, 1));
				} else if (args[0].equals("!addas")) {
					if (action.isAdmin()) {
						if(args.length == 3){
							rtrn = action.addFriends(args[1], args[2]);
						}else if (args.length == 2){
							rtrn = action.addFriends("who has added you to a group chat I'm in", args[1]);
						}
					} else {
						rtrn = "You are not an admin and therefore can not use this command.";
					}
				} else if (args[0].equals("!notify")) {
					rtrn = action.notifyChats(formName(args, 1));
				} else if (args[0].equals("easter")) {
					if (args.length == 2 && args[1].equals("egg")) {
						rtrn = "Whoa! Magical easter egg!";
					}
				} else if (args[0].equals("!choose")) {
					rtrn = action.choseElement(formName(args, 1));
				} else if (args[0].equals("!conch")) {
					rtrn = action.getRndmLine("txt\\magicconch.txt") + "\nTHE CONCH HAS SPOKEN";
				} else if (args[0].equals("!radio")) {
					if (args.length == 2 && args[1].equals("help")) {
						action.getFullFile("txt\\radioHelp.txt");
						rtrn = "Help has been delivered to you personally.";
					} else {
						if(!isShadowing){
							rtrn = radio.getCommand(args, msg.getChat(), action.isAdmin());
						}else{
							rtrn = "I am currently already in a call, please try again later.";
						}
					}
				} else if (args[0].equals("!shadow")){
					if(action.isAdmin()){
						if(args.length == 2){
							if(args[1].equals("on")){
								shadow = true;
								rtrn = "You can now shadow a call";
							}else if(args[1].equals("off")){
								shadow = false;
								isShadowing = false;
								rtrn = "Shadowing disabled.";
							}else{
								rtrn = "Invalid use of shadow command.";
							}
						}
						
					}else{
						rtrn = "This is an admin only command.";
					}
				} else if (args[0].equals("!summon")) {
					if(args.length == 2){
						if(action.isAdmin()){
							rtrn = "Set to ";
							if(args[1].equals("busy")){
								isBusy = true;
								rtrn += "busy";
							}else if (args[1].equals("free")){
								isBusy = false;
								rtrn += "free";
							}else{
								rtrn = "Failed to set to " + args[1];
							}
						}else{
							rtrn = "Invalid use of !summon";
						}
					}else{
						if(isBusy){
							Toolkit.getDefaultToolkit().beep();
							rtrn = "My creator is currently busy and may not respond.";
						}else{
							msg.getChat().openChat();
							Toolkit.getDefaultToolkit().beep();
							rtrn = "My creator has been alerted of your existence.";
						}
					}
				} else {
					rtrn = "I do not know how to " + body.substring(1) + ". Use !help to see a list of commands.";
					if (body.length() == 1) {
						Long randomNumber = Math.round(Math.random());
						if (randomNumber == 1) {
							rtrn = "Don't you worry about blank, let me worry about blank.";
						} else {
							rtrn = "Blank? BLANK?! You're not looking at the big picture!";
						}
					}
				}
			} else if (body.startsWith("http")) {
				try {
					if (args[0].contains("youtube.com/watch?")) {
						rtrn = action.youtubeLink(args[0]);
					} else if (args[0].contains("imgur.com")) {
						rtrn = action.imgurLink(args[0]);
					} else if (args[0].contains("twitter.com")) {
						rtrn = action.twitterLink(args[0]);
					} else if (args[0].contains("reddit.com")) {
						rtrn = action.redditLink(args[0]);
					} else if (args[0].contains("showthread")) {
						rtrn = action.forumLink(args[0]);
					}
				} catch (IOException e) {
					System.err.println("Link detected but could not be read due to " + e.getMessage());
				}
			}
			if (rtrn.length() != 0) {
				System.out.println(rtrn);
				msg.getChat().send(rtrn);
			}
		}
	}

	private String formName(String[] args, int start) {
		String name = "";
		for (int i = start; i < args.length; i++) {
			if (i < args.length - 1) {
				name = name.concat(args[i]) + " ";
			} else {
				name = name.concat(args[i]);
			}
		}
		return name;
	}
}
