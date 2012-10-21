package bot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import com.skype.Call;
import com.skype.Call.Status;
import com.skype.Chat;
import com.skype.Skype;
import com.skype.SkypeException;

/**
 * 
 * @author Sebastien 
 * TODO: 
 * Pull music from a repository 
 * Better way to end the radio function
 * Add to help file
 */

public class Radio {

	private String mainDir = System.getProperty("user.dir") + "\\radio\\";
	private String secondaryDir = "";
	private String dynamicDir = "";
	private String songTitle = "";
	private String mode = "repeat";
	private boolean playing = false;
	private String callID = "";
	private Call call;

	public String getCommand(String[] cmds, Chat chat) throws SkypeException{
		String rtrn = "Invalid command.";
		if(cmds.length == 2){
			if(cmds[1].equalsIgnoreCase("on")){
				boolean success = play(chat);
				if(success){
					rtrn = "Radio started.";
				}else{
					rtrn = "Radio already on.";
				}
			}else if(cmds[1].equalsIgnoreCase("off")){
				boolean success = stopRadio();
				if(success){
					rtrn = "Radio stopped.";
				}else{
					rtrn = "Radio failed to stop.";
				}
			}
		}
		if(isPlaying()){		
			if(cmds.length == 2){
				if(cmds[1].equals("list")){
					rtrn = listDir();
				}else if(cmds[1].equals("back")){
					String[] path = dynamicDir.split("\\\\");
					StringBuffer sb = new StringBuffer();
					for(int i=0; i<path.length-1; i++){
						sb.append(path[i] + "\\");
					}
					dynamicDir = sb.toString();
					if(dynamicDir.isEmpty()){
						rtrn = "Returned to root directory";
					}else{
						rtrn = "Directory shifted to " + dynamicDir;
					}
				}else if(cmds[1].equals("next")){
					getNextTrack();
					playSong(false);
					rtrn = "Now playing " + songTitle;
				}else if(cmds[1].equals("set")){
					updateDir();
					rtrn = "Updated directory to " + secondaryDir;
				}
			}
			if(cmds.length == 3){
				String command = cmds[1];
				if(cmds[1].equals("play") || cmds[1].equals("load")){
					int id;
					try{
						id = Integer.parseInt(cmds[2]);
						String[] list;
						if(command.equals("play")){
							list = getTracks(true);
						}else{
							list = getDirectories(true);
						}
						if(id < list.length && id >= 0){
							if(command.equals("play")){
								list = getTracks(true);
								songTitle = list[id];
								playSong(true);
								rtrn = "Now playing " + songTitle;
							}else{
								list = getDirectories(true);
								dynamicDir = list[id] + "\\";
								rtrn = "Directory shifted to " + dynamicDir;
							}
						}else{
							rtrn = "Invalid track ID.";
						}
					}catch(NumberFormatException e){
						rtrn = "Invalid track ID.";	
					}
				}else if(cmds[1].equals("mode")){
					boolean success = setMode(cmds[2]);
					if(success){
						rtrn = "Mode set to " + cmds[2];
					}else{
						rtrn = "Invalid mode.";
					}
				}else{
					rtrn = "Invalid use of command " + cmds[2];
				}
			}
		}
			
		return rtrn;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void resetRadio() {
		playing = false;
		callID = "";
		secondaryDir = "";
		dynamicDir = "";
	}

	public boolean stopRadio() throws SkypeException {
		if (callID.isEmpty()) {
			return false;
		}
		if (call == null) {
			return false;
		}
		resetRadio();
		try {
			Call[] calls = Skype.getAllActiveCalls();
			for(Call call : calls){
				call.finish();
			}
		} catch (SkypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean play(Chat chat) throws SkypeException {
		if (!callID.isEmpty()) {
			return false;
		}
		playing = true;
		callID = "LOADING";

		Skype.callChat(chat.getId());
		Call[] calls = Skype.getAllCalls();
		call = null;
		for (Call skypeCall : calls) {
			Status status = skypeCall.getStatus();
			if (status == Status.UNPLACED || status == Status.RINGING
					|| status == Status.ROUTING) {
				call = skypeCall;
				break;
			}
		}
		if (call == null) {
			stopRadio();
		}
		callID = call.getId();
		songTitle = "callIntro.wav";
		playSong(true);
		return true;
	}

	public String getCallID() {
		return callID;
	}
	
	public void updateDir(){
		if (!secondaryDir.equals(dynamicDir)) {
			secondaryDir = dynamicDir;
		}
	}

	public boolean playSong(boolean updateDirectory) {
		if (updateDirectory) {
			updateDir();
		}
		File f = new File(mainDir + secondaryDir + songTitle);
		if (!f.exists()) {
			System.out.println("Song does not exist at " + f.getAbsolutePath());
			return false;
		}
		try {
			System.out.println("Song filepath: " + f.getAbsolutePath());
			call.setFileInput(f);
		} catch (SkypeException e) {
			return false;
		}
		return true;
	}

	public String listDir() {
		String[] songs = getTracks(true);
		String[] files = getDirectories(true);

		StringBuilder sb = new StringBuilder("Listing tracks..." + '\n');
		for (int i = 0; i < songs.length; i++) {
			String trackID = Integer.toString(i);
			if (trackID.length() == 1) {
				trackID = "0" + i;
			}
			sb.append("Track " + trackID + " : " + songs[i] + '\n');
		}
		for (int i = 0; i < files.length; i++) {
			String trackID = Integer.toString(i);
			if (trackID.length() == 1) {
				trackID = "0" + i;
			}
			sb.append("Folder " + trackID + " : " + files[i] + "\\" + '\n');
		}

		sb.substring(0, sb.length() - 2);
		return sb.toString();
	}

	private String[] getTracks(boolean dynamic) {
		File directory;
		if(dynamic){
			directory = new File(mainDir + dynamicDir);
		}else{
			directory = new File(mainDir + secondaryDir);
		}
		String[] songs = directory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return !new File(current, name).isDirectory();
			}
		});
		return songs;
	}

