package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import aibus.AIData;

public class SaveLoadController {
	private SimAIBus controller;
	private String default_path = "";
	
	public SaveLoadController(SimAIBus controller) {
		this.controller = controller;
		try {
			this.default_path = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

			if(this.default_path == null)
				this.default_path = "";
		} catch (URISyntaxException e) {
			this.default_path = "";
		}
	}

	protected void openSaveChooser(AIData[] msg_list) {
		JFileChooser file_chooser = new JFileChooser(this.default_path);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("SimAIBus List File", "lstx");
		file_chooser.addChoosableFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		final int return_val = file_chooser.showSaveDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			File output = file_chooser.getSelectedFile();
			String ext = "";

			final String des = file_chooser.getFileFilter().getDescription();
			if(des.toUpperCase().contains("LIST"))
				ext = "lstx";
			else {
				int period = output.getName().indexOf(".");
				if(period<0) {
					JOptionPane.showMessageDialog(controller.getMainWindow(), "Please specify an extension and try again.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					ext = output.getName().substring(period+1, output.getName().length());
				}
			}

			if(!file_chooser.getFileFilter().accept(output)) {
				String fpath = output.getAbsolutePath() + "." + ext;
				output = new File(fpath);
			}

			if(output.exists()) {
				final int answer = JOptionPane.showConfirmDialog(controller.getMainWindow(), output.getName() + " already exists. Overwrite it?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(answer == JOptionPane.NO_OPTION)
					return;
			}

			saveListToFile(output, msg_list);
		}
	}

	protected AIData[] openLoadChooser() {
		JFileChooser file_chooser = new JFileChooser(this.default_path);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("SimAIBus List File", "lstx");
		file_chooser.addChoosableFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		final int return_val = file_chooser.showOpenDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			return loadList(file_chooser.getSelectedFile());
		}
		return new AIData[0];
	}
	
	private void saveListToFile(File save_file, AIData[] msg_list) {
		String save_string = "";

		for(int i=0;i<msg_list.length;i+=1) {
			AIData ai_msg = msg_list[i];
			save_string += "S: ";
			save_string += String.format("%X", ai_msg.sender);
			save_string += " R: ";
			save_string += String.format("%X", ai_msg.receiver);
			save_string += " D: ";

			for(int d=0;d<ai_msg.l;d+=1) {
				save_string += String.format("%X", ai_msg.data[d]);

				if(d<ai_msg.l - 1)
					save_string += " ";
			}

			if(i < msg_list.length - 1)
				save_string += '\n';
		}

		if(save_file.exists()) {
			try {
				save_file.delete();
			} catch(SecurityException e) {
				JOptionPane.showMessageDialog(controller.getMainWindow(), "Error saving file.\nMessage: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		try {
			Files.writeString(save_file.toPath(), save_string, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(controller.getMainWindow(), "Error saving file.\nMessage: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String default_path = save_file.getParent();
		
		if(default_path != null)
			this.default_path = save_file.getParent();
	}

	private AIData[] loadList(File load_file) {
		BufferedReader file_reader;
		ArrayList<AIData> msg_arraylist = new ArrayList<AIData>(0);

		try {
			file_reader = new BufferedReader(new FileReader(load_file));

			String msg_line = null;
			do {
				msg_line = file_reader.readLine();

				if(msg_line != null) {
					msg_line = msg_line.toUpperCase();
					
					final int s_loc = msg_line.indexOf("S:"), r_loc = msg_line.indexOf("R:"), d_loc = msg_line.indexOf("D:");
					if(s_loc < 0 || r_loc < 0 || d_loc < 0)
						continue;

					//Find the sender.
					int index = s_loc+2;
					byte sender = 0, receiver = 0;
					String byte_buffer = "";

					while(index < msg_line.length()) {
						if((msg_line.charAt(index) >= '0' && msg_line.charAt(index) <= '9')
							|| (msg_line.charAt(index) >= 'A' && msg_line.charAt(index) <= 'F')) {
							
							byte_buffer += msg_line.charAt(index);
						} else if(byte_buffer.length() > 0)
							break;

						index += 1;
					}

					sender = getHex(byte_buffer);

					//Find the receiver.
					index = r_loc+2;
					byte_buffer = "";

					while(index < msg_line.length()) {
						if((msg_line.charAt(index) >= '0' && msg_line.charAt(index) <= '9')
							|| (msg_line.charAt(index) >= 'A' && msg_line.charAt(index) <= 'F')) {
							
							byte_buffer += msg_line.charAt(index);
						} else if(byte_buffer.length() > 0)
							break;

						index += 1;
					}


					receiver = getHex(byte_buffer);

					//Find the data.
					ArrayList<Byte> byte_list = new ArrayList<Byte>(0);
					index = d_loc+2;
					byte_buffer = "";
					
					while(index < msg_line.length()) {
						if((msg_line.charAt(index) >= '0' && msg_line.charAt(index) <= '9')
							|| (msg_line.charAt(index) >= 'A' && msg_line.charAt(index) <= 'F')) {
							
							byte_buffer += msg_line.charAt(index);
						} else if(msg_line.charAt(index) == ' ') {
							if(byte_buffer.length() > 0)
								byte_list.add(Byte.valueOf(getHex(byte_buffer)));
							
							byte_buffer = "";
						} else if(byte_buffer.length() > 0)
							break;

						index += 1;

						if(index == msg_line.length()) {
							byte_list.add(Byte.valueOf(getHex(byte_buffer)));
						}
					}

					AIData new_msg = new AIData((short)byte_list.size(), sender, receiver);
					for(int i=0;i<byte_list.size();i+=1)
						new_msg.data[i] = byte_list.get(i);

					msg_arraylist.add(new_msg);
				}
			} while(msg_line != null);

			file_reader.close();
		} catch(IOException | IllegalArgumentException e) {
			JOptionPane.showMessageDialog(controller.getMainWindow(), "Error reading file.\nMessage: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return new AIData[0];
		}

		AIData[] msg_list = new AIData[msg_arraylist.size()];
		for(int i=0;i<msg_arraylist.size();i+=1)
			msg_list[i] = msg_arraylist.get(i);

		String default_path = load_file.getParent();
		
		if(default_path != null)
			this.default_path = load_file.getParent();

		return msg_list;
	}

	private static byte getHex(String txt) {
		int return_int = 0;
		txt = txt.toUpperCase();

		for(int i=0;i<txt.length();i+=1) {
			return_int <<= 4;
			
			if(txt.charAt(i) >= '0' && txt.charAt(i) <= '9')
				return_int |= txt.charAt(i) - '0';
			else if(txt.charAt(i) >= 'A' && txt.charAt(i) <= 'F')
				return_int |= 0xA + (txt.charAt(i) - 'A');
		}

		return (byte)return_int;
	}
}
