package bot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
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
 * Better way to convert music
 * Add to help file
 * Make starting directory modifiable
 * Add more intro songs and pick a random one
 * Add a mute feature
 * Fix start over/play for groups (?)
 * Get if radio is playing or not
 */

public class Radio {

	private String mainDir = System.getProperty("user.dir") + "\\radio\\";
	private String secondaryDir = "";
	private String dynamicDir = "";
	private String songTitle = "";
	private String mode = "repeat";
	private boolean playing = false;
	private Call[] calls;

	public String getCommand(String[] cmds, Chat chat) throws SkypeException{
		String rtrn = "Invalid command or radio not running.";
		if(cmds.length == 2){
			if(cmds[1].equals("on")){
				boolean success = play(chat);
				if(success){
					rtrn = "Radio started.";
				}else{
					rtrn = "Radio already on.";
					if(calls.length == 0){
						rtrn = "Can not start radio when a call is already going on.";
					}
				}
			}else if(cmds[1].equals("off")){
				boolean success = resetRadio();
				if(success){
					rtrn = "Radio stopped.";
				}else{
					rtrn = "Radio failed to stop.";
				}
			}else if(cmds[1].equals("status")){
				rtrn = "Tesla Wireless Radio is currently ";
				if(playing){
					rtrn += "online.";
				}else{
					rtrn += "offline.";
				}
			}
		}
		if(isPlaying()){		
			if(cmds.length == 2){
				if(cmds[1].equals("list")){
					rtrn = listDir();
				}else if(cmds[1].equals("back")){
					if(!dynamicDir.isEmpty()){
						dynamicDir = dynamicDir.substring(0, dynamicDir.length()-1); //removes the backslash at the end
					}
					int index = dynamicDir.lastIndexOf("\\");
					String msg;
					if(index != -1){
						dynamicDir = dynamicDir.substring(0, index) + "\\";
						msg = "Directory shifted to " + getDirTitle(true);
					}else{
						dynamicDir = "";
						msg = "Directory shifted to root directory";
					}
					rtrn = msg;
				}else if(cmds[1].equals("next")){
					getNextTrack();
					allPlaySong(false);
					rtrn = "Now playing " + songTitle;
				}else if(cmds[1].equals("set")){
					updateDir();
					rtrn = "Updated directory to " + getDirTitle(false);
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
								allPlaySong(true);
								rtrn = "Now playing " + songTitle;
							}else{
								list = getDirectories(true);
								dynamicDir += list[id] + "\\";
								rtrn = "Directory shifted to " + dynamicDir;
							}
						}else{
							rtrn = "Invalid track or directory.";
						}
					}catch(NumberFormatException e){
						rtrn = "Invalid track or directory.";	
					}
				}else if(cmds[1].equals("mode")){
					boolean success = setMode(cmds[2]);
					if(success){
						rtrn = "Mode set to " + cmds[2];
					}else{
						rtrn = "Invalid mode.";
					}
				}else if(cmds[1].equals("get")){
					if(cmds[2].equals("track")){
						rtrn = songTitle + " is currently playing in " + getDirTitle(false);
					}else if(cmds[2].equals("dir")){
						rtrn = "Currently playing from " + getDirTitle(false) + " and browsing through " + getDirTitle(true);
					}else if(cmds[2].equals("mode")){
						rtrn = "Current mode: " + mode;
					}else if(cmds[2].equals("listeners")){
						int numCalls=0;
						for(Call call : calls){
							if(callActive(call)){
								numCalls++;
							}
						}
						if(numCalls > 1){
							rtrn = "There are currently " + numCalls + " listeners.";
						}else{
							rtrn = "There is currently " + numCalls + " listener";
						}
					}
				}else{
					rtrn = "Invalid use of command " + cmds[1] + cmds[2];
				}
			}
		}
			
		return rtrn;
	}
	
	private String getDirTitle(boolean dynamic){
		String dir;
		if(dynamic){
			dir = dynamicDir;
		}else{
			dir = secondaryDir;
		}
		if(dir.isEmpty()){
			dir = "root directory";
		}
		return dir;
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean resetRadio() {
		playing = false;
		songTitle = "";
		mode = "repeat";
		secondaryDir = "";
		dynamicDir = "";
		for(int i=0; i<calls.length; i++){
			try {
				if(callActive(calls[i])){
					calls[i].hangup();
				}
			} catch (SkypeException e) {
				e.printStackTrace();
				return false;
			}
		}
		calls = null;
		return true;
	}

	public boolean play(Chat chat) throws SkypeException {
		playing = true;

		calls = Skype.callChat(chat.getId());
		songTitle = "callIntro.wav";
		allPlaySong(true);
		return true;
	}
	
	public void updateDir(){
		if (!secondaryDir.equals(dynamicDir)) {
			secondaryDir = dynamicDir;
		}
	}

	public void allPlaySong(boolean updateDirectory) {
		if (updateDirectory) {
			updateDir();
		}
		for(int i=0; i<calls.length; i++){
			playSong(i);
		}
	}
	
	private void playSong(int callerID){
		File f = getTrackPath();
		try {
			if(callActive(calls[callerID])){
				calls[callerID].setFileInput(f);
			}else{
				resetRadio();
			}
		} catch (SkypeException e) {
			System.out.println("Failed to attach track at call ID " + callerID + " at track " + f.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
	private File getTrackPath(){
		File f = new File(mainDir + secondaryDir + songTitle);
		if (!f.exists()) {
			System.out.println("Song does not exist at " + f.getAbsolutePath());
			return null;
		}
		return f;
	}
	
	public boolean callActive(Call call) throws SkypeException{
		Status status = call.getStatus();
		if(status == Status.UNPLACED || status == Status.INPROGRESS || status == Status.RINGING || status == Status.ROUTING){
			return true;
		}
		return false;
	}

	public String listDir() {
		String[] songs = getTracks(true);
		String[] files = getDirectories(true);

		StringBuilder sb = new StringBuilder("Listing tracks..." + '\n');
		if(songs == null){
			sb.append("No tracks found.");
		}else{
			for (int i = 0; i < songs.length; i++) {
				String trackID = Integer.toString(i);
				if (trackID.length() == 1) {
					trackID = "0" + i;
				}
				sb.append("Track " + trackID + " : " + songs[i] + '\n');
			}
		}
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				String folderID = Integer.toString(i);
				if (folderID.length() == 1) {
					folderID = "0" + i;
				}
				sb.append("Folder " + folderID + " : " + files[i] + "\\" + '\n');
			}
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

	public void songOver(String callID) {
		int callerID = -1;
		if(calls == null){
			return;
		}
		for(int i=0; i<calls.length; i++){
			if(calls[i].getId().equals(callID)){
				callerID = i;
				break;
			}
		}
		
		if(mode.equals("repeat")){
			checkSong();
		}else if(mode.equals("shuffle")){
			getRandomTrack();
		}else if(mode.equals("linear")){
			getNextTrack();
		}
		playSong(callerID);		
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
		if(tracks.length == 1){
			songTitle = tracks[0];
		}else{
			Random r = new Random();
			int id;
			do{
				id = r.nextInt(tracks.length);
			}while(songTitle.equals(tracks[id]));
			songTitle = tracks[id];
		}
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