	private String[] getDirectories(boolean dynamic) {
		File directory;
		if(dynamic){
			directory = new File(mainDir + dynamicDir);
		}else{
			directory = new File(mainDir + secondaryDir);
		}
		String[] files = directory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		return files;
	}
	
	private boolean setMode(String setMode){
		if(setMode.equalsIgnoreCase("repeat")){
			mode = setMode.toLowerCase();
		}else if(setMode.equalsIgnoreCase("shuffle")){
			mode = setMode.toLowerCase();
		}else if(setMode.equalsIgnoreCase("linear")){
			mode = setMode.toLowerCase();
		}else{
			return false;
		}
		return true;
	}

	public void songOver() {
		if(mode.equals("repeat")){
			checkSong();
			playSong(false);
		}else if(mode.equals("shuffle")){
			getRandomTrack();
			playSong(false);
		}else if(mode.equals("linear")){
			getNextTrack();
			playSong(false);
		}
		
	}
	
	private void checkSong(){
		String[] tracks = getTracks(false);
		for(String track : tracks){
			if(track.equals(songTitle)){
				return;
			}
		}
		songTitle = tracks[0];
	}
	
	private void getRandomTrack(){
		String[] tracks = getTracks(false);
		Random r = new Random();
		int id;
		do{
			id = r.nextInt(tracks.length);
		}while(songTitle.equals(tracks[id]));
		songTitle = tracks[id];
	}
	
	private void getNextTrack(){
		String[] tracks = getTracks(false);
		int id=-1;
		
		for(int i=0; i<tracks.length-1; i++){
			if(tracks[i].equals(songTitle)){
				id = ++i;
				break;
			}
		}
		if(id == -1){
			id = 0;
		}
		
		songTitle = tracks[id];
	}

}